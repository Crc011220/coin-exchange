package com.rc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.rc.domain.AccountDetail;
import com.rc.domain.Coin;
import com.rc.domain.Config;
import com.rc.dto.MarketDto;
import com.rc.feign.MarketServiceFeign;
import com.rc.mappers.AccountVoMappers;
import com.rc.service.AccountDetailService;
import com.rc.service.CoinService;
import com.rc.service.ConfigService;
import com.rc.vo.AccountVo;
import com.rc.vo.SymbolAssetVo;
import com.rc.vo.UserTotalAccountVo;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.A;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rc.domain.Account;
import com.rc.mapper.AccountMapper;
import com.rc.service.AccountService;
import org.springframework.util.CollectionUtils;

@Service
@Slf4j
public class AccountServiceImpl extends ServiceImpl<AccountMapper, Account> implements AccountService{

    @Autowired
    private AccountDetailService accountDetailService;

    @Autowired
    private CoinService coinService;

    @Autowired
    private ConfigService configService;

    @Autowired
    private MarketServiceFeign marketServiceFeign;

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
        UserTotalAccountVo  userTotalAccountVo = new UserTotalAccountVo();
        BigDecimal basicCoin = BigDecimal.ZERO; // 用户的总金额
        BigDecimal basicCoin2Cny = BigDecimal.ONE; // cny和平台币的汇率
        List<AccountVo> assertList = new ArrayList<>();
        List<Account> list = list(new LambdaQueryWrapper<Account>()
                .eq(Account::getUserId, userId));
        if (CollectionUtils.isEmpty(list)){
            userTotalAccountVo.setAssertList(assertList);
            userTotalAccountVo.setAmount(BigDecimal.ZERO);
            userTotalAccountVo.setAmountUs(BigDecimal.ZERO);
            return userTotalAccountVo;
        }


        AccountVoMappers Instance = Mappers.getMapper(AccountVoMappers.class);
        for (Account account : list) {
            AccountVo accountVo = Instance.toAccountVo(account);
            Long coinId = account.getCoinId();
            Coin coin = coinService.getById(coinId);

            if (coin == null || coin.getStatus() == (byte)0) {
                continue;
            }
            accountVo.setCoinName(coin.getName());
            accountVo.setCoinImgUrl(coin.getImg());
            accountVo.setCoinType(coin.getType());
            accountVo.setWithdrawFlag(coin.getWithdrawFlag());
            accountVo.setRechargeFlag(coin.getRechargeFlag());
            accountVo.setFeeRate(BigDecimal.valueOf(coin.getRate()));
            accountVo.setMinFeeNum(coin.getMinFeeNum());

            assertList.add(accountVo);
            BigDecimal volume = accountVo.getFreezeAmount().add(accountVo.getBalanceAmount());
            accountVo.setCarryingAmount(volume); // 总账户余额


            BigDecimal total = volume.multiply(getCurrentCoinPrice(coinId));
            basicCoin = basicCoin.add(total);
        }

        userTotalAccountVo.setAmount(basicCoin.multiply(basicCoin2Cny).setScale(8, RoundingMode.HALF_UP)); // 用户的总金额 人民币
        userTotalAccountVo.setAmountUs(basicCoin); // 用户的总金额 USDT平台币
        userTotalAccountVo.setAssertList(assertList);

        return userTotalAccountVo;
    }


    @Override
    public SymbolAssetVo getSymbolAssert(String symbol, Long userId) {

        /**
         * 远程调用获取市场
         */
        MarketDto marketDto = marketServiceFeign.findBySymbol(symbol);
        SymbolAssetVo symbolAssetVo = new SymbolAssetVo();
        // 查询报价货币
        @NotNull Long buyCoinId = marketDto.getBuyCoinId(); // 报价货币的Id
        Account buyCoinAccount = getCoinAccount(buyCoinId, userId);
        symbolAssetVo.setBuyAmount(buyCoinAccount.getBalanceAmount());
        symbolAssetVo.setBuyLockAmount(buyCoinAccount.getFreezeAmount());
        // 市场里面配置的值
        symbolAssetVo.setBuyFeeRate(marketDto.getFeeBuy());
        Coin buyCoin = coinService.getById(buyCoinId);
        symbolAssetVo.setBuyUnit(buyCoin.getName());
        // 查询基础汇报
        @NotBlank Long sellCoinId = marketDto.getSellCoinId();
        Account coinAccount = getCoinAccount(sellCoinId, userId);
        symbolAssetVo.setSellAmount(coinAccount.getBalanceAmount());
        symbolAssetVo.setSellLockAmount(coinAccount.getFreezeAmount());
        // 市场里面配置的值
        symbolAssetVo.setSellFeeRate(marketDto.getFeeSell());
        Coin sellCoin = coinService.getById(sellCoinId);
        symbolAssetVo.setSellUnit(sellCoin.getName());

        return symbolAssetVo;
    }

    @Override
    public void transferBuyAmount(Long fromUserId, Long toUserId, Long coinId, BigDecimal amount, String businessType, Long orderId) {
        Account fromAccount = getCoinAccount(coinId, fromUserId);
        if (fromAccount == null) {
            log.error("资金划转-资金账户异常，userId:{}, coinId:{}", fromUserId, coinId);
            throw new IllegalArgumentException("资金账户异常");
        } else {
            Account toAccount = getCoinAccount(toUserId, coinId);
            if (toAccount == null) {
                throw new IllegalArgumentException("资金账户异常");
            } else {
                boolean count1 = decreaseAmount(fromAccount, amount);
                boolean count2 = addAmount(toAccount, amount);
                if (count1 && count2) {
                    List<AccountDetail> accountDetails = new ArrayList<>(2);
                    AccountDetail fromAccountDetail = AccountDetail.builder()
                            .userId(fromUserId)
                            .coinId(coinId)
                            .accountId(fromAccount.getId())
                            .refAccountId(toAccount.getId())
                            .orderId(orderId)
                            .direction((byte) 2)
                            .businessType(businessType)
                            .amount(amount)
                            .fee(BigDecimal.ZERO)
                            .remark(businessType)
                            .created(new Date())
                            .build();
                    AccountDetail toAccountDetail = AccountDetail.builder()
                            .userId(toUserId)
                            .coinId(coinId)
                            .accountId(toAccount.getId())
                            .refAccountId(fromAccount.getId())
                            .orderId(orderId)
                            .direction((byte) 1)
                            .businessType(businessType)
                            .amount(amount)
                            .fee(BigDecimal.ZERO)
                            .remark(businessType)
                            .created(new Date())
                            .build();
                    accountDetails.add(fromAccountDetail);
                    accountDetails.add(toAccountDetail);

                    accountDetailService.saveBatch(accountDetails);
                } else {
                    throw new RuntimeException("资金划转失败");
                }
            }
        }
    }

    private boolean addAmount(Account account, BigDecimal amount) {
        account.setBalanceAmount(account.getBalanceAmount().add(amount));
        return updateById(account);
    }

    private boolean decreaseAmount(Account account, BigDecimal amount) {
        account.setBalanceAmount(account.getBalanceAmount().subtract(amount));
        return updateById(account);
    }


    @Override
    public void transferSellAmount(Long fromUserId, Long toUserId, Long coinId, BigDecimal amount, String businessType, Long orderId) {
        Account fromAccount = getCoinAccount(coinId, fromUserId);
        if (fromAccount == null) {
            log.error("资金划转-资金账户异常，userId:{}, coinId:{}", fromUserId, coinId);
            throw new IllegalArgumentException("资金账户异常");
        } else {
            Account toAccount = getCoinAccount(toUserId, coinId);
            if (toAccount == null) {
                throw new IllegalArgumentException("资金账户异常");
            } else {
                boolean count1 = addAmount(fromAccount, amount);
                boolean count2 = decreaseAmount(toAccount, amount);
                if (count1 && count2) {
                    List<AccountDetail> accountDetails = new ArrayList<>(2);
                    AccountDetail fromAccountDetail = AccountDetail.builder()
                            .userId(fromUserId)
                            .coinId(coinId)
                            .accountId(fromAccount.getId())
                            .refAccountId(toAccount.getId())
                            .orderId(orderId)
                            .direction((byte) 2)
                            .businessType(businessType)
                            .amount(amount)
                            .fee(BigDecimal.ZERO)
                            .remark(businessType)
                            .created(new Date())
                            .build();

                    AccountDetail toAccountDetail = AccountDetail.builder()
                            .userId(toUserId)
                            .coinId(coinId)
                            .accountId(toAccount.getId())
                            .refAccountId(fromAccount.getId())
                            .orderId(orderId)
                            .direction((byte) 1)
                            .businessType(businessType)
                            .amount(amount)
                            .fee(BigDecimal.ZERO)
                            .remark(businessType)
                            .created(new Date())
                            .build();

                    accountDetails.add(fromAccountDetail);
                    accountDetails.add(toAccountDetail);

                    accountDetailService.saveBatch(accountDetails);
                } else {
                    throw new RuntimeException("资金划转失败");
                }
            }
        }
    }



    // 获取当前币种的价格
    private BigDecimal getCurrentCoinPrice(Long coinId) {
        Config platformCoinId = configService.getConfigByCode("PLATFORM_COIN_ID");
        if (platformCoinId == null) {
            throw new IllegalArgumentException("平台币的id不存在, 请联系管理员");
        }
        Long basicCoinId = Long.valueOf(platformCoinId.getValue());
        if (basicCoinId.equals(coinId)) { // 该币就是基础币 1:1兑换
            return BigDecimal.ONE;
        }
        // 不是基础币 需要换算 使用基础币作为报价货币
        MarketDto marketDto = marketServiceFeign.findBySellAndBuyCoinId(basicCoinId, coinId);
        // 存在交易对
        if (marketDto != null) {
            return marketDto.getOpenPrice();
        } else{
            log.info("不存在该交易对, 请联系管理员进行添加");
            return BigDecimal.ZERO;
        }
    }


}
