package com.rc.service.impl;

import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rc.mapper.CashWithdrawalsMapper;
import com.rc.domain.CashWithdrawals;
import com.rc.service.CashWithdrawalsService;
@Service
public class CashWithdrawalsServiceImpl extends ServiceImpl<CashWithdrawalsMapper, CashWithdrawals> implements CashWithdrawalsService{

}
