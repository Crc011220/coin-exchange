package com.rc.event;

import com.alibaba.fastjson.JSONObject;
import com.rc.enums.KlineType;
import com.rc.model.MessagePayload;
import com.rc.rocket.Source;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MimeTypeUtils;

import java.util.List;

/**
 * K 线推送事件
 */
@Component
@Slf4j
@Data
public class KlineEvent implements Runnable,Event {
    @Autowired
    private Source source;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 交易对标识符
     */
    private String symbol;

    /**
     * 通道
     */
    private String channel;

    /**
     * redis key 前缀
     */
    private String keyPrefix;

    private static final String KLINE_GROUP = "market.%s.kline.%s" ;

    public KlineEvent() {
    }

    public KlineEvent(String symbol, String channel, String keyPrefix) {
        this.symbol = symbol;
        this.channel = channel;
        this.keyPrefix = keyPrefix;
    }

    /**
     * 事件触发处理机制
     */
    @Override
    public void handle() {
        for (KlineType klineType : KlineType.values()) {
            String key = keyPrefix + symbol + ":" + klineType.getValue();
            List<Object> range = redisTemplate.opsForList().range(key, 0, 1);
            if (!CollectionUtils.isEmpty(range)){
                Object lineData = range.get(0);
                MessagePayload messagePayload = new MessagePayload();

                messagePayload.setChannel(String.format(KLINE_GROUP, symbol, klineType.getValue().toLowerCase()));
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("tick", lineData);
                messagePayload.setBody(jsonObject.toString());

                source.subscribeGroupOutput()
                        .send(MessageBuilder
                                .withPayload(messagePayload)
                                .setHeader(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON)
                                .build()
                        );
            }
        }
    }

    /**
     * 让线程池调度
     */
    @Override
    public void run() {
        handle();
    }
}
