package com.rc.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "重置登录密码")
public class UnsetPasswordParam extends GeetestForm{

    @ApiModelProperty(value = "国家码")
    private String countryCode;

    @ApiModelProperty(value = "手机号码")
    private String mobile;

    @ApiModelProperty(value = "新的登录密码")
    private String password;

    @ApiModelProperty(value = "验证码")
    private String validateCode;

}
