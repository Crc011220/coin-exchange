package com.rc.controller;

import com.lmax.disruptor.EventHandler;
import com.rc.disruptor.OrderEvent;
import com.rc.disruptor.OrderEventHandler;
import com.rc.enums.OrderDirection;
import com.rc.model.MergeOrder;
import com.rc.model.OrderBooks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.TreeMap;

@RestController

public class MatchController {

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
}
