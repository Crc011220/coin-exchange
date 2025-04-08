package com.rc.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@ApiModel(description = "重置交易密码")
public class UnsetPasswordParam {
    @ApiModelProperty(value = "新的交易密码")
    @NotBlank
    private String payPassword;
    @ApiModelProperty(value = "验证码")
    @NotBlank
    private String validateCode;
}
