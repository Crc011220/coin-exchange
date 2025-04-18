package com.rc.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.alicp.jetcache.Cache;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.CreateCache;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rc.domain.Account;
import com.rc.domain.CashWithdrawAuditRecord;
import com.rc.domain.Config;
import com.rc.dto.UserBankDto;
import com.rc.dto.UserDto;
import com.rc.feign.UserBankServiceFeign;
import com.rc.feign.UserServiceFeign;
import com.rc.mapper.CashWithdrawAuditRecordMapper;
import com.rc.model.CashSellParam;
import com.rc.service.AccountService;
import com.rc.service.ConfigService;
import com.rc.util.MobileUtils;
import org.apache.commons.lang.StringUtils;
import org.checkerframework.checker.units.qual.C;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rc.mapper.CashWithdrawalsMapper;
import com.rc.domain.CashWithdrawals;
import com.rc.service.CashWithdrawalsService;
import org.springframework.util.CollectionUtils;

@Service
public class CashWithdrawalsServiceImpl extends ServiceImpl<CashWithdrawalsMapper, CashWithdrawals> implements CashWithdrawalsService {

    @Autowired
    private UserServiceFeign userServiceFeign;

    @Autowired
    private CashWithdrawAuditRecordMapper cashWithdrawAuditRecordMapper;

    @Autowired
    private AccountService accountService;

    @Autowired
    private ConfigService configService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private UserBankServiceFeign userBankServiceFeign;

    @CreateCache(name = "CASH_WITHDRAWALS_LOCK:", expire = 100, timeUnit = TimeUnit.SECONDS, cacheType = CacheType.BOTH)
    private Cache<String, String> lock;

    @Override
    public Page<CashWithdrawals> findByPage(Page<CashWithdrawals> page, Long userId, String userName, String mobile, Byte status, String numMin, String numMax, String startTime, String endTime) {

        Map<Long, UserDto> basicUsers = null;
        LambdaQueryWrapper<CashWithdrawals> cashWithdrawalsLambdaQueryWrapper = new LambdaQueryWrapper<>();
        if (userId != null || StringUtils.isNotBlank(userName) || StringUtils.isNotBlank(mobile)) { // 说明携带了用户的相关信息
            basicUsers = userServiceFeign.getBasicUsers(userId == null ? null : Collections.singletonList(userId), userName, mobile);
            if (CollectionUtils.isEmpty(basicUsers)) { // 没有用户
                return page;
            }

            Set<Long> userIds = basicUsers.keySet();
            cashWithdrawalsLambdaQueryWrapper.in(CashWithdrawals::getUserId, userIds);
        }
        // 添加其他的条件
        cashWithdrawalsLambdaQueryWrapper
                .eq(status != null, CashWithdrawals::getStatus, status)
                .between(
                        StringUtils.isNotEmpty(numMin) && StringUtils.isNotEmpty(numMax),
                        CashWithdrawals::getNum,
                        new BigDecimal(numMin==null? "0" :numMin), new BigDecimal(numMax==null? "0" :numMax)
                )
                .between(
                        StringUtils.isNotEmpty(startTime) && StringUtils.isNotEmpty(endTime),
                        CashWithdrawals::getCreated,
                        startTime, endTime + "23:59:59"
                );

        Page<CashWithdrawals> pageData = page(page, cashWithdrawalsLambdaQueryWrapper);
        List<CashWithdrawals> records = pageData.getRecords();
        if (!CollectionUtils.isEmpty(records)) {
            List<Long> collect = records.stream().map(CashWithdrawals::getUserId).collect(Collectors.toList());

            if (basicUsers == null) {
                basicUsers = userServiceFeign.getBasicUsers(collect, null, null);
            }
            Map<Long, UserDto> finalBasicUsers = basicUsers;
            records.forEach(cashWithdrawals -> {
                UserDto userDto = finalBasicUsers.get(cashWithdrawals.getUserId());
                if (userDto != null) {
                    cashWithdrawals.setUserName(userDto.getUsername());
                    cashWithdrawals.setRealName(userDto.getRealName());
                }
            });
        }
        return pageData;
    }

    @Override
    public boolean updateWithdrawalsStatus(CashWithdrawAuditRecord cashWithdrawAuditRecord, Long userId) {
        // 上锁
        return lock.tryLockAndRun(cashWithdrawAuditRecord.getId()+"", 300, TimeUnit.SECONDS, () -> {
            CashWithdrawals cashWithdrawals = getById(cashWithdrawAuditRecord.getId());
            if (cashWithdrawals == null) {
                throw new IllegalArgumentException("现金的审核记录不存在");
            }
            CashWithdrawAuditRecord cashWithdrawAuditRecordNew = new CashWithdrawAuditRecord();
            cashWithdrawAuditRecordNew.setAuditUserId(userId);
            cashWithdrawAuditRecordNew.setRemark(cashWithdrawAuditRecord.getRemark());
            cashWithdrawAuditRecordNew.setCreated(new Date());
            cashWithdrawAuditRecordNew.setStatus(cashWithdrawAuditRecord.getStatus());
            int step = cashWithdrawals.getStep() + 1;
            cashWithdrawAuditRecordNew.setStep((byte) step);
            cashWithdrawAuditRecordNew.setOrderId(cashWithdrawals.getId());
            int insert = cashWithdrawAuditRecordMapper.insert(cashWithdrawAuditRecordNew);

            if (insert > 0) {
                cashWithdrawals.setStatus(cashWithdrawAuditRecord.getStatus());
                cashWithdrawals.setRemark(cashWithdrawAuditRecord.getRemark());
                cashWithdrawals.setLastTime(new Date());
                cashWithdrawals.setAccountId(userId);
                cashWithdrawals.setStep((byte) step);
                boolean b = updateById(cashWithdrawals);

                if (b){
                    //审核通过 开始取款
                    Boolean isOk =  accountService.decreaseAccountAmount(userId, cashWithdrawals.getUserId(),
                            cashWithdrawals.getCoinId(), cashWithdrawals.getId(),
                            cashWithdrawals.getNum(), cashWithdrawals.getFee(),
                             cashWithdrawals.getRemark(), "withdrawls_out", (byte) 2
                    );
                } else{
                    // 审核不通过
                    throw new IllegalArgumentException("审核不通过");
                }
            }
        });
    }

    @Override
    public Page<CashWithdrawals> findUserCashWithdraw(Page<CashWithdrawals> page, Long userId, Byte status) {
        return page(page, new LambdaQueryWrapper<CashWithdrawals>()
                .eq(CashWithdrawals::getUserId, userId)
                .eq(status!=null,CashWithdrawals::getStatus, status));
    }

    /**
     * GCN的卖出操作
     *
     * @param userId
     * @param cashSellParam
     * @return
     */
    @Override
    public boolean sell(Long userId, CashSellParam cashSellParam) {
        //1 参数校验
        checkCashSellParam(cashSellParam);
        Map<Long, UserDto> basicUsers = userServiceFeign.getBasicUsers(Collections.singletonList(userId), null, null);
        if (CollectionUtils.isEmpty(basicUsers)) {
            throw new IllegalArgumentException("用户的id错误");
        }

        // 得到用户的dto
        UserDto userDto = basicUsers.get(userId);
        String mobile = userDto.getMobile();
        // 转换手机格式 +xx xxx xxx xxx
        String convertedMobile = MobileUtils.convertToE164Format(mobile);
        // 2 手机验证码
        validatePhoneCode(convertedMobile,cashSellParam.getValidateCode()) ;

        // 3 支付密码
        checkUserPayPassword(userDto.getPaypassword(), cashSellParam.getPayPassword());

        // 4 远程调用查询用户的银行卡
        UserBankDto userBankInfo = userBankServiceFeign.getUserBankInfo(userId);
        if (userBankInfo == null) {
            throw new IllegalArgumentException("该用户暂未绑定银行卡信息");
        }
        String remark = RandomUtil.randomNumbers(6); // 随机生成一个6位数的订单号

        // 5 通过数量得到本次交易的金额
        BigDecimal amount = getCashWithdrawalsAmount(cashSellParam.getNum());

        // 6 计算本次的手续费
        BigDecimal fee = getCashWithdrawalsFee(amount);

        // 7 查询用户的账号ID
        Account account = accountService.findByUserAndCoin(userId, "GCN");
        // 7 订单的创建
        CashWithdrawals cashWithdrawals = new CashWithdrawals();
        cashWithdrawals.setUserId(userId);
        cashWithdrawals.setAccountId(account.getId());
        cashWithdrawals.setCoinId(cashSellParam.getCoinId());
        cashWithdrawals.setStatus((byte) 0);
        cashWithdrawals.setStep((byte) 1);
        cashWithdrawals.setNum(cashSellParam.getNum());
        cashWithdrawals.setMum(amount.subtract(fee)); // 实际金额 = amount-fee
        cashWithdrawals.setFee(fee);
        cashWithdrawals.setBank(userBankInfo.getBank());
        cashWithdrawals.setBankCard(userBankInfo.getBankCard());
        cashWithdrawals.setBankAddr(userBankInfo.getBankAddr());
        cashWithdrawals.setBankProv(userBankInfo.getBankProv());
        cashWithdrawals.setBankCity(userBankInfo.getBankCity());
        cashWithdrawals.setTruename(userBankInfo.getRealName());
        cashWithdrawals.setRemark(remark);
        boolean save = save(cashWithdrawals);
        if (save) { //
            // 扣减总资产--account-->accountDetail
            accountService.lockUserAmount(userId, cashWithdrawals.getCoinId(), cashWithdrawals.getMum(),
                    "withdrawals_out", cashWithdrawals.getId(), cashWithdrawals.getFee());
        }
        return save;
    }

    /**
     * 计算本次的手续费
     *
     * @param amount
     * @return
     */
    private BigDecimal getCashWithdrawalsFee(BigDecimal amount) {
        // 1 通过总金额* 费率 = 手续费
        // 2 若金额较小---->最小的提现的手续费

        // 最小的提现费用
        Config withdrawMinPoundage = configService.getConfigByCode("WITHDRAW_MIN_POUNDAGE");
        BigDecimal withdrawMinPoundageFee = new BigDecimal(withdrawMinPoundage.getValue());

        // 提现的费率
        Config withdrawPoundageRate = configService.getConfigByCode("WITHDRAW_POUNDAGE_RATE");


        // 通过费率计算的手续费
        BigDecimal poundageFee = amount.multiply(new BigDecimal(withdrawPoundageRate.getValue())).setScale(2, RoundingMode.HALF_UP);

        // 哪个大返回哪个
        return poundageFee.min(withdrawMinPoundageFee).equals(poundageFee) ? withdrawMinPoundageFee : poundageFee;
    }

    /**
     * 通过数量计算金额
     *
     * @param num 数量
     * @return
     */
    private BigDecimal getCashWithdrawalsAmount(BigDecimal num) {
        //
        Config rateConfig = configService.getConfigByCode("USDT2CNY");
        return num.multiply(new BigDecimal(rateConfig.getValue())).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 支付密码的校验
     *
     * @param payDBPassword 用户的支付密码
     * @param payPassword 输入的支付密码
     */
    private void checkUserPayPassword(String payDBPassword, String payPassword) {
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        boolean matches = bCryptPasswordEncoder.matches(payPassword, payDBPassword);
        if (!matches) {
            throw new IllegalArgumentException("支付密码错误");
        }
    }

    /**
     * 校验用户的手机验证码
     *
     * @param mobile 手机号
     * @param validateCode 验证码
     */
    private void validatePhoneCode(String mobile, String validateCode) {

        // 验证:SMS:CASH_WITHDRAWS:mobile
        String code = redisTemplate.opsForValue().get("SMS:CASH_WITHDRAWS:" + mobile);
        if (!validateCode.equals(code)) {
            throw new IllegalArgumentException("验证码错误");
        }

    }

    /**
     * 1 手机验证码
     * 2 支付密码
     * 3 提现相关的验证
     *
     * @param cashSellParam 参数
     */
    private void checkCashSellParam(CashSellParam cashSellParam) {
        // 1 提现状态
        Config cashWithdrawalsStatus = configService.getConfigByCode("WITHDRAW_STATUS");
        if (Integer.valueOf(cashWithdrawalsStatus.getValue()) != 1) { //提现状态，0表示不能提现，1表示可以提现
            throw new IllegalArgumentException("提现暂未开启");
        }
        // 2 提现的金额
        @NotNull BigDecimal cashSellParamNum = cashSellParam.getNum();
        // 最小的提现额度100
        Config cashWithdrawalsConfigMin = configService.getConfigByCode("WITHDRAW_MIN_AMOUNT");
        if (cashSellParamNum.compareTo(new BigDecimal(cashWithdrawalsConfigMin.getValue())) < 0) {
            throw new IllegalArgumentException("检查提现的金额");
        }
        // 最大的提现额度50000
        Config cashWithdrawalsConfigMax = configService.getConfigByCode("WITHDRAW_MAX_AMOUNT");
        if (cashSellParamNum.compareTo(new BigDecimal(cashWithdrawalsConfigMax.getValue())) >= 0) {
            throw new IllegalArgumentException("检查提现的金额");
        }
    }

}
