package com.rc.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rc.domain.CashRecharge;
import com.baomidou.mybatisplus.extension.service.IService;
import com.rc.domain.CashRechargeAuditRecord;
import com.rc.model.CashParam;
import com.rc.vo.CashTradeVo;

public interface CashRechargeService extends IService<CashRecharge>{

    // 条件分页查询gcn重置记录
    Page<CashRecharge> findByPage(Page<CashRecharge> page, Long coinId, Long userId, String userName, String mobile, Byte status, String numMin, String numMax, String startTime, String endTime);

    // 充值审核
    boolean cashRechargeAudit(Long userId, CashRechargeAuditRecord cashRechargeAuditRecord);

    // 查询当前用户的充值记录
    Page<CashRecharge> findUserCashRecharge(Page<CashRecharge> page, Long userId, Byte status);

    // 进行gcn的充值
    CashTradeVo buy(Long userId, CashParam cashParam);
}
