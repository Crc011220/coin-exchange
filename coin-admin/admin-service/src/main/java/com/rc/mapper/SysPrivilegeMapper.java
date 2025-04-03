package com.rc.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.rc.domain.SysPrivilege;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Set;

@Mapper
public interface SysPrivilegeMapper extends BaseMapper<SysPrivilege> {
    /**
     * 使用角色Id 查询权限
     * @param roleId
     * @return
     */
    List<SysPrivilege> selectByRoleId(Long roleId);

    /**
     * 使用角色的ID 查询权限的id
     * @param roleId
     * @return
     */
    Set<Long> getPrivilegesByRoleId(Long roleId);
}

