package com.rc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.tio.core.Tio;
import org.tio.websocket.common.WsResponse;
import org.tio.websocket.starter.EnableTioWebSocketServer;
import org.tio.websocket.starter.TioWebSocketServerBootstrap;

import java.util.Date;

@SpringBootApplication
@EnableTioWebSocketServer // 启动tio-websocket服务
@EnableScheduling // 启动定时任务
public class ChanServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ChanServiceApplication.class, args);
    }

}
