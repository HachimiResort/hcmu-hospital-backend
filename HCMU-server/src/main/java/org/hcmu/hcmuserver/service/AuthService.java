package org.hcmu.hcmuserver.service;

import com.github.yulichang.base.MPJBaseService;
import org.hcmu.hcmucommon.result.Result;
import org.hcmu.hcmupojo.dto.UserDTO.UserEmailVerifyDTO;
import org.hcmu.hcmupojo.dto.UserDTO.UserRegisterDTO;
import org.hcmu.hcmupojo.dto.UserDTO.UserLoginDTO;
import org.hcmu.hcmupojo.entity.User;

/**
 *  用户登录注册相关接口
 *
 * @author Kyy008
 * @since 2025-10-23
 */
public interface AuthService extends MPJBaseService<User> {
    /**
     * 用户登录
     * @param userLogin 用户登录信息
     * @return BaseResponse 返回信息
     */
    Result login(UserLoginDTO userLogin);

    /**
     * 用户退出
     * @return BaseResponse 返回信息
     */
    Result logout(Long userId);

    /**
     * 用户注册
     * @param userRegister 用户注册信息
     * @return BaseResponse 返回信息
     */
    Result getRegisterCode(UserRegisterDTO userRegister);

    /*
     * 用户注册验证
     * @param email 邮箱
     */
    Result verifyRegister(UserEmailVerifyDTO userEmailVerifyDTO);
}
