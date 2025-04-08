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
        Map<String, String> uploadPolicy = getUploadPolicy(30L,3*1024*1024L,dir,"");
        return R.ok(uploadPolicy);
    }

    private Map<String, String> getUploadPolicy(Long expireTime, Long maxFileSize, String dir, String callbackUrl) {
        // Create response map for frontend
        Map<String, String> respMap = new LinkedHashMap<>();

        try {
            // Create S3 presigner with explicit endpoint configuration
            S3Presigner presigner = S3Presigner.builder()
                    .region(Region.of(region))
                    // Force the use of the standard AWS endpoint
                    .endpointOverride(null)
                    .build();

            // Calculate expiration time
            long expireEndTime = System.currentTimeMillis() + expireTime * 1000;

            // Generate a pre-signed URL for each potential file (we'll use a placeholder)
            String objectKey = dir + "/${filename}";

            // Create a presigned request - note this is for PUT object
            PutObjectRequest objectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .contentType("application/octet-stream")
                    .build();

            PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofSeconds(expireTime))
                    .putObjectRequest(objectRequest)
                    .build();

            PresignedPutObjectRequest presignedRequest = presigner.presignPutObject(presignRequest);
            URL presignedUrl = presignedRequest.url();

            // Manually construct the proper S3 endpoint URL
            String s3Endpoint = "https://" + bucketName + ".s3." + region + ".amazonaws.com";

            // Build response with hardcoded S3 endpoint
            respMap.put("url", presignedRequest.url().toString());  // Base URL with directory
            respMap.put("host", s3Endpoint);  // Full S3 endpoint
            respMap.put("dir", dir);
            respMap.put("expire", String.valueOf(expireEndTime / 1000));

            // Extract query parameters from the presigned URL
            Map<String, String> queryParams = extractQueryParams(presignedUrl.getQuery());

            // Add AWS specific query parameters
            respMap.put("X-Amz-Algorithm", queryParams.get("X-Amz-Algorithm"));
            respMap.put("X-Amz-Credential", queryParams.get("X-Amz-Credential"));
            respMap.put("X-Amz-Date", queryParams.get("X-Amz-Date"));
            respMap.put("X-Amz-Expires", queryParams.get("X-Amz-Expires"));
            respMap.put("X-Amz-SignedHeaders", queryParams.get("X-Amz-SignedHeaders"));
            respMap.put("X-Amz-Signature", queryParams.get("X-Amz-Signature"));

            // For file size limitation, we'll inform the frontend
            respMap.put("maxFileSize", String.valueOf(maxFileSize));

            // Add callback URL if provided
            if (callbackUrl != null && !callbackUrl.isEmpty()) {
                respMap.put("callbackUrl", callbackUrl);
            }

            // Close the presigner
            presigner.close();

        } catch (Exception e) {
            e.printStackTrace();
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