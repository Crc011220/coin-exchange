package com.rc.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rc.domain.EntrustOrder;
import com.rc.model.R;
import com.rc.service.EntrustOrderService;
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
@RequestMapping ("/entrustOrders")
@Api(tags = "委托订单的controller")
public class EntrustOrderController {

    @Autowired
    private EntrustOrderService entrustOrderService;

    @GetMapping
    @ApiOperation(value = "分页查询委托订单记录")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", value = "页码"),
            @ApiImplicitParam(name = "size", value = "每页数量"),
            @ApiImplicitParam(name = "symbol", value = "交易对"),
            @ApiImplicitParam(name = "type", value = "类型(买入还是卖出)")
    })
    public R<Page<EntrustOrder>> findByPage(@ApiIgnore Page<EntrustOrder> page, String symbol, Integer type) {
        Long userId = Long.valueOf(SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString());
        Page<EntrustOrder> entrustOrderPage = entrustOrderService.findByPage(page, symbol, type, userId);
        return R.ok(entrustOrderPage);
    }


}
