package com.rc.rocket;

import com.rc.disruptor.DisruptorTemplate;
import com.rc.domain.EntrustOrder;
import com.rc.model.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.stereotype.Service;

import static com.rc.util.BeanUtils.entrustOrder2Order;

@Service
@Slf4j
public class MessageConsumerListener {

    @Autowired
    private DisruptorTemplate disruptorTemplate ;

    @StreamListener("order-in")
    public  void handleMessage(EntrustOrder order){

        log.info("接收到了委托单:{}",order);
        disruptorTemplate.onData(entrustOrder2Order(order));
    }
}
