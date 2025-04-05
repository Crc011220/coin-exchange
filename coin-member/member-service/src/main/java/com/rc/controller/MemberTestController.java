package com.rc.controller;

import com.rc.model.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Api(tags = "会员系统接口的测试")
public class MemberTestController {

    @ApiOperation(value = "这是一个测试接口",authorizations = {@Authorization("Authorization")})
    @GetMapping("/member/test")
    public R<String> test(){
       return R.ok("测试成功");
    }

}

