package org.hcmu.hcmuserver.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.hcmu.hcmucommon.annotation.AutoLog;
import org.hcmu.hcmucommon.result.Result;
import org.hcmu.hcmupojo.dto.AppointmentDTO;
import org.hcmu.hcmupojo.dto.PageDTO;
import org.hcmu.hcmupojo.dto.RoleDTO;
import org.hcmu.hcmupojo.dto.UserDTO;
import org.hcmu.hcmuserver.service.AppointmentService;
import org.hcmu.hcmuserver.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户控制器
 * @Author Kyy008
 * @Date 2024-10-24
 */
@Tag(name = "用户接口", description = "用户相关接口")
@RestController
@RequestMapping("users")
@Validated
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private AppointmentService appointmentService;

    // TODO: 开放获取所有成员的权限
    // 获取所有用户的信息
    @AutoLog("获取所有用户的信息")
    @Operation(description = "获取所有用户的信息", summary = "获取所有用户的信息")
    @GetMapping("")
    public Result<PageDTO<UserDTO.UserListDTO>> getAllUsers(@ModelAttribute UserDTO.UserGetRequestDTO userGetRequestDTO) {
        return userService.findAllUsers(userGetRequestDTO);
    }

    // 根据用户ID获取单个用户的信息
    @AutoLog("根据个人信息")
    @Operation(description = "根据个人信息", summary = "根据个人信息")
    @GetMapping("/{userId}")
    public Result getUserById(@PathVariable Long userId) {
        return userService.findUserById(userId);
    }

    @AutoLog("修改用户信息")
    @Operation(description = "修改用户信息", summary = "修改用户信息('ALT_MB') || isSelf")
    @PutMapping("/{userId}")
    @PreAuthorize("@ex.hasSysAuthority('ALT_MB') || @ex.isSelf(#userId)")
    public Result<String> updateUserById(@PathVariable Long userId, @RequestBody @Valid UserDTO.UserUpdateDTO userUpdate) {
        return userService.updateUserById(userId, userUpdate);
    }

    @AutoLog("获取权限信息")
    @Operation(description = "获取权限信息", summary = "获取权限信息")
    @GetMapping("/permissions")
    public Result getPermissions() {
        return userService.findPermissionBySelf();
    }


    @AutoLog("设置用户角色")
    @Operation(description = "设置用户角色", summary = "设置用户角色('ALT_MB')")
    @PutMapping("/{userId}/role")
    @PreAuthorize("@ex.hasSysAuthority('ALT_MB')")
    public Result<String> updateUserRole(@PathVariable Long userId, @RequestBody @Valid RoleDTO.RoleUserUpdateDTO roleUserUpdateDTO) {
        return userService.updateUserRole(userId, roleUserUpdateDTO.getRoleId());
    }

    @AutoLog("修改密码")
    @PostMapping("/password")
    @Operation(summary = "修改密码")
    public Result changePassword(@RequestBody @Valid UserDTO.UserPasswordDTO userPassword) {
        return userService.changePassword(userPassword);
    }

    @AutoLog("申请重新绑定邮箱")
    @Operation(description = "申请重新绑定邮箱", summary = "申请重新绑定邮箱")
    @PostMapping("/email")
    public Result applyUpdateEmail(@RequestBody @Valid UserDTO.UserEmailVerifyDTO userEmailVerifyDTO) {
        return userService.getRebindEmailCode(userEmailVerifyDTO);
    }

    @AutoLog("绑定邮箱验证")
    @Operation(description = "绑定邮箱验证", summary = "绑定邮箱验证")
    @PostMapping("/email/verify")
    public Result verifyEmailCode(@RequestBody @Valid UserDTO.UserEmailVerifyDTO userEmailVerifyDTO) {
        return userService.verifyEmailCode(userEmailVerifyDTO);
    }

    @AutoLog("批量删除用户")
    @Operation(description = "批量删除用户（逻辑删除）", summary = "批量删除用户('DEL_USER')")
    @DeleteMapping("/batch")
    @PreAuthorize("@ex.hasSysAuthority('DEL_MB')")
    public Result<String> batchDeleteUsers(@RequestBody List<Long> userIds) {
        return userService.batchDeleteUsers(userIds);
    }

    @AutoLog("删除用户（逻辑删除）")
    @Operation(description = "删除用户（逻辑删除）", summary = "删除用户('DEL_USER')")
    @DeleteMapping("/{userId}")
    @PreAuthorize("@ex.hasSysAuthority('DEL_MB')")
    public Result<String> deleteUser(@PathVariable Long userId) {
        return userService.deleteUserById(userId);
    }

    @AutoLog("根据用户id查找预约")
    @Operation(description = "根据用户id查找预约", summary = "根据用户id查找预约('CHECK_APPOINTMENT')")
    @GetMapping("/{userId}/appointment")
    @PreAuthorize("@ex.hasSysAuthority('CHECK_APPOINTMENT') || isSelf")
    public Result<PageDTO<AppointmentDTO.AppointmentListDTO>> getAppointmentByPatientId(@PathVariable Long userId, @ModelAttribute AppointmentDTO.AppointmentGetRequestDTO appointmentGetRequestDTO) {
        appointmentGetRequestDTO.setPatientUserId(userId);
        return appointmentService.getAppointments(appointmentGetRequestDTO);
    }
}
