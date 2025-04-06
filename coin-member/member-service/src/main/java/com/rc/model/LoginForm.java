package com.rc.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "前端的登录参数")
public class LoginForm {

    @ApiModelProperty(value = "电话的国家区号")
    private String countryCode ;

    @ApiModelProperty(value = "用户名称")
    private String username ;

    @ApiModelProperty(value = "用户密码")
    private String password ;

    @ApiModelProperty(value = "用户的UUID")
    private String uuid ;

    @ApiModelProperty(value = "极验的challenge")
    private String geetest_challenge ;

    @ApiModelProperty(value = "极验的validate")
    private String geetest_validate ;

    @ApiModelProperty(value = "极验的seccode")
    private String geetest_seccode ;
}

