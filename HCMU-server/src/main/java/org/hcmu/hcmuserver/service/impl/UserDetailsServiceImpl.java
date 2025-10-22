package org.hcmu.hcmuserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import com.github.yulichang.wrapper.MPJLambdaWrapper;
import lombok.extern.slf4j.Slf4j;
import org.hcmu.hcmucommon.enumeration.PermissionEnum;
import org.hcmu.hcmucommon.exception.ServiceException;
import org.hcmu.hcmupojo.LoginUser;
import org.hcmu.hcmupojo.entity.Permission;
import org.hcmu.hcmupojo.entity.User;
import org.hcmu.hcmupojo.entity.relation.RolePermission;
import org.hcmu.hcmupojo.entity.relation.UserRole;
import org.hcmu.hcmuserver.mapper.user.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * 是spring security框架内的组件,用于用户身份认证
 * 将会在ProviderManage中被DaoAuthenticationProvider调用
 */
@Service
@Slf4j
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserMapper userMapper;


    @Override
    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {

        //查询用户信息
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUserName,userName);
        User user = userMapper.selectOne(queryWrapper);
        //如果没有查询到用户就抛出异常
        if(Objects.isNull(user)){
            throw new ServiceException("用户不存在!");
        }

        //查询用户权限信息
        MPJLambdaWrapper<User> permissionQueryWrapper = new MPJLambdaWrapper<User>()
                .select(Permission::getKeyValue)
                .leftJoin(UserRole.class, UserRole::getUserId,User::getUserId)
                .leftJoin(RolePermission.class, RolePermission::getRoleId, UserRole::getRoleId)
                .leftJoin(Permission.class, Permission::getPermissionId, RolePermission::getPermissionId)
                .eq(User::getUserName, userName);
        List<PermissionEnum> list = userMapper.selectJoinList(PermissionEnum.class,permissionQueryWrapper);
        //把数据封装成UserDetails返回

        return new LoginUser(user,list);
    }
}
