package com.rc.service;

import com.rc.domain.UserAuthAuditRecord;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface UserAuthAuditRecordService extends IService<UserAuthAuditRecord>{

    //根据用户id查询用户认证审核记录
    List<UserAuthAuditRecord> getUserAuthAuditRecordList(Long id);
}
