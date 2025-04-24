package com.rc.feign;

import com.rc.config.feign.OAuth2FeignConfig;
import com.rc.dto.MarketDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "exchange-service", configuration = OAuth2FeignConfig.class, path = "/markets")
public interface MarketServiceFeign {

    @GetMapping("/getMarket")
    MarketDto findBySellAndBuyCoinId(@RequestParam Long sellCoinId, @RequestParam Long buyCoinId);

    @GetMapping("/getMarket/symbol")
    MarketDto findBySymbol(@RequestParam("symbol") String symbol);
}
