package com.rc.feign;

import com.rc.config.feign.OAuth2FeignConfig;
import com.rc.dto.MarketDto;
import com.rc.dto.TradeMarketDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "exchange-service", configuration = OAuth2FeignConfig.class, path = "/markets")
public interface MarketServiceFeign {

    @GetMapping("/getMarket")
    MarketDto findBySellAndBuyCoinId(@RequestParam Long sellCoinId, @RequestParam Long buyCoinId);

    @GetMapping("/getMarket/symbol")
    MarketDto findBySymbol(@RequestParam("symbol") String symbol);

    @GetMapping("/getMarket/tradeMarkets")
    List<MarketDto> tradeMarkets();

    String getDepthData(String symbol, int value);

    List<TradeMarketDto> queryMarkesByIds(String marketIds);
}
