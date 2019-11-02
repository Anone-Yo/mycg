package com.changgou.token;

import com.alibaba.fastjson.JSON;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.RsaSigner;
import org.springframework.security.jwt.crypto.sign.RsaVerifier;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.interfaces.RSAPrivateKey;
import java.util.HashMap;
import java.util.Map;

/**
 * 创建令牌
 */
public class CreateJwtTest {
    /**
     * 创建令牌
     */
    @Test
    public void createJwtTest() {
        //秘钥库密码
        String secret="changgou";
        //秘钥密码
        String password="changgou";
        //秘钥的别名
        String alias="changgou";

        //访问证书的路径--->获取私钥
        ClassPathResource classPathResource = new ClassPathResource("changgou.jks");
        //创建秘钥工程
        KeyStoreKeyFactory keyStoreKeyFactory = new KeyStoreKeyFactory(classPathResource,password.toCharArray());
        //读取秘钥对
        KeyPair keyPair = keyStoreKeyFactory.getKeyPair(alias, password.toCharArray());
        //读取私钥===指定RSA算法
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        //封装载荷
        Map<String,Object> tokenMap=new HashMap<String, Object>();
        tokenMap.put("company","anone");
        tokenMap.put("name","kobe");
        tokenMap.put("roles","user");
        //生成令牌
        Jwt jwt = JwtHelper.encode(JSON.toJSONString(tokenMap), new RsaSigner(privateKey));

        //获取token 取出令牌
        String jwtEncoded = jwt.getEncoded();
        System.out.println(jwtEncoded);
    }

    /**
     * 解析令牌
     */
    @Test
    public void testParseToken() {
        //获取令牌
        String token="eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJyb2xlcyI6InVzZXIiLCJuYW1lIjoia29iZSIsImNvbXBhbnkiOiJhbm9uZSJ9.W09MgAY6TVVP07vim4mXt_zi654yJhyNVZkEaIps9eCF3n0Z8g3Pq48MPM4dHmtLlHaMsYRSETkL5Ev0aXM612ZR0SdF7TXgMIxula_Zv0hJTrCV7VHrc_UdRMWbXZqVtTaeHeSlz-oDbNjCicxWwyvoV2NG-fBycimign53hVsku8RIAmhFL6q_73RrvusqfpdJWgNdSo0VMRvuO0OkKn5xjmR7KXi_rnrRXiRJ7YhQ9mr9FVZFRnev63vF2GaUVBCufZRNr22O0pv0ZKbfyB9ivHV7xSMPfja80Y3rubFAJz_2nepruPKYYr8VXI_R9J6__j4a505PxMLFPw29ew";

        //获取公钥
        String publicKey="-----BEGIN PUBLIC KEY-----MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEArF80H6MdOshJqftPRAJYO5I30ivPZzR+16VxC6XI0ngFfEhV2I4t40hdxzY5LqwQ9WYSBGTbQrsrGONwSPljx484HUsS3Gkkbm4DJPENBb5aE0YBmDf2YVfe+mwFkqCCqDmXibYV79zWurT0X9M+olYwT6CGaoHluP1maCNFb3Gehs1N7JqjgiutC2lqyIC/Re6a5vWYzRrUg99DZGFc7dIHsAEJ2XL3NiRaMr5TpahD2pwSl7tmtOBoFR1OITx+dDLGogxRsu3wiS5w4qzFt6X+s8bvoUNSZB01YjeHjfdqjuPCfycvr6kKpBaEoMAgbhOb43hxABwqciB/+vysdwIDAQAB-----END PUBLIC KEY-----";

        //解析令牌
        Jwt jwt = JwtHelper.decodeAndVerify(token, new RsaVerifier(publicKey));

        //获取数据信息
        String claims = jwt.getClaims();
        System.out.println(claims);

        //获取令牌信息
        String jwtEncoded = jwt.getEncoded();
        System.out.println(jwtEncoded);
    }
}
