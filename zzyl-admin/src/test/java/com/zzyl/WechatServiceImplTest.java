package com.zzyl;

import com.zzyl.nursing.service.WechatService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class WechatServiceImplTest {

    @Autowired
    private WechatService wechatService;

    @Test
    public void testGetOpenid() {
        String openid = wechatService.getOpenid("0d324wGa1Sb6LL0fzkIa1p0eft424wG1");
        System.out.println(openid);//o3CsK6_C6f4WP9b0AxXNJOkc6q9Q
    }

    @Test
    public void testGetPhone() {
        String phone = wechatService.getPhone("0948ba3b69bd88c680dcdc424ad0c2828c8ce2b1334874b8fe795c0a412b402f");
        System.out.println(phone);
    }
}