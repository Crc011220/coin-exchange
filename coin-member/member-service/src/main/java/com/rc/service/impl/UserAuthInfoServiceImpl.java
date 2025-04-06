package com.rc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rc.mapper.UserAuthInfoMapper;
import com.rc.domain.UserAuthInfo;
import com.rc.service.UserAuthInfoService;
@Service
public class UserAuthInfoServiceImpl extends ServiceImpl<UserAuthInfoMapper, UserAuthInfo> implements UserAuthInfoService{

    @Override
    public List<UserAuthInfo> getUserAuthInfoByUserId(Long id) {
        List<UserAuthInfo> list = list(new QueryWrapper<UserAuthInfo>().lambda().eq(UserAuthInfo::getUserId, id));
        return list == null? Collections.emptyList() : list;
    }

    @Override
    public List<UserAuthInfo> getUserAuthInfoByCode(Long authCode) {
        return list(new QueryWrapper<UserAuthInfo>().lambda().eq(UserAuthInfo::getAuthCode, authCode));
    }
}
