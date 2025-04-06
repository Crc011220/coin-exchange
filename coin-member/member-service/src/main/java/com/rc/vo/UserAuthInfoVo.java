package com.rc.vo;

import com.rc.domain.User;
import com.rc.domain.UserAuthAuditRecord;
import com.rc.domain.UserAuthInfo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "用户认证信息")
public class UserAuthInfoVo implements Serializable {
    @ApiModelProperty(value = "用户信息")
    private User user;
    @ApiModelProperty(value = "用户认证信息列表")
    private List<UserAuthInfo> userAuthInfoList;
    @ApiModelProperty(value = "用户认证审核记录列表")
    private List<UserAuthAuditRecord> userAuthAuditRecordList;

}
