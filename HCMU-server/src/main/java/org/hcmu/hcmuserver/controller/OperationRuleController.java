package org.hcmu.hcmuserver.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.hcmu.hcmucommon.annotation.AutoLog;
import org.hcmu.hcmucommon.result.Result;
import org.hcmu.hcmupojo.dto.OperationRuleDTO;
import org.hcmu.hcmuserver.service.OperationRuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "运营规则接口", description = "运营规则配置相关接口")
@RestController
@RequestMapping("rules")
@Validated
public class OperationRuleController {

    @Autowired
    private OperationRuleService operationRuleService;

    @AutoLog("查询运营规则列表")
    @Operation(summary = "查询运营规则列表", description = "查询运营规则列表（CHECK_RULE）")
    @GetMapping("")
    @PreAuthorize("@ex.hasSysAuthority('CHECK_RULE')")
    public Result<List<OperationRuleDTO.RuleListDTO>> getAllRules() {
        return operationRuleService.getAllRules();
    }

    @AutoLog("更新运营规则")
    @Operation(summary = "更新运营规则", description = "根据配置代号更新运营规则（ALT_RULE）")
    @PutMapping("/{code}")
    @PreAuthorize("@ex.hasSysAuthority('ALT_RULE')")
    public Result<OperationRuleDTO.RuleListDTO> updateRule(
            @PathVariable Integer code,
            @RequestBody @Valid OperationRuleDTO.RuleUpdateRequest request) {
        return operationRuleService.updateRule(code, request);
    }
}
