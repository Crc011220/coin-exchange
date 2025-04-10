package com.rc.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rc.domain.Coin;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface CoinService extends IService<Coin>{

    //分页条件查询数字货币
    Page<Coin> findByPage(String name, String type, Byte status, String title, String walletType, Page<Coin> page);

    //通过状态查询所有的币种信息
    List<Coin> getCoinsByStatus(Byte status);
}
