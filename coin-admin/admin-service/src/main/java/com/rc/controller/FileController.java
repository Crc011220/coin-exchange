package com.rc.controller;

import cn.hutool.core.date.DateUtil;
import com.rc.model.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Objects;

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
}