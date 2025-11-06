package org.hcmu.hcmuserver.service;

import org.hcmu.hcmucommon.result.Result;
import org.hcmu.hcmupojo.dto.OperationRuleDTO;

import java.util.List;

public interface OperationRuleService {
    Result<List<OperationRuleDTO.RuleListDTO>> getAllRules();

    Result<OperationRuleDTO.RuleListDTO> updateRule(Integer code, OperationRuleDTO.RuleUpdateRequest request);
}
