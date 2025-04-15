package com.rc.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rc.domain.AccountDetail;
import com.rc.model.R;
import com.rc.service.AccountDetailService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

@RestController
@RequestMapping("/accountDetails")
public class AccountDetailController {

    @Autowired
    private AccountDetailService accountDetailService;
    @GetMapping("/records")
    @ApiOperation(value = "分页查询资金账户流水")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "current", value = "当前页"),
            @ApiImplicitParam(name = "size", value = "每页显示的条数"),
            @ApiImplicitParam(name = "accountId", value = "账户的Id"),
            @ApiImplicitParam(name = "coinId", value = "币种的Id"),
            @ApiImplicitParam(name = "userId", value = "用户的Id"),
            @ApiImplicitParam(name = "userName", value = "用户的名称"),
            @ApiImplicitParam(name = "mobile", value = "用户的手机号"),
            @ApiImplicitParam(name = "amountStart", value = "金额最小"),
            @ApiImplicitParam(name = "amountEnd", value = "金额最大"),
            @ApiImplicitParam(name = "startTime", value = "充值开始时间"),
            @ApiImplicitParam(name = "endTime", value = "充值结束时间"),

    })
    public R<Page<AccountDetail>> findByPage(
            @ApiIgnore Page<AccountDetail> page,
            Long accountId,
            Long coinId,
            Long userId, String userName,
            String mobile, String amountStart, String amountEnd,
            String startTime, String endTime
            ){
        Page<AccountDetail> pageData = accountDetailService.findByPage(page, accountId, coinId, userId, userName, mobile, amountStart, amountEnd, startTime, endTime);
        return R.ok(pageData);
    }
}
