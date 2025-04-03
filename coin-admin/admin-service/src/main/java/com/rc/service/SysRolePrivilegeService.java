package com.rc.service;

import com.rc.domain.SysMenu;
import com.rc.domain.SysRolePrivilege;
import com.baomidou.mybatisplus.extension.service.IService;
import com.rc.model.RolePrivilegesParam;

import java.util.List;

public interface SysRolePrivilegeService extends IService<SysRolePrivilege>{

    // 根据角色ID查询菜单及权限
    List<SysMenu> findSysMenuAndPrivileges(Long roleId);

    // 授予角色权限
    boolean grantPrivileges(RolePrivilegesParam rolePrivilegesParam);
}
