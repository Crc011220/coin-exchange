package com.rc.rocket;


import com.alibaba.fastjson.JSON;
import com.rc.model.MessagePayload;
import com.rc.vo.ResponseEntity;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.tio.core.Tio;
import org.tio.websocket.common.WsResponse;
import org.tio.websocket.starter.TioWebSocketServerBootstrap;

@Component
@Slf4j
public class RocketMessageListener {

    @Autowired
    private TioWebSocketServerBootstrap bootstrap;

    @StreamListener("tio-group")
    public void messageHandler(MessagePayload message){
        log.info("接收到rocket消息: {}", JSON.toJSONString(message));

        ResponseEntity responseEntity = new ResponseEntity();
        responseEntity.setSubbed(message.getChannel());
        responseEntity.put("result", message.getBody());

        // 推送给前端
        if (StringUtils.hasText(message.getUserId())) {
            Tio.sendToUser(bootstrap.getServerTioConfig(), message.getUserId(), responseEntity.build());
            return;
        }

        String group = message.getChannel();
        Tio.sendToGroup(bootstrap.getServerTioConfig(), group, responseEntity.build());
    }

}
