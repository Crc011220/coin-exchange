package com.rc.config.rocket;

import com.rc.domain.ExchangeTrade;
import com.rc.service.EntrustOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Component
@Slf4j
public class ExchangeTradeListener {

    @Autowired
    private EntrustOrderService entrustOrderService;

    @Transactional
    @StreamListener("exchange-trade-in")
    public void onMessage(List<ExchangeTrade> message) {



        if (CollectionUtils.isEmpty(message)){
            return ;
        }

        for (ExchangeTrade exchangeTrade : message) {
            if (exchangeTrade != null){
                // 交易完成 更新数据库
                entrustOrderService.doMatch(exchangeTrade);
            }
        }
    }

    @Transactional
    @StreamListener("cancel-order-in")
    public void receiveCancelOrder(String orderId){
        entrustOrderService.cancelEntrustOrderToDb(orderId);
    }


}
