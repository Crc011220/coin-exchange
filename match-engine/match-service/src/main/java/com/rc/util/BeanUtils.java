package com.rc.util;

import com.rc.domain.EntrustOrder;
import com.rc.model.Order;

public class BeanUtils {
    public static Order entrustOrder2Order(EntrustOrder entrustOrder) {
        Order order = new Order();
        order.setSymbol(entrustOrder.getSymbol()); // 设置交易对
        order.setAmount(entrustOrder.getVolume().add(entrustOrder.getDeal().negate())); // 设置交易额 = 总数量 - 成交数量
        order.setPrice(entrustOrder.getPrice()); // 设置交易价格
        order.setTime(entrustOrder.getCreated().getTime()); // 设置交易时间
        order.setOrderId(entrustOrder.getId().toString()); // 设置交易的id
        order.setOrderDirection(entrustOrder.getType().intValue()); // 设置交易的方向
        return order;
    }
}
