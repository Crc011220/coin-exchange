package com.rc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rc.domain.Market;
import com.rc.service.MarketService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rc.mapper.EntrustOrderMapper;
import com.rc.domain.EntrustOrder;
import com.rc.service.EntrustOrderService;
@Service
public class EntrustOrderServiceImpl extends ServiceImpl<EntrustOrderMapper, EntrustOrder> implements EntrustOrderService{

    @Autowired
    private MarketService marketService;

    @Override
    public Page<EntrustOrder> findByPage(Page<EntrustOrder> page, String symbol, Integer type, Long userId) {


        return page(page, new LambdaQueryWrapper<EntrustOrder>()
                .eq(EntrustOrder::getUserId, userId)
                .eq(StringUtils.isNotEmpty(symbol), EntrustOrder::getSymbol, symbol)
                .eq(type != null && type != 0, EntrustOrder::getStatus, type)
                .orderByDesc(EntrustOrder::getCreated)
        );
    }
}
