package com.rc.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rc.domain.AccountDetail;
import com.baomidou.mybatisplus.extension.service.IService;
public interface AccountDetailService extends IService<AccountDetail>{

    // 条件分页 查询资金流水
    Page<AccountDetail> findByPage(Page<AccountDetail> page, Long accountId, Long coinId, Long userId, String userName, String mobile, String amountStart, String amountEnd, String startTime, String endTime);
}
