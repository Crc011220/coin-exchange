package com.rc.service;

import com.rc.domain.SysMenu;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface SysMenuService extends IService<SysMenu>{

    // 根据用户ID获取菜单列表
    List<SysMenu> getMenusByUserId(Long userId);

}
