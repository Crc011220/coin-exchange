package com.rc.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rc.mapper.SysRolePrivilegeUserMapper;
import com.rc.domain.SysRolePrivilegeUser;
import com.rc.service.SysRolePrivilegeUserService;
@Service
public class SysRolePrivilegeUserServiceImpl extends ServiceImpl<SysRolePrivilegeUserMapper, SysRolePrivilegeUser> implements SysRolePrivilegeUserService{

}
