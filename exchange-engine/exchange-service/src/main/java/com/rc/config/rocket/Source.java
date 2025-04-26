package com.rc.config.rocket;

import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;

public interface Source {

    @Output("order-out")
    MessageChannel orderOut();
}
