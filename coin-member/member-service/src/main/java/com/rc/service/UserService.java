package com.rc.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rc.domain.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.rc.model.UserAuthForm;
import io.swagger.models.auth.In;

public interface UserService extends IService<User>{

    // 根据条件分页查询用户
    Page<User> findByPage(Page<User> page, String mobile, Long userId, String userName, String realName,
                          Integer status, Integer reviewStatus);

    // 根据用户ID查询直接邀请的用户
    Page<User> findDirectInvitePage(Page<User> page, Long userId);

    // 更新用户的审核状态
    void updateUserAuthStatus(Long id, Byte authStatus, Long authCode, String remark);

    // 验证用户身份
    boolean identifyVerify(Long aLong, UserAuthForm userAuthForm);

}
