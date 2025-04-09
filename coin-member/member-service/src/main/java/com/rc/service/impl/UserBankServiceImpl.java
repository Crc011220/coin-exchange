package com.rc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rc.domain.User;
import com.rc.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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

    @Autowired
    private UserService userService;

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

    @Override
    public boolean bindBank(Long userId, UserBank userBank) {
        String payPassword = userBank.getPayPassword();
        User user = userService.getById(userId);
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        if (!bCryptPasswordEncoder.matches(payPassword, user.getPaypassword())){
            throw new IllegalArgumentException("支付密码不正确");
        }

        Long id = userBank.getId(); // 有id代表修改操作
        if (id != null){
            UserBank userBankDb = getById(id);
            if (userBankDb == null){
                throw new IllegalArgumentException("该银行卡不存在, 用户银行卡id输入错误");
            }
            return updateById(userBank);
        }
        userBank.setUserId(userId);
        return save(userBank);
    }
}
