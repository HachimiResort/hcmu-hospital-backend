package org.hcmu.hcmupojo;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hcmu.hcmucommon.enumeration.PermissionEnum;
import org.hcmu.hcmupojo.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 该类为携带了用户权限信息的User类(是信息更为全面的User类)
 * 用于SpringSecurity的用户认证
 */
@Data
@NoArgsConstructor
public class LoginUser implements UserDetails {

    private User user;

    private List<PermissionEnum> permissions;

    public LoginUser(User user, List<PermissionEnum> permissions) {
        this.user = user;
        this.permissions = permissions;
    }

    @JSONField(serialize = false)
    private List<SimpleGrantedAuthority> authorities;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if(authorities!=null){
            return authorities;
        }
        //把permissions中String类型的权限信息封装成SimpleGrantedAuthority对象
       authorities = new ArrayList<>(0);
//        for (PermissionEnum permission : permissions) {
//            SimpleGrantedAuthority authority = new SimpleGrantedAuthority(permission.getName());
//            authorities.add(authority);
//        }
//        authorities = permissions.stream()
//                .map(SimpleGrantedAuthority::new)
//                .collect(Collectors.toList());

//        返回空列表 本项目使用自定义的权限认证函数 因此不需要springSecurity的权限列表
        return authorities;
//        return null;
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUserName();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
