package com.rc.service.impl;

import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rc.domain.CoinWithdraw;
import com.rc.mapper.CoinWithdrawMapper;
import com.rc.service.CoinWithdrawService;
@Service
public class CoinWithdrawServiceImpl extends ServiceImpl<CoinWithdrawMapper, CoinWithdraw> implements CoinWithdrawService{

}
