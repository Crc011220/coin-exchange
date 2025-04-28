package com.rc.feign;

import com.rc.config.feign.OAuth2FeignConfig;
import com.rc.domain.DepthItemVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient(name = "match-service", contextId = "orderBooksFeignClient", configuration = OAuth2FeignConfig.class)
public interface OrderBooksFeignClient {


    /**
     * 远程调用深度数据
     * @param symbol 交易对
     * @return buy和sell的List<DepthItemVo>, map key: bids, asks
     */
    @GetMapping("/match/depth")
    Map<String, List<DepthItemVo>> getDepth(@RequestParam() String symbol) ;
}

