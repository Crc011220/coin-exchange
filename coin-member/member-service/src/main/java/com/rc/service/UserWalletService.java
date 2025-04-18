package com.rc.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rc.domain.UserWallet;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface UserWalletService extends IService<UserWallet>{

    // 根据用户id分页查询用户钱包
    Page<UserWallet> findByPage(Page<UserWallet> page, Long userId);

    // 查询用户的提币地址
    List<UserWallet> findUserWallets(Long userId, Long coinId);

    // 删除用户的提币地址
    boolean deleteUserWallet(Long addressId, String payPassword);
}
