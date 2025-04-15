package com.rc.service.impl;

import com.alicp.jetcache.AutoReleaseLock;
import com.alicp.jetcache.Cache;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.CreateCache;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rc.domain.CashRecharge;
import com.rc.domain.CashRechargeAuditRecord;
import com.rc.dto.UserDto;
import com.rc.feign.UserServiceFeign;
import com.rc.mapper.CashRechargeAuditRecordMapper;
import com.rc.mapper.CashRechargeMapper;
import com.rc.service.AccountService;
import com.rc.service.CashRechargeService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class CashRechargeServiceImpl extends ServiceImpl<CashRechargeMapper, CashRecharge> implements CashRechargeService {

    @Autowired
    private UserServiceFeign userServiceFeign;

    // 分布式锁
    @CreateCache(name = "CASH_RECHARGE_LOCK", timeUnit = TimeUnit.SECONDS, expire = 100, cacheType = CacheType.BOTH)
    private Cache<String, String> cache;

    @Autowired
    private CashRechargeAuditRecordMapper cashRechargeAuditRecordMapper;

    @Autowired
    private AccountService accountService;
    /**
     * 条件分页查询
     *
     * @param page      分页参数
     * @param coinId    币种的ID
     * @param userId    用户的Id
     * @param userName  用户的名称
     * @param mobile    用户的手机号
     * @param status    审核的状态
     * @param numMin    充值数量的最小值
     * @param numMax    充值数量的最大值
     * @param startTime 充值的开始时间
     * @param endTime   充值数量的结束时间
     * @return
     */
    @Override
    public Page<CashRecharge> findByPage(Page<CashRecharge> page, Long coinId, Long userId, String userName,
                                         String mobile, Byte status, String numMin, String numMax, String startTime,
                                         String endTime) {
        LambdaQueryWrapper<CashRecharge> cashRechargeLambdaQueryWrapper = new LambdaQueryWrapper<>();
        // 1 使用用户相关的字段进行查询
        Map<Long, UserDto> basicUsers = null;
        if (userId != null || !StringUtils.isEmpty(userName) || !StringUtils.isEmpty(mobile)) {
            basicUsers = userServiceFeign.getBasicUsers(userId == null ? null : Arrays.asList(userId), userName, mobile);
            if (CollectionUtils.isEmpty(basicUsers)) { // 没有用户
                return page;
            }
            cashRechargeLambdaQueryWrapper.in(CashRecharge::getUserId, basicUsers.keySet()); // 使用用户的信息做条件
        }
        // 添加其他的条件
        cashRechargeLambdaQueryWrapper.eq(coinId != null, CashRecharge::getCoinId, coinId)
                .eq(status != null, CashRecharge::getStatus, status)
                .between(
                        !(StringUtils.isEmpty(numMin) || StringUtils.isEmpty(numMax)),
                        CashRecharge::getNum,
                        new BigDecimal(numMin==null? "0" :numMin), new BigDecimal(numMax==null? "0" :numMax)
                )
                .between(
                        !(StringUtils.isEmpty(startTime) || StringUtils.isEmpty(endTime)),
                        CashRecharge::getCreated,
                        startTime, endTime + "23:23:59"
                );
        // 查询
        Page<CashRecharge> pageData = page(page, cashRechargeLambdaQueryWrapper);
        // 获取查询的数据
        List<CashRecharge> records = pageData.getRecords();
        if(!CollectionUtils.isEmpty(records)){
            if(basicUsers==null){ // 说明前面没有使用用户的信息查询用户
                List<Long> userIds = records.stream().map(CashRecharge::getUserId).collect(Collectors.toList());
                basicUsers =  userServiceFeign.getBasicUsers(userIds,null,null) ;
            }
            Map<Long, UserDto> finalBasicUsers = basicUsers;
            records.forEach(record->{
                UserDto userDto = finalBasicUsers.get(record.getUserId());
                if(userDto!=null){
                    record.setUserName(userDto.getUsername());
                    record.setRealName(userDto.getRealName());
                }
            });
        }
        return pageData ;
    }

    @Override
    public boolean cashRechargeAudit(Long userId, CashRechargeAuditRecord cashRechargeAuditRecord) {
        return cache.tryLockAndRun(cashRechargeAuditRecord.getId() + "" , 300, TimeUnit.SECONDS, () -> {

            Long rechargeId = cashRechargeAuditRecord.getId();
            CashRecharge cashRecharge = getById(rechargeId);
            if (cashRecharge == null) {
                throw new IllegalArgumentException("充值记录不存在");
            }
            if (cashRecharge.getStatus() == 1) {
                throw new IllegalArgumentException("充值记录审核已经通过");
            }

            CashRechargeAuditRecord cashRechargeAuditRecordDb = new CashRechargeAuditRecord();
            cashRechargeAuditRecordDb.setAuditUserId(userId);
            cashRechargeAuditRecordDb.setStatus(cashRechargeAuditRecord.getStatus());
            cashRechargeAuditRecordDb.setRemark(cashRechargeAuditRecord.getRemark());
            Integer step = cashRecharge.getStep() + 1;
            cashRechargeAuditRecordDb.setStep(step.byteValue());
            // 保存审核记录
            int i = cashRechargeAuditRecordMapper.insert(cashRechargeAuditRecordDb);

            if (i == 0) {
                throw new IllegalArgumentException("保存审核记录失败");
            }

            cashRecharge.setStatus(cashRechargeAuditRecord.getStatus());
            cashRecharge.setAuditRemark(cashRechargeAuditRecord.getRemark());
            cashRecharge.setStep(step.byteValue());
            if (cashRechargeAuditRecord.getStatus() == 2) { // 审核不通过
                updateById(cashRecharge);
            } else { // 审核通过,充值到用户的账户
                //userId是审批人，充值人是cashRecharge.getUserId
                // 转账
                boolean isOk = accountService.transferAccountAmount(userId, cashRecharge.getUserId(), cashRecharge.getCoinId(),
                        cashRecharge.getNum(), cashRecharge.getFee(), cashRecharge.getId(),
                        cashRechargeAuditRecord.getRemark(), "cash_recharge", (byte)1 );
                if (isOk) {
                    cashRecharge.setLastTime(new Date());
                    updateById(cashRecharge);
                }
            }
        });
    }
}

