package com.rc.model;

import com.rc.domain.SysMenu;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

/**
 * 登录的返回值
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value = "登录的结果")
public class LoginResult {

    /**
     * 登录产生的token, 来自authorization-server
     */
    @ApiModelProperty(value = "登录产生的token, 来自authorization-server")
    private String token ;

    /**
     * 用户的前端的菜单数据
     */
    @ApiModelProperty(value = "用户的前端的菜单数据")
    private List<SysMenu> menus ;

    /**
     * 权限数据
     */
    @ApiModelProperty(value = "权限数据")
    private List<SimpleGrantedAuthority> authorities ;

}

