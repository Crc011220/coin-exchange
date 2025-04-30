package com.rc.task;

import com.rc.event.DepthEvent;
import com.rc.event.MarketEvent;
import com.rc.event.TradeEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 行情的任务触发
 */
@Component
public class MarketTickerTask {

    @Autowired
    private MarketEvent marketEvent;

    @Autowired
    private TradeEvent tradeEvent;

    @Autowired
    private DepthEvent depthEvent;
    
    /**
     * 推送交易对信息
     */
    @Scheduled(fixedRate = 1000)
    public void pushMarkets() {
        marketEvent.handle();
    }

    /**
     * 推送市场深度
     */
    @Scheduled(fixedRate = 500)
    public void pushDepths() {
        depthEvent.handle();
    }

    /**
     * 推送实时成交订单数据
     */
    @Scheduled(fixedRate = 500)
    public void pushTrades() {
        tradeEvent.handle();
    }
 }
