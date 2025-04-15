package com.rc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rc.dto.UserDto;
import com.rc.feign.UserServiceFeign;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rc.mapper.AccountDetailMapper;
import com.rc.domain.AccountDetail;
import com.rc.service.AccountDetailService;
import org.springframework.util.CollectionUtils;

@Service
public class AccountDetailServiceImpl extends ServiceImpl<AccountDetailMapper, AccountDetail> implements AccountDetailService{

    @Autowired
    private UserServiceFeign userServiceFeign;
    @Override
    public Page<AccountDetail> findByPage(Page<AccountDetail> page, Long accountId, Long coinId, Long userId, String userName, String mobile, String amountStart, String amountEnd, String startTime, String endTime) {
        Map<Long, UserDto> basicUsers = null;
        LambdaQueryWrapper<AccountDetail> accountDetailLambdaQueryWrapper = new LambdaQueryWrapper<>();
        if (userId != null || StringUtils.isNotEmpty(userName) || StringUtils.isNotEmpty(mobile)) {
            basicUsers = userServiceFeign.getBasicUsers(userId == null ? null : Collections.singletonList(userId), userName, mobile);
            if(CollectionUtils.isEmpty(basicUsers)){
                return page;
            }
            Set<Long> userIds = basicUsers.keySet();
            accountDetailLambdaQueryWrapper.in(AccountDetail::getUserId, userIds);
        }
        // 添加其他的条件
        accountDetailLambdaQueryWrapper
                .eq(accountId != null, AccountDetail::getAccountId, accountId)
                .eq(coinId != null, AccountDetail::getCoinId, coinId)
                .between(
                        StringUtils.isNotEmpty(amountStart) && StringUtils.isNotEmpty(amountEnd),
                        AccountDetail::getAmount,
                        new BigDecimal(amountStart==null? "0" :amountStart), new BigDecimal(amountEnd==null? "0" :amountEnd)
                )
                .between(
                        StringUtils.isNotEmpty(startTime) && StringUtils.isNotEmpty(endTime),
                        AccountDetail::getCreated,
                        startTime, endTime + "23:59:59"
                );
        Page<AccountDetail> accountDetailPage = page(page, accountDetailLambdaQueryWrapper);
        List<AccountDetail> records = accountDetailPage.getRecords();
        if (!CollectionUtils.isEmpty(records)){
            List<Long> userIds = records.stream().map(AccountDetail::getUserId).collect(Collectors.toList());
            if(basicUsers == null){
                basicUsers = userServiceFeign.getBasicUsers(userIds, null, null);
            }

            Map<Long, UserDto> finalBasicUsers = basicUsers;
            records.forEach(accountDetail -> {
                UserDto userDto = finalBasicUsers.get(accountDetail.getUserId());
                if (userDto != null) {
                    accountDetail.setUserName(userDto.getUsername());
                    accountDetail.setRealName(userDto.getRealName());
                }
            });
        }
        return accountDetailPage;
    }
}
