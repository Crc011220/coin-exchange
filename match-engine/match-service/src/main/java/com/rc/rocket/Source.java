package com.rc.rocket;

import com.mysql.cj.protocol.Message;
import com.mysql.cj.protocol.MessageSender;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;

public interface Source {

    // 盘口数据输出
    @Output("trade-plate-out")
    MessageChannel plateOut();

    // 成交订单输出
    @Output("completed-order-out")
    MessageChannel completedOrderOut();

    // 交易数据输出
    @Output("exchange-trades-out")
    MessageChannel exchangeTradesOut();

    // 取消订单输出
    @Output("exchange-trades-out")
    MessageChannel cancelOrderOut();
}
