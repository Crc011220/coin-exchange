package com.rc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rc.mapper.WebConfigMapper;
import com.rc.domain.WebConfig;
import com.rc.service.WebConfigService;
@Service
public class WebConfigServiceImpl extends ServiceImpl<WebConfigMapper, WebConfig> implements WebConfigService{

    @Override
    public Page<WebConfig> findByPage(Page<WebConfig> page, String name, String type) {
        return page(page, new LambdaQueryWrapper<WebConfig>()
                .like(StringUtils.isNotEmpty(name), WebConfig::getName, name)
                .eq(StringUtils.isNotEmpty(type), WebConfig::getType, type)
        );
    }
}
