package com.rc.service.impl;

import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rc.domain.ForexAccountDetail;
import com.rc.mapper.ForexAccountDetailMapper;
import com.rc.service.ForexAccountDetailService;
@Service
public class ForexAccountDetailServiceImpl extends ServiceImpl<ForexAccountDetailMapper, ForexAccountDetail> implements ForexAccountDetailService{

}
