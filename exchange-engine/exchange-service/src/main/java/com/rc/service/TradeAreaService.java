package com.rc.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rc.domain.TradeArea;
import com.baomidou.mybatisplus.extension.service.IService;
import com.rc.vo.TradeAreaMarketVo;

import java.util.List;

public interface TradeAreaService extends IService<TradeArea>{

    // 分页查询交易区域
    Page<TradeArea> findByPage(Page<TradeArea> page, String name, Byte status);

    // 查询交易区域
    List<TradeArea> findAll(Byte status);

    // 查询交易区域以及区域下的市场
    List<TradeAreaMarketVo> findTradeAreaMarkets();

    // 查询用户收藏的交易区域以及区域下的市场
    List<TradeAreaMarketVo> findFavoriteTradeAreaMarkets(Long userId);
}
