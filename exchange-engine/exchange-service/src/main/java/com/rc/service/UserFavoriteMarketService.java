package com.rc.service;

import com.rc.domain.UserFavoriteMarket;
import com.baomidou.mybatisplus.extension.service.IService;
public interface UserFavoriteMarketService extends IService<UserFavoriteMarket>{

    // 用户取消收藏
    boolean deleteUserFavorite(Long marketId, Long userId);
}
