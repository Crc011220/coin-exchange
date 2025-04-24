package com.rc.rocket;

import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.context.annotation.Configuration;

/**
 * 开启Stream的开发
 */
@Configuration
@EnableBinding(Sink.class) //
public class RocketStreamConfig {
}
