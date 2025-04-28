package com.rc.model;

import com.rc.enums.OrderDirection;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

@Data
@Slf4j
public class OrderBooks {

    /**
     * 买入的限价交易 价格从高到底
     * eg: 价格越高，越容易买到
     * Key: 价格
     * MergeOrder 同价格的订单，订单按照时间排序
     */
    private TreeMap<BigDecimal, MergeOrder> buyLimitPrice;

    /**
     * 卖出的限价交易，价格从低到高
     * eg: 价格越低，卖出的越容易
     */
    private TreeMap<BigDecimal, MergeOrder> sellLimitPrice;
    /**
     * 交易的币种
     */
    private String symbol;

    /**
     * 交易币种的精度
     */
    private int coinScale;

    /**
     * 基币的精度
     */
    private int baseCoinScale;

    // 买方交易盘口
    private TradePlate buyTradePlate;

    // 卖方交易盘口
    private TradePlate sellTradePlate;


    /**
     * 日期格式器
     */
    private SimpleDateFormat dateTimeFormat;

    public OrderBooks(String symbol){
        this(symbol,4,4) ;
    }

    public OrderBooks(String symbol,int coinScale,int baseCoinScale){
        this.symbol = symbol ;
        this.coinScale = coinScale ;
        this.baseCoinScale = baseCoinScale ;
        this.initialize();
    }



    /**
     * 初始化订单队列
     */
    public void initialize() {
        log.info("init CoinTrader for symbol {}", symbol);
        // 载入比较器
        buyLimitPrice = new TreeMap<>(Comparator.reverseOrder()); //价格从大到小

        sellLimitPrice = new TreeMap<>(Comparator.naturalOrder()); // 价格从小到大

        dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    }

    /**
     * 获取当前的交易队列
     * @param orderDirection 交易方向
     */
    public TreeMap<BigDecimal,MergeOrder> getCurrentOrders(OrderDirection orderDirection){
        return orderDirection == OrderDirection.BUY ? this.buyLimitPrice : this.sellLimitPrice ;
    }


    /**
     * 获取当前交易队列的迭代器
     * @param orderDirection 交易方向
     */
    public Iterator<Map.Entry<BigDecimal,MergeOrder>> getCurrentOrderIterator(OrderDirection orderDirection){
        return  getCurrentOrders(orderDirection).entrySet().iterator();
    }

    /**
     * 将订单添加到限价队列里面，限价队列的数据是使用价格和时间排序的
     * @param order 订单
     */
    public void addOrder(Order order) {
        TreeMap<BigDecimal, MergeOrder> limitPriceMap = getCurrentOrders(OrderDirection.getOrderDirection(order.getOrderDirection()));

        MergeOrder mergeOrder = buyLimitPrice.get(order.getPrice());
        // 注意，此处均为单线程操作，无需考虑并发问题，当为集群或多线程时， 需要添加锁/分布式锁
        if (mergeOrder == null) { // 之前不存在
            mergeOrder = new MergeOrder();
            // 之前的二叉树里面不存在该节点，插入进去
            limitPriceMap.put(order.getPrice(), mergeOrder);
        }
        // 添加到水平的订单里面
        mergeOrder.add(order);

        if (order.getOrderDirection() == OrderDirection.BUY.getCode()) {
            buyTradePlate.add(order);
        } else {
            sellTradePlate.add(order);
        }

    }

    /**
     * 从交易队列里面移除
     * @param order 订单
     */
    public void cancelOrder(Order order) {
        // 获取当前要操作的数据容器
        TreeMap<BigDecimal, MergeOrder> limitPriceMap =
                getCurrentOrders(OrderDirection.getOrderDirection(order.getOrderDirection())) ;

        MergeOrder mergeOrder = limitPriceMap.get(order.getPrice());
        if (mergeOrder == null || mergeOrder.size() <= 0) {
            return;
        }
        Iterator<Order> iterator = mergeOrder.iterator();
        while (iterator.hasNext()) {
            Order each = iterator.next();
            if (each.getOrderId().equals(order.getOrderId())) {
                iterator.remove(); // 移除该订单
            }
        }
        // 订单移除成功后，需要判断，移除此订单后，是否要移除之前的二叉树结点
        if (mergeOrder.size() <= 0) {
            limitPriceMap.remove(order.getPrice());
        }

        if (order.getOrderDirection() == OrderDirection.BUY.getCode()) {
            buyTradePlate.remove(order);
        } else {
            sellTradePlate.remove(order);
        }

    }


    /**
     * 获取排在队列里面的第一个数据
     * @param orderDirection 方向
     */
    public Map.Entry<BigDecimal ,MergeOrder> getBestSuitPriceMergeOrder(OrderDirection orderDirection){
        return getCurrentOrders(orderDirection).firstEntry();
    }



}
