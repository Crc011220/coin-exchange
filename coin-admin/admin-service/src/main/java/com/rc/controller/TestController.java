package com.rc.controller;

import com.rc.domain.SysUser;
import com.rc.model.R;
import com.rc.service.SysUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Api(tags = "后台管理系统测试接口")
public class TestController {

    @Autowired
    private SysUserService sysUserService;

    @ApiOperation(value = "获取系统用户信息", notes = "根据用户ID获取系统用户信息")
    @GetMapping("/sysUser/info/{id}")
    @ApiImplicitParams({@ApiImplicitParam(name = "id", value = "用户ID", required = true, dataType = "Long", paramType = "path")})
    public R<SysUser> getSysUser(@PathVariable("id") Long id) {
        SysUser byId = sysUserService.getById(id);
        return R.ok(byId);
    }
}
