package com.rc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rc.mapper.UserAddressMapper;
import com.rc.domain.UserAddress;
import com.rc.service.UserAddressService;
@Service
public class UserAddressServiceImpl extends ServiceImpl<UserAddressMapper, UserAddress> implements UserAddressService{

    @Override
    public Page<UserAddress> findByPage(Page<UserAddress> page, Long userId) {
        return page(page, new LambdaQueryWrapper<UserAddress>()
                .eq(userId != null, UserAddress::getUserId, userId));
    }

    @Override
    public List<UserAddress> getUserAddressByUserId(Long userId) {
        return list(new LambdaQueryWrapper<UserAddress>()
                .eq(UserAddress::getUserId,userId)
                .orderByDesc(UserAddress::getCreated));
    }

    @Override
    public UserAddress getUserAddressByUserIdAndCoinId(String userId, Long coinId) {
        return getOne(new LambdaQueryWrapper<UserAddress>()
                .eq(UserAddress::getUserId, userId)
                .eq(UserAddress::getCoinId, coinId));
    }
}
