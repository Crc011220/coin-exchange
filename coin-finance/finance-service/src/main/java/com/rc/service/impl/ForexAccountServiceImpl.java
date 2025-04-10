package com.rc.service.impl;

import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rc.mapper.ForexAccountMapper;
import com.rc.domain.ForexAccount;
import com.rc.service.ForexAccountService;
@Service
public class ForexAccountServiceImpl extends ServiceImpl<ForexAccountMapper, ForexAccount> implements ForexAccountService{

}
