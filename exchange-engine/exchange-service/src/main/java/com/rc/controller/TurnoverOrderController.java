package com.rc.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rc.domain.TurnoverOrder;
import com.rc.model.R;
import com.rc.service.TurnoverOrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

@RestController
@RequestMapping("/turnoverOrders")
@Api(tags = "成交记录")
public class TurnoverOrderController {

    @Autowired
    private TurnoverOrderService turnoverOrderService;

    @GetMapping
    @ApiOperation(value = "分页查询成交记录")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", value = "页码"),
            @ApiImplicitParam(name = "size", value = "每页数量"),
            @ApiImplicitParam(name = "symbol", value = "交易对"),
            @ApiImplicitParam(name = "type", value = "类型(买入还是卖出)")
    })
    public R<Page<TurnoverOrder>> findByPage(@ApiIgnore Page<TurnoverOrder> page, String symbol, Integer type) {
        Long userId = Long.valueOf(SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString());
        return R.ok(turnoverOrderService.findByPage(page, symbol, type, userId));
    }
}
