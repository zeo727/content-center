package com.itmuch.contentcenter.interceptor;

import com.alibaba.nacos.client.utils.StringUtils;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import io.netty.util.internal.StringUtil;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

public class TokenRelayRequestIntecepor implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate requestTemplate) {
        // 1. 获取到token
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        ServletRequestAttributes attributes = (ServletRequestAttributes) requestAttributes;
        HttpServletRequest request = attributes.getRequest();
        String token = request.getHeader("X-Token");

        // 2.将token传递
        if(StringUtils.isNotBlank(token)){
            requestTemplate.header("X-Token",token);
        }

    }
}
