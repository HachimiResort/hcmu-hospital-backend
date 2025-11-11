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

    /**
     * 校验规则修改是否违反现有数据约束
     * @param ruleEnum 规则枚举
     * @param newValue 新的规则值
     * @return 校验结果，如果违反约束则返回错误信息
     */
    Result<Void> validateRuleChange(OpRuleEnum ruleEnum, Integer newValue);
}
