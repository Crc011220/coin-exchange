package com.rc.service.impl;

import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rc.domain.Account;
import com.rc.mapper.AccountMapper;
import com.rc.service.AccountService;
@Service
public class AccountServiceImpl extends ServiceImpl<AccountMapper, Account> implements AccountService{

}
