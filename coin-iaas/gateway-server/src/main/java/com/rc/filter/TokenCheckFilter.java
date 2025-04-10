package com.rc.filter;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.Set;

@Component
public class TokenCheckFilter implements GlobalFilter, Ordered {

    @Autowired
    private StringRedisTemplate redisTemplate;

    // 不需要token 能访问的url
    @Value("${no.token.access.urls:/admin/login,/admin/validate/code,/user/gt/register,/user/login,/user/users/register,/user/sms/sendTo,/user/users/setPassword}")
    private Set<String> noTokenAccessUrls;


    //拦截后做啥
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 不需要token 就能访问
        if (allowNoTokenAccess(exchange)) {
            return chain.filter(exchange);
        }
        // 获取用户的token
        String token = getToken(exchange);

        if (StringUtils.isEmpty(token)) { // token 为 Empty
            return buildUNAuthorizedResult(exchange);
        }

        return chain.filter(exchange);
    }

    private boolean allowNoTokenAccess(ServerWebExchange exchange) {
        String path = exchange.getRequest().getURI().getPath();
        if (noTokenAccessUrls.contains(path)) {
            return true;
        }
        return false;
    }

    private String getToken(ServerWebExchange exchange) {
        ServerHttpRequest request = exchange.getRequest();
        HttpHeaders headers = request.getHeaders();
        String authorization = headers.getFirst(HttpHeaders.AUTHORIZATION);
        if (Objects.isNull(authorization) || authorization.trim().isEmpty()) {
            return null;
        }
        return authorization.replace("bearer ", "");
    }

    // 响应没有token的错误
    private Mono<Void> buildUNAuthorizedResult(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED); // 401
        response.getHeaders().set("Content-Type", "application/json;charset=UTF-8");
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("error", "unauthorized");
        jsonObject.put("error_description", "invalid_token");
        DataBuffer dataBuffer = response.bufferFactory().wrap(jsonObject.toJSONString().getBytes());
        return response.writeWith(Flux.just(dataBuffer));
    }


    //拦截顺序
    @Override
    public int getOrder() {
        return 0;
    }
}
