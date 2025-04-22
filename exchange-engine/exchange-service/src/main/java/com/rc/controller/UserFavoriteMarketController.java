package com.rc.controller;

import cn.hutool.core.lang.Snowflake;
import com.rc.domain.Market;
import com.rc.domain.UserFavoriteMarket;
import com.rc.model.R;
import com.rc.service.MarketService;
import com.rc.service.UserFavoriteMarketService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/userFavorites")
@Api(tags = "用户收藏和取消收藏市场")
public class UserFavoriteMarketController {

    @Autowired
    private UserFavoriteMarketService userFavoriteMarketService;

    @Autowired
    private MarketService marketService;

    @Autowired
    private Snowflake snowflake;

    @PostMapping("/addFavorite")
    @ApiOperation("用户收藏某个市场")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "market", value = "市场的交易对和类型")
    })
    public R addFavorite(@RequestBody Market market) {
        UserFavoriteMarket userFavoriteMarket = new UserFavoriteMarket();
        Long userId = Long.valueOf(SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString());

        userFavoriteMarket.setUserId(userId);

        String symbol = market.getSymbol();

        Market marketDb = marketService.getMarketBySymbol(symbol);
        userFavoriteMarket.setMarketId(marketDb.getId());
        userFavoriteMarket.setType(Integer.valueOf(market.getType()));
        userFavoriteMarket.setId(snowflake.nextId()); // 雪花算法

        boolean save = userFavoriteMarketService.save(userFavoriteMarket);
        if (save) {
            return R.ok();
        }
        return R.fail("收藏失败");
    }


    @DeleteMapping("/{symbol}")
    @ApiOperation("用户取消收藏某个市场")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "symbol", value = "市场的交易对")
    })
    public R removeFavorite(@PathVariable String symbol) {
        Market marketBySymbol = marketService.getMarketBySymbol(symbol);
        Long userId = Long.valueOf(SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString());
        boolean delete = userFavoriteMarketService.deleteUserFavorite(marketBySymbol.getId(), userId);
        if (delete) {
            return R.ok();
        }
        return R.fail("取消收藏失败");
    }

}
