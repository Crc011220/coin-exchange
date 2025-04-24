package com.rc.service;

import com.rc.domain.Account;
import com.baomidou.mybatisplus.extension.service.IService;
import com.rc.vo.SymbolAssetVo;
import com.rc.vo.UserTotalAccountVo;

import java.math.BigDecimal;

public interface AccountService extends IService<Account>{


    // 用户资金的转账
    boolean transferAccountAmount(Long adminId, Long userId, Long coinId, BigDecimal num, BigDecimal fee,
                                  Long orderId, String remark, String businessType, Byte direction);

    /**
     * 给用户扣钱
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

    // 获取当前用户货币的资产情况
    Account findByUserAndCoin(Long userId, String coinName);

    /**
     * 扣减总资产 增加冻结资产
     * @param userId
     *  用户的id
     * @param coinId
     * 币种的id
     * @param mum
     * 锁定的金额
     * @param type
     *      资金流水的类型
     * @param orderId
     *      订单的Id
     * @param fee
     *  本次操作的手续费
     */
    void lockUserAmount(Long userId, Long coinId, BigDecimal mum, String type, Long orderId, BigDecimal fee);


    // 获取当前用户的总资产
    UserTotalAccountVo getUserTotalAccount(Long userId);

    // 统计用户交易对的资产
    SymbolAssetVo getSymbolAssert(String symbol, Long userId);

}
