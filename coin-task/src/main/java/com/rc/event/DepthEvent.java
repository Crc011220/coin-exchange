package com.rc.event;

import com.alibaba.fastjson.JSONObject;
import com.rc.dto.MarketDto;
import com.rc.enums.DepthMergeType;
import com.rc.feign.MarketServiceFeign;
import com.rc.model.MessagePayload;
import com.rc.rocket.Source;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MimeTypeUtils;

import java.util.List;

/**
 * 深度盘口数据事件
 */
@Component
@Slf4j
public class DepthEvent implements Event {

    @Autowired
    private Source source;

    @Autowired
    private MarketServiceFeign marketServiceFeign;

    private static final String DEPTH_GROUP = "market.%s/depth.step%s"; // 需要填充 具体交易对 和 深度类型




    /**
     * 推送市场合并深度
     */
    @Override
    public void handle() {
        List<MarketDto> markets = marketServiceFeign.tradeMarkets();

        if (CollectionUtils.isEmpty(markets)){
            return;
        }

        for (MarketDto market : markets) {
            String symbol = market.getSymbol();

            for (DepthMergeType depthMergeType : DepthMergeType.values()) {
                String data = marketServiceFeign.getDepthData(symbol, depthMergeType.getValue());

                MessagePayload messagePayload = new MessagePayload();
                messagePayload.setChannel(String.format(DEPTH_GROUP, symbol, depthMergeType.getValue()));
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("tick", data);
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
}

