package com.lyh.common.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JwtUtils {

    // 密钥，生产环境请务必配置得足够复杂且不要泄露
    private static final String SECRET_KEY = "MySuperSecretKeyForMicroserviceArchitecture";
    // Token 有效期：这里设置长一点，比如 7 天。因为我们主要靠 Redis 控制过期时间
    private static final long EXPIRATION_TIME = 1000 * 60 * 60 * 24 * 7;

    /**
     * 生成 Token
     * 将 userId 作为 payload 存入 token 中
     */
    public static String generateToken(Long userId, String username) {
        return Jwts.builder()
                .claim("userId", userId)
                .claim("username", username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }

    /**
     * 解析 Token 获取其中的 UserId
     */
    public static Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody();
        return claims.get("userId", Long.class);
    }
    
    /**
     * 解析 Token 获取所有声明信息
     */
    public static Map<String, Object> parseToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody();
        
        Map<String, Object> result = new HashMap<>();
        result.put("userId", claims.get("userId"));
        result.put("username", claims.get("username"));
        return result;
    }
}
