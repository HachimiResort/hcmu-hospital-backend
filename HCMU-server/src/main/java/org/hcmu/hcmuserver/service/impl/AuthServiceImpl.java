package org.hcmu.hcmuserver.service.impl;


import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.yulichang.base.MPJBaseServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.hcmu.hcmucommon.enumeration.RedisEnum;
import org.hcmu.hcmucommon.exception.ServiceException;
import org.hcmu.hcmucommon.result.Result;
import org.hcmu.hcmucommon.utils.JwtUtil;
import org.hcmu.hcmucommon.utils.RedisUtil;
import org.hcmu.hcmupojo.LoginUser;
import org.hcmu.hcmupojo.dto.UserDTO;
import org.hcmu.hcmupojo.dto.UserDTO.UserLoginDTO;
import org.hcmu.hcmupojo.dto.UserDTO.UserRegisterDTO;
import org.hcmu.hcmupojo.dto.UserDTO.UserEmailVerifyDTO;
import org.hcmu.hcmupojo.entity.PendingUser;
import org.hcmu.hcmupojo.entity.Role;
import org.hcmu.hcmupojo.entity.relation.UserRole;
import org.hcmu.hcmuserver.mapper.role.RoleMapper;
import org.hcmu.hcmuserver.mapper.user.PendingUserMapper;
import org.hcmu.hcmuserver.mapper.user.UserMapper;
import org.hcmu.hcmupojo.entity.User;
import org.hcmu.hcmuserver.mapper.user.UserRoleMapper;
import org.hcmu.hcmuserver.service.AuthService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.method.P;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
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

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private UserRoleMapper userRoleMapper;

    @Autowired
    private PendingUserMapper pendingUserMapper;

    @Autowired
    private MailServiceImpl mailService;

    @Override
    public Result login(UserLoginDTO userLoginDto) {
        // AuthenticationManager authenticate进行用户认证
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                userLoginDto.getUserName(), userLoginDto.getPassword());
        Authentication authenticate = authenticationManager.authenticate(authenticationToken);

        // 如果认证没通过，给出对应的提示
        if (Objects.isNull(authenticate)) {
            throw new ServiceException(HttpStatus.UNAUTHORIZED.value(), "登录失败!");
        }
        // 如果认证通过了，使用userid生成一个jwt jwt存入ResponseResult返回
        LoginUser loginUser = (LoginUser) authenticate.getPrincipal();
        Long userId = loginUser.getUser().getUserId();

        String jwt = JwtUtil.createJWT(userId.toString(), 1000 * 60 * 60 * 2L);
        Map<String, String> map = new HashMap<>();
        map.put("token", jwt);
        map.put("userId", userId.toString());
        // 把完整的用户信息存入redis userid作为key 如果用户没有退出动作，12小时后删除登录状态
        redisUtil.setCacheObject(RedisEnum.LOGIN.getDesc() + userId, loginUser, 2, TimeUnit.HOURS);
        return Result.success("登录成功!", map);
    }

    @Override
    public Result logout(Long userId) {
        // 删除redis中的值
        redisUtil.deleteObject("login:" + userId);
        return Result.success("退出成功!", null);
    }

    @Override
    public Result getRegisterCode(UserRegisterDTO userRegister) {
        // 判断两次密码是否一致
        if (!userRegister.checkPassword()) {
            throw new ServiceException(HttpStatus.BAD_REQUEST.value(), "两次密码不一致!");
        }

        // 判断用户名是否存在 - 使用BINARY确保用户名严格区分大小写
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.apply("BINARY user_name = {0}", userRegister.getUserName());
        User user_tmp = baseMapper.selectOne(queryWrapper);

        // 如果找到了
        if (Objects.nonNull(user_tmp)) {
            throw new ServiceException(HttpStatus.BAD_REQUEST.value(), "用户名已存在!");
        }

        // 判断邮箱是否存在
        queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getEmail, userRegister.getEmail());
        List<User> userList = baseMapper.selectList(queryWrapper);

        // 如果找到了
        if (userList.size() > 0) {
            throw new ServiceException(HttpStatus.BAD_REQUEST.value(), "该邮箱已被绑定!");
        }

        // 接下来判断是否是待注册用户
        LambdaQueryWrapper<PendingUser> pendingUserQueryWrapper = new LambdaQueryWrapper<>();
        pendingUserQueryWrapper
            .eq(PendingUser::getUserName, userRegister.getUserName())
            .or()
            .eq(PendingUser::getEmail, userRegister.getEmail());
        PendingUser pendingUser_tmp = pendingUserMapper.selectOne(pendingUserQueryWrapper);

        // 如果找到了
        if (Objects.nonNull(pendingUser_tmp)) {
            LambdaQueryWrapper<PendingUser> exactPendingUserQueryWrapper = new LambdaQueryWrapper<>();
            exactPendingUserQueryWrapper
                .eq(PendingUser::getUserName, userRegister.getUserName())
                .eq(PendingUser::getEmail, userRegister.getEmail())
                .eq(PendingUser::getName, userRegister.getName());
            PendingUser exactPendingUser = pendingUserMapper.selectOne(exactPendingUserQueryWrapper);
            if (Objects.isNull(exactPendingUser)) {
                throw new ServiceException(HttpStatus.BAD_REQUEST.value(), "注册信息与待注册用户信息不匹配!");
            }
        }


        userRegister.setPassword(passwordEncoder.encode(userRegister.getPassword()));

        // TODO: 生成6位随机验证码
        String code = RandomUtil.randomNumbers(6);
        userRegister.setCode(code);

//        将注册用户暂存到redis中
        redisUtil.setCacheObject(RedisEnum.REGISTER.getDesc() + userRegister.getEmail(), userRegister, 5, TimeUnit.MINUTES);


        mailService.sendVerifyCoed("注册邮箱验证", code, userRegister.getEmail());

        return Result.success("验证码发送成功!");
    }

    @Override
    public Result verifyRegister(UserEmailVerifyDTO userEmailVerifyDTO) {
        // 从redis中取出注册用户
        UserRegisterDTO userRegister = (UserRegisterDTO) redisUtil.getCacheObject(RedisEnum.REGISTER.getDesc() + userEmailVerifyDTO.getEmail());
        if (Objects.isNull(userRegister)) {
            throw new ServiceException(HttpStatus.BAD_REQUEST.value(), "验证码已过期!");
        }
        // 判断验证码是否正确
        if (!userRegister.getCode().equals(userEmailVerifyDTO.getCode())) {
            throw new ServiceException(HttpStatus.BAD_REQUEST.value(), "验证码错误!");
        }
//        从redis中删除
        redisUtil.deleteObject(RedisEnum.REGISTER.getDesc() + userEmailVerifyDTO.getEmail());

        Long roleId = 0L;

        LambdaQueryWrapper<PendingUser> exactPendingUserQueryWrapper = new LambdaQueryWrapper<>();
        exactPendingUserQueryWrapper
            .eq(PendingUser::getUserName, userRegister.getUserName())
            .eq(PendingUser::getEmail, userRegister.getEmail())
            .eq(PendingUser::getName, userRegister.getName());
        PendingUser exactPendingUser = pendingUserMapper.selectOne(exactPendingUserQueryWrapper);
        
        if (Objects.isNull(exactPendingUser)) {
            LambdaQueryWrapper<Role> roleQueryWrapper = new LambdaQueryWrapper<>();
            roleQueryWrapper.eq(Role::getIsDefault, 1);

            Role role = roleMapper.selectOne(roleQueryWrapper);
            roleId = role.getRoleId();
        } else {
            roleId = exactPendingUser.getRoleId();
            // 删除待注册用户信息
            pendingUserMapper.delete(exactPendingUserQueryWrapper);
        }


        // 将用户信息存入数据库
        User user = userRegister.toUser();
        baseMapper.insert(user);
  

        UserRole sysUserRole = new UserRole();
        sysUserRole.setUserId(user.getUserId());
        sysUserRole.setRoleId(roleId);

        userRoleMapper.insert(sysUserRole);

        // 注册成功将用户id返回
        Map<String, String> map = new HashMap<>();
        map.put("userId", user.getUserId().toString());

        return Result.success("注册成功!", map);
    }
}
