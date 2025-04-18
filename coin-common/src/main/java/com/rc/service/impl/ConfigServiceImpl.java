package com.rc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rc.mapper.ConfigMapper;
import com.rc.domain.Config;
import com.rc.service.ConfigService;
@Service
public class ConfigServiceImpl extends ServiceImpl<ConfigMapper, Config> implements ConfigService{

    @Override
    public Page<Config> findByPage(Page<Config> page, String type, String name, String code) {
        return page(page, new LambdaQueryWrapper<Config>()
               .like(StringUtils.isNotEmpty(type), Config::getType, type)
                .like(StringUtils.isNotEmpty(name), Config::getName, name)
                .like(StringUtils.isNotEmpty(code), Config::getCode, code)
        );
    }

    // 根据code查询费率
    @Override
    public Config getConfigByCode(String code) {
        return getOne(new LambdaQueryWrapper<Config>()
                .eq(Config::getCode, code)
        );
    }
}
