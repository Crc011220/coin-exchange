package com.rc.service.impl;

import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rc.domain.CashWithdrawAuditRecord;
import com.rc.mapper.CashWithdrawAuditRecordMapper;
import com.rc.service.CashWithdrawAuditRecordService;
@Service
public class CashWithdrawAuditRecordServiceImpl extends ServiceImpl<CashWithdrawAuditRecordMapper, CashWithdrawAuditRecord> implements CashWithdrawAuditRecordService{

}
