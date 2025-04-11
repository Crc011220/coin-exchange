package com.rc.controller;

import com.rc.domain.CoinConfig;
import com.rc.model.R;
import com.rc.service.CoinConfigService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/coinConfigs")
@Api(tags = "币种配置信息")
public class CoinConfigController {

    @Autowired
    private CoinConfigService coinConfigService;

    @GetMapping("/info/{id}")
    @ApiOperation(value = "查询币种的详细信息")
    @ApiImplicitParams(
            @ApiImplicitParam(name = "id", value = "币种的id")
    )
    public R<CoinConfig> getCoinConfig(@PathVariable("id") Long id){
        return R.ok(coinConfigService.findByCoinId(id));
    }

    @PatchMapping
    @ApiOperation(value = "修改币种配置信息")
    @ApiImplicitParams(
            @ApiImplicitParam(name = "coinConfig" ,value = "coinConfig json")
    )
    public R updateCoinConfig(@RequestBody @Validated CoinConfig coinConfig){
        boolean update = coinConfigService.saveOrUpdate(coinConfig);
        if (update) {
            return R.ok();
        }
        return R.fail("修改失败");
    }

}
