package com.xhzb.framework.interceptor.impl;

import com.xhzb.common.exception.base.BaseException;
import com.xhzb.common.utils.StringUtils;
import com.xhzb.common.utils.UserThreadLocal;
import com.xhzb.framework.web.service.TokenService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.xmlbeans.ThreadLocalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@Slf4j
public class WechatInterceptor implements HandlerInterceptor {

    @Autowired
    private TokenService tokenService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        log.debug("拦截器已执行");
        //获取Token
        String token = request.getHeader("Authorization");
        if(StringUtils.isEmpty(token)){
            throw new BaseException("请先登录");
        }
        //解析Token
        Claims claims = tokenService.parseToken(token);
        if(claims == null || claims.isEmpty()){
            throw new BaseException("请先登录");
        }
        Long userId = claims.get("userId", Long.class);
        //将用户信息放入ThreadLocal
        UserThreadLocal.set(userId);
        //放行
        return true;
    }


    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        log.debug("拦截器已执行完毕");
        //清空数据
        UserThreadLocal.remove();
    }


}
