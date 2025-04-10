package com.rc.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rc.domain.CoinType;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface CoinTypeService extends IService<CoinType>{

    // 条件分页查询货币类型

    Page<CoinType> findByPage(Page<CoinType> page, String code);

    //查询所有的币种类型
    List<CoinType> listByStatus(Byte status);
}
