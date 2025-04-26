package com.rc.boot;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.rc.disruptor.DisruptorTemplate;
import com.rc.domain.EntrustOrder;
import com.rc.mapper.EntrustOrderMapper;
import com.rc.model.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

import static com.rc.util.BeanUtils.entrustOrder2Order;

@Component
public class DataLoaderCmdLine implements CommandLineRunner {

    @Autowired
    private DisruptorTemplate disruptorTemplate;

    @Autowired
    private EntrustOrderMapper entrustOrderMapper;


    @Override
    public void run(String... args) throws Exception {
        List<EntrustOrder> entrustOrders =
                entrustOrderMapper.selectList(
                        new LambdaQueryWrapper<EntrustOrder>()
                                .eq(EntrustOrder::getStatus, 0)
                                .orderByAsc(EntrustOrder::getCreated)
                );
        if (!CollectionUtils.isEmpty(entrustOrders)) {
            for (EntrustOrder entrustOrder : entrustOrders) {
                disruptorTemplate.onData(entrustOrder2Order(entrustOrder));
            }
        }

    }
}

