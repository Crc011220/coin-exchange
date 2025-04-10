package com.rc.controller;

import com.rc.model.R;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Api(tags = "财务系统的测试接口")
public class TestController {

    @GetMapping("/test")
    public R<String> test(){
        return R.ok("测试成功") ;
    }
}

