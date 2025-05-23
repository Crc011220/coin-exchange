package com.rc.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rc.domain.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.rc.dto.UserDto;
import com.rc.model.*;

import java.util.List;
import java.util.Map;

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
    boolean unsetPayPassword(Long userId, UnsetPayPasswordParam unsetPayPasswordParam);

    // 获取用户的邀请列表
    List<User> getUserInvites(Long userId);

    // 通过用户id批量获取用户基础信息
    Map<Long,UserDto> getBasicUsers(
            List<Long> ids,
            String userName,
            String mobile
    );
    // 用户注册
    boolean register(RegisterParam registerParam);

    // 重置登录密码
    boolean unsetLoginPassword(UnsetPasswordParam unsetPasswordParam);
}
