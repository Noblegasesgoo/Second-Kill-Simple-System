package com.zhao.seckill.utils;

import io.jsonwebtoken.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author noblegasesgoo
 * @version 0.0.1
 * @date 2022/2/14 10:56
 * @description
 */
public class JWTUtil {

    private static final String secretKey = "zhaoliminwanglu1314!@#$$";

    /**
     * 创建 JWT token
     * @param userId
     * @return token
     */
    public static String createToken(Long userId){
        Map<String,Object> claims = new HashMap<>();
        claims.put("userId",userId);
        JwtBuilder jwtBuilder = Jwts.builder()
                .signWith(SignatureAlgorithm.HS256, secretKey) // 签发算法，秘钥为 secretKey
                .setClaims(claims) // 载荷(body)
                .setIssuedAt(new Date()) // 设置签发时间
                .setExpiration(new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000 * 30 * 12));// 一年的有效时间

        String token = jwtBuilder.compact();
        return token;
    }

    /**
     * 解析 token
     * @param token
     * @return 解析后的 body 内容
     */
    public static Map<String, Object> checkToken(String token){

        try {
            Jwt parse = Jwts.parser().setSigningKey(secretKey).parse(token);

            return (Map<String, Object>) parse.getBody();
        }catch (Exception e){

            e.printStackTrace();
        }

        return null;
    }

    /**
     * 从token中获取登录用户id
     * @param token
     * @return 登录用户id
     */
    public static Long getUserIdFromToken(String token){
        Long userId;

        try {
            Claims claims = getClaimsFromToken(token);
            userId = claims.get("userId", Long.class);
        } catch (Exception e) {
            userId = null;
        }

        return userId;
    }

    /**
     * 从token中获取荷载
     * @param token
     * @return
     */
    private static Claims getClaimsFromToken(String token) {
        Claims claims = null;
        try {
            claims = Jwts.parser()
                    .setSigningKey(secretKey)
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return claims;
    }
}
