package com.rc.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.rc.vo.ResponseEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.tio.core.ChannelContext;
import org.tio.core.Tio;
import org.tio.http.common.HttpRequest;
import org.tio.http.common.HttpResponse;
import org.tio.websocket.common.WsRequest;
import org.tio.websocket.common.WsResponse;
import org.tio.websocket.server.handler.IWsMsgHandler;

@Component
@Slf4j
public class WebSocketMessageHandler implements IWsMsgHandler {

    //握手时走这个方法，业务可以在这里获取cookie，request参数等
    @Override
    public HttpResponse handshake(HttpRequest httpRequest, HttpResponse httpResponse, ChannelContext channelContext) throws Exception {
        String clientIp = httpRequest.getClientIp();
        log.info("开始和客户端{}建立连接",clientIp);
        return httpResponse;
    }

    // 握手成功后走的方法
    @Override
    public void onAfterHandshaked(HttpRequest httpRequest, HttpResponse httpResponse, ChannelContext channelContext) throws Exception {
        log.info("和客户端握手成功");
    }

    // 字节消息（binaryType = arraybuffer）过来后会走这个方法
    @Override
    public Object onBytes(WsRequest wsRequest, byte[] bytes, ChannelContext channelContext) throws Exception {

        return null;
    }

    // 当客户端发close flag时，会走这个方法
    @Override
    public Object onClose(WsRequest wsRequest, byte[] bytes, ChannelContext channelContext) throws Exception {
        Tio.remove(channelContext, "remove channel context");
        return null;
    }

    // 字符消息（binaryType = blob）过来后会走这个方法
    @Override
    public Object onText(WsRequest wsRequest, String s, ChannelContext channelContext) throws Exception {

        if (s.equals("ping")) {
            return WsResponse.fromText("pong", "utf-8");
        }

        JSONObject payload  = JSON.parseObject(s);
        String sub = payload.getString("sub"); // 订阅组
        String req = payload.getString("req"); //当前request
        String cancel = payload.getString("cancel"); //取消的订阅组
        String id = payload.getString("id"); // 订阅的id

        //如果用户已登录，同时绑定用户
        String authorization = payload.getString("authorization");

        if(StringUtils.hasText(sub)){ // 订阅的组有内容
            Tio.bindGroup(channelContext, sub);
        }

        if(StringUtils.hasText(cancel)){ // 取消订阅的组有内容
            Tio.unbindGroup(cancel, channelContext);
        }

        if (StringUtils.hasText(authorization) && authorization.startsWith("bearer ")) {
            String accessToken = authorization.replaceAll("bearer ", ""); // 去掉bearer 获取token
            Jwt jwt = JwtHelper.decode(accessToken);
            String jwtJsonStr = jwt.getClaims();
            JSONObject jwtJson = JSON.parseObject(jwtJsonStr);
            String userId = jwtJson.getString("user_name");
            Tio.bindUser(channelContext, userId); // 有用户的时候绑定用户

        }

        ResponseEntity responseEntity = new ResponseEntity();
        responseEntity.setSubbed(sub);
        responseEntity.setCanceled(cancel);
        responseEntity.setId(id);
        responseEntity.setEvent(req);
        responseEntity.setStatus("ok");
        responseEntity.setCh(sub);
        responseEntity.setEvent(req);
        return responseEntity.build();
    }
}
