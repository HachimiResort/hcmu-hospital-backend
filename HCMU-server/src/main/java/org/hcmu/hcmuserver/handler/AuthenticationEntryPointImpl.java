package org.hcmu.hcmuserver.handler;

import com.alibaba.fastjson.JSON;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.hcmu.hcmucommon.result.Result;
import org.hcmu.hcmucommon.utils.WebUtils;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;


/**
 * 身份认证异常处理
 */
@Component
@Slf4j
public class AuthenticationEntryPointImpl implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) {
        String json = JSON.toJSONString(Result.error(HttpStatus.UNAUTHORIZED.value(),"认证失败请重新登录！"));
        //处理异常
        WebUtils.renderString(response,json);
    }
}
