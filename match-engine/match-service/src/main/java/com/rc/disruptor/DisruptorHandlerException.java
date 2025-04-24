package com.rc.disruptor;

import com.lmax.disruptor.ExceptionHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * 异常处理
 */
@Slf4j
public class DisruptorHandlerException implements ExceptionHandler<Object> {


    @Override
    public void handleEventException(Throwable ex, long sequence, Object event) {
        log.error("process data error sequence ==[{}] event==[{}] ,ex ==[{}]", sequence, event.toString(), ex.getMessage());
    }


    @Override
    public void handleOnStartException(Throwable ex) {
        log.error("start disruptor error ==[{}]!", ex.getMessage());
    }


    @Override
    public void handleOnShutdownException(Throwable ex) {
        log.error("shutdown disruptor error ==[{}]!", ex.getMessage());
    }

}

