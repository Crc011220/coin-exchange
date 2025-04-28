package com.rc.match;

import com.rc.model.Order;
import com.rc.model.OrderBooks;

public interface MatchService {

    /**
     * 执行撮合交易
     * @param orderBooks 盘口
     * @param order 订单
     */
    void match(OrderBooks orderBooks, Order order) ;
}

