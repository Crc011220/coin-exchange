package com.rc.service.impl;

import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rc.domain.AddressPool;
import com.rc.mapper.AddressPoolMapper;
import com.rc.service.AddressPoolService;
@Service
public class AddressPoolServiceImpl extends ServiceImpl<AddressPoolMapper, AddressPool> implements AddressPoolService{

}
