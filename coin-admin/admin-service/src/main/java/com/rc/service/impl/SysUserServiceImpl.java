package com.rc.service.impl;

import com.alibaba.csp.sentinel.util.StringUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rc.domain.SysUserRole;
import com.rc.service.SysUserRoleService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rc.mapper.SysUserMapper;
import com.rc.domain.SysUser;
import com.rc.service.SysUserService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService{

    @Autowired
    private SysUserRoleService sysUserRoleService;

    @Override
    public Page<SysUser> findByPage(Page<SysUser> page, String mobile, String fullName) {
        Page<SysUser> pageData = page(page, new LambdaQueryWrapper<SysUser>()
                // 模糊查询
                .like(!StringUtil.isEmpty(mobile), SysUser::getMobile, mobile)
                .like(!StringUtil.isEmpty(fullName), SysUser::getFullname, fullName));
        List<SysUser> records = pageData.getRecords();
        if(!CollectionUtils.isEmpty(records)){
            for (SysUser record : records) {
                List<SysUserRole> userRoles = sysUserRoleService.list(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, record.getId()));
                if (!CollectionUtils.isEmpty(userRoles)) {
                    record.setRole_strings(userRoles.stream()
                            .map(sysUserRole -> sysUserRole.getRoleId().toString())
                            .collect(Collectors.joining(",")));
                }
            }
        }
        return pageData;
    }

    @Override
    @Transactional
    public boolean addUser(SysUser sysUser) {
        String password = sysUser.getPassword();
        String roleStrings = sysUser.getRole_strings();
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        String encodedPassword = bCryptPasswordEncoder.encode(password); // 加密密码
        sysUser.setPassword(encodedPassword); // 设置加密后的密码

        boolean save = super.save(sysUser);// 保存用户信息
        if (save) {
            // 给用户新增权限
            if (!StringUtil.isEmpty(roleStrings)){
                String[] roleIds = roleStrings.split(",");
                List<SysUserRole> sysUserRoles = new ArrayList<>(roleIds.length);
                for (String roleId : roleIds) {
                    SysUserRole sysUserRole = new SysUserRole();
                    sysUserRole.setRoleId(Long.parseLong(roleId));
                    sysUserRole.setUserId(sysUser.getId());
                    sysUserRoles.add(sysUserRole);
                }
                sysUserRoleService.saveBatch(sysUserRoles);
            }
        }
        return save;
    }

    @Override
    @Transactional
    public boolean updateUser(SysUser sysUser) {
        boolean save = super.updateById(sysUser);
        if (save) {
            // 更新用户权限
            sysUserRoleService.remove(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, sysUser.getId()));
            if (!StringUtil.isEmpty(sysUser.getRole_strings())) {
                String[] roleIds = sysUser.getRole_strings().split(",");
                List<SysUserRole> sysUserRoles = new ArrayList<>(roleIds.length);
                for (String roleId : roleIds) {
                    SysUserRole sysUserRole = new SysUserRole();
                    sysUserRole.setRoleId(Long.parseLong(roleId));
                    sysUserRole.setUserId(sysUser.getId());
                    sysUserRoles.add(sysUserRole);
                }
                sysUserRoleService.saveBatch(sysUserRoles);
            }
        }
        return save;
    }

    @Override
    public boolean removeByIds(Collection<? extends Serializable> idList) {
        boolean b = super.removeByIds(idList);
        // 还要删除用户权限
        sysUserRoleService.remove(new QueryWrapper<SysUserRole>().lambda().in(SysUserRole::getUserId, idList));
        return b;
    }
}
