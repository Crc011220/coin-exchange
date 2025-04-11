package com.rc.service;

import com.rc.domain.CoinConfig;
import com.baomidou.mybatisplus.extension.service.IService;
public interface CoinConfigService extends IService<CoinConfig>{

    //通过币种id查询币种配置信息
    CoinConfig findByCoinId(Long id);

    //新增或修改币种配置
    boolean updateOrSave(CoinConfig coinConfig);
}
