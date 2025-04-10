package com.rc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rc.mapper.CoinTypeMapper;
import com.rc.domain.CoinType;
import com.rc.service.CoinTypeService;
@Service
public class CoinTypeServiceImpl extends ServiceImpl<CoinTypeMapper, CoinType> implements CoinTypeService{

    @Override
    public Page<CoinType> findByPage(Page<CoinType> page, String code) {
        return page(page, new LambdaQueryWrapper<CoinType>().like(StringUtils.isNotEmpty(code), CoinType::getCode, code));
    }

    @Override
    public List<CoinType> listByStatus(Byte status) {
        return list(new LambdaQueryWrapper<CoinType>().eq(status!=null ,CoinType::getStatus,status));
    }

}
