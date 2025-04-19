package com.rc.service.impl;

import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rc.mapper.MarketMapper;
import com.rc.domain.Market;
import com.rc.service.MarketService;
@Service
public class MarketServiceImpl extends ServiceImpl<MarketMapper, Market> implements MarketService{

}
