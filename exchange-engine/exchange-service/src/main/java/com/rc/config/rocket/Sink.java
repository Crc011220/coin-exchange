package com.rc.config.rocket;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.messaging.MessageChannel;

public interface Sink {

    // 交易数据输入
    @Input("exchange-trade-in")
    MessageChannel exchangeTradeIn();

    // 取消订单输入
    @Input("cancel-order-in")
    MessageChannel cancelOrderIn();
}
