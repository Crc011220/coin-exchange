package com.rc.event;

import com.rc.model.MessagePayload;
import com.rc.rocket.Source;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeTypeUtils;

/**
 * 行情数据的K线
 */
@Component
@Slf4j
public class TradeKLineEvent implements Event {

    @Autowired
    private Source source;

    @Override
    public void handle() {
        MessagePayload messagePayload = new MessagePayload();
        messagePayload.setChannel("trade.Kline");
        messagePayload.setBody("trade kline data");

        source.subscribeGroupOutput()
                .send(MessageBuilder
                        .withPayload(messagePayload)
                        .setHeader(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON)
                        .build()
                );
    }
}

