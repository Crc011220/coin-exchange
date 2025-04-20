package com.rc.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rc.domain.WebConfig;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface WebConfigService extends IService<WebConfig>{

    // 条件分页查询 webConfig的name和type
    Page<WebConfig> findByPage(Page<WebConfig> page, String name, String type);

    // 查询所有的banner
    List<WebConfig> getBanners();

}
