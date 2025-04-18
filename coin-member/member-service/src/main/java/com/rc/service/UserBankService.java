package com.rc.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rc.domain.UserBank;
import com.baomidou.mybatisplus.extension.service.IService;
public interface UserBankService extends IService<UserBank>{

    // 根据用户ID分页 查询用户银行卡列表
    Page<UserBank> findByPage(Page<UserBank> page, Long usrId);

    // 通过用户id 查询用户银行卡
    UserBank getUserBankByUserId(Long userId);

    // 绑定用户的银行卡
    boolean bindBank(Long userId, UserBank userBank);

    // 根据用户id找到当前用户的银行卡
    UserBank getCurrentUserBank(Long userId);
}
