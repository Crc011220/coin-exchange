package com.rc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rc.domain.UserAuthAuditRecord;
import com.rc.service.UserAuthAuditRecordService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rc.domain.User;
import com.rc.mapper.UserMapper;
import com.rc.service.UserService;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService{

    @Autowired
    private UserAuthAuditRecordService userAuthAuditRecordService ;


    @Override
    public Page<User> findByPage(Page<User> page, String mobile, Long userId, String userName, String realName,
                                 Integer status, Integer reviewStatus) {
        return page(page,
                new LambdaQueryWrapper<User>()
               .like(StringUtils.isNotEmpty(mobile), User::getMobile, mobile)
               .like(StringUtils.isNotEmpty(userName), User::getUsername, userName)
               .like(StringUtils.isNotEmpty(realName), User::getRealName, realName)
               .eq(userId != null, User::getId, userId)
               .eq(status != null, User::getStatus, status)
               .eq(reviewStatus != null, User::getReviewsStatus, reviewStatus)
        );
    }

    @Override
    public Page<User> findDirectInvitePage(Page<User> page, Long userId) {
        return page(page, new LambdaQueryWrapper<User>()
               .eq(User::getDirectInviteid, userId)
        );
    }

    @Override
    @Transactional
    public void updateUserAuthStatus(Long id, Byte authStatus, Long authCode,String remark) {
        log.info("开始修改用户的审核状态,当前用户{},用户的审核状态{},图片的唯一code{}",id,authStatus,authCode);
        User user = getById(id);
        if(user!=null){
            user.setReviewsStatus(authStatus.intValue()); // 审核的状态
            updateById(user) ; // 修改用户状态
        }
        UserAuthAuditRecord userAuthAuditRecord = new UserAuthAuditRecord();
        userAuthAuditRecord.setUserId(id);
        userAuthAuditRecord.setStatus(authStatus);
        userAuthAuditRecord.setAuthCode(authCode);

        String usrStr = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString() ;
        userAuthAuditRecord.setAuditUserId(Long.valueOf(usrStr)); // 审核人的ID
        userAuthAuditRecord.setAuditUserName("---------------------------");// 审核人的名称 --> 远程调用admin-service 查询用户信息,没有事务
        userAuthAuditRecord.setRemark(remark);

        userAuthAuditRecordService.save(userAuthAuditRecord) ;
    }

}
