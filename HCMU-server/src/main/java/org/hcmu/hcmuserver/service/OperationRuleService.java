package org.hcmu.hcmuserver.service;

import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.validation.Valid;
import org.hcmu.hcmucommon.annotation.RequestKeyParam;
import org.hcmu.hcmucommon.result.Result;
import org.hcmu.hcmupojo.dto.OperationRuleDTO;
import org.hcmu.hcmupojo.dto.RoleDTO;
import org.hcmu.hcmupojo.entity.OperationRule;


/**
 * <p>
 * 运营规则配置表 服务类
 * </p>
 */
public interface OperationRuleService extends IService<OperationRule> {
    public Result<String> updateOperationRuleByCode(@RequestKeyParam Integer code, OperationRuleDTO.@Valid OperationRuleUpdateDTO ruleUpdateDTO);
}
