package com.rc.config;

import cn.hutool.core.lang.Snowflake;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IdConfig {

    @Value("${snow.app.id:1}")
    private Integer appId;
    @Value("${snow.data.id:1}")
    private Integer dataId;
    @Bean
    public Snowflake snowflake() {
        return new Snowflake(appId, dataId);
    }
}
