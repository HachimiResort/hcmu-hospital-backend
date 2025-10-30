package org.hcmu.hcmuserver.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import java.util.List;

import org.hcmu.hcmucommon.annotation.AutoLog;
import org.hcmu.hcmucommon.result.Result;
import org.hcmu.hcmupojo.dto.PageDTO;
import org.hcmu.hcmupojo.dto.PendingUserDTO;
import org.hcmu.hcmuserver.service.PendingUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 待注册用户控制器
 * @Author Generated
 * @Date 2024-10-31
 */
@Tag(name = "待注册用户接口", description = "待注册用户相关接口")
@RestController
@RequestMapping("pending-users")
@Validated
public class PendingUserController {

    @Autowired
    private PendingUserService pendingUserService;

    @AutoLog("获取所有待注册用户")
    @Operation(description = "获取所有待注册用户", summary = "获取所有待注册用户")
    @GetMapping("")
    @PreAuthorize("@ex.hasSysAuthority('CHECK_MB')")
    public Result<PageDTO<PendingUserDTO.PendingUserListDTO>> getAllPendingUsers(@ModelAttribute PendingUserDTO.PendingUserGetRequestDTO requestDTO) {
        return pendingUserService.findAllPendingUsers(requestDTO);
    }

    @AutoLog("根据ID获取待注册用户信息")
    @Operation(description = "根据ID获取待注册用户信息", summary = "根据ID获取待注册用户信息")
    @GetMapping("/{id}")
    public Result<PendingUserDTO.PendingUserInfoDTO> getPendingUserById(@PathVariable Long id) {
        return pendingUserService.findPendingUserById(id);
    }

    @AutoLog("删除待注册用户")
    @Operation(description = "删除待注册用户", summary = "删除待注册用户")
    @DeleteMapping("/{id}")
    @PreAuthorize("@ex.hasSysAuthority('DEL_MB')")
    public Result<String> deletePendingUser(@PathVariable Long id) {
        return pendingUserService.deletePendingUser(id);
    }

    @AutoLog("批量删除待注册用户")
    @Operation(description = "批量删除待注册用户", summary = "批量删除待注册用户")
    @DeleteMapping("/batch")
    @PreAuthorize("@ex.hasSysAuthority('DEL_MB')")
    public Result<String> batchDeletePendingUsers(@RequestBody @Valid List<Long> ids) {
        return pendingUserService.batchDeletePendingUsers(ids);
    }

    @AutoLog("上传待注册用户")
    @Operation(description = "上传待注册用户", summary = "上传待注册用户")
    @PostMapping("/import")
    @PreAuthorize("@ex.hasSysAuthority('ADD_MB')")
    public Result<String> importPendingUsers(@RequestParam("file") MultipartFile file, @RequestParam("roleId") Long roleId) {
        return pendingUserService.importPendingUsers(file, roleId);
    }
}