package org.hcmu.hcmuserver.service;

import com.github.yulichang.base.MPJBaseService;
import jakarta.validation.Valid;
import org.hcmu.hcmucommon.result.Result;
import org.hcmu.hcmupojo.dto.RoleDTO;
import org.hcmu.hcmupojo.dto.RoleDTO.RoleListDTO;
import org.hcmu.hcmupojo.dto.RoleDTO.RoleCreateDTO;
import org.hcmu.hcmupojo.entity.Role;

import java.util.List;

public interface RoleService extends MPJBaseService<Role> {
    /**
     * 创建角色
     *
     * @param roleCreateDTO 角色信息
     * @return BaseResponse 返回信息
     */
    Result<RoleListDTO> createRole(RoleCreateDTO roleCreateDTO);

    /**
     * 获取所有角色
     *
     * @return BaseResponse 返回信息
     */
    Result<List<RoleListDTO>> findAllRoles(RoleDTO.RoleGetRequestDTO roleGetRequestDTO);

    /**
     * 获取所有权限
     *
     * @return BaseResponse 返回信息
     */
    Result<List<RoleDTO.PermissionListDTO>> findAllPermissions();

    /**
     * 根据角色的信息
     *
     * @param roleId 角色id
     * @return BaseResponse 返回信息
     */
    Result<List<RoleDTO.PermissionListDTO>> findRolePermissionById(Long roleId);

    /**
     * 修改角色信息
     *
     * @param roleId     角色id
     * @param roleUpdate 角色信息
     * @return
     */
    Result<String> updateRoleById(Long roleId, RoleDTO.@Valid RoleUpdateDTO roleUpdate);

    /**
     * 修改角色权限
     */
    Result<String> updateRolePermissionById(Long roleId, @Valid List<RoleDTO.PermissionUpdateDTO> permissionList);

    /**
     * 删除角色
     */
    Result<String> deleteRoleById(Long roleId);
}
