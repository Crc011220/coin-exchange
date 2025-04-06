package com.rc.service;

import com.rc.domain.UserAuthInfo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface UserAuthInfoService extends IService<UserAuthInfo>{

    // 根据用户id 获取一个用户的审核记录 (因为用户没有被认证没有authCode，所以只能通过userId来获取)
    List<UserAuthInfo> getUserAuthInfoByUserId(Long id);

    // 根据认证authCode 获取认证详情
    List<UserAuthInfo> getUserAuthInfoByCode(Long authCode);
}
