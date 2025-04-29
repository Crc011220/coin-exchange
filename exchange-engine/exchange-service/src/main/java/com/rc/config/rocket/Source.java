package com.rc.config.rocket;

import com.mysql.cj.protocol.Message;
import com.mysql.cj.protocol.MessageSender;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;

public interface Source {

    @Output("order-out")
    MessageChannel orderOut();

}
