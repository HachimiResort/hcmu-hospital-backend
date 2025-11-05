package org.hcmu.hcmuserver.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.yulichang.base.MPJBaseServiceImpl;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import lombok.extern.slf4j.Slf4j;
import org.hcmu.hcmucommon.enumeration.PermissionEnum;
import org.hcmu.hcmucommon.enumeration.RedisEnum;
import org.hcmu.hcmucommon.exception.ServiceException;
import org.hcmu.hcmucommon.result.Result;
import org.hcmu.hcmucommon.utils.RedisUtil;
import org.hcmu.hcmupojo.LoginUser;
import org.hcmu.hcmupojo.dto.PageDTO;
import org.hcmu.hcmupojo.dto.UserDTO;
import org.hcmu.hcmupojo.entity.Permission;
import org.hcmu.hcmupojo.entity.Role;
import org.hcmu.hcmupojo.entity.Schedule;
import org.hcmu.hcmupojo.entity.User;
import org.hcmu.hcmupojo.entity.relation.RolePermission;
import org.hcmu.hcmupojo.entity.relation.UserRole;
import org.hcmu.hcmuserver.mapper.role.RoleMapper;
import org.hcmu.hcmuserver.mapper.schedule.ScheduleMapper;
import org.hcmu.hcmuserver.mapper.user.UserMapper;
import org.hcmu.hcmuserver.mapper.user.UserRoleMapper;
import org.hcmu.hcmuserver.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserServiceImpl extends MPJBaseServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private RedisUtil redisCache;
    
    @Autowired
    private UserRoleMapper UserRoleMapper;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private ScheduleMapper scheduleMapper;

    @Autowired
    private MailServiceImpl mailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public Result<PageDTO<UserDTO.UserListDTO>> findAllUsers(UserDTO.UserGetRequestDTO userGetRequestDTO) {
        MPJLambdaWrapper<User> queryWrapper = new MPJLambdaWrapper<>();
        queryWrapper.select(User::getUserId, User::getUserName, User::getName, User::getPhone, User::getEmail, User::getInfo, User::getSex, User::getNickname)
                .leftJoin(UserRole.class, UserRole::getUserId, User::getUserId)
                .select(UserRole::getRoleId)
                .leftJoin(Role.class, Role::getRoleId, UserRole::getRoleId)
                .selectAs(Role::getName, "roleName")
                .like(userGetRequestDTO.getUserName() != null, User::getUserName, userGetRequestDTO.getUserName())
                .like(userGetRequestDTO.getUserName() != null, User::getUserName, userGetRequestDTO.getUserName())
                .like(userGetRequestDTO.getRoleName() != null, Role::getName, userGetRequestDTO.getRoleName());
        IPage<UserDTO.UserListDTO> page = baseMapper.selectJoinPage(new Page<>(userGetRequestDTO.getPageNum(), userGetRequestDTO.getPageSize()), UserDTO.UserListDTO.class, queryWrapper);
        return Result.success(new PageDTO<>(page));
    }

    @Override
    public Result findUserById(Long userId) {
        MPJLambdaWrapper<User> queryWrapper = new MPJLambdaWrapper<User>();
        queryWrapper.select(User::getUserId, User::getUserName, User::getName, User::getPhone, User::getEmail, User::getInfo, User::getSex, User::getNickname)
                .select(UserRole::getRoleId)
                .selectAs(Role::getName, "roleName")
                .leftJoin(UserRole.class, UserRole::getUserId, User::getUserId)
                .leftJoin(Role.class, Role::getRoleId, UserRole::getRoleId)
                .eq(User::getUserId, userId);
        return Result.success(baseMapper.selectJoinOne(UserDTO.UserInfoDTO.class, queryWrapper));
    }

    @Override
    public Result<String> updateUserById(Long userId, UserDTO.UserUpdateDTO userUpdateDTO) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUserId, userId);
        User user = baseMapper.selectOne(queryWrapper);
        if (user == null) {
            return Result.error("用户不存在");
        }
        userUpdateDTO.updateUser(user);
        baseMapper.updateById(user);
        return Result.success("修改成功");
    }

    @Override
    public Result findPermissionBySelf() {
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = loginUser.getUser().getUserId();
        MPJLambdaWrapper<UserRole> queryWrapper = new MPJLambdaWrapper<UserRole>()
                .select(Permission::getKeyValue)
                .leftJoin(RolePermission.class, RolePermission::getRoleId, UserRole::getRoleId)
                .leftJoin(Permission.class, Permission::getPermissionId, RolePermission::getPermissionId)
                .eq(UserRole::getUserId, userId);
        List<PermissionEnum> permissions = UserRoleMapper.selectJoinList(PermissionEnum.class, queryWrapper);

        return Result.success(permissions);
    }



    @Override
    public Result<String> updateUserRole(Long userId, Long roleId) {
        LambdaQueryWrapper<UserRole> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserRole::getUserId, userId);

        LambdaQueryWrapper<Role> roleQueryWrapper = new LambdaQueryWrapper<>();
        roleQueryWrapper.eq(Role::getRoleId, roleId);
        Role role = roleMapper.selectOne(roleQueryWrapper);

        LambdaQueryWrapper<User> userQueryWrapper = new LambdaQueryWrapper<>();
        userQueryWrapper.eq(User::getUserId, userId);
        User user = baseMapper.selectOne(userQueryWrapper);
        if (user == null) {
            return Result.error("用户不存在");
        }
        if (role == null) {
            return Result.error("角色不存在");
        }


        UserRole UserRole = UserRoleMapper.selectOne(queryWrapper);
        if (UserRole == null) {
            UserRole = new UserRole();
            UserRole.setUserId(userId);
            UserRole.setRoleId(roleId);
            UserRoleMapper.insert(UserRole);
        } else {
            UserRole.setRoleId(roleId);
            UserRoleMapper.updateById(UserRole);
        }

        return Result.success("设置此用户为 " + role.getName() + " 成功");
    }


    @Override
    public Result changePassword(UserDTO.UserPasswordDTO userPassword) {
        // 获取SecurityContextHolder中的用户id
        UsernamePasswordAuthenticationToken authentication = (UsernamePasswordAuthenticationToken) SecurityContextHolder
                .getContext().getAuthentication();
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        // LoginUser loginUser = redisCache.getCurrentUser();
        Long userId = loginUser.getUser().getUserId();
        // 获取用户信息
        User user = baseMapper.selectById(userId);
        // 判断旧密码是否正确
        if (!passwordEncoder.matches(userPassword.getOldPassword(), user.getPassword())) {
            return Result.error("旧密码错误!");
        }
        // 判断两次密码是否一致
        if (!userPassword.checkPassword()) {
            return Result.error("两次密码不一致!");
        }
        // 修改密码
        user.setPassword(passwordEncoder.encode(userPassword.getNewPassword()));
        baseMapper.updateById(user);
        return Result.success("修改密码成功!", null);
    }

    @Override
    public Result getRebindEmailCode(UserDTO.UserEmailVerifyDTO userEmailVerifyDTO) {
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = loginUser.getUser().getUserId();

        String email = userEmailVerifyDTO.getEmail();
        // 如果是自己本来的邮箱
        if (baseMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUserId, userId).eq(User::getEmail, email)) != null) {
            return Result.error("请绑定新邮箱！");
        }
        if (baseMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getEmail, email)) != null) {
            return Result.error("邮箱已被绑定");
        }

        String code = RandomUtil.randomNumbers(6);
        // 将验证码存入redis
        redisCache.setCacheObject(RedisEnum.REBIND.getDesc() + email, code, 5, TimeUnit.MINUTES);
        // 发送邮件
        mailService.sendVerifyCoed("绑定邮箱验证码", code, email);

        return Result.success("验证码发送成功!");
    }

    @Override
    public Result verifyEmailCode(UserDTO.UserEmailVerifyDTO userEmailVerifyDTO) {
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = loginUser.getUser().getUserId();
        // 从redis中取出验证码
        String code = (String) redisCache.getCacheObject(RedisEnum.REBIND.getDesc() + userEmailVerifyDTO.getEmail());
        if (Objects.isNull(code)) {
            throw new ServiceException(HttpStatus.BAD_REQUEST.value(), "验证码已过期!");
        }
        // 判断验证码是否正确
        if (!code.equals(userEmailVerifyDTO.getCode())) {
            throw new ServiceException(HttpStatus.BAD_REQUEST.value(), "验证码错误!");
        }
        // 从redis中删除
        redisCache.deleteObject(RedisEnum.REBIND.getDesc() + userEmailVerifyDTO.getEmail());

        // 更新用户邮箱
        User user = baseMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUserId, userId));
        user.setEmail(userEmailVerifyDTO.getEmail());
        baseMapper.updateById(user);

        return Result.success("邮箱修改成功!");
    }

    @Override
    public Result<String> batchDeleteUsers(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Result.error("用户ID列表不能为空");
        }

        // 查询这些用户的角色信息，检查是否为默认角色
        MPJLambdaWrapper<User> queryWrapper = new MPJLambdaWrapper<>();
        queryWrapper.select(User::getUserId)
                .leftJoin(UserRole.class, UserRole::getUserId, User::getUserId)
                .leftJoin(Role.class, Role::getRoleId, UserRole::getRoleId)
                .in(User::getUserId, userIds)
                .eq(Role::getIsDefault, 1); // 只选择默认角色的用户

        List<User> usersToDelete = baseMapper.selectJoinList(User.class, queryWrapper);

        if (usersToDelete.isEmpty()) {
            return Result.error("没有找到符合条件的用户（角色为默认角色）");
        }

        // 获取符合条件的用户ID
        List<Long> validUserIds = usersToDelete.stream()
                .map(User::getUserId)
                .collect(Collectors.toList());

        // 检查是否存在关联的排班记录
        LambdaQueryWrapper<Schedule> scheduleQueryWrapper = new LambdaQueryWrapper<>();
        scheduleQueryWrapper.in(Schedule::getDoctorUserId, validUserIds);
        Long scheduleCount = scheduleMapper.selectCount(scheduleQueryWrapper);
        if (scheduleCount > 0) {
            return Result.error("不能删除这些用户，存在关联的排班记录");
        }

        // 逻辑删除这些用户
        LambdaQueryWrapper<User> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.in(User::getUserId, validUserIds);
        int deletedRows = baseMapper.delete(deleteWrapper);

        return Result.success("成功删除 " + deletedRows + " 个用户");
    }

    @Override
    public Result<String> deleteUserById(Long userId) {
        // 检查用户是否存在
        User user = baseMapper.selectById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }

        // 查询用户的角色信息，检查是否为默认角色
        MPJLambdaWrapper<UserRole> queryWrapper = new MPJLambdaWrapper<>();
        queryWrapper.select(Role::getIsDefault)
                .leftJoin(Role.class, Role::getRoleId, UserRole::getRoleId)
                .eq(UserRole::getUserId, userId);

        Role role = UserRoleMapper.selectJoinOne(Role.class, queryWrapper);
        if (role == null || role.getIsDefault() != 1) {
            return Result.error("不能删除此用户，只有默认角色的用户才能被删除");
        }

        // 检查是否存在关联的排班记录
        LambdaQueryWrapper<Schedule> scheduleQueryWrapper = new LambdaQueryWrapper<>();
        scheduleQueryWrapper.eq(Schedule::getDoctorUserId, userId);
        Long scheduleCount = scheduleMapper.selectCount(scheduleQueryWrapper);
        if (scheduleCount > 0) {
            return Result.error("不能删除此用户，存在关联的排班记录");
        }

        // 逻辑删除用户
        baseMapper.deleteById(userId);

        return Result.success("用户删除成功");
    }

}
