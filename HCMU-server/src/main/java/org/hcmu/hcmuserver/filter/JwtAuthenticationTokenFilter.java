package org.hcmu.hcmuserver.filter;

import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;

import lombok.extern.slf4j.Slf4j;
import org.hcmu.hcmucommon.utils.JwtUtil;
import org.hcmu.hcmucommon.utils.RedisUtil;
import org.hcmu.hcmupojo.LoginUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 主要用于校验token是否合法
 * 用在UsernamePasswordAuthenticationFilter之前
 */
@Component
@Slf4j
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {
    @Autowired
    private RedisUtil redisCache;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        // 获取token
        String token = request.getHeader("Authorization");
        // 1. 无token
        if (!StringUtils.hasText(token)) {
            // 放行
            filterChain.doFilter(request, response);
            return;
        }

        // 2.token无法解析
        Long userId = null;
        try {
            userId = Long.parseLong(JwtUtil.getUserId(token));
        } catch (Exception e) {
            // throw new ServiceException(HttpStatus.UNAUTHORIZED.value(), "授权验证失败！");
            filterChain.doFilter(request, response);
            return;
        }

        // 3. 用户携带token,但是Redis中没有对应的登录信息
        LoginUser loginUser = redisCache.getCacheObject("login:" + userId);
        if (ObjectUtils.isNull(loginUser)) {
            // throw new ServiceException(HttpStatus.UNAUTHORIZED.value(), "用户未登录！");
            filterChain.doFilter(request, response);
            return;
        }

        // 存入SecurityContextHolder
        // TODO 获取权限信息封装到Authentication中
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(loginUser,
                null, loginUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        // 放行
        filterChain.doFilter(request, response);
    }

}
