package com.rc.service;

import com.rc.domain.Account;
import com.baomidou.mybatisplus.extension.service.IService;

import java.math.BigDecimal;

public interface AccountService extends IService<Account>{


    // 用户资金的转账
    boolean transferAccountAmount(Long adminId, Long userId, Long coinId, BigDecimal num, BigDecimal fee,
                                  Long orderId, String remark, String businessType, Byte direction);
}
