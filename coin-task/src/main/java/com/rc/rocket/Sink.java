package com.rc.rocket;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.messaging.MessageChannel;

public interface Sink {

    // 交易数据输入
    @Input("exchange-trades-in")
    MessageChannel exchangeTradesIn();

    // 取消订单输入
    MessageChannel messageGroupChannel();
}
