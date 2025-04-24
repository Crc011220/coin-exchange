package com.rc.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rc.domain.UserAddress;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface UserAddressService extends IService<UserAddress>{

    // 根据用户id分页查询 用户地址列表
    Page<UserAddress> findByPage(Page<UserAddress> page, Long userId);

    // 获取用户的提供地址
    List<UserAddress> getUserAddressByUserId(Long userId);

    // 获取用户的某种币的钱包地址 （使用用户id和币种id）
    UserAddress getUserAddressByUserIdAndCoinId(String userId, Long coinId);
}
