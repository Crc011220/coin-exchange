package com.rc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.rc.domain.AccountDetail;
import com.rc.domain.Coin;
import com.rc.domain.Config;
import com.rc.service.AccountDetailService;
import com.rc.service.CoinService;
import com.rc.service.ConfigService;
import com.rc.vo.UserTotalAccountVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rc.domain.Account;
import com.rc.mapper.AccountMapper;
import com.rc.service.AccountService;
import org.springframework.util.CollectionUtils;

@Service
public class AccountServiceImpl extends ServiceImpl<AccountMapper, Account> implements AccountService{

    @Autowired
    private AccountDetailService accountDetailService;

    @Autowired
    private CoinService coinService;

    @Autowired
    private ConfigService configService;

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

        boolean save = accountDetailService.save(accountDetail);
        if (save) {
            coinAccount.setBalanceAmount(coinAccount.getBalanceAmount().add(num));
            return updateById(coinAccount);
        }

        return false;
    }

    // 余额扣减操作
    @Override
    public Boolean decreaseAccountAmount(Long adminId, Long userId, Long coinId, Long orderId, BigDecimal num, BigDecimal fee, String remark, String businessType, Byte direction) {
        Account coinAccount = getCoinAccount(coinId, userId);
        if (coinAccount == null) {
            throw new IllegalArgumentException("用户当前的币种的余额不存在");
        }

        AccountDetail accountDetail = new AccountDetail();
        accountDetail.setUserId(userId);
        accountDetail.setAccountId(coinAccount.getId());
        accountDetail.setRefAccountId(coinAccount.getId());
        accountDetail.setRemark(remark);
        accountDetail.setOrderId(orderId);
        accountDetail.setBusinessType(businessType);
        accountDetail.setDirection(direction);
        boolean save = accountDetailService.save(accountDetail);
        if (save){
            BigDecimal subtract = coinAccount.getBalanceAmount().subtract(num);
            if (subtract.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("用户当前的币种的余额不足");
            }
            coinAccount.setBalanceAmount(subtract);
            return updateById(coinAccount);
        }
        return false;
    }

    @Override
    public Account findByUserAndCoin(Long userId, String coinName) {
        Coin coin = coinService.getCoinByCoinName(coinName);
        if (coin == null) {
            throw new IllegalArgumentException("币种不存在");
        }
        Account account = getOne(new LambdaQueryWrapper<Account>()
                .eq(Account::getUserId, userId)
                .eq(Account::getCoinId, coin.getId())
        );
        if (account == null) {
            throw new IllegalArgumentException("该资产不存在");
        }

        Config sellRateConfig = configService.getConfigByCode("USDT2CNY");
        account.setSellRate(new BigDecimal(sellRateConfig.getValue())); // 出售的费率

        Config setBuyRateConfig = configService.getConfigByCode("CNY2USDT");
        account.setBuyRate(new BigDecimal(setBuyRateConfig.getValue())); // 买进来的费率


        return account ;
    }

    /**
     * 扣减总资产 增加冻结资产
     *
     * @param userId  用户的id
     * @param coinId  币种的id
     * @param mum     锁定的金额
     * @param type    资金流水的类型
     * @param orderId 订单的Id
     * @param fee     本次操作的手续费
     */
    @Override
    public void lockUserAmount(Long userId, Long coinId, BigDecimal mum, String type, Long orderId, BigDecimal fee) {
        Account account = getOne(new LambdaQueryWrapper<Account>()
                .eq(Account::getUserId, userId)
                .eq(Account::getCoinId, coinId)
        );
        if (account == null) {
            throw new IllegalArgumentException("您输入的资产类型不存在");
        }
        BigDecimal balanceAmount = account.getBalanceAmount();
        if (balanceAmount.compareTo(mum) < 0) { // 库存的操作
            throw new IllegalArgumentException("账号的资金不足");
        }
        account.setBalanceAmount(balanceAmount.subtract(mum));
        account.setFreezeAmount(account.getFreezeAmount().add(mum));
        boolean updateById = updateById(account);
        if (updateById) {  // 增加流水记录
            AccountDetail accountDetail = new AccountDetail(
                    null,
                    userId,
                    coinId,
                    account.getId(),
                    account.getId(), // 如果该订单时邀请奖励,有我们的ref的account,否则,值和account 是一样的
                    orderId,
                    (byte) 2, // 出帐
                    type,
                    mum,
                    fee,
                    "用户提现",
                    null,
                    null,
                    null
            );
            accountDetailService.save(accountDetail);
        }
    }

    @Override
    public UserTotalAccountVo getUserTotalAccount(Long userId) {
        List<Account> list = list(new LambdaQueryWrapper<Account>()
                .eq(Account::getUserId, userId));
        if (CollectionUtils.isEmpty(list)){
            return null;
        }
        return null;
    }
}
