package com.rc.service.impl;

import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rc.domain.CashRecharge;
import com.rc.mapper.CashRechargeMapper;
import com.rc.service.CashRechargeService;
@Service
public class CashRechargeServiceImpl extends ServiceImpl<CashRechargeMapper, CashRecharge> implements CashRechargeService{

}
