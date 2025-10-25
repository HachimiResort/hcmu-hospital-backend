package org.hcmu.hcmuserver.controller;

import org.hcmu.hcmupojo.dto.UserDTO;
import org.hcmu.hcmupojo.dto.UserDTO.UserRegisterDTO;
import org.hcmu.hcmupojo.dto.UserDTO.UserLoginDTO;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.hcmu.hcmucommon.result.Result;
import org.hcmu.hcmucommon.annotation.AutoLog;
import org.hcmu.hcmucommon.annotation.Idempotent;
import org.hcmu.hcmuserver.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * 登录控制器
 * @Author Kyy008
 * @Date 2025-10-23
 */
@Tag(name = "用户登录注册相关接口", description = "用户登录注册相关接口")
@RestController
@RequestMapping("auth")
@Validated
public class AuthController {
    @Autowired
    private AuthService authService;

    @AutoLog("用户登录")
    @PostMapping("/login")
    @Operation(summary = "用户登录")
    @Idempotent
    public Result login(@RequestBody @Valid UserLoginDTO userLogin) {
        return authService.login(userLogin);
    }

    @AutoLog("用户退出")
    @PostMapping("/logout/{userId}")
    @Operation(summary = "用户退出")
    public Result logout(@PathVariable Long userId) {
        return authService.logout(userId);
    }

    @AutoLog("用户发起注册")
    @PostMapping("/register")
    @Operation(summary = "用户发起注册", description = "用户发起注册")
    @Idempotent
    public Result getRegisterCode(@RequestBody @Valid UserRegisterDTO userRegister) {
        return authService.getRegisterCode(userRegister);
    }

    @AutoLog("用户注册验证")
    @PostMapping("/register/verify")
    @Operation(summary = "用户注册验证", description = "用户注册验证")
    @Idempotent
    public Result verifyRegister(@RequestBody @Valid UserDTO.UserEmailVerifyDTO userEmailVerifyDTO) {
        return authService.verifyRegister(userEmailVerifyDTO);
    }

}
