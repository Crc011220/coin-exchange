package com.rc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rc.domain.Market;
import com.rc.domain.UserFavoriteMarket;
import com.rc.dto.CoinDto;
import com.rc.feign.CoinServiceFeign;
import com.rc.service.MarketService;
import com.rc.service.UserFavoriteMarketService;
import com.rc.vo.MergeDeptVo;
import com.rc.vo.TradeAreaMarketVo;
import com.rc.vo.TradeMarketVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
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

    @Autowired
    private CoinServiceFeign coinServiceFeign;

    @Autowired
    private UserFavoriteMarketService userFavoriteMarketService;

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
        return tradeAreaMarketVos;
    }

    @Override
    public List<TradeAreaMarketVo> findFavoriteTradeAreaMarkets(Long userId) {
        List<UserFavoriteMarket> list = userFavoriteMarketService.list(new LambdaQueryWrapper<UserFavoriteMarket>().eq(UserFavoriteMarket::getUserId, userId));
        if (CollectionUtils.isEmpty(list)){
            return Collections.emptyList();
        }
        List<Long> marketIds = list.stream().map(UserFavoriteMarket::getMarketId).collect(Collectors.toList());

        TradeAreaMarketVo tradeAreaMarketVo = new TradeAreaMarketVo();
        tradeAreaMarketVo.setAreaName("自选");
        List<Market> markets = marketService.listByIds(marketIds);
        List<TradeMarketVo> tradeMarketVos = markets2marketVos(markets);
        tradeAreaMarketVo.setMarkets(tradeMarketVos);
        return Arrays.asList(tradeAreaMarketVo);
    }

    private List<TradeMarketVo> markets2marketVos(List<Market> markets) {
        return markets.stream().map(this::toConvertVo).collect(Collectors.toList());
    }

    private TradeMarketVo toConvertVo(Market market) {
        TradeMarketVo tradeMarketVo = new TradeMarketVo();
        tradeMarketVo.setName(market.getName());
        tradeMarketVo.setSymbol(market.getSymbol());
        tradeMarketVo.setImage(market.getImg()); // 报价货币的图片

        // TODO
        tradeMarketVo.setHigh(market.getOpenPrice()); // 开盘价
        tradeMarketVo.setLow(market.getOpenPrice()); // 获取k线数据
        tradeMarketVo.setPrice(market.getOpenPrice()); // 获取k线数据
        tradeMarketVo.setCnyPrice(market.getOpenPrice()); // 获取k线数据

        Long buyCoinId = market.getBuyCoinId();
        List<CoinDto> coins = coinServiceFeign.findCoins(Collections.singletonList(buyCoinId));
        if (coins.size() > 1 || CollectionUtils.isEmpty(coins)){
            throw new IllegalArgumentException("报价货币错误");
        }
        CoinDto coinDto = coins.get(0);
        tradeMarketVo.setPriceUnit(coinDto.getName()); // 报价货币的名称

        tradeMarketVo.setVolume(BigDecimal.ZERO); // 日交易量
        tradeMarketVo.setAmount(BigDecimal.ZERO); // 日总交易额

        tradeMarketVo.setTradeMin(market.getTradeMin()); // 最小交易额
        tradeMarketVo.setTradeMax(market.getTradeMax()); // 最大交易额

        tradeMarketVo.setNumMin(market.getNumMin()); // 单笔最小委托量
        tradeMarketVo.setNumMax(market.getNumMax()); // 单笔最大委托量

        tradeMarketVo.setSellFeeRate(market.getFeeSell()); // 卖出手续费率
        tradeMarketVo.setBuyFeeRate(market.getFeeBuy()); // 买入手续费率

        tradeMarketVo.setNumScale(market.getNumScale()); // 数量小数位数


        tradeMarketVo.setSort(market.getSort()); // 排序

        tradeMarketVo.setMergeDepth(getMergeDepths(market.getMergeDepth())); // 合并深度
        tradeMarketVo.setChange(0.00); // 涨跌幅


        return tradeMarketVo;
    }

    // 获取合并深度
    private List<MergeDeptVo> getMergeDepths(String mergeDepth) {
        String[] split = mergeDepth.split(",");

        if (split.length != 3){
            throw new IllegalArgumentException("合并深度错误");
        }

        MergeDeptVo minVo = new MergeDeptVo();
        minVo.setMergeType("MIN");
        minVo.setValue(getDeptValue(Integer.valueOf(split[0])));

        MergeDeptVo defaultVo = new MergeDeptVo();
        defaultVo.setMergeType("DEFAULT");
        minVo.setValue(getDeptValue(Integer.valueOf(split[1])));


        MergeDeptVo maxVo = new MergeDeptVo();
        maxVo.setMergeType("MAX");
        minVo.setValue(getDeptValue(Integer.valueOf(split[2])));

        ArrayList<MergeDeptVo> mergeDeptVos = new ArrayList<>();
        mergeDeptVos.add(minVo);
        mergeDeptVos.add(defaultVo);
        mergeDeptVos.add(maxVo);

        return mergeDeptVos;
    }

    private BigDecimal getDeptValue(Integer scale) {
        BigDecimal bigDecimal = BigDecimal.valueOf(Math.pow(10, scale));
        return BigDecimal.ONE.divide(bigDecimal).setScale(scale, RoundingMode.HALF_UP);
    }

}
