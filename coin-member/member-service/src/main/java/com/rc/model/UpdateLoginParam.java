package com.rc.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@ApiModel(description = "修改用户登录密码的参数")
public class UpdateLoginParam {

    @ApiModelProperty(value = "原始密码")
    @JsonProperty("oldpassword")
    @NotBlank
    private String oldPassword;

    @ApiModelProperty(value = "新密码")
    @JsonProperty("newpassword")
    @NotBlank
    private String newPassword;

    @ApiModelProperty(value = "验证码")
    private String validateCode;
}
