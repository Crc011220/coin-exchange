package com.rc.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rc.domain.UserBank;
import com.baomidou.mybatisplus.extension.service.IService;
public interface UserBankService extends IService<UserBank>{

    // 根据用户ID分页 查询用户银行卡列表
    Page<UserBank> findByPage(Page<UserBank> page, Long usrId);
}
