package com.rc.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rc.domain.CashWithdrawals;
import com.rc.domain.CoinRecharge;
import com.rc.domain.CoinWithdraw;
import com.baomidou.mybatisplus.extension.service.IService;
public interface CoinWithdrawService extends IService<CoinWithdraw>{

    // 分页条件查询提现记录
    Page<CoinWithdraw> findByPage(Page<CoinWithdraw> page, Long coinId, Long userId, String userName, String mobile, Byte status, String numMin, String numMax, String startTime, String endTime);

    // 查询用户提现记录
    Page<CoinWithdraw> findUserCoinWithdraw(Page<CoinWithdraw> page, Long coinId, Long userId);
}
