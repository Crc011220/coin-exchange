package com.rc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rc.mapper.UserBankMapper;
import com.rc.domain.UserBank;
import com.rc.service.UserBankService;
@Service
public class UserBankServiceImpl extends ServiceImpl<UserBankMapper, UserBank> implements UserBankService{

    @Override
    public Page<UserBank> findByPage(Page<UserBank> page, Long usrId) {
        return page(page, new LambdaQueryWrapper<UserBank>()
                .eq(usrId != null, UserBank::getUserId, usrId));
    }

    @Override
    public UserBank getUserBankByUserId(Long userId) {
        // 1代表该银行卡启用
        return getOne(new LambdaQueryWrapper<UserBank>().eq(UserBank::getUserId, userId).eq(UserBank::getStatus, 1));
    }

    //TODO
    @Override
    public boolean bindBank(Long userId, UserBank userBank) {
        Long id = userBank.getId(); // 有id代表修改操作
        if (id != null){
            UserBank byId = getById(id);
            if (byId != null){
                userBank.setLastUpdateTime(new Date());
                return updateById(userBank);
            }
        }
        return false;
    }
}
