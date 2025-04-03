package com.rc.service.impl;

import com.alibaba.csp.sentinel.util.StringUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rc.mapper.SysUserMapper;
import com.rc.domain.SysUser;
import com.rc.service.SysUserService;
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService{

    @Override
    public Page<SysUser> findByPage(Page<SysUser> page, String mobile, String fullName) {
        return page(page, new LambdaQueryWrapper<SysUser>()
                // 模糊查询
                .like(!StringUtil.isEmpty(mobile), SysUser::getMobile, mobile)
                .like(!StringUtil.isEmpty(fullName), SysUser::getFullname, fullName));

    }

    @Override
    public boolean addUser(SysUser sysUser) {
        return false;
    }
}
