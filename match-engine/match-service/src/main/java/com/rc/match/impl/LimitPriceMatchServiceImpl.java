package com.rc.match.impl;

import com.rc.enums.OrderDirection;
import com.rc.match.MatchService;
import com.rc.match.MatchServiceFactory;
import com.rc.match.MatchStrategy;
import com.rc.model.*;
import com.rc.rocket.Source;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.rc.enums.OrderDirection.getOrderDirection;

@Service
@Slf4j
public class LimitPriceMatchServiceImpl implements MatchService, InitializingBean {

    @Autowired
    private Source source;

    /**
     * 进行订单的撮合交易
     */
    @Override
    public void match(OrderBooks orderBooks, Order order) {
      log.info("开始撮合");

      // order价格校验
      if(order.getPrice().compareTo(BigDecimal.ZERO) <= 0){
          log.error("订单价格错误");
          return;
      }

      // 获取一个挂单队列
      Iterator<Map.Entry<BigDecimal, MergeOrder>> marketQueueIterator;
      if (order.getOrderDirection() == OrderDirection.BUY.getCode()) {
          marketQueueIterator = orderBooks.getCurrentOrderIterator(OrderDirection.SELL);
      } else {
          marketQueueIterator = orderBooks.getCurrentOrderIterator(OrderDirection.BUY);
      }

      boolean exit = false;
      List<Order> completedOrders = new ArrayList<>(); // 已完成的订单
      List<ExchangeTrade> exchangeTrades = new ArrayList<>(); // 成交记录

      // 遍历队列，找到一个价格小于等于订单的，进行撮合
      while (marketQueueIterator.hasNext() && !exit) {
          Map.Entry<BigDecimal, MergeOrder> entry = marketQueueIterator.next();
          BigDecimal price = entry.getKey();
          MergeOrder mergeOrder = entry.getValue();

          // 买入，如果卖单价格大于订单价格，则跳出循环，无法买入
          if (order.getOrderDirection() == OrderDirection.BUY.getCode() && price.compareTo(order.getPrice()) < 0) {
              break;
          }
          // 卖出，如果买单价格小于订单价格，则跳出循环，无法卖出
          if (order.getOrderDirection() == OrderDirection.SELL.getCode() && price.compareTo(order.getPrice()) > 0) {
              break;
          }

          // 找到了合适的价格，进行交易
          Iterator<Order> orderIterator = mergeOrder.iterator();
          while (orderIterator.hasNext()) {
              Order marker = orderIterator.next();

              ExchangeTrade exchangeTrade = processMatch(order, marker,  orderBooks);
              exchangeTrades.add(exchangeTrade); // 添加成交记录到exchangeTrades

              if(order.isCompleted()){
                  completedOrders.add(order);
                  exit = true;
                  break;
              }

              if(marker.isCompleted()){
                  completedOrders.add(marker);
                  orderIterator.remove();
              }
          }

          if (mergeOrder.size() <= 0) { // mergeOrder为空，则从队列中移除
              marketQueueIterator.remove();
          }
      }

      // 订单未完成
      if (order.getAmount().compareTo(order.getTradedAmount()) > 0){
          orderBooks.addOrder(order);
      }
      // 发送交易记录
      if (!exchangeTrades.isEmpty()){
          exchangeTradesHandler(exchangeTrades) ;
      }

      if (!completedOrders.isEmpty()) {
          completedOrdersHandler(completedOrders);
          TradePlate tradePlate = order.getOrderDirection() == OrderDirection.BUY.getCode()
                            ? orderBooks.getBuyTradePlate() : orderBooks.getSellTradePlate();
          sendTradePlate(tradePlate);
      }

    }

    // 发送盘口数据,供以后我们前端的数据更新
    private void sendTradePlate(TradePlate tradePlate) {
        source.plateOut()
                .send(MessageBuilder
                .withPayload(tradePlate)
                        .setHeader(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON)
                        .build());
    }

    // 订单的完成
    private void completedOrdersHandler(List<Order> completedOrders) {
        source.completedOrderOut()
                .send(MessageBuilder
                        .withPayload(completedOrders)
                        .setHeader(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON)
                        .build());
    }

    //  处理订单的记录
    private void exchangeTradesHandler(List<ExchangeTrade> exchangeTrades) {
        source.exchangeTradesOut()
                .send(MessageBuilder
                        .withPayload(exchangeTrades)
                        .setHeader(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON)
                        .build());

    }


    /**
     * 进行委托单的匹配撮合交易
     *
     * @param taker  吃单 当前正在提交的订单，它会去匹配订单簿上的挂单
     * @param marker 挂单 在市场上已有的订单，等待被吃单匹配
     * @return ExchangeTrade 交易记录
     */

    private ExchangeTrade processMatch(Order taker, Order marker, OrderBooks orderBooks) {
        // 成交的价格
        BigDecimal dealPrice = marker.getPrice();
        // 本次需要的数量
        BigDecimal needAmount = calcTradeAmount(taker);
        // 本次提供的数量
        BigDecimal providerAmount = calcTradeAmount(marker);
        // 成交的数量
        BigDecimal turnoverAmount = needAmount.compareTo(providerAmount) <= 0 ? needAmount : providerAmount;

        if (turnoverAmount.compareTo(BigDecimal.ZERO) == 0) {
            return null; // 无法成交
        }

        // 设置本次吃单的成交数据
        taker.setTradedAmount(taker.getTradedAmount().add(turnoverAmount));
        BigDecimal takerTurnover = turnoverAmount.multiply(dealPrice)
                .setScale(orderBooks.getCoinScale(), RoundingMode.HALF_UP);
        taker.setTurnover(takerTurnover);

        // 设置本次挂单的成交数据
        marker.setTradedAmount(marker.getTradedAmount().add(turnoverAmount));
        BigDecimal markerTurnover = turnoverAmount.multiply(dealPrice)
                .setScale(orderBooks.getBaseCoinScale(), RoundingMode.HALF_UP);
        marker.setTurnover(markerTurnover);

        ExchangeTrade exchangeTrade = new ExchangeTrade();
        exchangeTrade.setAmount(turnoverAmount); // 设置购买的数量
        exchangeTrade.setPrice(dealPrice);  // 设置购买的价格
        exchangeTrade.setTime(System.currentTimeMillis()); // 设置成交的时间
        exchangeTrade.setSymbol(orderBooks.getSymbol());  // 设置成交的交易对
        exchangeTrade.setDirection(getOrderDirection(taker.getOrderDirection()));  // 设置交易的方法
        exchangeTrade.setSellOrderId(marker.getOrderId()); // 设置出售方的id
        exchangeTrade.setBuyOrderId(taker.getOrderId()); // 设置买方的id
        exchangeTrade.setBuyTurnover(taker.getTurnover()); // 设置买方的交易额
        exchangeTrade.setSellTurnover(marker.getTurnover()); // 设置卖方的交易额

        // 更新盘口
        if (taker.getOrderDirection() == OrderDirection.BUY.getCode()) {
            orderBooks.getBuyTradePlate().remove(marker, turnoverAmount);
        } else {
            orderBooks.getSellTradePlate().remove(marker, turnoverAmount);
        }


        return exchangeTrade;

    }

    // 计算本次的交易额
    private BigDecimal calcTradeAmount(Order order) {
        return order.getAmount().subtract(order.getTradedAmount());
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        MatchServiceFactory.addMatchService(MatchStrategy.LIMIT_PRICE,this);
    }
}
