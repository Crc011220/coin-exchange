package com.rc.service.impl;

import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rc.mapper.UserAccountFreezeMapper;
import com.rc.domain.UserAccountFreeze;
import com.rc.service.UserAccountFreezeService;
@Service
public class UserAccountFreezeServiceImpl extends ServiceImpl<UserAccountFreezeMapper, UserAccountFreeze> implements UserAccountFreezeService{

}
