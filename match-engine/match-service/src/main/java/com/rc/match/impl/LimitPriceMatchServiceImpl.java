package com.rc.match.impl;

import com.rc.match.MatchService;
import com.rc.match.MatchServiceFactory;
import com.rc.match.MatchStrategy;
import com.rc.model.Order;
import com.rc.model.OrderBooks;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class LimitPriceMatchServiceImpl implements MatchService, InitializingBean {

    /**
     * 进行订单的撮合交易
     */
    @Override
    public void match(OrderBooks orderBooks, Order order) {
      log.info("开始撮合");

      orderBooks.addOrder(order);
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        MatchServiceFactory.addMatchService(MatchStrategy.LIMIT_PRICE,this);
    }
}
