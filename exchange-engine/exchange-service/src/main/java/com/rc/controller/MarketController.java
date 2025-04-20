package com.rc.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rc.domain.Market;
import com.rc.model.R;
import com.rc.service.MarketService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;

@RestController
@RequestMapping("/markets")
@Api(tags = "交易市场的controller")
public class MarketController {

    @Autowired
    private MarketService marketService;

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
}
