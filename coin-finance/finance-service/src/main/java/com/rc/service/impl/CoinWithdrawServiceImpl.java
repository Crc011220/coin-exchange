package com.rc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rc.domain.CashWithdrawals;
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
import com.rc.domain.CoinWithdraw;
import com.rc.mapper.CoinWithdrawMapper;
import com.rc.service.CoinWithdrawService;
import org.springframework.util.CollectionUtils;

@Service
public class CoinWithdrawServiceImpl extends ServiceImpl<CoinWithdrawMapper, CoinWithdraw> implements CoinWithdrawService{

    @Autowired
    private UserServiceFeign userServiceFeign;

    @Override
    public Page<CoinWithdraw> findByPage(Page<CoinWithdraw> page, Long coinId, Long userId, String userName, String mobile, Byte status, String numMin, String numMax, String startTime, String endTime) {
        LambdaQueryWrapper<CoinWithdraw> coinWithdrawLambdaQueryWrapper = new LambdaQueryWrapper<>();
        // 1 使用用户相关的字段进行查询
        Map<Long, UserDto> basicUsers = null;
        if (userId != null || !StringUtils.isEmpty(userName) || !StringUtils.isEmpty(mobile)) {
            basicUsers = userServiceFeign.getBasicUsers(userId == null ? null : Arrays.asList(userId), userName, mobile);
            if (CollectionUtils.isEmpty(basicUsers)) { // 没有用户
                return page;
            }
            coinWithdrawLambdaQueryWrapper.in(CoinWithdraw::getUserId, basicUsers.keySet()); // 使用用户的信息做条件
        }
        // 添加其他的条件
        coinWithdrawLambdaQueryWrapper.eq(coinId != null, CoinWithdraw::getCoinId, coinId)
                .eq(status != null, CoinWithdraw::getStatus, status)
                .between(
                        !(StringUtils.isEmpty(numMin) || StringUtils.isEmpty(numMax)),
                        CoinWithdraw::getNum,
                        new BigDecimal(numMin==null? "0" :numMin), new BigDecimal(numMax==null? "0" :numMax)
                )
                .between(
                        !(StringUtils.isEmpty(startTime) || StringUtils.isEmpty(endTime)),
                        CoinWithdraw::getCreated,
                        startTime, endTime + "23:23:59"
                );
        // 查询
        Page<CoinWithdraw> pageData = page(page, coinWithdrawLambdaQueryWrapper);
        // 获取查询的数据
        List<CoinWithdraw> records = pageData.getRecords();
        if(!CollectionUtils.isEmpty(records)){
            if(basicUsers==null){ // 说明前面没有使用用户的信息查询用户
                List<Long> userIds = records.stream().map(CoinWithdraw::getUserId).collect(Collectors.toList());
                basicUsers =  userServiceFeign.getBasicUsers(userIds,null,null) ;
            }
            Map<Long, UserDto> finalBasicUsers = basicUsers;
            records.forEach(coinWithdraw->{
                UserDto userDto = finalBasicUsers.get(coinWithdraw.getUserId());
                if(userDto!=null){
                    coinWithdraw.setUserName(userDto.getUsername());
                    coinWithdraw.setRealName(userDto.getRealName());
                }
            });
        }
        return pageData ;
    }

    @Override
    public Page<CoinWithdraw> findUserCoinWithdraw(Page<CoinWithdraw> page, Long coinId, Long userId) {
        return page(page,new LambdaQueryWrapper<CoinWithdraw>()
                .eq(coinId!=null,CoinWithdraw::getCoinId,coinId)
                .eq(CoinWithdraw::getUserId ,userId));
    }
}
