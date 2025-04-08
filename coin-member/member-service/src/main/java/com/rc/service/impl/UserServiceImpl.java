package com.rc.service.impl;

import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rc.config.IdAutoConfiguration;
import com.rc.domain.UserAuthAuditRecord;
import com.rc.geetest.GeetestLib;
import com.rc.model.UserAuthForm;
import com.rc.service.UserAuthAuditRecordService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rc.domain.User;
import com.rc.mapper.UserMapper;
import com.rc.service.UserService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;

@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService{

    @Autowired
    private UserAuthAuditRecordService userAuthAuditRecordService ;

    @Autowired
    private GeetestLib geetestLib ;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate ;

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

    @Override
    public boolean identifyVerify(Long id, UserAuthForm userAuthForm) {
        User user = getById(id);
        Assert.notNull(user, "认证的用户不存在");
        Byte authStatus = user.getAuthStatus();
        if (!authStatus.equals((byte) 0)) {
            throw new IllegalArgumentException("该用户已经认证成功了");
        }
        // 执行认证
        checkForm(userAuthForm); // 极验
        // 实名认证
        boolean check = IdAutoConfiguration.check(userAuthForm.getRealName(), userAuthForm.getIdCard());
        if (!check) {
            throw new IllegalArgumentException("该用户信息错误,请检查");
        }

        // 设置用户的认证属性
        user.setAuthtime(new Date());
        user.setAuthStatus((byte) 1);
        user.setRealName(userAuthForm.getRealName());
        user.setIdCard(userAuthForm.getIdCard());
        user.setIdCardType(userAuthForm.getIdCardType());

        return updateById(user);
    }


    private void checkForm(UserAuthForm userAuthForm) {
        userAuthForm.check(userAuthForm, geetestLib, redisTemplate);
    }


    @Override
    public User getById(Serializable id) {
        User user = super.getById(id);
        if (user == null){
            throw new IllegalArgumentException("请输入正确的用户ID");
        }
        Byte seniorAuthStatus = null;
        String seniorAuthDesc = null;
        Integer reviewsStatus = user.getReviewsStatus(); //用户被审核的状态，0待审核，1通过，2拒绝
        if (reviewsStatus == null){ //用户没有上传资料
            seniorAuthStatus = 3;
            seniorAuthDesc = "用户没有上传资料";
        } else {
            switch (reviewsStatus){
                case 1:
                    seniorAuthStatus = 1;
                    seniorAuthDesc = "用户认证通过";
                    break;
                case 2:
                    seniorAuthStatus = 2;
                    // 查询被拒绝原因
                    List<UserAuthAuditRecord> userAuthAuditRecordList = userAuthAuditRecordService.getUserAuthAuditRecordList(user.getId());
                    if (!CollectionUtils.isEmpty(userAuthAuditRecordList)){
                        seniorAuthDesc = userAuthAuditRecordList.get(0).getRemark();
                    }
                    seniorAuthDesc = "原因未知";
                    break;
                case 0:
                    seniorAuthStatus = 0;
                    seniorAuthDesc = "等待审核";
                    break;
            }
        }

        user.setSeniorAuthStatus(seniorAuthStatus);
        user.setSeniorAuthDesc(seniorAuthDesc);
        return user;
    }
}
