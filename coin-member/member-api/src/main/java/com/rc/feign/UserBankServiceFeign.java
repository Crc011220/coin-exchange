package com.rc.feign;

import com.rc.config.feign.OAuth2FeignConfig;
import com.rc.dto.UserBankDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 若FeignClient 里面的name 相同时,spring 创建对象就会报错:它认为它们2个对象是一样的，加上contextId 解决
 */
@FeignClient(name = "member-service",contextId = "userBankServiceFeign" ,configuration = OAuth2FeignConfig.class ,path = "/userBanks")
public interface UserBankServiceFeign {

    @GetMapping("/{userId}/info")
    UserBankDto getUserBankInfo(@PathVariable Long userId) ;
}

