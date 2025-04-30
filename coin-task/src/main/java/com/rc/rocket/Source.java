package com.rc.rocket;

import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;

public interface Source {
    // 发送数据

    @Output("subscribe-group-out")
    MessageChannel subscribeGroupOutput();
}
