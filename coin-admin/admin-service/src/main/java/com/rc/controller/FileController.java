package com.rc.controller;

import cn.hutool.core.date.DateUtil;
import com.rc.model.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 文件上传
 */
@RestController
@Api(tags = "文件上传")
public class FileController {

    @Autowired
    private S3Client s3Client;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Value("${aws.s3.region}")
    private String region;

    @Value("${aws.s3.access-key}")
    private String accessKey;

    @Value("${aws.s3.secret-key}")
    private String secretKey;

    @ApiOperation(value = "文件上传（S3）")
    @PostMapping("/image/AliYunImgUpload")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "file", value = "你要上传的文件", required = true)
    })
    public R<String> fileUpload(@RequestParam("file") MultipartFile file) throws IOException {

        String fileName = DateUtil.today().replaceAll("-", "/") + "/" + URLEncoder.encode(Objects.requireNonNull(file.getOriginalFilename()), "UTF-8");

        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(fileName)
                        .contentType(file.getContentType())
                        .build(),
                software.amazon.awssdk.core.sync.RequestBody.fromInputStream(file.getInputStream(), file.getSize())
        );

        //https://coin-exchange-imgs.s3.ap-southeast-2.amazonaws.com/2025/04/04/1.png 正确的url
        //https://coin-exchange-imgs.ap-southeast-2.amazonaws.com/2025/04/04/1.png 错误的url

        String fileUrl = "https://" + bucketName + ".s3." + region + ".amazonaws.com/" + fileName;

        return R.ok(fileUrl);
    }

    @GetMapping("/image/pre/upload")
    @ApiOperation(value = "获取一个上传的票据")
    public R<Map<String, String>> asyncUpload() {
        String dir = DateUtil.today().replaceAll("-", "/");
        Map<String, String> uploadPolicy = getUploadPolicy(30L, 3 * 1024 * 1024L, dir, "");
        return R.ok(uploadPolicy);
    }

    private Map<String, String> getUploadPolicy(Long expireTime, Long maxFileSize, String dir, String callbackUrl) {
        Map<String, String> respMap = new LinkedHashMap<>();

        try (S3Presigner presigner = S3Presigner.builder()
                .region(Region.of(region))
                .build()) {

            // 生成对象 key
            String objectKey = dir + "/" + UUID.randomUUID().toString();

            // 注意：这里为文件上传设置正确的 Content-Type
            // 对于直接上传二进制文件，使用 application/octet-stream
            // 如果客户端要上传图片，可以考虑使用 image/jpeg 或 image/png 等
            PutObjectRequest objectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();

            PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofSeconds(expireTime))
                    .putObjectRequest(objectRequest)
                    .build();

            // 生成预签名 URL
            PresignedPutObjectRequest presignedRequest = presigner.presignPutObject(presignRequest);
            URL presignedUrl = presignedRequest.url();

            // 构建响应
            respMap.put("url", presignedUrl.toString());
            respMap.put("objectKey", objectKey);

            // 不要重新设置这些参数，直接使用预签名 URL 生成的值
            // 从 URL 中提取所有查询参数
            Map<String, String> queryParams = extractQueryParams(presignedUrl.getQuery());
            queryParams.forEach(respMap::put);

            respMap.put("maxFileSize", String.valueOf(maxFileSize));
            respMap.put("bucket", bucketName);
            respMap.put("region", region);

            if (callbackUrl != null && !callbackUrl.isEmpty()) {
                respMap.put("callbackUrl", callbackUrl);
            }

        } catch (Exception e) {
            e.printStackTrace();
            respMap.put("error", "Error generating upload policy: " + e.getMessage());
        }

        return respMap;
    }


    /**
     * Helper method to extract query parameters from URL
     *
     * @param query The query string
     * @return Map of query parameters
     */
    private Map<String, String> extractQueryParams(String query) {
        Map<String, String> queryParams = new HashMap<>();
        if (query != null && !query.isEmpty()) {
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                int idx = pair.indexOf("=");
                try {
                    String key = idx > 0 ? URLDecoder.decode(pair.substring(0, idx), "UTF-8") : pair;
                    String value = idx > 0 && pair.length() > idx + 1 ?
                            URLDecoder.decode(pair.substring(idx + 1), "UTF-8") : null;
                    queryParams.put(key, value);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        }
        return queryParams;
    }

}