package com.rc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rc.domain.User;
import com.rc.mapper.UserMapper;
import com.rc.service.UserService;
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService{

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
}
