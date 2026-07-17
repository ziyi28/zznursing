package com.zzyl.framework.interceptor;
import cn.hutool.core.map.MapUtil;
import com.zzyl.common.exception.base.BaseException;
import com.zzyl.common.utils.thread.UserThreadLocal;
import com.zzyl.framework.web.service.TokenService;
import io.jsonwebtoken.Claims;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @Author: ziyi
 * @Date: 2026/5/25 21:23
 * @Version: v1.0.0
 * @Description: TODO
 **/
@Component
public class MemberInterceptor implements HandlerInterceptor {
    @Autowired
    TokenService tokenService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //1.如果访问静态资源则放行
        if (!(handler instanceof HandlerMethod)){
            return true;
        }
        //2.获取token
        String token = request.getHeader("Authorization");
        if (StringUtils.isEmpty(token)){
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            throw new BaseException("认证失败");
        }
        Claims claims = tokenService.parseToken(token);
        if (ObjectUtils.isEmpty(claims)){
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            throw new BaseException("认证失败");
        }
        Long userId = MapUtil.get(claims, "userId", Long.class);
        if (ObjectUtils.isEmpty(userId)){
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            throw new BaseException("认证失败");
        }
        UserThreadLocal.set(userId);
        return true;

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) throws Exception {
        UserThreadLocal.remove();
    }
}
