package com.rc.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rc.domain.UserWallet;
import com.baomidou.mybatisplus.extension.service.IService;
public interface UserWalletService extends IService<UserWallet>{

    // 根据用户id分页查询用户钱包
    Page<UserWallet> findByPage(Page<UserWallet> page, Long userId);
}
