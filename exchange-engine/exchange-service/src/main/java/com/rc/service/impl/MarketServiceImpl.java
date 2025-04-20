package com.rc.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rc.dto.CoinDto;
import com.rc.feign.CoinServiceFeign;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rc.mapper.MarketMapper;
import com.rc.domain.Market;
import com.rc.service.MarketService;
@Service
@Slf4j
public class MarketServiceImpl extends ServiceImpl<MarketMapper, Market> implements MarketService{

    @Autowired
    private CoinServiceFeign coinServiceFeign;

    @Override
    public Page<Market> findByPage(Page<Market> page, Long tradeAreaId, Byte status) {
        return page(page, new LambdaQueryWrapper<Market>()
                .eq(tradeAreaId != null, Market::getTradeAreaId, tradeAreaId)
                .eq(status != null, Market::getStatus, status)
        );
    }

    @Override
    public List<Market> getMarketsByTradeAreaId(Long id) {
        return list(new LambdaQueryWrapper<Market>().eq(Market::getTradeAreaId, id).eq(Market::getStatus, 1).orderByAsc(Market::getSort));
    }

    @Override
    public boolean save(Market market){
        log.info("开始新增市场信息：{}", JSON.toJSONString(market));
        Long buyCoinId = market.getBuyCoinId(); // 买方币种ID
        Long sellCoinId = market.getSellCoinId(); // 卖方市场ID
        List<CoinDto> coins = coinServiceFeign.findCoins(Arrays.asList(buyCoinId, sellCoinId));
        if (coins.size() != 2){
            throw new IllegalArgumentException("货币输入错误");
        }
        // 获取两个货币信息 判断币种
        CoinDto coinDto = coins.get(0);
        CoinDto buyCoin = null;
        CoinDto sellCoin = null;
        if (coinDto.getId().equals(sellCoinId)){
            sellCoin = coinDto;
            buyCoin = coins.get(1);
        } else{
            buyCoin = coinDto;
            sellCoin = coins.get(1);
        }

        market.setName(sellCoin.getName() + "/" + buyCoin.getName()); //交易市场名称的显示
        market.setTitle(buyCoin.getTitle() + "/" + sellCoin.getTitle()); //交易市场标题的显示
        market.setImg(sellCoin.getImg()); //交易市场图标
        market.setSymbol(buyCoin.getName() + sellCoin.getName()); //交易市场标识的显示

        return super.save(market);
    }
}
