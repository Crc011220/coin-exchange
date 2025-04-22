package com.rc.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rc.domain.EntrustOrder;
import com.baomidou.mybatisplus.extension.service.IService;
public interface EntrustOrderService extends IService<EntrustOrder>{


    // 查询用户的委托记录

    Page<EntrustOrder> findByPage(Page<EntrustOrder> page, String symbol, Integer status, Long userId);
}
