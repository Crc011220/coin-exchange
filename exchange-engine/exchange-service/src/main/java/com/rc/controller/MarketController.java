package com.rc.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rc.domain.DepthItemVo;
import com.rc.domain.Market;
import com.rc.domain.TurnoverOrder;
import com.rc.dto.MarketDto;
import com.rc.feign.MarketServiceFeign;
import com.rc.mappers.MarketDtoMappers;
import com.rc.model.R;
import com.rc.service.MarketService;
import com.rc.service.TurnoverOrderService;
import com.rc.vo.DepthsVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/markets")
@Api(tags = "交易市场的controller")
public class MarketController implements MarketServiceFeign {

    @Autowired
    private MarketService marketService;

    @Autowired
    private TurnoverOrderService turnoverOrderService;

    @GetMapping
    @ApiOperation(value = "分页查询交易市场")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "current", value = "当前页"),
            @ApiImplicitParam(name = "size", value = "每页显示的条数"),
            @ApiImplicitParam(name = "tradeAreaId", value = "交易市场的Id"),
            @ApiImplicitParam(name = "status", value = "交易市场状态")
    })
    @PreAuthorize("hasAuthority('trade_market_query')")
    public R<Page<Market>> findByPage(@ApiIgnore Page<Market> page, Long tradeAreaId, Byte status){
        return R.ok(marketService.findByPage(page, tradeAreaId, status));
    }

    @PostMapping("/setStatus")
    @ApiOperation(value = "设置交易市场状态")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "market", value = "市场的json数据"),
    })
    @PreAuthorize("hasAuthority('trade_market_update')")
    public R setStatus(@RequestBody Market market){
        boolean b = marketService.updateById(market);
        if (b){
            return R.ok();
        }
        return R.fail("设置失败");
    }

    @PostMapping
    @ApiOperation(value = "新增一个交易市场")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "market", value = "市场的json数据"),
    })
    @PreAuthorize("hasAuthority('trade_market_create')")
    public R save(@RequestBody Market market){
        boolean b = marketService.save(market);
        if (b){
            return R.ok();
        }
        return R.fail("新增失败");
    }

    @PatchMapping
    @ApiOperation(value = "修改一个交易市场")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "market", value = "市场的json数据"),
    })
    @PreAuthorize("hasAuthority('trade_market_update')")
    public R update(@RequestBody Market market){
        boolean b = marketService.updateById(market);
        if (b){
            return R.ok();
        }
        return R.fail("修改失败");
    }

    @GetMapping("/all")
    @ApiOperation(value = "查询所有的交易市场")
    public R<List<Market>> listMarkets(){
        return R.ok(marketService.list());
    }

    // 使用报价货币和基础货币查询交易市场
    @Override
    public MarketDto findBySellAndBuyCoinId(Long sellCoinId, Long buyCoinId) {
        return marketService.findBySellAndBuyCoinId(sellCoinId, buyCoinId);
    }

    @Override
    public MarketDto findBySymbol(String symbol) {
        Market markerBySymbol = marketService.getMarketBySymbol(symbol);
        return MarketDtoMappers.INSTANCE.toMarketDto(markerBySymbol);
    }

    @ApiOperation(value = "通过的交易对以及深度查询当前的市场的深度数据")
    @GetMapping("/depth/{symbol}/{dept}")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "symbol", value = "交易对"),
            @ApiImplicitParam(name = "dept", value = "深度类型"),
    })
    public R<DepthsVo> findDeptVosSymbol(@PathVariable String symbol, @PathVariable String dept) {
        // 交易市场
        Market market = marketService.getMarketBySymbol(symbol);

        DepthsVo depthsVo = new DepthsVo();
        depthsVo.setCnyPrice(market.getOpenPrice()); // CNY的价格
        depthsVo.setPrice(market.getOpenPrice()); // GCN的价格
//        Map<String, List<DepthItemVo>> depthMap = orderBooksFeignClient.querySymbolDepth(symbol);
//        if (!CollectionUtils.isEmpty(depthMap)) {
//            depthsVo.setAsks(depthMap.get("asks"));
//            depthsVo.setBids(depthMap.get("bids"));
//        }
        depthsVo.setAsks(Arrays.asList(new DepthItemVo(BigDecimal.valueOf(7.000000), BigDecimal.valueOf(100)), new DepthItemVo(BigDecimal.valueOf(6.99990), BigDecimal.valueOf(200))));
        depthsVo.setBids(Arrays.asList(new DepthItemVo(BigDecimal.valueOf(7.000000), BigDecimal.valueOf(100)), new DepthItemVo(BigDecimal.valueOf(6.99990), BigDecimal.valueOf(200))));
        return R.ok(depthsVo);
    }

    @ApiOperation(value = "查询成交记录")
    @GetMapping("/trades/{symbol}")
    public R<List<TurnoverOrder>> findSymbolTurnoverOrder(@PathVariable("symbol") String symbol) {
        List<TurnoverOrder> turnoverOrders = turnoverOrderService.findBySymbol(symbol);
        return R.ok(turnoverOrders);
    }
}
