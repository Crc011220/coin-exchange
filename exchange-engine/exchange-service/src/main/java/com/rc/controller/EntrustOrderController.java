package com.rc.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rc.domain.EntrustOrder;
import com.rc.model.R;
import com.rc.param.OrderParam;
import com.rc.service.EntrustOrderService;
import com.rc.vo.TradeEntrustOrderVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
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


    @GetMapping("/history/{symbol}")
    @ApiOperation(value = "查询历史的委托单记录")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "current",value = "当前页") ,
            @ApiImplicitParam(name = "size",value = "条数") ,
    })
    public R<Page<TradeEntrustOrderVo>> historyEntrustOrder(@ApiIgnore Page<EntrustOrder> page , @PathVariable("symbol") String symbol){
        Long userId = Long.valueOf(SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString()) ;
        Page<TradeEntrustOrderVo> pageData = entrustOrderService.getHistoryEntrustOrder(page,symbol,userId) ;
        return R.ok(pageData) ;
    }



    @GetMapping("/{symbol}")
    @ApiOperation(value = "查询未完成的委托单记录")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "current",value = "当前页") ,
            @ApiImplicitParam(name = "size",value = "条数") ,
    })
    public R<Page<TradeEntrustOrderVo>> entrustOrders(@ApiIgnore Page<EntrustOrder> page , @PathVariable("symbol") String symbol){
        Long userId = Long.valueOf(SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString()) ;
        Page<TradeEntrustOrderVo> pageData = entrustOrderService.getEntrustOrder(page,symbol,userId) ;
        return R.ok(pageData) ;
    }


    @PostMapping
    @ApiOperation(value = "委托单的下单操作")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "orderParam",value = "orderParam json数据")
    })
    public R createEntrustOrder(@RequestBody OrderParam orderParam){
        Long userId = Long.valueOf(SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString()) ;
        Boolean isOk = entrustOrderService.createEntrustOrder(userId,orderParam) ;
        return isOk ? R.ok() :R.fail("创建失败") ;
    }


    @ApiOperation(value = "委托单的撤销操作")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id" ,value = "委托单的id")
    })
    @DeleteMapping("/{id}")
    public R deleteEntrustOrder(@PathVariable("id") Long orderId){
        entrustOrderService.cancelEntrustOrder(orderId) ;
        return R.ok("取消成功") ;
    }


}
