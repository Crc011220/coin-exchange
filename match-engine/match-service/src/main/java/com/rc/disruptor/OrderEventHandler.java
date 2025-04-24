package com.rc.disruptor;

import com.lmax.disruptor.EventHandler;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class OrderEventHandler implements EventHandler<OrderEvent> {

    // 事件处理 如何消费事件
    @Override
    public void onEvent(OrderEvent orderEvent, long l, boolean b) throws Exception {
        log.info("开始接受订单事件--------------------------{}", orderEvent);



        log.info("完成接受订单事件--------------------------{}", orderEvent);

    }
}
