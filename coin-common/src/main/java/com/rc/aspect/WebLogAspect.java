package com.rc.aspect;

import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import com.alibaba.fastjson.JSON;
import com.rc.model.WebLog;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;

@Aspect
@Component
@Order(1)
@Slf4j
public class WebLogAspect {

    //日志记录
    // 定义切点
    @Pointcut("execution( * com.rc.controller.*.*(..))") // 所有com.rc.controller包及其子包下的所有类的所有方法
    public void webLog() {
    }

    // 记录日志的环绕通知
    @Around(value = "webLog()")
    public Object recordWebLog(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        Object result = null;
        StopWatch stopWatch = new StopWatch(); // 创建计时器
        stopWatch.start(); //  开始计时器
        result = proceedingJoinPoint.proceed(proceedingJoinPoint.getArgs()); // 不需要我们自己处理这个异常
        stopWatch.stop(); // 记时结束

        // 获取请求的上下文
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = requestAttributes.getRequest();
        // 获取登录的用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // 获取方法
        MethodSignature methodSignature = (MethodSignature) proceedingJoinPoint.getSignature();
        Method method = methodSignature.getMethod();
        // 获取方法上swagger的ApiOperation注解
        ApiOperation annotation = method.getAnnotation(ApiOperation.class);
        // 获取目标对象的类型名称
        String className = proceedingJoinPoint.getTarget().getClass().getName();
        // 获取请求的url 地址
        String requestUrl = request.getRequestURL().toString();
        WebLog webLog = WebLog.builder()
                .basePath(StrUtil.removeSuffix(requestUrl, URLUtil.url(requestUrl).getPath())) //http://localhost:8080/xx/xx 保留basePath，去掉xx/xx
                .description(annotation == null ? "no desc" : annotation.value())
                .ip(request.getRemoteAddr())
                .parameter(getMethodParameter(method, proceedingJoinPoint.getArgs()))
                .method(className + "." + method.getName())
//                .result(request == null ? "" : JSON.toJSONString(request)) //write javaBean error, fastjson version 1.2.68, class org.springframework.security.web.servletapi.HttpServlet3RequestFactory$Servlet3SecurityContextHolderAwareRequestWrapper, method : getAsyncContext
                .result(request == null ? "" : JSON.toJSONString(getRequestInfo(request)))
//                .recodeTime(System.currentTimeMillis())
                .spendTime((int) stopWatch.getTotalTimeMillis())
                .uri(request.getRequestURI())
                .url(request.getRequestURL().toString())
                .username(authentication == null ? "anonymous" : authentication.getPrincipal().toString())
                .build();
        log.info(JSON.toJSONString(webLog, true));
        return result;
    }

    /**
     * {
     * "":value,
     * "":"value"
     * }
     *
     * @param method
     * @param args
     * @return
     */
    private Object getMethodParameter(Method method, Object[] args) {
        LocalVariableTableParameterNameDiscoverer localVariableTableParameterNameDiscoverer = new LocalVariableTableParameterNameDiscoverer();
        String[] parameterNames = localVariableTableParameterNameDiscoverer.getParameterNames(method);
        Map<String, Object> methodParameters = new HashMap<>();
        if (args != null) {
            for (int i = 0; i < parameterNames.length; i++) {
                if(parameterNames[i].equals("password") || parameterNames[i].equals("file")){
                    methodParameters.put(parameterNames[i], "受限的类型");
                } else {
                    methodParameters.put(parameterNames[i], args[i]);
                }
            }
        }
        return methodParameters;
    }

    private Map<String, String> getRequestInfo(HttpServletRequest request) {
        Map<String, String> requestInfo = new HashMap<>();
        requestInfo.put("method", request.getMethod());
        requestInfo.put("requestURI", request.getRequestURI());
        requestInfo.put("queryString", request.getQueryString());
        requestInfo.put("remoteAddr", request.getRemoteAddr());
        return requestInfo;
    }

}
