package com.rc.controller;

import com.rc.domain.Account;
import com.rc.feign.AccountServiceFeign;
import com.rc.model.R;
import com.rc.service.AccountService;
import com.rc.vo.SymbolAssetVo;
import com.rc.vo.UserTotalAccountVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/account")
@Api(tags = "资产服务的控制器")
public class AccountController implements AccountServiceFeign {


    @Autowired
    private AccountService accountService ;

    @GetMapping("/{coinName}")
    @ApiOperation(value = "获取当前用户的货币的资产情况")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "coinName" ,value = "货币的名称")
    })
    public R<Account> getUserAccount(@PathVariable("coinName") String coinName){
        Long userId = Long.valueOf(SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString()) ;
        Account account = accountService.findByUserAndCoin(userId,coinName) ;
        return R.ok(account) ;
    }


    @GetMapping("/total")
    @ApiOperation(value = "获取当前用户的总资产情况")
    public R<UserTotalAccountVo> total(){
        Long userId = Long.valueOf(SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString()) ;
        UserTotalAccountVo userTotalAccount = accountService.getUserTotalAccount(userId);
        return R.ok(userTotalAccount) ;
    }

    @GetMapping("/asset/{symbol}")
    @ApiOperation(value = "交易货币的资产")
    public R<SymbolAssetVo> getSymbolAssert(@PathVariable String symbol) {
        Long userId = Long.valueOf(SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString());
        SymbolAssetVo symbolAssetVo = accountService.getSymbolAssert(symbol, userId);
        return R.ok(symbolAssetVo);
    }

    @Override
    public void lockUserAmount(Long userId, Long coinId, BigDecimal mum, String type, Long orderId, BigDecimal fee) {
        accountService.lockUserAmount(userId, coinId, mum, type, orderId, fee);
    }

    @Override
    public void transferBuyAmount(Long fromUserId, Long toUserId, Long coinId, BigDecimal amount, String businessType, Long orderId) {
//        accountService.transferBuyAmount(fromUserId, toUserId, coinId, amount, businessType, orderId);
    }

    @Override
    public void transferSellAmount(Long fromUserId, Long toUserId, Long coinId, BigDecimal amount, String businessType, Long orderId) {
//        accountService.transferSellAmount(fromUserId, toUserId, coinId, amount, businessType, orderId);
    }
}

