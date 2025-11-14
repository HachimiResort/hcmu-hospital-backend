package org.hcmu.hcmuserver.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.hcmu.hcmucommon.annotation.AutoLog;
import org.hcmu.hcmucommon.result.Result;
import org.hcmu.hcmupojo.dto.RoleDTO;
import org.hcmu.hcmupojo.dto.RoleDTO.PermissionListDTO;
import org.hcmu.hcmupojo.dto.RoleDTO.RoleGetRequestDTO;
import org.hcmu.hcmupojo.dto.RoleDTO.RoleListDTO;
import org.hcmu.hcmupojo.dto.RoleDTO.RoleCreateDTO;
import org.hcmu.hcmupojo.dto.RoleDTO.RoleUpdateDTO;
import org.hcmu.hcmupojo.dto.RoleDTO.PermissionUpdateDTO;
import org.hcmu.hcmuserver.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色控制器
 *
 * @Author Kyy008
 * @Date 2025-11-14
 */
@Tag(name = "角色接口", description = "角色相关接口")
@RestController
@RequestMapping("roles")
@Validated
@Slf4j
public class RoleController {

    @Autowired
    private RoleService roleService;

    @AutoLog("创建新角色")
    @Operation(description = "创建新角色", summary = "创建新角色('MASTER_ROLE')")
    @PostMapping("")
    @PreAuthorize("@ex.hasSysAuthority('MASTER_ROLE')")
    public Result<RoleListDTO> createRole(@RequestBody @Valid RoleCreateDTO roleCreate) {
        return roleService.createRole(roleCreate);
    }

    @AutoLog("获取所有角色")
    @Operation(description = "获取所有角色", summary = "获取所有角色")
    @GetMapping("")
    public Result<List<RoleListDTO>> getAllRoles(@ModelAttribute RoleGetRequestDTO roleGetRequestDTO) {
        return roleService.findAllRoles(roleGetRequestDTO);
    }

    @AutoLog("获取所有权限信息")
    @Operation(description = "获取所有权限信息", summary = "获取所有权限信息('MASTER_ROLE')")
    @GetMapping("/permissions")
    @PreAuthorize("@ex.hasSysAuthority('MASTER_ROLE')")
    public Result<List<PermissionListDTO>> getAllPermissions() {
        return roleService.findAllPermissions();
    }

    @AutoLog("获取角色的权限信息")
    @Operation(description = "获取角色的权限信息", summary = "获取角色的权限信息('MASTER_ROLE')")
    @GetMapping("/{roleId}/permissions")
    @PreAuthorize("@ex.hasSysAuthority('MASTER_ROLE')")
    public Result<List<PermissionListDTO>> getRolePermissions(@PathVariable Long roleId) {
        log.info("根据角色ID查权限信息Controller: {}", roleId);
        return roleService.findRolePermissionById(roleId);
    }

    @AutoLog("更新角色信息")
    @Operation(description = "更新角色信息", summary = "更新角色信息('MASTER_ROLE')")
    @PutMapping("/{roleId}")
    @PreAuthorize("@ex.hasSysAuthority('MASTER_ROLE')")
    public Result<String> updateRole(@PathVariable Long roleId, @RequestBody @Valid RoleUpdateDTO roleUpdate) {
        return roleService.updateRoleById(roleId, roleUpdate);
    }

    @AutoLog("更新角色的权限信息")
    @Operation(description = "更新角色的权限信息", summary = "更新角色的权限信息('MASTER_ROLE')")
    @PutMapping("/{roleId}/permissions")
    @PreAuthorize("@ex.hasSysAuthority('MASTER_ROLE')")
    public Result<String> updateRolePermissions(@PathVariable Long roleId,
                                                      @RequestBody @Valid List<PermissionUpdateDTO> permissionList) {
        return roleService.updateRolePermissionById(roleId, permissionList);
    }

    @AutoLog("删除角色")
    @Operation(description = "删除角色", summary = "删除角色('MASTER_ROLE')")
    @DeleteMapping("/{roleId}")
    @PreAuthorize("@ex.hasSysAuthority('MASTER_ROLE')")
    public Result<String> deleteRole(@PathVariable Long roleId) {
        return roleService.deleteRoleById(roleId);
    }
}
