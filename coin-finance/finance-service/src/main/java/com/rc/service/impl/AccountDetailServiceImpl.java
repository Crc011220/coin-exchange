package com.rc.service.impl;

import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rc.mapper.AccountDetailMapper;
import com.rc.domain.AccountDetail;
import com.rc.service.AccountDetailService;
@Service
public class AccountDetailServiceImpl extends ServiceImpl<AccountDetailMapper, AccountDetail> implements AccountDetailService{

}
