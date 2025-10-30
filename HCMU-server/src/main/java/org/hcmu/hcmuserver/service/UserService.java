package org.hcmu.hcmuserver.service;

import com.github.yulichang.base.MPJBaseService;
import org.hcmu.hcmucommon.result.Result;
import org.hcmu.hcmupojo.dto.PageDTO;
import org.hcmu.hcmupojo.dto.UserDTO;
import org.springframework.web.multipart.MultipartFile;
import org.hcmu.hcmupojo.entity.User;

/**
 * <p>
 *  用户服务接口
 * </p>
 *
 * @author Jiasheng Wang
 * @since 2024-03-20
 */

public interface UserService extends MPJBaseService<User> {

    /**
     * 创建用户
     * @return Result 返回信息
     */
    Result<PageDTO<UserDTO.UserListDTO>> findAllUsers(UserDTO.UserGetRequestDTO userGetRequestDTO);

    /**
     * 根据用户的信息
     * @param userId 用户id
     * @return Result 返回信息
     */
    Result findUserById(Long userId);

    /**
     * 修改用户信息
     * @param userId 用户id
     * @param userUpdate 用户信息
     * @return
     */
    Result<String> updateUserById(Long userId, UserDTO.UserUpdateDTO userUpdate);

    /**
     * 更新用户系统角色
     * @param userId
     * @param roleId
     * @return
     */
    Result<String> updateUserRole(Long userId, Long roleId);


    /**
     * 获取自己的权限信息
     * @return Result 返回信息
     */
    Result findPermissionBySelf();

    /**
     * 修改密码
     * @param userPassword 用户密码信息
     * @return Result 返回信息
     */
    Result changePassword(UserDTO.UserPasswordDTO userPassword);

    /**
     * 重新绑定邮箱
     */
    public  Result getRebindEmailCode(UserDTO.UserEmailVerifyDTO userEmailVerifyDTO);

    /**
     * 用户验证邮箱验证码
     * @param userEmailVerifyDTO 邮箱
     */
    Result verifyEmailCode(UserDTO.UserEmailVerifyDTO userEmailVerifyDTO);

    /**
     * 导入待注册用户
     * @param file 表格文件
     * @param roleId 角色ID
     * @return Result 返回信息
     */
    Result<String> importPendingUsers(MultipartFile file, Long roleId);

}
