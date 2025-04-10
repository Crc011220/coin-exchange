package com.rc.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rc.domain.AdminAddress;
import com.baomidou.mybatisplus.extension.service.IService;
public interface AdminAddressService extends IService<AdminAddress>{

    //查询归集地址
    Page<AdminAddress> findByPage(Page<AdminAddress> page, Long coinId);
}
