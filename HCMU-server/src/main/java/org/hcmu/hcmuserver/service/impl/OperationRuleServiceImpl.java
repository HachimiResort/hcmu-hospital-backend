package org.hcmu.hcmuserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.yulichang.base.MPJBaseServiceImpl;
import com.github.yulichang.wrapper.MPJLambdaWrapper;

import io.swagger.v3.oas.models.security.SecurityScheme.In;
import lombok.extern.slf4j.Slf4j;
import org.hcmu.hcmucommon.enumeration.OpRuleEnum;
import org.hcmu.hcmucommon.result.Result;
import org.hcmu.hcmupojo.dto.OperationRuleDTO;
import org.hcmu.hcmupojo.dto.OperationRuleDTO.RuleInfo;
import org.hcmu.hcmupojo.entity.OperationRule;
import org.hcmu.hcmuserver.mapper.operationrule.OperationRuleMapper;
import org.hcmu.hcmuserver.service.OperationRuleService;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OperationRuleServiceImpl extends MPJBaseServiceImpl<OperationRuleMapper, OperationRule> implements OperationRuleService {

    @Override
    public Result<List<OperationRuleDTO.RuleListDTO>> getAllRules() {
        MPJLambdaWrapper<OperationRule> queryWrapper = new MPJLambdaWrapper<>();
        queryWrapper.selectAll(OperationRule.class);
        List<OperationRule> existingRules = baseMapper.selectList(queryWrapper);
        
        List<OperationRuleDTO.RuleListDTO> items = OperationRuleDTO.RuleListDTO.convertList(existingRules);
        return Result.success(items);
    }

    @Override
    public Result<OperationRuleDTO.RuleListDTO> updateRule(Integer code, OperationRuleDTO.RuleUpdateRequest request) {
        if (code == null) {
            return Result.error("规则编码不能为空");
        }

        OpRuleEnum ruleDefinition = Arrays.stream(OpRuleEnum.values())
                .filter(item -> item.getCode().equals(code))
                .findFirst()
                .orElse(null);
        if (ruleDefinition == null) {
            return Result.error("规则不存在");
        }

        if (request.getValue() != null) {
            Integer min = ruleDefinition.getMinValue();
            Integer max = ruleDefinition.getMaxValue();
            if (min != null && request.getValue() < min) {
                return Result.error("配置值不能小于 " + min);
            }
            if (max != null && request.getValue() > max) {
                return Result.error("配置值不能大于 " + max);
            }
        }

        MPJLambdaWrapper<OperationRule> queryWrapper = new MPJLambdaWrapper<>();
        queryWrapper.eq(OperationRule::getCode, code)
            .last("limit 1");

        OperationRule existing = baseMapper.selectOne(queryWrapper);
        if (existing == null) {
            baseMapper.insert(new OperationRule() {{
                setCode(code);
                setName(ruleDefinition.name());
                if (request.getValue() != null) {
                    setValue(request.getValue());
                } else {
                    setValue(ruleDefinition.getDefaultValue());
                }
                if (request.getEnabled() != null) {
                    setEnabled(request.getEnabled());
                } else {
                    setEnabled(1);
                }
            }});
            OperationRule created = baseMapper.selectOne(queryWrapper);
            return Result.success(OperationRuleDTO.RuleListDTO.convert(created));
        }

        request.updateRule(existing);


        baseMapper.updateById(existing);

        OperationRule refreshed = baseMapper.selectOne(queryWrapper);

        return Result.success(OperationRuleDTO.RuleListDTO.convert(refreshed));
    }

    @Override
    public RuleInfo getRuleValueByCode(OpRuleEnum opRuleEnum) {
        if (opRuleEnum == null) {
            return null;
        }

        MPJLambdaWrapper<OperationRule> queryWrapper = new MPJLambdaWrapper<>();
        queryWrapper.eq(OperationRule::getCode, opRuleEnum.getCode())
            .last("limit 1");

        OperationRule existing = baseMapper.selectOne(queryWrapper);
        if (existing != null) {
            return new RuleInfo() {{
                setValue(existing.getValue());
                setEnabled(existing.getEnabled());
            }};
        } else {
            Integer code = opRuleEnum.getCode();
            baseMapper.insert(new OperationRule() {{
                setCode(code);
                setName(opRuleEnum.name());
                setValue(opRuleEnum.getDefaultValue());
                setEnabled(1);
            }});
            return new RuleInfo() {{
                setValue(opRuleEnum.getDefaultValue());
                setEnabled(1);
            }};
        }
    }

}
