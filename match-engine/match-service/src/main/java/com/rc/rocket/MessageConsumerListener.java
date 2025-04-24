package com.rc.rocket;

import com.rc.disruptor.DisruptorTemplate;
import com.rc.model.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MessageConsumerListener {

    @Autowired
    private DisruptorTemplate disruptorTemplate ;

    @StreamListener("order.in")
    public  void handleMessage(Order order){
        log.info("接收到了委托单:{}",order);
        disruptorTemplate.onData(order);
    }
}
