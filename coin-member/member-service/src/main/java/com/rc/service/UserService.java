package com.rc.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rc.domain.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.rc.model.UnsetPasswordParam;
import com.rc.model.UpdateLoginParam;
import com.rc.model.UpdatePhoneParam;
import com.rc.model.UserAuthForm;
import io.swagger.models.auth.In;

import java.util.List;

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

    // 修改手机号
    boolean updatePhone(Long userId, UpdatePhoneParam updatePhoneParam);

    // 检查新的手机号是否可用
    boolean checkNewPhone(String mobile, String countryCode);

    // 修改登录密码
    boolean updateLoginPassword(Long userId, UpdateLoginParam updateLoginParam);

    // 修改交易密码
    boolean updatePayPassword(Long userId, UpdateLoginParam updateLoginParam);

    // 重置交易密码
    boolean unsetPayPassword(Long userId, UnsetPasswordParam unsetPasswordParam);

    // 获取用户的邀请列表
    List<User> getUserInvites(Long userId);
}
