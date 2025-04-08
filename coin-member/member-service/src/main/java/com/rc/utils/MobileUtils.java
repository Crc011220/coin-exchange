package com.rc.utils;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MobileUtils {
    // 将手机号转换为 E.164 格式
    public static String convertToE164Format(String mobile) {
        PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();
        try {
            Phonenumber.PhoneNumber number = phoneNumberUtil.parse(mobile, "AU"); // 默认区域是 AU
            if (phoneNumberUtil.isValidNumber(number)) {
                // 返回 E.164 格式的手机号
                return phoneNumberUtil.format(number, PhoneNumberUtil.PhoneNumberFormat.E164);
            } else {
                log.error("无效的手机号: {}", mobile);
                return null;
            }
        } catch (NumberParseException e) {
            log.error("解析手机号时发生错误: {}", mobile, e);
            return null;
        }
    }
}
