package com.rc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rc.domain.CashRecharge;
import com.rc.dto.UserDto;
import com.rc.feign.UserServiceFeign;
import com.rc.mapper.CashRechargeMapper;
import com.rc.service.CashRechargeService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CashRechargeServiceImpl extends ServiceImpl<CashRechargeMapper, CashRecharge> implements CashRechargeService {

    @Autowired
    private UserServiceFeign userServiceFeign;

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
                        startTime, endTime + "23:59:59"
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
        return pageData;
    }
}

