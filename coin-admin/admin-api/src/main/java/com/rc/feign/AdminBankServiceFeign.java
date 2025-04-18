package com.rc.feign;

import com.rc.config.feign.OAuth2FeignConfig;
import com.rc.dto.AdminBankDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(value = "admin-service", path = "/adminBanks", configuration = OAuth2FeignConfig.class)
public interface AdminBankServiceFeign {

    @GetMapping("list")
    List<AdminBankDto> getAllAdminBanks();
}
