package com.rc.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.rc.domain.SysMenu;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SysMenuMapper extends BaseMapper<SysMenu> {

    // 查询角色对应的菜单
    List<SysMenu> selectMenusByUserId(Long userId);

}