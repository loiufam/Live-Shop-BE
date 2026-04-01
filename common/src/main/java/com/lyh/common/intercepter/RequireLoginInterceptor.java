package com.lyh.common.intercepter;

import org.apache.commons.lang3.StringUtils;
import com.alibaba.fastjson.JSON;
import com.lyh.common.annotation.RequireLogin;
import com.lyh.common.constants.CommonConstants;
import com.lyh.common.context.UserContext;
import com.lyh.common.domin.UserInfo;
import com.lyh.common.result.CommonCodeMsg;
import com.lyh.common.result.Result;
import com.lyh.common.utils.JwtUtils;
import com.lyh.redis.constants.CommonRedisKey;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;

public class RequireLoginInterceptor implements HandlerInterceptor {

    private StringRedisTemplate redisTemplate;

    public RequireLoginInterceptor(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // 判断当前请求是否是一个 api 请求
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            // 从请求头中获取 Feign 请求标识，以此来判断该请求是否是 Feign
            String feignRequest = request.getHeader(CommonConstants.FEIGN_REQUEST_KEY);

            // Feign请求标识不为空 && 不是 Feign 请求 && 访问的接口方法贴了 @RequireLogin
            if (!StringUtils.isEmpty(feignRequest)
                    && CommonConstants.FEIGN_REQUEST_FALSE.equals(feignRequest)
                    && handlerMethod.getMethodAnnotation(RequireLogin.class) != null) {

                // 设置响应类型为 json
                response.setContentType("application/json;charset=utf-8");
                // 从请求头中获取 token
                String token = request.getHeader(CommonConstants.TOKEN_NAME);
                if (StringUtils.isEmpty(token)) {
                    // 如果 token 为空，返回 token 无效信息
                    response.getWriter().write(JSON.toJSONString(Result.error(CommonCodeMsg.TOKEN_INVALID)));
                    return false;
                }
                
                // 检查 Redis 中是否存在该 token
                String redisKey = CommonRedisKey.commonRedisKey.USER_TOKEN.getRealKey(token);
                String userIdStr = redisTemplate.opsForValue().get(redisKey);
                
                if (userIdStr == null) {
                    response.getWriter().write(JSON.toJSONString(Result.error(CommonCodeMsg.TOKEN_INVALID)));
                    return false;
                }
                
                // 从 JWT 中解析用户信息
                try {
                    Map<String, Object> claims = JwtUtils.parseToken(token);
                    UserInfo userInfo = new UserInfo();
                    userInfo.setId(Long.parseLong(claims.get("userId").toString()));
                    userInfo.setUsername((String) claims.get("username"));
                    
                    // 将用户信息存入上下文
                    UserContext.setUser(userInfo);
                } catch (Exception e) {
                    response.getWriter().write(JSON.toJSONString(Result.error(CommonCodeMsg.TOKEN_INVALID)));
                    return false;
                }
            }
        }

        // 如果不是接口请求，就直接放行
        return true;
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 请求完成后清除 ThreadLocal，防止内存泄漏
        UserContext.clear();
    }
}
