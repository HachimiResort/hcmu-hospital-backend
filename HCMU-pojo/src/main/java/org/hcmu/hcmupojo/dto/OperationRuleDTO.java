package org.hcmu.hcmupojo.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;
import org.hcmu.hcmucommon.enumeration.OpRuleEnum;
import org.hcmu.hcmupojo.entity.OperationRule;

import java.time.LocalDateTime;
import java.util.List;

public class OperationRuleDTO {

    @Data
    public static class RuleListDTO {
        private Integer code;
        private String name;
        private String key;
        private String description;
        private OpRuleEnum.OpRuleType type;
        private Integer value;
        private Integer defaultValue;
        private Integer enabled;
        private Integer minValue;
        private Integer maxValue;
        private LocalDateTime updateTime;

        static public RuleListDTO convert(OperationRule rule) {
            RuleListDTO dto = new RuleListDTO();
            dto.setCode(rule.getCode());
            dto.setName(rule.getName());
            dto.setKey(rule.getKey());
            dto.setDescription(rule.getDescription());
            dto.setValue(rule.getValue());
            dto.setEnabled(rule.getEnabled());
            dto.setUpdateTime(rule.getUpdateTime());

            OpRuleEnum ruleEnum = OpRuleEnum.getByCode(rule.getCode());
            if (ruleEnum != null) {
                dto.setType(ruleEnum.getType());
                dto.setDefaultValue(ruleEnum.getDefaultValue());
                dto.setMinValue(ruleEnum.getMinValue());
                dto.setMaxValue(ruleEnum.getMaxValue());
            }
            return dto;
        }

        static public List<RuleListDTO> convertList(List<OperationRule> rules) {
            return rules.stream().map(RuleListDTO::convert).toList();
        }
    }

    @Data
    public static class RuleUpdateRequest {
        private Integer value;

        @Min(0)
        @Max(1)
        private Integer enabled;

        public void updateRule(OperationRule rule) {
            if (this.value != null) {
                rule.setValue(this.value);
            }
            if (this.enabled != null) {
                rule.setEnabled(this.enabled);
            }
        }
    }
}
