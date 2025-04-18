package com.rc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rc.mapper.CoinMapper;
import com.rc.domain.Coin;
import com.rc.service.CoinService;
@Service
public class CoinServiceImpl extends ServiceImpl<CoinMapper, Coin> implements CoinService{

    @Override
    public Page<Coin> findByPage(String name, String type, Byte status, String title, String walletType, Page<Coin> page) {
        return page(page, new LambdaQueryWrapper<Coin>()
                .like(StringUtils.isNotEmpty(name), Coin::getName, name)
                .like(StringUtils.isNotEmpty(type), Coin::getType, type)
                .eq(status != null, Coin::getStatus, status)
                .like(StringUtils.isNotEmpty(title), Coin::getTitle, title)
                .like(StringUtils.isNotEmpty(walletType), Coin::getWallet, walletType)
        );
    }

    @Override
    public List<Coin> getCoinsByStatus(Byte status) {
        return list(new LambdaQueryWrapper<Coin>().eq(Coin::getStatus, status));
    }

    @Override
    public Coin getCoinByCoinName(String coinName) {
        return getOne(new LambdaQueryWrapper<Coin>().eq(Coin::getName, coinName));
    }
}
