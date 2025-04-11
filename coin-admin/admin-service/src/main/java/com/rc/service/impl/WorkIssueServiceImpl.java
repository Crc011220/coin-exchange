package com.rc.service.impl;

import com.alibaba.csp.sentinel.util.StringUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rc.dto.UserDto;
import com.rc.feign.UserServiceFeign;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rc.mapper.WorkIssueMapper;
import com.rc.domain.WorkIssue;
import com.rc.service.WorkIssueService;
import org.springframework.util.CollectionUtils;

@Service
public class WorkIssueServiceImpl extends ServiceImpl<WorkIssueMapper, WorkIssue> implements WorkIssueService{

    @Autowired
    private UserServiceFeign userServiceFeign ;


    @Override
    public Page<WorkIssue> findByPage(Page<WorkIssue> page, Integer status, String startTime, String endTime) {
        Page<WorkIssue> pageData = page(page, new LambdaQueryWrapper<WorkIssue>()
                .eq(status != null, WorkIssue::getStatus, status)
                .between(StringUtil.isNotEmpty(startTime) && StringUtil.isNotEmpty(endTime), WorkIssue::getCreated, startTime, endTime + " 23:59:59")
        );


        if(CollectionUtils.isEmpty(pageData.getRecords())){
            return pageData ;
        }
        List<Long> userIds = pageData.getRecords()
                .stream()
                .map(WorkIssue::getUserId)
                .collect(Collectors.toList());

        Map<Long, UserDto> idMappings = userServiceFeign.getBasicUsers(userIds,null,null);

        pageData.getRecords().forEach(workIssue->{
            UserDto userDto = idMappings.get(workIssue.getUserId());
            workIssue.setUserName(userDto==null?"测试用户":userDto.getUsername());
            workIssue.setRealName(userDto==null?"测试用户":userDto.getRealName());
        });

        return pageData;
    }

}
