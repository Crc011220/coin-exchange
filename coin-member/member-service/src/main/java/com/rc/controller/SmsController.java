package com.rc.controller;

import com.rc.domain.Sms;
import com.rc.model.R;
import com.rc.service.SmsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sms")
@Api(tags = "sms短信发送平台")
public class SmsController {


    @Autowired
    private SmsService smsService;

    @PostMapping("/sendTo")
    @ApiOperation(value = "发送短信")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "sms", value = "短信的json数据")
    })
    public R sendSms(@RequestBody @Validated Sms sms) {
        boolean isOk = smsService.sendSms(sms);
        if (isOk) {
            return R.ok();
        }
        return R.fail("发送失败");
    }
}

