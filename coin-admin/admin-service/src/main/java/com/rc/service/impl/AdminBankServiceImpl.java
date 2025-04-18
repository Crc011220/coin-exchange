package com.rc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rc.dto.AdminBankDto;
import com.rc.mappers.AdminBankDtoMappers;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rc.mapper.AdminBankMapper;
import com.rc.domain.AdminBank;
import com.rc.service.AdminBankService;
import org.springframework.util.CollectionUtils;

@Service
public class AdminBankServiceImpl extends ServiceImpl<AdminBankMapper, AdminBank> implements AdminBankService{

    @Override
    public Page<AdminBank> findByPage(Page<AdminBank> page, String bankCard) {
        return page(page, new LambdaQueryWrapper<AdminBank>()
                .like(StringUtils.isNotEmpty(bankCard), AdminBank::getBankCard, bankCard)
        );
    }

    @Override
    public List<AdminBankDto> getAllAdminBanks() {
        List<AdminBank> adminBanks = list(new LambdaQueryWrapper<AdminBank>().eq(AdminBank::getStatus, 1)); // 状态为1的银行卡(启用的银行卡)
        if (CollectionUtils.isEmpty(adminBanks)){
            return Collections.emptyList() ;
        }

        return AdminBankDtoMappers.INSTANCE.toConvertDto(adminBanks);
    }

}
