package org.hcmu.hcmuserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.github.yulichang.base.MPJBaseService;
import com.github.yulichang.base.MPJBaseServiceImpl;
import com.github.yulichang.query.MPJLambdaQueryWrapper;
import com.github.yulichang.query.MPJQueryWrapper;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.hcmu.hcmucommon.annotation.RequestKeyParam;
import org.hcmu.hcmucommon.result.Result;
import org.hcmu.hcmupojo.dto.OperationRuleDTO;
import org.hcmu.hcmupojo.dto.RoleDTO;
import org.hcmu.hcmupojo.entity.OperationRule;
import org.hcmu.hcmupojo.entity.Role;
import org.hcmu.hcmuserver.mapper.operationrule.OperationRuleMapper;
import org.hcmu.hcmuserver.service.OperationRuleService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 运营规则配置表 服务实现类
 * </p>
 */
@Slf4j
@Service
public class OperationRuleServiceImpl extends MPJBaseServiceImpl<OperationRuleMapper, OperationRule> implements OperationRuleService {

    @Override
    public Result<String> updateOperationRuleByCode(@RequestKeyParam Integer code,@Valid OperationRuleDTO.OperationRuleUpdateDTO ruleUpdateDTO) {
        if (code == null) {
            return Result.error("配置代号不能为空");
        }
        // 根据code查询规则
        MPJLambdaWrapper<OperationRule> wrapper = new MPJLambdaWrapper<>();
        wrapper.eq(OperationRule::getCode, code)
                .eq(OperationRule::getIsDeleted, 0);
        OperationRule operationRule = baseMapper.selectOne(wrapper);

        if (operationRule == null) {
            return Result.error("规则不存在，配置代号: " + code);
        }
        // 使用DTO更新实体
        ruleUpdateDTO.updateRuleDTO(operationRule);

        baseMapper.updateById(operationRule);
        return Result.success("修改成功");
    }

}
