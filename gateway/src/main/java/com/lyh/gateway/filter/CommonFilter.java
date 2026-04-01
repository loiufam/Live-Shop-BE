package com.lyh.gateway.filter;

import com.lyh.common.constants.CommonConstants;
import com.lyh.redis.constants.CommonRedisKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

/**
 * 定义全局过滤器，功能如下:
 * 1.把客户端真实IP通过请求同的方式传递给微服务
 * 2.在请求头中添加FEIGN_REQUEST的请求头，值为0，标记请求不是Feign调用，而是客户端调用
 * 3.刷新Token的有效时间
 */
@Component
public class CommonFilter implements GlobalFilter {

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final List<String> WHITE_LIST = Arrays.asList("/user/login", "/user/register");
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        for (String whitePath : WHITE_LIST) {
            if (pathMatcher.match(whitePath, path)) {
                return proceedWithEnhancement(exchange, chain);
            }
        }

        String token = request.getHeaders().getFirst(CommonConstants.TOKEN_NAME);
        if (StringUtils.isEmpty(token)) {
            return unauthorizedResponse(exchange); // 没有 Token，直接拦截返回 401
        }

        String redisKey = CommonRedisKey.commonRedisKey.USER_TOKEN.getRealKey(token);
        if (!Boolean.TRUE.equals(redisTemplate.hasKey(redisKey))) {
            return unauthorizedResponse(exchange); // Token 在 Redis 里不存在或已过期，拦截返回 401
        }

        // ================= 3. 放行并执行增强逻辑 (Pre + Post 阶段) =================
        return proceedWithEnhancement(exchange, chain).then(Mono.fromRunnable(() -> {
            // Post 拦截逻辑：既然能走到这里，说明 Token 绝对合法，直接续期
            redisTemplate.expire(redisKey,
                    CommonRedisKey.commonRedisKey.USER_TOKEN.getExpireTime(),
                    CommonRedisKey.commonRedisKey.USER_TOKEN.getUnit());
        }));
    }

    private Mono<Void> proceedWithEnhancement(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest enhancedRequest = exchange.getRequest().mutate()
                .header(CommonConstants.REAL_IP, exchange.getRequest().getRemoteAddress().getHostString())
                .header(CommonConstants.FEIGN_REQUEST_KEY, CommonConstants.FEIGN_REQUEST_FALSE)
                .build();
        return chain.filter(exchange.mutate().request(enhancedRequest).build());
    }

    private Mono<Void> unauthorizedResponse(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        return response.setComplete();
    }

}
