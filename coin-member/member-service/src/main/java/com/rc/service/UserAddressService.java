package com.rc.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rc.domain.UserAddress;
import com.baomidou.mybatisplus.extension.service.IService;
public interface UserAddressService extends IService<UserAddress>{

    // 根据用户id分页查询 用户地址列表
    Page<UserAddress> findByPage(Page<UserAddress> page, Long userId);
}
