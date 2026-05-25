package com.zzyl;

import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;

/**
 * @Author: ziyi
 * @Date: 2026/5/25 17:57
 * @Version: v1.0.0
 * @Description: TODO
 **/
public class HttpTest {

    @Test
    public void getTest() {
        String result = HttpUtil.get("https://www.baidu.com");
        System.out.println("result = " + result);
    }

    @Test
    public void getTestByParams() {
        Map<String, Object> params = new HashMap<>();
        params.put("pageNum", 1);
        params.put("pageSize", 5);
        String result = HttpUtil.get("http://localhost:8080/nursing/project/list", params);
        System.out.println("result = " + result);
    }

    @Test
    public void testCreate() {
        Map<String, Object> params = new HashMap<>();
        params.put("pageNum", 1);
        params.put("pageSize", 5);
        HttpResponse response = HttpUtil.createGet("http://localhost:8080/nursing/project/list").header("authorization", "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ4dXhpYW4iLCJsb2dpbl91c2VyX2tleSI6ImQ1MDhjNThjLTUwZWEtNDczMS1iN2ZhLTNhMzY0NGEyZjY4MiJ9.jwjjSXedkh8cxXCyqA-Q5RDLC7XBEYB4JGml4taX0QoDFgyTsiDuY0qk1Se42greYIFOT9_RY_6OW5t9DgUXyQ")
                .form(params)
                .execute();

        String body = response.body();

        System.out.println("body = " + body);

    }
    @Test
    public void testCreate2() {
        String url = "http://localhost:8080/nursing/project";
        HashMap<String, Object> paramMap = new HashMap<>();
        paramMap.put("name", "护理项目测试");
        paramMap.put("orderNo", 1);
        paramMap.put("unit", "次");
        paramMap.put("price", 10.00);
        paramMap.put("image", "https://yjy-slwl-oss.oss-cn-hangzhou.aliyuncs.com/ae7cf766-fb7b-49ff-a73c-c86c25f280e1.png");
        paramMap.put("nursingRequirement", "无特殊要求");
        paramMap.put("status", 1);
        HttpResponse response = HttpUtil.createPost(url)
                .body(JSON.toJSONString(paramMap))
                .header("authorization", "Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ4dXhpYW4iLCJsb2dpbl91c2VyX2tleSI6ImQ1MDhjNThjLTUwZWEtNDczMS1iN2ZhLTNhMzY0NGEyZjY4MiJ9.jwjjSXedkh8cxXCyqA-Q5RDLC7XBEYB4JGml4taX0QoDFgyTsiDuY0qk1Se42greYIFOT9_RY_6OW5t9DgUXyQ")
                .execute();
        System.out.println(response.body());
    }
}
