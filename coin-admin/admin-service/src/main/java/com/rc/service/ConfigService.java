package com.rc.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rc.domain.Config;
import com.baomidou.mybatisplus.extension.service.IService;
public interface ConfigService extends IService<Config>{

    // 条件分页查询 通过规格类型，规则名称，规则代码
    Page<Config> findByPage(Page<Config> page, String type, String name, String code);
}
