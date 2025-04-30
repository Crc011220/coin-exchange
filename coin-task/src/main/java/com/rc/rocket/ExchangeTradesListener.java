package com.rc.rocket;

import com.rc.domain.ExchangeTrade;
import com.rc.dto.CreateKLineDto;
import com.rc.service.TradeKlineService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.List;

@Component
@Slf4j
public class ExchangeTradesListener {

    @StreamListener("exchange-trades-in")
    public void exchangeTradesHandler(List<ExchangeTrade> exchangeTrades) {

        log.info("接收到了交易记录:{}",exchangeTrades);

        if (CollectionUtils.isEmpty(exchangeTrades)){
            return;
        }

        for (ExchangeTrade exchangeTrade : exchangeTrades) {
            CreateKLineDto create = exchangeTrade2CreateKLine(exchangeTrade);
            TradeKlineService.queue.offer(create);
        }
    }

    private CreateKLineDto exchangeTrade2CreateKLine(ExchangeTrade exchangeTrade) {
        CreateKLineDto create = new CreateKLineDto();
        create.setPrice(exchangeTrade.getPrice());
        create.setSymbol(exchangeTrade.getSymbol());
        create.setVolume(exchangeTrade.getAmount());
        return create;
    }


}
