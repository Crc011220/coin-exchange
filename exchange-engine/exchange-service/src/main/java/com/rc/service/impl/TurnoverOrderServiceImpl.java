package com.rc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rc.domain.TurnoverOrder;
import com.rc.mapper.TurnoverOrderMapper;
import com.rc.service.TurnoverOrderService;
@Service
public class TurnoverOrderServiceImpl extends ServiceImpl<TurnoverOrderMapper, TurnoverOrder> implements TurnoverOrderService{

    @Override
    public Page<TurnoverOrder> findByPage(Page<TurnoverOrder> page, String symbol, Integer type, Long userId) {
        return page(page, new LambdaQueryWrapper<TurnoverOrder>()
                .eq(StringUtils.isNotEmpty(symbol), TurnoverOrder::getSymbol, symbol)
                .eq(type != null && type != 0, TurnoverOrder::getStatus, type)
                .and(userId != null, wrapper ->
                        wrapper.eq(TurnoverOrder::getBuyUserId, userId)
                                .or()
                                .eq(TurnoverOrder::getSellUserId, userId)
                )
                .orderByDesc(TurnoverOrder::getCreated)
        );
    }


    @Override
    public List<TurnoverOrder> getBuyTurnoverOrder(Long orderId, Long userId) {
        return list(new LambdaQueryWrapper<TurnoverOrder>().eq(TurnoverOrder::getOrderId, orderId)
                .eq(TurnoverOrder::getBuyUserId, userId)
        );
    }



    @Override
    public List<TurnoverOrder> getSellTurnoverOrder(Long orderId,Long userId) {
        return list(new LambdaQueryWrapper<TurnoverOrder>().eq(TurnoverOrder::getOrderId, orderId)
                .eq(TurnoverOrder::getSellUserId, userId)
        );

    }


    @Override
    public List<TurnoverOrder> findBySymbol(String symbol) {
        return list(
                new LambdaQueryWrapper<TurnoverOrder>()
                        .eq(TurnoverOrder::getSymbol, symbol)
                        .orderByDesc(TurnoverOrder::getCreated)
                        .eq(TurnoverOrder::getStatus,1)
                        .last("limit 60")
        );
    }


}
