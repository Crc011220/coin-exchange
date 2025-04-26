package com.rc.rocket;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.messaging.MessageChannel;

public interface Sink {

    @Input("order-in")
    public MessageChannel messageChannel() ;
}
