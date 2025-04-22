package com.rc.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rc.domain.TradeArea;
import com.rc.model.R;
import com.rc.service.TradeAreaService;
import com.rc.vo.TradeAreaMarketVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;

@RestController
@RequestMapping("/tradeAreas")
@Api(tags = "交易区域的controller")
public class TradeAreaController {

    @Autowired
    private TradeAreaService tradeAreaService;

    @GetMapping
    @ApiOperation(value = "分页查询交易区域")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "current", value = "当前页"),
            @ApiImplicitParam(name = "size", value = "每页显示的条数"),
            @ApiImplicitParam(name = "name", value = "交易区名称"),
            @ApiImplicitParam(name = "status", value = "交易区状态")
    })
    @PreAuthorize("hasAuthority('trade_area_query')")
    public R<Page<TradeArea>> findByPage(@ApiIgnore Page<TradeArea> page, String name, Byte status) {
        return R.ok(tradeAreaService.findByPage(page, name, status));
    }

    @PostMapping
    @ApiOperation(value = "新增一个交易区域")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "tradeArea", value = "tradeAreaJson"),
    })
    @PreAuthorize("hasAuthority('trade_area_create')")
    public R save(@RequestBody TradeArea tradeArea) {

        boolean save = tradeAreaService.save(tradeArea);
        if (save){
            return R.ok();
        }
        return R.fail("新增失败");
    }

    @PatchMapping
    @ApiOperation(value = "编辑一个交易区域")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "tradeArea", value = "tradeAreaJson"),
    })
    @PreAuthorize("hasAuthority('trade_area_update')")
    public R update(@RequestBody TradeArea tradeArea) {
        boolean update = tradeAreaService.updateById(tradeArea);
        if (update){
            return R.ok();
        }
        return R.fail("编辑失败");
    }

    @PostMapping("/status")
    @ApiOperation(value = "修改status为禁用或者启用")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "tradeArea", value = "tradeAreaJson"),
    })
    @PreAuthorize("hasAuthority('trade_area_update')")
    public R updateStatus(@RequestBody TradeArea tradeArea) {
        boolean update = tradeAreaService.updateById(tradeArea);
        if (update){
            return R.ok();
        }
        return R.fail("修改失败");
    }

    @GetMapping("/all")
    @ApiOperation(value = "查询交易区域")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "status", value = "交易区域的状态"),
    })
    @PreAuthorize("hasAuthority('trade_area_query')")
    public R<List<TradeArea>> findAll(Byte status) {
        List<TradeArea> tradeAreas = tradeAreaService.findAll(status);
        return R.ok(tradeAreas);
    }

    @GetMapping("/markets")
    @ApiOperation(value = "查询交易区域以及区域下的市场")
    public R<List<TradeAreaMarketVo>> getTradeAreaMarkets() {
        List<TradeAreaMarketVo> tradeAreas = tradeAreaService.findTradeAreaMarkets();
        return R.ok(tradeAreas);
    }

    @GetMapping("/market/favorite")
    @ApiOperation(value = "查询用户收藏的交易区域以及区域下的市场")
    public R<List<TradeAreaMarketVo>> getFavoriteTradeAreaMarkets() {
        Long userId = Long.valueOf(SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString());
        List<TradeAreaMarketVo> tradeAreas = tradeAreaService.findFavoriteTradeAreaMarkets(userId);
        return R.ok(tradeAreas);
    }
}
