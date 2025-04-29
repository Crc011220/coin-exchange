package com.rc.controller;

import com.lmax.disruptor.EventHandler;
import com.rc.disruptor.OrderEvent;
import com.rc.disruptor.OrderEventHandler;
import com.rc.domain.DepthItemVo;
import com.rc.enums.OrderDirection;
import com.rc.feign.OrderBooksFeignClient;
import com.rc.model.MergeOrder;
import com.rc.model.OrderBooks;
import com.rc.model.TradePlate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@RestController

public class MatchController implements OrderBooksFeignClient {

    @Autowired
    private EventHandler<OrderEvent>[] handler;

    @GetMapping("/match/order")
    public TreeMap<BigDecimal, MergeOrder> getTradeData(
            @RequestParam(required = true) String symbol,
            @RequestParam(required = true) Integer orderDirection) {
        for (EventHandler<OrderEvent> orderEventEventHandler : handler) {
            OrderEventHandler orderEventHandler = (OrderEventHandler) orderEventEventHandler;
            if(orderEventHandler.getSymbol().equals(symbol)){
                OrderBooks orderBooks = orderEventHandler.getOrderBooks();
                return orderBooks.getCurrentOrders(OrderDirection.getOrderDirection(orderDirection));
            }
        }
        return null;
    }

    @Override
    public Map<String, List<DepthItemVo>> getDepth(@RequestParam() String symbol) {
        Map<String, List<DepthItemVo>> depths = new HashMap<>();
        for (EventHandler<OrderEvent> eventHandler : handler) {
            OrderEventHandler orderEventHandler = (OrderEventHandler) eventHandler;
            // 找到对应的深度数据
            if (orderEventHandler.getSymbol().equals(symbol)) {
                OrderBooks orderBooks = orderEventHandler.getOrderBooks();
                TradePlate buyTradePlate = orderBooks.getBuyTradePlate(); //空指针
                TradePlate sellTradePlate = orderBooks.getSellTradePlate(); //空指针
                depths.put("bids", buyTradePlate.getItems());
                depths.put("asks", sellTradePlate.getItems());
                return depths;
            }
        }
        return null;
    }


}
