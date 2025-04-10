package com.rc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rc.domain.Coin;
import com.rc.service.CoinService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rc.domain.AdminAddress;
import com.rc.mapper.AdminAddressMapper;
import com.rc.service.AdminAddressService;
@Service
public class AdminAddressServiceImpl extends ServiceImpl<AdminAddressMapper, AdminAddress> implements AdminAddressService{

    @Autowired
    private CoinService coinService;
    @Override
    public Page<AdminAddress> findByPage(Page<AdminAddress> page, Long coinId) {
        return page(page, new LambdaQueryWrapper<AdminAddress>().eq(coinId != null,AdminAddress::getCoinId, coinId));
    }

    // 让归集地址能够包含CoinType
    @Override
    public boolean save(AdminAddress adminAddress) {
        Long coinId = adminAddress.getCoinId();
        Coin coin = coinService.getById(coinId);
        if (coin == null) {
            throw  new IllegalArgumentException("输入的币种id错误");
        }
        String type = coin.getType();
        adminAddress.setCoinType(type);
        return super.save(adminAddress);
    }
}
