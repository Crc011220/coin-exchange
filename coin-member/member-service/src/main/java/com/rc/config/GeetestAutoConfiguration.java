package com.rc.config;

import com.rc.geetest.GeetestLib;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(GeetestProperties.class)
public class GeetestAutoConfiguration {

    private GeetestProperties geetestProperties ;

    public GeetestAutoConfiguration(GeetestProperties geetestProperties){
        this.geetestProperties = geetestProperties ;
    }

    @Bean
    public GeetestLib geetestLib(){
        return new GeetestLib(geetestProperties.getGeetestId(), geetestProperties.getGeetestKey());
    }
}

