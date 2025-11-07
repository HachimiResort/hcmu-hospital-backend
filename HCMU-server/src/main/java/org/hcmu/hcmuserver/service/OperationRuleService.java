package org.hcmu.hcmuserver.service;

import org.hcmu.hcmucommon.enumeration.OpRuleEnum;
import org.hcmu.hcmucommon.result.Result;
import org.hcmu.hcmupojo.dto.OperationRuleDTO;
import org.hcmu.hcmupojo.dto.OperationRuleDTO.RuleInfo;

import java.util.List;

public interface OperationRuleService {
    Result<List<OperationRuleDTO.RuleListDTO>> getAllRules();

    Result<OperationRuleDTO.RuleListDTO> updateRule(Integer code, OperationRuleDTO.RuleUpdateRequest request);

    RuleInfo getRuleValueByCode(OpRuleEnum opRuleEnum);
}
