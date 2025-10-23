package org.hcmu.hcmuserver.service.impl;


import com.github.yulichang.base.MPJBaseServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.hcmu.hcmucommon.enumeration.RedisEnum;
import org.hcmu.hcmucommon.exception.ServiceException;
import org.hcmu.hcmucommon.result.Result;
import org.hcmu.hcmucommon.utils.JwtUtil;
import org.hcmu.hcmucommon.utils.RedisUtil;
import org.hcmu.hcmupojo.LoginUser;
import org.hcmu.hcmupojo.dto.UserDTO.UserLoginDTO;
import org.hcmu.hcmuserver.mapper.user.UserMapper;
import org.hcmu.hcmupojo.entity.User;
import org.hcmu.hcmuserver.service.AuthService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class AuthServiceImpl extends MPJBaseServiceImpl<UserMapper, User> implements AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private RedisUtil redisUtil;

    @Override
    public Result login(UserLoginDTO userLoginDto) {
        log.info("用户登录:{}", userLoginDto);
        // AuthenticationManager authenticate进行用户认证
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                userLoginDto.getUserAccount(), userLoginDto.getPassword());
        Authentication authenticate = authenticationManager.authenticate(authenticationToken);

        // 如果认证没通过，给出对应的提示
        if (Objects.isNull(authenticate)) {
            throw new ServiceException(HttpStatus.UNAUTHORIZED.value(), "登录失败!");
        }
        // 如果认证通过了，使用userid生成一个jwt jwt存入ResponseResult返回
        LoginUser loginUser = (LoginUser) authenticate.getPrincipal();
        Long userId = loginUser.getUser().getUserId();
        //TODO: token两小时过期
        String jwt = JwtUtil.createJWT(userId.toString(), 1000 * 60 * 60 * 2L);
        Map<String, String> map = new HashMap<>();
        map.put("token", jwt);
        map.put("userId", userId.toString());
        // 把完整的用户信息存入redis userid作为key 如果用户没有退出动作，12小时后删除登录状态
        redisUtil.setCacheObject(RedisEnum.LOGIN.getDesc() + userId, loginUser, 2, TimeUnit.HOURS);
        return Result.success("登录成功!", map);

    }
}
