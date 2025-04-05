package com.rc.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rc.domain.SysUser;
import com.baomidou.mybatisplus.extension.service.IService;
public interface SysUserService extends IService<SysUser>{

    // 通过手机号或姓名分页查询员工用户
    Page<SysUser> findByPage(Page<SysUser> page, String mobile, String fullName);

    // 新增员工用户
    boolean addUser(SysUser sysUser);

    // 修改员工用户
    boolean updateUser(SysUser sysUser);
}
