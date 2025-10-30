package org.hcmu.hcmuserver.service;

import com.github.yulichang.base.MPJBaseService;

import java.util.List;

import org.hcmu.hcmucommon.result.Result;
import org.hcmu.hcmupojo.dto.PageDTO;
import org.hcmu.hcmupojo.dto.PendingUserDTO;
import org.hcmu.hcmupojo.entity.PendingUser;
import org.springframework.web.multipart.MultipartFile;

/**
 * <p>
 *  待注册用户服务接口
 * </p>
 *
 * @author Generated
 * @since 2024-10-31
 */
public interface PendingUserService extends MPJBaseService<PendingUser> {

    /**
     * 获取所有待注册用户
     * @param requestDTO 请求参数
     * @return Result 返回信息
     */
    Result<PageDTO<PendingUserDTO.PendingUserListDTO>> findAllPendingUsers(PendingUserDTO.PendingUserGetRequestDTO requestDTO);

    /**
     * 根据ID获取待注册用户信息
     * @param id 待注册用户ID
     * @return Result 返回信息
     */
    Result<PendingUserDTO.PendingUserInfoDTO> findPendingUserById(Long id);

    /**
     * 删除待注册用户
     * @param id 待注册用户ID
     * @return Result 返回信息
     */
    Result<String> deletePendingUser(Long id);

    /**
     * 导入待注册用户
     * @param file 表格文件
     * @param roleId 角色ID
     * @return Result 返回信息
     */
    Result<String> importPendingUsers(MultipartFile file, Long roleId);

    /**
     * 批量删除待注册用户
     * @param ids 待注册用户ID数组
     * @return Result 返回信息
     */
    public Result<String> batchDeletePendingUsers(List<Long> ids);
}