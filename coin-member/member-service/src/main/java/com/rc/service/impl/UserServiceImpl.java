package com.rc.service.impl;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.RandomUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rc.config.IdAutoConfiguration;
import com.rc.domain.Sms;
import com.rc.domain.UserAuthAuditRecord;
import com.rc.dto.UserDto;
import com.rc.geetest.GeetestLib;
import com.rc.mappers.UserDtoMapper;
import com.rc.model.*;
import com.rc.service.SmsService;
import com.rc.service.UserAuthAuditRecordService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rc.domain.User;
import com.rc.mapper.UserMapper;
import com.rc.service.UserService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import static com.rc.constant.Constants.HIDDEN_FIELD;
import static com.rc.utils.MobileUtils.convertToE164Format;

@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService{

    @Autowired
    private UserAuthAuditRecordService userAuthAuditRecordService ;

    @Autowired
    private GeetestLib geetestLib ;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate ;

    @Autowired
    private StringRedisTemplate stringRedisTemplate ;

    @Autowired
    private SmsService smsService;

    @Override
    public Page<User> findByPage(Page<User> page, String mobile, Long userId, String userName, String realName,
                                 Integer status, Integer reviewStatus) {
        return page(page,
                new LambdaQueryWrapper<User>()
               .like(StringUtils.isNotEmpty(mobile), User::getMobile, mobile)
               .like(StringUtils.isNotEmpty(userName), User::getUsername, userName)
               .like(StringUtils.isNotEmpty(realName), User::getRealName, realName)
               .eq(userId != null, User::getId, userId)
               .eq(status != null, User::getStatus, status)
               .eq(reviewStatus != null, User::getReviewsStatus, reviewStatus)
        );
    }

    @Override
    public Page<User> findDirectInvitePage(Page<User> page, Long userId) {
        return page(page, new LambdaQueryWrapper<User>()
               .eq(User::getDirectInviteid, userId)
        );
    }

    @Override
    @Transactional
    public void updateUserAuthStatus(Long id, Byte authStatus, Long authCode,String remark) {
        log.info("开始修改用户的审核状态,当前用户{},用户的审核状态{},图片的唯一code{}",id,authStatus,authCode);
        User user = getById(id);
        if(user!=null){
            user.setReviewsStatus(authStatus.intValue()); // 审核的状态
            updateById(user) ; // 修改用户状态
        }
        UserAuthAuditRecord userAuthAuditRecord = new UserAuthAuditRecord();
        userAuthAuditRecord.setUserId(id);
        userAuthAuditRecord.setStatus(authStatus);
        userAuthAuditRecord.setAuthCode(authCode);

        String usrStr = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString() ;
        userAuthAuditRecord.setAuditUserId(Long.valueOf(usrStr)); // 审核人的ID
        userAuthAuditRecord.setAuditUserName("---------------------------");// 审核人的名称 --> 远程调用admin-service 查询用户信息,没有事务
        userAuthAuditRecord.setRemark(remark);

        userAuthAuditRecordService.save(userAuthAuditRecord) ;
    }

    @Override
    public boolean identifyVerify(Long id, UserAuthForm userAuthForm) {
        User user = getById(id);
        Assert.notNull(user, "认证的用户不存在");
        Byte authStatus = user.getAuthStatus();
        if (!authStatus.equals((byte) 0)) {
            throw new IllegalArgumentException("该用户已经认证成功了");
        }
        // 执行认证
        checkForm(userAuthForm); // 极验
        // 实名认证
        boolean check = IdAutoConfiguration.check(userAuthForm.getRealName(), userAuthForm.getIdCard());
        if (!check) {
            throw new IllegalArgumentException("该用户信息错误,请检查");
        }

        // 设置用户的认证属性
        user.setAuthtime(new Date());
        user.setAuthStatus((byte) 1);
        user.setRealName(userAuthForm.getRealName());
        user.setIdCard(userAuthForm.getIdCard());
        user.setIdCardType(userAuthForm.getIdCardType());

        return updateById(user);
    }


    private void checkForm(UserAuthForm userAuthForm) {
        userAuthForm.check(geetestLib, redisTemplate);
    }


    @Override
    public User getById(Serializable id) {
        User user = super.getById(id);
        if (user == null){
            throw new IllegalArgumentException("请输入正确的用户ID");
        }
        Byte seniorAuthStatus = null;
        String seniorAuthDesc = null;
        Integer reviewsStatus = user.getReviewsStatus(); //用户被审核的状态，0待审核，1通过，2拒绝
        if (reviewsStatus == null){ //用户没有上传资料
            seniorAuthStatus = 3;
            seniorAuthDesc = "用户没有上传资料";
        } else {
            switch (reviewsStatus){
                case 1:
                    seniorAuthStatus = 1;
                    seniorAuthDesc = "用户认证通过";
                    break;
                case 2:
                    seniorAuthStatus = 2;
                    // 查询被拒绝原因
                    List<UserAuthAuditRecord> userAuthAuditRecordList = userAuthAuditRecordService.getUserAuthAuditRecordList(user.getId());
                    if (!CollectionUtils.isEmpty(userAuthAuditRecordList)){
                        seniorAuthDesc = userAuthAuditRecordList.get(0).getRemark();
                    }
                    seniorAuthDesc = "原因未知";
                    break;
                case 0:
                    seniorAuthStatus = 0;
                    seniorAuthDesc = "等待审核";
                    break;
            }
        }

        user.setSeniorAuthStatus(seniorAuthStatus);
        user.setSeniorAuthDesc(seniorAuthDesc);
        return user;
    }


    @Override
    public boolean updatePhone(Long userId, UpdatePhoneParam updatePhoneParam) {
        User user = getById(userId);

        String oldMobile = user.getMobile(); // 旧的手机号 --- > 验证旧手机的验证码
        String oldMobileCode = stringRedisTemplate.opsForValue().get("SMS:VERIFY_OLD_PHONE:" + oldMobile);
        if(!updatePhoneParam.getOldValidateCode().equals(oldMobileCode)){
            throw new IllegalArgumentException("旧手机的验证码错误") ;
        }

        String newPhoneCode = stringRedisTemplate.opsForValue().get("SMS:CHANGE_PHONE_VERIFY:" + updatePhoneParam.getNewMobilePhone());
        if(!updatePhoneParam.getValidateCode().equals(newPhoneCode)){
            throw new IllegalArgumentException("新手机的验证码错误") ;
        }

        user.setMobile(updatePhoneParam.getNewMobilePhone());
        return updateById(user);
    }

    @Override
    public boolean checkNewPhone(String mobile, String countryCode) {
        //1 新的手机号,没有旧的用户使用
        int count = count(new LambdaQueryWrapper<User>().eq(User::getMobile, mobile).eq(User::getCountryCode,countryCode));
        if(count>0){
            throw new IllegalArgumentException("该手机号已经被占用") ;
        }
        // 2 向新的手机发送短信
        Sms sms = new Sms();
        sms.setMobile(mobile);
        sms.setCountryCode(countryCode);
        sms.setTemplateCode("CHANGE_PHONE_VERIFY"); // 模板代码  -- > 校验手机号
        return smsService.sendSms(sms) ;

    }

    @Override
    public boolean updateLoginPassword(Long userId, UpdateLoginParam updateLoginParam) {
        User user =  getById(userId);
        if (user == null){
            throw new IllegalArgumentException("请输入正确的用户ID");
        }
        // 校验之前的密码
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        boolean matches = bCryptPasswordEncoder.matches(updateLoginParam.getOldPassword(), user.getPassword());
        if (!matches){
            throw new IllegalArgumentException("请输入正确的密码");
        }

        //校验手机验证码
        String validateCode = updateLoginParam.getValidateCode();
        // 转成E.164 format格式 因为aws发送信息后保存在redis的是E.164
        String formattedPhone = convertToE164Format(user.getMobile());
        String phoneValidateCode = stringRedisTemplate.opsForValue().get("SMS:CHANGE_LOGIN_PWD_VERIFY:" + formattedPhone);

        if (!validateCode.equals(phoneValidateCode)){
            throw new IllegalArgumentException("手机验证码错误");
        }

        user.setPassword(bCryptPasswordEncoder.encode(updateLoginParam.getNewPassword()));
        return updateById(user);
    }

    @Override
    public boolean updatePayPassword(Long userId, UpdateLoginParam updateLoginParam) {
        User user =  getById(userId);
        if (user == null){
            throw new IllegalArgumentException("请输入正确的用户ID");
        }
        // 校验之前的密码
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        boolean matches = bCryptPasswordEncoder.matches(updateLoginParam.getOldPassword(), user.getPaypassword());
        if (!matches){
            throw new IllegalArgumentException("请输入正确的密码");
        }

        //校验手机验证码
        String validateCode = updateLoginParam.getValidateCode();
        // 转成E.164 format格式 因为aws发送信息后保存在redis的是E.164
        String formattedPhone = convertToE164Format(user.getMobile());
        String phoneValidateCode = stringRedisTemplate.opsForValue().get("SMS:CHANGE_PAY_PWD_VERIFY:" + formattedPhone);
        if (!validateCode.equals(phoneValidateCode)){
            throw new IllegalArgumentException("手机验证码错误");
        }

        user.setPaypassword(bCryptPasswordEncoder.encode(updateLoginParam.getNewPassword()));
        return updateById(user);
    }

    @Override
    public boolean unsetPayPassword(Long userId, UnsetPayPasswordParam unsetPayPasswordParam) {
        User user =  getById(userId);
        if (user == null){
            throw new IllegalArgumentException("请输入正确的用户ID");
        }
        // 校验手机验证码
        String validateCode = unsetPayPasswordParam.getValidateCode();
        // 转成E.164 format格式 因为aws发送信息后保存在redis的是E.164
        String formattedPhone = convertToE164Format(user.getMobile());
        String phoneValidateCode = stringRedisTemplate.opsForValue().get("SMS:FORGOT_PAY_PWD_VERIFY:" + formattedPhone);
        if (!validateCode.equals(phoneValidateCode)){
            throw new IllegalArgumentException("手机验证码错误");
        }
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        user.setPaypassword(bCryptPasswordEncoder.encode(unsetPayPasswordParam.getPayPassword()));
        return updateById(user);
    }

    @Override
    public List<User> getUserInvites(Long userId) {
        List<User> list = list(new LambdaQueryWrapper<User>().eq(User::getDirectInviteid, userId));
        list.forEach(user -> {
            user.setPaypassword(HIDDEN_FIELD);
            user.setPassword(HIDDEN_FIELD);
            user.setAccessKeyId(HIDDEN_FIELD);
            user.setAccessKeySecret(HIDDEN_FIELD);
        });
        return CollectionUtils.isEmpty(list) ? Collections.emptyList() : list;
    }


    @Override
    public Map<Long, UserDto> getBasicUsers(List<Long> ids, String userName, String mobile) {
        if(CollectionUtils.isEmpty(ids) && StringUtils.isEmpty(userName) && StringUtils.isEmpty(mobile)){
            return Collections.emptyMap() ;
        }
        List<User> list = list(new LambdaQueryWrapper<User>()
                .in(User::getId, ids)
                .like(StringUtils.isNotEmpty(userName), User::getUsername, userName)
                .like(StringUtils.isNotEmpty(mobile), User::getMobile, mobile)
        );

        if(CollectionUtils.isEmpty(list)){
            return Collections.emptyMap() ;
        }
        // 对象的转化
        List<UserDto> userDtos = UserDtoMapper.INSTANCE.convert2Dto(list);
        return userDtos.stream().collect(Collectors.toMap(UserDto::getId, userDto -> userDto));
    }

    /**
     * 用户的注册
     *
     * @param registerParam 注册的表单参数
     * @return
     */
    @Override
    public boolean register(RegisterParam registerParam) {
        log.info("用户开始注册{}", JSON.toJSONString(registerParam, true));
        String mobile = registerParam.getMobile();
        String email = registerParam.getEmail();
        // 1 简单的校验
        if (StringUtils.isEmpty(email) && StringUtils.isEmpty(mobile)) {
            throw new IllegalArgumentException("手机号或邮箱不能同时为空");
        }
        // 2 查询校验
        int count = count(new LambdaQueryWrapper<User>()
                .eq(!StringUtils.isEmpty(email), User::getEmail, email)
                .eq(!StringUtils.isEmpty(mobile), User::getMobile, mobile)
        );
        if(count>0){
            throw new IllegalArgumentException("手机号或邮箱已经被注册");
        }

        registerParam.check(geetestLib, redisTemplate); // 进行极验的校验
        User user = getUser(registerParam); // 构建一个新的用户
        return save(user);
    }

    private User getUser(RegisterParam registerParam) {
        User user = new User();
        user.setCountryCode(registerParam.getCountryCode());
        user.setEmail(registerParam.getEmail());
        user.setMobile(registerParam.getMobile());
        String encodePwd = new BCryptPasswordEncoder().encode(registerParam.getPassword());
        user.setPassword(encodePwd);
        user.setPaypassSetting(false);
        user.setStatus((byte) 1);
        user.setType((byte) 1);
        user.setAuthStatus((byte) 0);
        user.setLogins(0);
        user.setInviteCode(RandomUtil.randomString(6)); // 用户的邀请码
        if (!StringUtils.isEmpty(registerParam.getInvitionCode())) {
            User userPre = getOne(new LambdaQueryWrapper<User>().eq(User::getInviteCode, registerParam.getInvitionCode()));
            if (userPre != null) {
                user.setDirectInviteid(String.valueOf(userPre.getId())); // 邀请人的id , 需要查询
                user.setInviteRelation(String.valueOf(userPre.getId())); // 邀请人的id , 需要查询
            }

        }
        return user;
    }

    @Override
    public boolean unsetLoginPassword(UnsetPasswordParam unsetPasswordParam) {
        log.info("忘记登录密码后重置{}", JSON.toJSONString(unsetPasswordParam, true));

        unsetPasswordParam.check(geetestLib, redisTemplate);
        String s = stringRedisTemplate.opsForValue().get("SMS:FORGOT_VERIFY:" + unsetPasswordParam.getMobile());
        if (!unsetPasswordParam.getValidateCode().equals(s)) {
            throw new IllegalArgumentException("验证码错误");
        }
        String mobile = unsetPasswordParam.getMobile();
        User user = getOne(new LambdaQueryWrapper<User>().eq(User::getMobile, mobile));
        if (user == null) {
            throw new IllegalArgumentException("该用户不存在");
        }

        user.setPassword(new BCryptPasswordEncoder().encode(unsetPasswordParam.getPassword()));
        return updateById(user);
    }
}
