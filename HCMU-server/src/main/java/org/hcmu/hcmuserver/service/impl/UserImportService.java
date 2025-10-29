package org.hcmu.hcmuserver.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.hcmu.hcmucommon.exception.ServiceException;
import org.hcmu.hcmupojo.entity.User;
import org.hcmu.hcmuserver.mapper.user.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 用户导入服务实现类
 */
@Service("userImportService")
public class UserImportService extends AbstractImportServiceImpl<User, UserMapper> {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public String validateData(User user) {
        // 验证用户名
        if (StrUtil.isBlank(user.getUserName())) {
            return "用户名为必填项";
        }
        if (user.getUserName().length() > 50) {
            return "用户名长度不能超过50个字符";
        }

        // 检查用户名唯一性
        LambdaQueryWrapper<User> usernameWrapper = new LambdaQueryWrapper<>();
        usernameWrapper.eq(User::getUserName, user.getUserName());
        if (baseMapper.selectCount(usernameWrapper) > 0) {
            return StrUtil.format("用户名[{}]已存在", user.getUserName());
        }

        // 验证密码
        if (StrUtil.isBlank(user.getPassword())) {
            return "密码为必填项";
        }

        // 验证手机号
        if (StrUtil.isNotBlank(user.getPhone())) {
            if (user.getPhone().length() > 20) {
                return "手机号长度不能超过20个字符";
            }
            // 检查手机号唯一性
            LambdaQueryWrapper<User> phoneWrapper = new LambdaQueryWrapper<>();
            phoneWrapper.eq(User::getPhone, user.getPhone());
            if (baseMapper.selectCount(phoneWrapper) > 0) {
                return StrUtil.format("手机号[{}]已被绑定", user.getPhone());
            }
        }

        // 验证邮箱
        if (StrUtil.isNotBlank(user.getEmail())) {
            if (user.getEmail().length() > 100) {
                return "邮箱长度不能超过100个字符";
            }
            // 检查邮箱唯一性
            LambdaQueryWrapper<User> emailWrapper = new LambdaQueryWrapper<>();
            emailWrapper.eq(User::getEmail, user.getEmail());
            if (baseMapper.selectCount(emailWrapper) > 0) {
                return StrUtil.format("邮箱[{}]已被绑定", user.getEmail());
            }
        }

        // 验证性别
        if (user.getSex() != null) {
            try {
                int sex = Integer.parseInt(user.getSex());
                if (sex < 0 || sex > 2) {
                    return "性别只能是0(未知)、1(男)、2(女)";
                }
            } catch (NumberFormatException e) {
                return "性别格式错误，只能是数字";
            }
        }

        // 验证状态
        if (user.getStatus() == null) {
            return "账户状态为必填项";
        }
        if (user.getStatus() < 0 || user.getStatus() > 2) {
            return "账户状态只能是0(禁用)、1(正常)、2(待激活)";
        }

        // 验证逻辑删除标识
        if (user.getIsDeleted() == null) {
            user.setIsDeleted(0); // 默认未删除
        } else if (user.getIsDeleted() < 0 || user.getIsDeleted() > 1) {
            return "逻辑删除标识只能是0(未删除)、1(已删除)";
        }

        // 设置默认时间
        if (user.getCreateTime() == null) {
            user.setCreateTime(LocalDateTime.now());
        }
        if (user.getUpdateTime() == null) {
            user.setUpdateTime(LocalDateTime.now());
        }

        // 密码加密
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        return null; // 验证通过
    }
}