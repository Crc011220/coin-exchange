package com.rc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rc.mapper.CoinConfigMapper;
import com.rc.domain.CoinConfig;
import com.rc.service.CoinConfigService;
@Service
public class CoinConfigServiceImpl extends ServiceImpl<CoinConfigMapper, CoinConfig> implements CoinConfigService{

    @Override
    public CoinConfig findByCoinId(Long id) {
        // coinConfig的id和coin的id是一样的
        return getOne(new LambdaQueryWrapper<CoinConfig>().eq(CoinConfig::getId, id));
    }
}
