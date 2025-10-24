package org.hcmu.hcmuserver.expression;

import com.github.yulichang.wrapper.MPJLambdaWrapper;
import lombok.extern.slf4j.Slf4j;
import org.hcmu.hcmucommon.enumeration.PermissionEnum;
import org.hcmu.hcmupojo.LoginUser;
import org.hcmu.hcmupojo.entity.Permission;
import org.hcmu.hcmupojo.entity.User;
import org.hcmu.hcmuserver.mapper.user.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * <p>
 * 自定义权限验证表达式
 * </p>
 * 
 * @author ZhiChao Li
 * @since 2024-03-22
 */
@Component("ex")
@Slf4j
public class SysExpression {

    @Autowired
    private UserMapper userMapper;

    public boolean hasSysAuthority(PermissionEnum authority) {
        // 获取当前用户的权限
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<PermissionEnum> permissions = loginUser.getPermissions();
        // 判断用户权限集合中是否存在authority
        return permissions.contains(authority);
    }


    public boolean awaitFurtherAuthority() {
        return true;
    }

    public boolean isSelf(Long userId) {
        // 获取当前用户的id
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long id = loginUser.getUser().getUserId();
        // 判断传入的id是否是当前用户的id
        return id.equals(userId);
    }



}
