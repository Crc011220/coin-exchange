package com.rc.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.rc.domain.SysUser;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {
}