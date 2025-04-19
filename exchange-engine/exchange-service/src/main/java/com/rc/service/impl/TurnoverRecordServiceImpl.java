package com.rc.service.impl;

import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rc.mapper.TurnoverRecordMapper;
import com.rc.domain.TurnoverRecord;
import com.rc.service.TurnoverRecordService;
@Service
public class TurnoverRecordServiceImpl extends ServiceImpl<TurnoverRecordMapper, TurnoverRecord> implements TurnoverRecordService{

}
