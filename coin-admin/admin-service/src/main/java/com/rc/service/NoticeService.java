package com.rc.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rc.domain.Notice;
import com.baomidou.mybatisplus.extension.service.IService;
public interface NoticeService extends IService<Notice>{

    // 根据条件分页查询公告
    Page<Notice> findByPage(Page<Notice> page, String title, String startTime, String endTime, Integer status);

    // 删除一个公告 (前台查询，不带权限)
    Page<Notice> findNoticeForSimple(Page<Notice> page);
}
