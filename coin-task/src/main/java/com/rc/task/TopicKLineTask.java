package com.rc.task;

import com.rc.constant.Constants;
import com.rc.dto.MarketDto;
import com.rc.event.KlineEvent;
import com.rc.feign.MarketServiceFeign;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * K 线数据的推送
 */
@Component
@Slf4j
public class TopicKLineTask{

    @Autowired
    private MarketServiceFeign marketServiceFeign;

    private ExecutorService executor = null;

    {
        executor = new ThreadPoolExecutor(
                5,
                10,
                100L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(30),
                new ThreadPoolExecutor.CallerRunsPolicy());
    }

    /**
     * 每3秒推送K线数据
     */
    @Scheduled(fixedRate = 3000)
    public void pushKline() {
        List<MarketDto> marketDtos = marketServiceFeign.tradeMarkets();
        if (CollectionUtils.isEmpty(marketDtos)) {
            return;
        }
        marketDtos.forEach(marketDto -> {
            KlineEvent klineEvent = new KlineEvent(marketDto.getSymbol(),"market.%s.kline.%s",
                    Constants.REDIS_KEY_TRADE_KLINE);
            executor.submit(klineEvent);
        });
    }
}

