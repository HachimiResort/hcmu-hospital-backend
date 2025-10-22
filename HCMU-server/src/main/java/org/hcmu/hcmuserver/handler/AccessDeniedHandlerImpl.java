package org.hcmu.hcmuserver.handler;

import com.alibaba.fastjson.JSON;

import org.hcmu.hcmucommon.utils.WebUtils;
import org.hcmu.hcmupojo.BaseResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 权限认证异常处理
 */
@Component
public class AccessDeniedHandlerImpl implements AccessDeniedHandler {
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) {
        String json = JSON.toJSONString(BaseResponse.error(HttpStatus.FORBIDDEN.value(),"您的权限不足"));
        //处理异常
        WebUtils.renderString(response,json);
    }
}
