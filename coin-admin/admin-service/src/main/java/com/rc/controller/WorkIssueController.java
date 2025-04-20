package com.rc.controller;

import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rc.domain.WorkIssue;
import com.rc.model.R;
import com.rc.service.WorkIssueService;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

@RestController
@RequestMapping("/workIssues")
@Api(tags = "客户工单的控制器")
public class WorkIssueController {


    @Autowired
    private WorkIssueService workIssueService;

    @GetMapping
    @ApiOperation(value = "分页条件查询工单")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "status", value = "工单当前的处理状态"),
            @ApiImplicitParam(name = "startTime", value = "工单创建的起始时间"),
            @ApiImplicitParam(name = "endTime", value = "工单创建的截至时间"),
            @ApiImplicitParam(name = "current", value = "当前页"),
            @ApiImplicitParam(name = "size", value = "每页显示的条数"),
    })
    @PreAuthorize("hasAuthority('work_issue_query')")
    public R<Page<WorkIssue>> findByPage(@ApiIgnore Page<WorkIssue> page, Integer status, String startTime, String endTime) {
        page.addOrder(OrderItem.desc("last_update_time"));
        Page<WorkIssue> workIssuePage = workIssueService.findByPage(page, status, startTime, endTime);
        return R.ok(workIssuePage);
    }


    @PatchMapping
    @ApiOperation(value = "回复某个工单")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "工单的ID"),
            @ApiImplicitParam(name = "answer", value = "工单的answer"),
    })
    @PreAuthorize("hasAuthority('work_issue_update')")
    public R work_issue_update(Long id, String answer) {
        WorkIssue workIssue = new WorkIssue();
        workIssue.setId(id);
        workIssue.setAnswer(answer);

        // 设置回复人id
        String name = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        workIssue.setAnswerUserId(Long.valueOf(name));
        workIssue.setAnswerName(name);

        workIssue.setStatus(2); // 已回复

        boolean updateById = workIssueService.updateById(workIssue);
        if (updateById) {
            return R.ok();
        }
        return R.fail("回复失败");
    }

    @GetMapping("/issueList")
    @ApiOperation(value = "前台查询:分页获取工单列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "current", value = "当前页"),
            @ApiImplicitParam(name = "size", value = "每页显示的条数"),
            @ApiImplicitParam(name = "userId", value = "用户id"),
    })
    public R<Page<WorkIssue>> getIssueList(Page<WorkIssue> page){
        String name = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        return R.ok(workIssueService.getIssueList(page, Long.valueOf(name))) ;
    }

    @PostMapping("/addWorkIssue")
    @ApiOperation(value = "前台添加工单")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "workIssue", value = "workIssue json"),
    })
    public R addIssue(@RequestBody WorkIssue workIssue){
        workIssue.setUserId(Long.valueOf(SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString()));
        workIssue.setStatus(1);
        boolean save = workIssueService.save(workIssue);
        if (save){
            return R.ok() ;
        }
        return R.fail("添加失败") ;
    }


}

