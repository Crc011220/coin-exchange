package com.rc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rc.domain.CoinRecharge;
import com.rc.dto.UserDto;
import com.rc.feign.UserServiceFeign;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rc.domain.CoinRecharge;
import com.rc.mapper.CoinRechargeMapper;
import com.rc.service.CoinRechargeService;
import org.springframework.util.CollectionUtils;

@Service
public class CoinRechargeServiceImpl extends ServiceImpl<CoinRechargeMapper, CoinRecharge> implements CoinRechargeService{

    @Autowired
    private UserServiceFeign userServiceFeign;

    @Override
    public Page<CoinRecharge> findByPage(Page<CoinRecharge> page, Long coinId, Long userId, String userName, String mobile, Byte status, String numMin, String numMax, String startTime, String endTime) {
        LambdaQueryWrapper<CoinRecharge> coinRechargeLambdaQueryWrapper = new LambdaQueryWrapper<>();
        // 1 使用用户相关的字段进行查询
        Map<Long, UserDto> basicUsers = null;
        if (userId != null || !StringUtils.isEmpty(userName) || !StringUtils.isEmpty(mobile)) {
            basicUsers = userServiceFeign.getBasicUsers(userId == null ? null : Arrays.asList(userId), userName, mobile);
            if (CollectionUtils.isEmpty(basicUsers)) { // 没有用户
                return page;
            }
            coinRechargeLambdaQueryWrapper.in(CoinRecharge::getUserId, basicUsers.keySet()); // 使用用户的信息做条件
        }
        // 添加其他的条件
        coinRechargeLambdaQueryWrapper.eq(coinId != null, CoinRecharge::getCoinId, coinId)
                .eq(status != null, CoinRecharge::getStatus, status)
                .between(
                        !(StringUtils.isEmpty(numMin) || StringUtils.isEmpty(numMax)),
                        CoinRecharge::getAmount,
                        new BigDecimal(numMin==null? "0" :numMin), new BigDecimal(numMax==null? "0" :numMax)
                )
                .between(
                        !(StringUtils.isEmpty(startTime) || StringUtils.isEmpty(endTime)),
                        CoinRecharge::getCreated,
                        startTime, endTime + "23:23:59"
                );
        // 查询
        Page<CoinRecharge> pageData = page(page, coinRechargeLambdaQueryWrapper);
        // 获取查询的数据
        List<CoinRecharge> records = pageData.getRecords();
        if(!CollectionUtils.isEmpty(records)){
            if(basicUsers==null){ // 说明前面没有使用用户的信息查询用户
                List<Long> userIds = records.stream().map(CoinRecharge::getUserId).collect(Collectors.toList());
                basicUsers =  userServiceFeign.getBasicUsers(userIds,null,null) ;
            }
            Map<Long, UserDto> finalBasicUsers = basicUsers;
            records.forEach(coinRecharge->{
                UserDto userDto = finalBasicUsers.get(coinRecharge.getUserId());
                if(userDto!=null){
                    coinRecharge.setUserName(userDto.getUsername());
                    coinRecharge.setRealName(userDto.getRealName());
                }
            });
        }
        return pageData ;
    }

    @Override
    public Page<CoinRecharge> findUserCoinRecharge(Page<CoinRecharge> page, Long coinId, Long userId) {
        return page(page,new LambdaQueryWrapper<CoinRecharge>()
                .eq(coinId!=null,CoinRecharge::getCoinId,coinId)
                .eq(CoinRecharge::getUserId ,userId)
        );
    }

}
