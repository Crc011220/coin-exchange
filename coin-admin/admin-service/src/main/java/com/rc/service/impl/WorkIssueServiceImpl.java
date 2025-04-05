package com.rc.service.impl;

import com.alibaba.csp.sentinel.util.StringUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rc.mapper.WorkIssueMapper;
import com.rc.domain.WorkIssue;
import com.rc.service.WorkIssueService;
@Service
public class WorkIssueServiceImpl extends ServiceImpl<WorkIssueMapper, WorkIssue> implements WorkIssueService{

    @Override
    public Page<WorkIssue> findByPage(Page<WorkIssue> page, Integer status, String startTime, String endTime) {
        return page(page, new LambdaQueryWrapper<WorkIssue>()
               .eq(status != null, WorkIssue::getStatus, status)
               .between(StringUtil.isNotEmpty(startTime) && StringUtil.isNotEmpty(endTime), WorkIssue::getCreated, startTime, endTime + " 23:59:59")
        );
    }
}
