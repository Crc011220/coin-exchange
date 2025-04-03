package com.rc.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.rc.domain.Notice;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface NoticeMapper extends BaseMapper<Notice> {
}