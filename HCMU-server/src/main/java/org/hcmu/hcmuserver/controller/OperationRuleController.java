package org.hcmu.hcmuserver.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.hcmu.hcmucommon.annotation.AutoLog;
import org.hcmu.hcmucommon.annotation.RequestKeyParam;
import org.hcmu.hcmucommon.result.Result;
import org.hcmu.hcmupojo.dto.OperationRuleDTO;
import org.hcmu.hcmupojo.entity.OperationRule;
import org.hcmu.hcmuserver.service.OperationRuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "运营规则接口", description = "运营规则相关接口")
@RestController
@RequestMapping("rules")
@Validated
public class OperationRuleController {

    @Autowired
    private OperationRuleService operationRuleService;

    @AutoLog("修改运营规则")
    @Operation(description = "修改运营规则", summary = "根据规则编码修改运营规则('UPDATE_RULE')")
    @PutMapping("{code}")
    @PreAuthorize("@ex.hasSysAuthority('UPDATE_RULE')")
    public Result<String> updateOperationRuleById(@RequestKeyParam Integer code,@Valid OperationRuleDTO.OperationRuleUpdateDTO ruleUpdateDTO){
        return operationRuleService.updateOperationRuleByCode(code, ruleUpdateDTO);
    }

}
