package com.rc.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rc.domain.CashRecharge;
import com.rc.domain.CoinRecharge;
import com.baomidou.mybatisplus.extension.service.IService;
public interface CoinRechargeService extends IService<CoinRecharge>{

    // 分页条件查询充值记录
    Page<CoinRecharge> findByPage(Page<CoinRecharge> page, Long coinId, Long userId, String userName, String mobile, Byte status, String numMin, String numMax, String startTime, String endTime);

    // 查询用户充币记录
    Page<CoinRecharge> findUserCoinRecharge(Page<CoinRecharge> page, Long coinId, Long userId);
}
