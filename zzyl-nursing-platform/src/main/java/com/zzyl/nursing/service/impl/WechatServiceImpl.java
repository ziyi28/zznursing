package com.zzyl.nursing.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.zzyl.common.exception.base.BaseException;
import com.zzyl.nursing.service.WechatService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author: ziyi
 * @Date: 2026/5/25 18:57
 * @Version: v1.0.0
 * @Description: TODO
 **/
@Service
public class WechatServiceImpl implements WechatService {
    // 登录
    private static final String REQUEST_URL = "https://api.weixin.qq.com/sns/jscode2session?grant_type=authorization_code";

    // 获取token
    private static final String TOKEN_URL = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential";

    // 获取手机号
    private static final String PHONE_REQUEST_URL = "https://api.weixin.qq.com/wxa/business/getuserphonenumber?access_token=";
    // appid
    @Value("${wechat.appid}")
    private String appid;
    // 密钥
    @Value("${wechat.secret}")
    private String secret;
    @Override
    public String getOpenid(String code) {
        Map<String,Object> params = new HashMap<>();
        params.put("appid",appid);
        params.put("secret",secret);
        params.put("js_code",code);
        HttpResponse response = HttpUtil.createGet(REQUEST_URL).form(params).execute();
        JSONObject jsonObject = JSON.parseObject(response.body());
        if(ObjectUtil.isNotEmpty(jsonObject.getInteger("errcode"))) {
            throw new RuntimeException(jsonObject.getString("errmsg"));
        }
        return jsonObject.getString("openid");
    }

    @Override
    public String getPhone(String detailCode) {
        String token = getToken();
        String url = PHONE_REQUEST_URL + token;
        Map<String,Object> params = new HashMap<>();
        params.put("code",detailCode);
        HttpResponse response = HttpUtil.createPost(url).body(JSON.toJSONString(params)).execute();
        String result = response.body();
        JSONObject jsonObject = JSON.parseObject(result);
        if(jsonObject.getInteger("errcode") != 0) {
            throw new RuntimeException(jsonObject.getString("errmsg"));
        }


        return jsonObject.getJSONObject("phone_info").getString("phoneNumber");
    }

    @Override
    public String getToken() {
        Map<String,Object> params = new HashMap<>();
        params.put("appid",appid);
        params.put("secret",secret);
        String result = HttpUtil.get(TOKEN_URL, params);
        JSONObject jsonObject = JSON.parseObject(result);
        if(ObjectUtil.isNotEmpty(jsonObject.getInteger("errcode"))) {
            throw new RuntimeException(jsonObject.getString("errmsg"));
        }

        return jsonObject.getString("access_token");
    }
}
