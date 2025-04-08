package com.rc.controller;

import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rc.domain.UserBank;
import com.rc.model.R;
import com.rc.service.UserBankService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/userBanks")
@Api(tags = "会员的银行卡管理")
public class UserBankController {


    @Autowired
    private UserBankService userBankService ;


    @GetMapping
    @ApiOperation(value = "分页查询用户的银行卡")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "usrId" ,value = "会员的Id") ,
            @ApiImplicitParam(name = "current" ,value = "当前页")  ,
            @ApiImplicitParam(name = "size" ,value = "每页显示的条数")
    })
    @PreAuthorize("hasAuthority('user_bank_query')")
    public R<Page<UserBank>> findByPage(Page<UserBank> page , Long usrId){
        page.addOrder(OrderItem.desc("last_update_time")) ;
        Page<UserBank> userBankPage = userBankService.findByPage(page ,usrId) ;
        return R.ok(userBankPage) ;
    }


    @PostMapping("/status")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id" ,value = "银行卡的Id") ,
            @ApiImplicitParam(name = "status" ,value = "银行卡的状态") ,
    })
    @ApiOperation(value = "修改银行卡的状态")
    public R updateStatus(Long id ,Byte status){
        UserBank userBank = new UserBank();
        userBank.setId(id);
        userBank.setStatus(status);
        boolean updateById = userBankService.updateById(userBank);
        if(updateById){
            return R.ok() ;
        }
        return R.fail("银行卡状态修改失败") ;
    }


    @PatchMapping
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userBank" ,value = "银行卡的json数据") ,
    })
    @ApiOperation(value = "修改银行卡")
    public R updateStatus(@RequestBody  @Validated UserBank userBank){
        boolean updateById = userBankService.updateById(userBank);
        if(updateById){
            return R.ok() ;
        }
        return R.fail("银行卡状态修改失败") ;
    }

    @GetMapping("/current")
    @ApiOperation(value = "查询用户的卡号")
    public R<UserBank> currentUserBank(){
        String idStr = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        UserBank userBank =  userBankService.getUserBankByUserId(Long.valueOf(idStr)) ;
        return R.ok(userBank) ;
    }


    @PostMapping("/bind")
    @ApiOperation("绑定用户的卡号")
    public R bindUserBank(@RequestBody @Validated UserBank userBank){
        String idStr = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        boolean isOk = userBankService.bindBank(Long.valueOf(idStr),userBank);
        return isOk ? R.ok() : R.fail("绑定失败");

    }


}

