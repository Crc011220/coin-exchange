package com.rc.service;

import com.rc.domain.Account;
import com.baomidou.mybatisplus.extension.service.IService;

import java.math.BigDecimal;

public interface AccountService extends IService<Account>{


    // 用户资金的转账
    boolean transferAccountAmount(Long adminId, Long userId, Long coinId, BigDecimal num, BigDecimal fee,
                                  Long orderId, String remark, String businessType, Byte direction);

    // 给用户扣钱

    /**
     *
     * @param adminId 操作人
     * @param userId 用户id
     * @param coinId 币种id
     * @param num 扣钱数量
     * @param orderId 订单编号
     * @param fee 手续费
     * @param remark 备注
     * @param businessType 业务类型
     * @param direction 方向
     * @return
     */
    Boolean decreaseAccountAmount(Long adminId, Long userId, Long coinId, Long orderId, BigDecimal num,
                                  BigDecimal fee, String remark, String businessType, Byte direction);
}
