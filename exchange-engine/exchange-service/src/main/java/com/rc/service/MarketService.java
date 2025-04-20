package com.rc.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rc.domain.Market;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface MarketService extends IService<Market>{

    // 分页查询交易市场
    Page<Market> findByPage(Page<Market> page, Long tradeAreaId, Byte status);

    // 根据交易区域ID查询交易市场
    List<Market> getMarketsByTradeAreaId(Long id);
}
