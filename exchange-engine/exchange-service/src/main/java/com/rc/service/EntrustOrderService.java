package com.rc.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rc.domain.EntrustOrder;
import com.baomidou.mybatisplus.extension.service.IService;
import com.rc.domain.ExchangeTrade;
import com.rc.param.OrderParam;
import com.rc.vo.TradeEntrustOrderVo;

public interface EntrustOrderService extends IService<EntrustOrder>{


    // 查询用户的委托记录
    Page<EntrustOrder> findByPage(Page<EntrustOrder> page, String symbol, Integer status, Long userId);


    // 查询用户的历史委托记录
    Page<TradeEntrustOrderVo> getHistoryEntrustOrder(Page<EntrustOrder> page, String symbol, Long userId);

    // 查询未完成的委托单
    Page<TradeEntrustOrderVo> getEntrustOrder(Page<EntrustOrder> page, String symbol, Long userId);

    // 创建委托单
    Boolean createEntrustOrder(Long userId, OrderParam orderParam);

    // 数据库里面委托单的取消
    void cancelEntrustOrder(Long orderId);

    // 更新委托单数据
    void doMatch(ExchangeTrade exchangeTrade);

    // 数据库里面委托单的取消
    void cancelEntrustOrderToDb(String orderId);
}
