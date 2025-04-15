package com.rc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.rc.domain.AccountDetail;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rc.domain.Account;
import com.rc.mapper.AccountMapper;
import com.rc.service.AccountService;
@Service
public class AccountServiceImpl extends ServiceImpl<AccountMapper, Account> implements AccountService{

    private Account getCoinAccount(Long coinId, Long userId) {
        return getOne(new LambdaQueryWrapper<Account>()
                .eq(Account::getCoinId, coinId)
                .eq(Account::getUserId, userId)
                .eq(Account::getStatus, 1)
        );
    }
    @Override
    public boolean transferAccountAmount(Long adminId, Long userId, Long coinId, BigDecimal num, BigDecimal fee,
                                         Long orderId, String remark, String businessType, Byte direction) {
        Account coinAccount = getCoinAccount(coinId, userId);
        if (coinAccount == null) {
            throw new IllegalArgumentException("用户当前的币种的余额不存在");
        }

        AccountDetail accountDetail = new AccountDetail();
        accountDetail.setUserId(userId);
        accountDetail.setCoinId(coinId);
        accountDetail.setAmount(num);
        accountDetail.setFee(fee);
        accountDetail.setOrderId(orderId);
        accountDetail.setAccountId(adminId);
        accountDetail.setBusinessType(businessType);
        accountDetail.setDirection(direction);
        accountDetail.setRemark(remark);
        accountDetail.setCreated(new Date());
        accountDetail.setRefAccountId(adminId);

        boolean save = accountDetailService.save(accountDetail); //need to add account controller
        if (save) {
            coinAccount.setBalanceAmount(coinAccount.getBalanceAmount().add(num));
            return updateById(coinAccount);
        }

        return save;
    }
}
