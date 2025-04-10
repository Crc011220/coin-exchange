package com.rc.service.impl;

import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rc.mapper.CoinServerMapper;
import com.rc.domain.CoinServer;
import com.rc.service.CoinServerService;
@Service
public class CoinServerServiceImpl extends ServiceImpl<CoinServerMapper, CoinServer> implements CoinServerService{

}
