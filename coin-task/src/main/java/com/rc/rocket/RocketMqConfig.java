package com.rc.rocket;

import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.context.annotation.Configuration;

/**
 * 开启Stream
 */
@Configuration
@EnableBinding(value = Source.class)
public class RocketMqConfig {
}

