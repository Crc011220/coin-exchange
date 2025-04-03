package com.rc.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.rc.domain.SysRole;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SysRoleMapper extends BaseMapper<SysRole> {
    String getUserRoleCode(Long userId);

}