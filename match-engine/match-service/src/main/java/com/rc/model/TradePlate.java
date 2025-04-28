package com.rc.model;

import com.rc.domain.DepthItemVo;
import com.rc.enums.OrderDirection;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * 交易的盘口数据,以后前端可以查询该数据
 */
@Data
public class TradePlate {

    /**
     * 判断数据的详情
     */
    private LinkedList<DepthItemVo> items = new LinkedList<>();
    /**
     * 最大支持的深度
     */
    private int maxDepth = 100;

    /**
     * 订单的方向
     */
    private OrderDirection direction;

    /**
     * 交易对
     */
    private String symbol;

    public TradePlate(String symbol, OrderDirection direction) {
        this.symbol = symbol;
        this.direction = direction;
    }


    // 从盘口里面移除订单
    public void remove(Order order){
        remove(order, order.getAmount().subtract(order.getTradedAmount()));
    }

    public void remove(Order order, BigDecimal tradedAmount) {

        if(items.isEmpty()){
            return;
        }
        if(order.getOrderDirection() != direction.getCode()){
            return;
        }

        Iterator<DepthItemVo> iterator = items.iterator();
        while (iterator.hasNext()){
            DepthItemVo next = iterator.next();
            if(order.getPrice().compareTo(next.getPrice()) == 0){
                next.setVolume(next.getVolume().subtract(tradedAmount));
                if (next.getVolume().compareTo(BigDecimal.ZERO) <= 0) { // 如果移除后，该价格下的数量为0，则移除该价格下的所有数据
                    iterator.remove();
                }
            }
        }
    }

    // 添加盘口数据
    public void add(Order order) {
        if(order.getOrderDirection() != direction.getCode()){
            return;
        }
        int i =0;
        for(i=0; i<items.size(); i++){
            DepthItemVo depthItemVo = items.get(i);
            if( // 还没到
                    (direction.getCode() == OrderDirection.BUY.getCode() && order.getPrice().compareTo(depthItemVo.getPrice()) < 0)
                    || (direction.getCode() == OrderDirection.SELL.getCode() && order.getPrice().compareTo(depthItemVo.getPrice()) > 0)
            ) {
                continue;
            } else if (order.getPrice().compareTo(depthItemVo.getPrice()) == 0){ // 价格相同, 修改数量
                // 当前 volume + 订单总量（amount） - 已成交量（tradedAmount）
                depthItemVo.setVolume(depthItemVo.getVolume().add(order.getAmount()).subtract(order.getTradedAmount()));
                return;
            } else { // 价格不同，插入
                break;
            }
        }
        if (i < maxDepth){
            DepthItemVo depthItemVo = new DepthItemVo();
            depthItemVo.setPrice(order.getPrice());
            depthItemVo.setVolume(order.getAmount().subtract(order.getTradedAmount()));
            items.add(i, depthItemVo);
        }

    }

}
