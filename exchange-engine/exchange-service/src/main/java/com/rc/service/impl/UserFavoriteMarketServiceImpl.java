package com.rc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.rc.mapper.UserFavoriteMarketMapper;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rc.domain.UserFavoriteMarket;
import com.rc.service.UserFavoriteMarketService;

@Service
public class UserFavoriteMarketServiceImpl extends ServiceImpl<UserFavoriteMarketMapper, UserFavoriteMarket> implements UserFavoriteMarketService{

    @Override
    public boolean deleteUserFavorite(Long marketId, Long userId) {
        return remove(new LambdaQueryWrapper<UserFavoriteMarket>()
                .eq(UserFavoriteMarket::getMarketId, marketId)
                .eq(UserFavoriteMarket::getUserId, userId)
        );
    }
}
