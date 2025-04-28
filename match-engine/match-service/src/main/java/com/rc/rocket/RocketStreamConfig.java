package com.rc.rocket;

import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.context.annotation.Configuration;

/**
 * 开启Stream
 */
@Configuration
@EnableBinding({Sink.class, Source.class})
public class RocketStreamConfig {
}
