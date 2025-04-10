package com.rc.service.impl;

import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rc.domain.CoinBalance;
import com.rc.mapper.CoinBalanceMapper;
import com.rc.service.CoinBalanceService;
@Service
public class CoinBalanceServiceImpl extends ServiceImpl<CoinBalanceMapper, CoinBalance> implements CoinBalanceService{

}
