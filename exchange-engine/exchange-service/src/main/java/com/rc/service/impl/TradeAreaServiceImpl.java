package com.rc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rc.domain.Market;
import com.rc.service.MarketService;
import com.rc.vo.TradeAreaMarketVo;
import com.rc.vo.TradeMarketVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rc.domain.TradeArea;
import com.rc.mapper.TradeAreaMapper;
import com.rc.service.TradeAreaService;
import org.springframework.util.CollectionUtils;

@Service
public class TradeAreaServiceImpl extends ServiceImpl<TradeAreaMapper, TradeArea> implements TradeAreaService{

    @Autowired
    private MarketService marketService;
    @Override
    public Page<TradeArea> findByPage(Page<TradeArea> page, String name, Byte status) {
        return page(page, new LambdaQueryWrapper<TradeArea>()
                .like(StringUtils.isNotEmpty(name), TradeArea::getName, name)
                .eq(status != null, TradeArea::getStatus, status)
        );
    }

    @Override
    public List<TradeArea> findAll(Byte status) {
        return list(new LambdaQueryWrapper<TradeArea>().
                eq(status != null, TradeArea::getStatus, status)
        );
    }

    @Override
    public List<TradeAreaMarketVo> findTradeAreaMarkets() {
        List<TradeArea> list = list(new LambdaQueryWrapper<TradeArea>().eq(TradeArea::getStatus, 1).orderByAsc(TradeArea::getSort));
        if (CollectionUtils.isEmpty(list)){
            return Collections.emptyList();
        }
        List<TradeAreaMarketVo> tradeAreaMarketVos = new ArrayList<>();
        for (TradeArea tradeArea : list) {
            List<Market> markets = marketService.getMarketsByTradeAreaId(tradeArea.getId());

            if (!CollectionUtils.isEmpty(markets)){
                TradeAreaMarketVo tradeAreaMarketVo = new TradeAreaMarketVo();
                tradeAreaMarketVo.setAreaName(tradeArea.getName());
                tradeAreaMarketVo.setMarkets(markets2marketVos(markets));
                tradeAreaMarketVos.add(tradeAreaMarketVo);
            }
        }
        return null;
    }

    private List<TradeMarketVo> markets2marketVos(List<Market> markets) {
        return markets.stream().map(this::toConvertVo).collect(Collectors.toList());
    }

    private TradeMarketVo toConvertVo(Market market) {
        TradeMarketVo tradeMarketVo = new TradeMarketVo();
        tradeMarketVo.setName(market.getName());
        tradeMarketVo.setSymbol(market.getSymbol());
        tradeMarketVo.setImage(market.getImg()); // 报价货币的图片

        tradeMarketVo.setHigh();
        tradeMarketVo.setLow();
        tradeMarketVo.setPrice();


        return tradeMarketVo;
    }

}
