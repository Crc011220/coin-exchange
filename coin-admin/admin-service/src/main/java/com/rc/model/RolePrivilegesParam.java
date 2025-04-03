package com.rc.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Collections;
import java.util.List;

@Data
@ApiModel(value = "接收角色权限参数")
public class RolePrivilegesParam {
    @ApiModelProperty(value = "角色ID")
    private Long roleId;
    @ApiModelProperty(value = "角色的权限ID列表")
    private List<Long> privilegeIds = Collections.emptyList();
}
