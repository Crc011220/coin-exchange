package com.rc.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rc.domain.TurnoverOrder;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface TurnoverOrderService extends IService<TurnoverOrder>{

    // 分页查询成交记录
    Page<TurnoverOrder> findByPage(Page<TurnoverOrder> page, String symbol, Integer type, Long userId);


    /**
     * 获取买入的订单的成功的记录
     * @param orderId
     * @return
     */
    List<TurnoverOrder> getBuyTurnoverOrder(Long orderId, Long userId);

    /**
     * 获取卖出订单的成交记录
     * @param orderId
     * @return
     */
    List<TurnoverOrder> getSellTurnoverOrder(Long orderId,Long userId);

    /**
     * 根据交易市场查询我们的成交记录
     * @param symbol
     * @return
     */
    List<TurnoverOrder> findBySymbol(String symbol);
}
