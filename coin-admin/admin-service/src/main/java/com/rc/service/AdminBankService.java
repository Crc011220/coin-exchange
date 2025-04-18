package com.rc.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rc.domain.AdminBank;
import com.baomidou.mybatisplus.extension.service.IService;
import com.rc.dto.AdminBankDto;

import java.util.List;

public interface AdminBankService extends IService<AdminBank>{

    // 条件查询 通过银行卡号查询
    Page<AdminBank> findByPage(Page<AdminBank> page, String bankCard);

    // 查询所有银行卡信息
    List<AdminBankDto> getAllAdminBanks();

}
