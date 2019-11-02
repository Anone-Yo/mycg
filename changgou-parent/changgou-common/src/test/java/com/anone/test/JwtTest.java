package com.anone.test;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.DefaultJwtBuilder;
import io.jsonwebtoken.impl.DefaultJwtParser;
import org.junit.Test;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 测试Jwt的创建 和解析
 */
public class JwtTest {
    /**
     * 创建jwt
     */
    @Test
    public void createJwt() {
       /* JwtBuilder jwtBuilder=new DefaultJwtBuilder();
        jwtBuilder.setIssuer("anone"); //颁发者
        jwtBuilder.setIssuedAt(new Date()); //颁发时间
        jwtBuilder.setSubject("hello"); //设置主题
        //加密钥
        jwtBuilder.signWith(SignatureAlgorithm.HS256,"1234");
        String compact = jwtBuilder.compact();
        System.out.println(compact);*/
       JwtBuilder jwtBuilder= Jwts.builder()
               .setIssuedAt(new Date())//设置颁发日期
               .setIssuer("anone")//设置颁发者
               .setSubject("主题内容：666")//设置主题
              .setExpiration(new Date())//设置过期时间
               .signWith(SignatureAlgorithm.HS256,"aaaa");

        //自定义claims
        Map<String,Object> map=new HashMap<>();
        map.put("username","kobe");
        map.put("address","LA");
        jwtBuilder.addClaims(map);

        String jwtString = jwtBuilder.compact(); //签名
        System.out.println(jwtString);
    }

    /**
     * 解析jwt
     */
    @Test
    public void parseJwt() {
        //解析令牌
        String jwt="eyJhbGciOiJIUzI1NiJ9.eyJpYXQiOjE1Njc2MDk2NzIsImlzcyI6ImFub25lIiwic3ViIjoi5Li76aKY5YaF5a6577yaNjY2In0.pROB4fx08sUXyIyJi4S5GKZaqr4S97yyvzrbNwf4yOs";
        String jwtString = Jwts.parser().setSigningKey("aaaa").parse(jwt).getBody().toString();
        System.out.println(jwtString);
    }
}
