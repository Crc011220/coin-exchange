package com.rc.rocket;

import com.rc.disruptor.DisruptorTemplate;
import com.rc.domain.EntrustOrder;
import com.rc.model.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.stereotype.Service;

import java.util.Date;

import static com.rc.util.BeanUtils.entrustOrder2Order;

@Service
@Slf4j
public class MessageConsumerListener {

    @Autowired
    private DisruptorTemplate disruptorTemplate ;

    @StreamListener("order-in")
    public void handleMessage(EntrustOrder entrustOrder){
        Order order = null;
        if (entrustOrder.getStatus() == 2){ //order需要取消
            order = new Order();
            order.setOrderId(entrustOrder.getId().toString());
            order.setCancelOrder(true);
            order.setCancelTime(new Date().getTime());
        } else{
            order = entrustOrder2Order(entrustOrder);
        }

        log.info("接收到了委托单:{}",order);
        disruptorTemplate.onData(order);
    }
}
