package com.rc.service.impl;

import com.rc.constant.LoginConstant;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.rc.constant.LoginConstant.*;

// 查询权限，用户名 放入jwt
@Service
public class UserServiceDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        String loginType = requestAttributes.getRequest().getParameter("login_type");
        if (StringUtils.isEmpty(loginType)) {
            throw new AuthenticationServiceException("请添加login_type参数，登录类型不能为空");
        }
        UserDetails userDetails = null;
        try {
            String grantType = requestAttributes.getRequest().getParameter("grant_type");
            if (LoginConstant.REFRESH_TOKEN.equals(grantType.toUpperCase())) {
                username = adjustUsername(username, loginType); // 为refresh_token 时，需要将id->username
            }


            switch (loginType) {
                case LoginConstant.ADMIN_TYPE: // 管理员登录
                    userDetails = loadAdminUserByUsername(username);
                    break;
                case LoginConstant.MEMBER_TYPE: // 会员登录
                    userDetails = loadMemberUserByUsername(username);
                    break;
                default:
                    throw new AuthenticationServiceException("暂不支持的登录方式" + loginType);
            }
        } catch (IncorrectResultSizeDataAccessException e) {
            throw new UsernameNotFoundException("用户名" + username + "不存在");
        }
        return userDetails;
    }

    private String adjustUsername(String username, String loginType) {
        //管理员纠正
        if (ADMIN_TYPE.equals(loginType)) {
            return jdbcTemplate.queryForObject(QUERY_ADMIN_USER_WITH_ID, String.class, username);
        }
        //会员纠正
        if (MEMBER_TYPE.equals(loginType)) {
            return jdbcTemplate.queryForObject(QUERY_MEMBER_USER_WITH_ID, String.class, username);
        }
        return username;

    }

    // 通过用户id获取权限列表
    private Collection<? extends GrantedAuthority> getSysyUserPermissions(long id) {
        String code = jdbcTemplate.queryForObject(QUERY_ROLE_CODE_SQL, String.class, id);
        List<String> permissions = null; //全县名称
        if (ADMIN_CODE.equals(code)) { // 管理员
            permissions = jdbcTemplate.queryForList(QUERY_ALL_PERMISSIONS, String.class);
        } else { // 普通用户，需要使用 角色-权限 关系表查询权限
            permissions = jdbcTemplate.queryForList(QUERY_PERMISSION_SQL, String.class, id);
        }
        if (permissions == null || permissions.isEmpty()) {
            return Collections.EMPTY_SET;
        }
        return permissions
                .stream()
                .distinct() // 去重
                .map(
                        perm -> new SimpleGrantedAuthority(perm) // perm - >security可以识别的权限
                )
                .collect(Collectors.toSet());

    }

    private UserDetails loadAdminUserByUsername(String username) {
        return jdbcTemplate.queryForObject(QUERY_ADMIN_SQL, new RowMapper<User>() {
            @Override
            public User mapRow(ResultSet resultSet, int i) throws SQLException {
                if (resultSet.wasNull()) {
                    throw new UsernameNotFoundException("用户名" + username + "不存在");
                }
                long id = resultSet.getLong("id");
                String password = resultSet.getString("password");
                int status = resultSet.getInt("status");
                return new User(
                        String.valueOf(id),
                        password,
                        status == 1, // 1表示启用，0表示禁用
                        true, // 账户永不过期
                        true, // 账户不锁定
                        true,
                        getSysyUserPermissions(id) // 权限列表
                );
            }
        }, username);
    }


    private UserDetails loadMemberUserByUsername(String username) {
        return jdbcTemplate.queryForObject(QUERY_MEMBER_SQL, new RowMapper<User>() {
            @Override
            public User mapRow(ResultSet resultSet, int i) throws SQLException {
                if (resultSet.wasNull()) {
                    throw new UsernameNotFoundException("用户名" + username + "不存在");
                }
                long id = resultSet.getLong("id");
                String password = resultSet.getString("password");
                int status = resultSet.getInt("status");
                return new User(
                        String.valueOf(id),
                        password,
                        status == 1, // 1表示启用，0表示禁用用户
                        true, // 账户永不过期
                        true, // 账户不锁定
                        true,
                        Arrays.asList(new SimpleGrantedAuthority("ROLE_USER")) // 权限列表
                );
            }
        }, username, username);
    }
}
