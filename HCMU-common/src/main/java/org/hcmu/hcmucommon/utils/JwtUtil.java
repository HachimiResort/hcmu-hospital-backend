package org.hcmu.hcmucommon.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.nio.charset.StandardCharsets;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

public class JwtUtil {

    //默认有效期为
    public static final Long JWT_TTL = 60 * 60 *1000L;// 60 * 60 *1000  一个小时
    //设置秘钥明文
    public static final String JWT_KEY = "HCMU-Hospital-hachimi";

    public static String getUUID(){
        String token = UUID.randomUUID().toString().replaceAll("-", "");
        return token;
    }

    /**
     * 生成jtw
     * @param subject token中要存放的数据（json格式）
     * @param ttlMillis token超时时间
     * @return
     */
    public static String createJWT(String subject, Long ttlMillis) {
        JwtBuilder builder = getJwtBuilder(subject, ttlMillis, getUUID());// 设置过期时间
        return builder.compact();
    }

    private static JwtBuilder getJwtBuilder(String subject, Long ttlMillis, String uuid) {
        SecretKey secretKey = generalKey();
        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);
        if(ttlMillis==null){
            ttlMillis=JwtUtil.JWT_TTL;
        }
        long expMillis = nowMillis + ttlMillis;
        Date expDate = new Date(expMillis);
        return Jwts.builder()
                .id(uuid)              //唯一的ID
                .subject(subject)   // 主题  可以是JSON数据
                .issuer("HCMU")     // 签发者
                .issuedAt(now)      // 签发时间
                .signWith(secretKey) //使用HS256对称加密算法签名
                .expiration(expDate);
    }


    /**
     * 生成加密后的秘钥 secretKey
     * @return
     */
    public static SecretKey generalKey() {
        // 使用更安全的密钥生成方式
        String keyString = JWT_KEY;
        // 确保密钥长度足够（至少32字节用于HS256）
        if (keyString.length() < 32) {
            keyString = keyString + "0".repeat(32 - keyString.length());
        }
        byte[] keyBytes = keyString.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Token解密
     *
     * @param token     加密后的token
     * @return
     */
    public static Claims parseJWT(String token) {
        SecretKey secretKey = generalKey();

        // 使用新版本JJWT API解析JWT
        Claims claims = Jwts.parser()
                // 设置签名的秘钥
                .verifyWith(secretKey)
                .build()
                // 设置需要解析的jwt
                .parseSignedClaims(token).getPayload();
        return claims;
    }

    public static String getUserId(String token) throws Exception {
        return parseJWT(token).getSubject();
    }
}
