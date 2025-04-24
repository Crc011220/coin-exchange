package com.rc.disruptor;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = DisruptorProperties.PREFIX)
public class DisruptorProperties {

    public static final String PREFIX = "spring.disruptor";


    //          RingBuffer缓冲区大小, 默认 1024 * 1024
    private int ringBufferSize = 1024 * 1024;

    /**
     * 是否为多生产者，如果是则通过 RingBuffer.createMultiProducer创建一个多生产者的RingBuffer，
     * 否则通过RingBuffer.createSingleProducer创建一个单生产者的RingBuffer
     */
    private boolean multiProducer = false;
}

