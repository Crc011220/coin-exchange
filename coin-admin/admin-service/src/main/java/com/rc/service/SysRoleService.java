package com.rc.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rc.domain.SysRole;
import com.baomidou.mybatisplus.extension.service.IService;
public interface SysRoleService extends IService<SysRole>{

    // 判断用户是否是超级管理员
    boolean isSuperAdmin(Long userId);

    // 使用角色的名称 模糊分页角色查询
    Page<SysRole> findByPage(Page<SysRole> page, String name);
}
