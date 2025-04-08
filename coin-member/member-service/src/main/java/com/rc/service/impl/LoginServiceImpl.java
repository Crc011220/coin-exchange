package com.rc.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.enums.ApiErrorCode;
import com.baomidou.mybatisplus.extension.exceptions.ApiException;
import com.rc.feign.JwtToken;
import com.rc.feign.OAuth2FeignClient;
import com.rc.geetest.GeetestLib;
import com.rc.geetest.GeetestLibResult;
import com.rc.model.LoginForm;
import com.rc.model.LoginUser;
import com.rc.service.LoginService;
import com.rc.util.IpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class LoginServiceImpl implements LoginService {


    @Autowired
    private OAuth2FeignClient oAuth2FeignClient;

    @Value("${basic.token:Basic Y29pbi1hcGk6Y29pbi1zZWNyZXQ=}")
    private String basicToken;

    @Autowired
    private StringRedisTemplate strRedisTemplate;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private GeetestLib geetestLib;

    /**
     * 会员的登录
     *
     * @param loginForm 登录的表单参数
     * @return 登录的结果
     */
    @Override
    public LoginUser login(LoginForm loginForm) {
        log.info("用户{}开始登录", loginForm.getUsername());
        checkFormData(loginForm);
        LoginUser loginUser = null;
        log.info("密码:{}", loginForm.getPassword());
        // 登录就是使用用户名和密码换一个token 而已--->远程调用->authorization-server
        ResponseEntity<JwtToken> tokenResponseEntity = oAuth2FeignClient.getToken("password",
                loginForm.getUsername(), loginForm.getPassword(), "member_type", basicToken);

        if (tokenResponseEntity.getStatusCode() == HttpStatus.OK) {
            JwtToken jwtToken = tokenResponseEntity.getBody();
            log.info("远程调用成功,结果为", JSON.toJSONString(jwtToken, true));
            // token 必须包含bearer
            loginUser = new LoginUser(loginForm.getUsername(), jwtToken.getExpiresIn(), jwtToken.getTokenType() + " " + jwtToken.getAccessToken(), jwtToken.getRefreshToken());
            // 使用网关解决登出的问题:
            // token 是直接存储的
            strRedisTemplate.opsForValue().set(jwtToken.getAccessToken(), "", jwtToken.getExpiresIn(), TimeUnit.SECONDS);
        }
        return loginUser;
    }

    /**
     * 校验数据
     * 极验证的数据校验
     *
     * @param loginForm
     */
    private void checkFormData(LoginForm loginForm) {
        String challenge = loginForm.getGeetest_challenge();
        String validate = loginForm.getGeetest_validate();
        String seccode = loginForm.getGeetest_seccode();
        int status = 0;
        String userId = "";
        // session必须取出值，若取不出值，直接当做异常退出
        String statusStr = redisTemplate.opsForValue().get(GeetestLib.GEETEST_SERVER_STATUS_SESSION_KEY + ":" + loginForm.getUuid()).toString();
        status = Integer.parseInt(statusStr);
        userId = redisTemplate.opsForValue().get(GeetestLib.GEETEST_SERVER_USER_KEY + ":" + loginForm.getUuid()).toString();
        GeetestLibResult result = null;
        if (status == 1) {
            /*
            自定义参数,可选择添加
                user_id 客户端用户的唯一标识，确定用户的唯一性；作用于提供进阶数据分析服务，可在register和validate接口传入，不传入也不影响验证服务的使用；若担心用户信息风险，可作预处理(如哈希处理)再提供到极验
                client_type 客户端类型，web：电脑上的浏览器；h5：手机上的浏览器，包括移动应用内完全内置的web_view；native：通过原生sdk植入app应用的方式；unknown：未知
                ip_address 客户端请求sdk服务器的ip地址
            */
            Map<String, String> paramMap = new HashMap<>();
            paramMap.put("user_id", userId);
            paramMap.put("client_type", "web");
            ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            paramMap.put("ip_address", IpUtil.getIpAddr(servletRequestAttributes.getRequest()));
            result = geetestLib.successValidate(challenge, validate, seccode, paramMap);
            log.info("验证的结果为{}", JSON.toJSONString(result));
        } else {
            result = geetestLib.failValidate(challenge, validate, seccode);
        }
        if(result.getStatus()!=1){
            log.error("验证异常",JSON.toJSONString(result,true));
            throw new IllegalArgumentException("验证码验证异常") ;
        }

    }
}

