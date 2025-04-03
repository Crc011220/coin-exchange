package com.rc.service;

import com.rc.domain.SysPrivilege;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface SysPrivilegeService extends IService<SysPrivilege>{

    // 获取所有权限
    List<SysPrivilege> getAllSysPrivilege(Long menuId, Long roleId);
}
