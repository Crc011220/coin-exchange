package com.rc.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rc.domain.WorkIssue;
import com.baomidou.mybatisplus.extension.service.IService;
public interface WorkIssueService extends IService<WorkIssue>{

    // 条件查询 日期和状态
    Page<WorkIssue> findByPage(Page<WorkIssue> page, Integer status, String startTime, String endTime);
}
