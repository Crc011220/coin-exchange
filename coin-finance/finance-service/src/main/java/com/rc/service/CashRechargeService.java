package com.rc.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rc.domain.CashRecharge;
import com.baomidou.mybatisplus.extension.service.IService;
import com.rc.domain.CashRechargeAuditRecord;

public interface CashRechargeService extends IService<CashRecharge>{

    // 条件分页查询gcn重置记录
    Page<CashRecharge> findByPage(Page<CashRecharge> page, Long coinId, Long userId, String userName, String mobile, Byte status, String numMin, String numMax, String startTime, String endTime);

    // 充值审核
    boolean cashRechargeAudit(Long userId, CashRechargeAuditRecord cashRechargeAuditRecord);
}
