package org.hcmu.hcmuserver.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.hcmu.hcmucommon.result.Result;
import org.hcmu.hcmupojo.dto.LogDTO;
import org.hcmu.hcmuserver.service.LogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 日志控制器
 * @author JiaSheng Wang
 * @since 2024/5/15
 */
@Tag(name = "日志接口", description = "日志相关接口")
@RestController
@RequestMapping("logs")
@Validated
public class LogController {

    @Autowired
    private LogService logService;

    // 查找日志
    @Operation(summary = "查找日志('LOG_PAGE')", description = "查找日志")
    @GetMapping("")
    @PreAuthorize("@ex.hasSysAuthority('LOG_PAGE')")
    public Result<PageDTO<LogDTO.LogListDTO>> findLogs(@ModelAttribute LogDTO.LogGetRequestDTO logGetRequestDTO) {
        return logService.findLogs(logGetRequestDTO);
    }
}
