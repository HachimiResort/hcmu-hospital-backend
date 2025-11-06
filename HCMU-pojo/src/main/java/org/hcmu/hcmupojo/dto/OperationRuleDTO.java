package org.hcmu.hcmupojo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hcmu.hcmupojo.entity.OperationRule;

import java.time.LocalDateTime;

@Data
public class OperationRuleDTO {
    @Data
    public  class OperationRuleGetRequestDTO {
        private Integer code;
    }

    @Data
    public class OperationRuleListDTO {
        private Long id;
        private Integer code;
        private String name;
        private Integer value;
        private String key;
        private String description;
        private Integer enabled;
        private LocalDateTime createTime;
        private LocalDateTime updateTime;
        private Integer isDeleted;
    }

    @Data
    public class OperationRuleUpdateDTO {
        @NotNull(message = "配置值不能为空")
        private Integer value;

        @NotNull(message = "启用状态不能为空")
        private Integer enabled;

        public void updateRuleDTO(OperationRule operationRule){
            if (value != null){
                operationRule.setValue(value);
            }
            if (enabled != null){
                operationRule.setEnabled(enabled);
            }
        }
    }
}
