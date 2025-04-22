package com.rc.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rc.domain.TurnoverOrder;
import com.baomidou.mybatisplus.extension.service.IService;
public interface TurnoverOrderService extends IService<TurnoverOrder>{

    // 分页查询成交记录
    Page<TurnoverOrder> findByPage(Page<TurnoverOrder> page, String symbol, Integer type, Long userId);
}
