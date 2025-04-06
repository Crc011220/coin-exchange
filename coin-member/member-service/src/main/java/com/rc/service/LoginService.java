package com.rc.service;

import com.rc.model.LoginForm;
import com.rc.model.LoginUser;

public interface LoginService {

    /**
     * 会员的登录
     * @param loginForm
     * 登录的表单参数
     * @return
     * 登录的结果
     */
    LoginUser login(LoginForm loginForm);
}

