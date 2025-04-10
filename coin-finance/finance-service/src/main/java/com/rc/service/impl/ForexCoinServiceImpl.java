package com.rc.service.impl;

import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rc.mapper.ForexCoinMapper;
import com.rc.domain.ForexCoin;
import com.rc.service.ForexCoinService;
@Service
public class ForexCoinServiceImpl extends ServiceImpl<ForexCoinMapper, ForexCoin> implements ForexCoinService{

}
