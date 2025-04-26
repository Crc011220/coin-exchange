package com.rc.disruptor;

import com.lmax.disruptor.EventHandler;
import com.rc.match.MatchServiceFactory;
import com.rc.match.MatchStrategy;
import com.rc.model.Order;
import com.rc.model.OrderBooks;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;


@Data
@Slf4j
public class OrderEventHandler implements EventHandler<OrderEvent> {

    private OrderBooks orderBooks;

    private String symbol;

    public OrderEventHandler(OrderBooks orderBooks) {
        this.orderBooks = orderBooks;
        this.symbol = orderBooks.getSymbol();
    }

    // 事件处理 如何消费事件
    @Override
    public void onEvent(OrderEvent orderEvent, long l, boolean b) throws Exception {
        Order order = (Order) orderEvent.getSource();
        if(!order.getSymbol().equals(symbol)){ // 不是本symbol的订单, 不需要处理
            return;
        }
        log.info("开始接受订单事件--------------------------{}", orderEvent);
        MatchServiceFactory.getMatchService(MatchStrategy.LIMIT_PRICE).match(orderBooks,order);



        log.info("完成接受订单事件--------------------------{}", orderEvent);

    }
}
