package org.hcmu.hcmuserver.service;

import com.github.yulichang.base.MPJBaseService;
import org.hcmu.hcmucommon.result.Result;
import org.hcmu.hcmupojo.dto.UserDTO;
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
    Result login(UserDTO.UserLoginDTO userLogin);
}
