package com.rc.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rc.domain.CashWithdrawAuditRecord;
import com.rc.domain.CashWithdrawals;
import com.baomidou.mybatisplus.extension.service.IService;
import com.rc.model.CashSellParam;

public interface CashWithdrawalsService extends IService<CashWithdrawals>{

    // 条件分页查询 GCN提现记录
    Page<CashWithdrawals> findByPage(Page<CashWithdrawals> page, Long userId, String userName, String mobile, Byte status, String numMin, String numMax, String startTime, String endTime);

    // 审核提现记录
    boolean updateWithdrawalsStatus(CashWithdrawAuditRecord cashWithdrawAuditRecord, Long userId);

    // 查询当前用户的提现记录
    Page<CashWithdrawals> findUserCashWithdraw(Page<CashWithdrawals> page, Long userId, Byte status);

    // 卖出GCN 拿人民币
    boolean sell(Long userId, CashSellParam cashSellParam);


}
