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
import com.rc.mapper.CashWithdrawalsMapper;
import com.rc.domain.CashWithdrawals;
import com.rc.service.CashWithdrawalsService;
import org.springframework.util.CollectionUtils;

@Service
public class CashWithdrawalsServiceImpl extends ServiceImpl<CashWithdrawalsMapper, CashWithdrawals> implements CashWithdrawalsService {

    @Autowired
    private UserServiceFeign userServiceFeign;

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
}
