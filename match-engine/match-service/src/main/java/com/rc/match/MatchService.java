package com.rc.match;

import com.rc.model.Order;
import com.rc.model.OrderBooks;

public interface MatchService {

    /**
     * 执行撮合交易
     * @param order
     */
    void match(OrderBooks orderBooks, Order order) ;
}

