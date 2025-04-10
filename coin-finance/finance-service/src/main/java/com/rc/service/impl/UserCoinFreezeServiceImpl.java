package com.rc.service.impl;

import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rc.mapper.UserCoinFreezeMapper;
import com.rc.domain.UserCoinFreeze;
import com.rc.service.UserCoinFreezeService;
@Service
public class UserCoinFreezeServiceImpl extends ServiceImpl<UserCoinFreezeMapper, UserCoinFreeze> implements UserCoinFreezeService{

}
