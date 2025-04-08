package com.rc.service;

import com.rc.domain.Sms;
import com.baomidou.mybatisplus.extension.service.IService;
public interface SmsService extends IService<Sms>{


    /**
     * 短信的发现
     * @param sms
     *  短信
     * @return
     * 是否发送成功
     */
    boolean sendMsg(Sms sms);
}


