package com.rc.service.impl;

import com.alibaba.fastjson.JSON;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rc.domain.Sms;
import com.rc.mapper.SmsMapper;
import com.rc.service.SmsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SmsServiceImpl extends ServiceImpl<SmsMapper, Sms> implements SmsService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Value("${aws.s3.region}")
    private String region;

    @Value("${aws.s3.access-key}")
    private String accessKey;

    @Value("${aws.s3.secret-key}")
    private String secretKey;

    // 发送短信的方法
    @Override
    public boolean sendMsg(Sms sms) {
        log.info("发送短信{}", JSON.toJSONString(sms, true));

        // 生成验证码并设置短信内容
        String code = generateVerificationCode();
        sms.setContent("Your verification code is：" + code + "，the validity period is 5 minutes.");

        // 使用 AWS SNS 发送短信
        try {
            boolean result = sendSmsUsingSNS(sms);
            if (result) {
                sms.setStatus(1); // 短信发送成功
                return save(sms); // 保存发送记录
            } else {
                return false; // 短信发送失败
            }
        } catch (Exception e) {
            log.error("发送短信时发生错误: ", e);
        }
        return false;
    }

    // 生成 6 位验证码
    private String generateVerificationCode() {
        return String.format("%06d", (int) (Math.random() * 1000000)); // 生成 6 位验证码
    }

    // 使用 AWS SNS 发送短信
    private boolean sendSmsUsingSNS(Sms sms) {
        // 设置 AWS SNS 凭证
        AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
        AmazonSNS snsClient = AmazonSNSClient.builder()
                .withRegion(region)
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .build();

        // 构建 PublishRequest 对象
        PublishRequest publishRequest = new PublishRequest()
                .withPhoneNumber(sms.getMobile()) // 接收短信的手机号码
                .withMessage(sms.getContent()); // 短信内容

        try {
            // 发送短信
            PublishResult publishResult = snsClient.publish(publishRequest);
            log.info("短信发送成功，MessageId: {}", publishResult.getMessageId());
            return true; // 短信发送成功
        } catch (Exception e) {
            log.error("发送短信失败: ", e);
        }
        return false; // 短信发送失败
    }

}