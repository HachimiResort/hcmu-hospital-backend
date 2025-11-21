package org.hcmu.hcmupojo.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hcmu.hcmupojo.entity.Schedule;
import org.hcmu.hcmupojo.entity.ScheduleTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ScheduleTemplateDTO {

    @Data
    public static class TemplateGetRequestDTO {
        private Integer pageNum = 1;
        private Integer pageSize = 10;
        private String templateName;
    }

    @Data
    public static class TemplateListDTO {
        @NotNull(message = "排班模板ID不能为空")
        private Long templateId;
        @NotNull(message = "模板名称不能为空")
        private String templateName;
        @NotNull(message = "创建时间不能为空")
        private LocalDateTime createTime;
        @NotNull(message = "更新时间不能为空")
        private LocalDateTime updateTime;
    }

    @Data
    public static class TemplateCreateDTO {
        @NotBlank(message = "模板名称不能为空")
        private String templateName;
    }

    @Data
    public static class TemplateUpdateDTO {
        private String templateName;

        public void updateTemplate(ScheduleTemplate template) {
            if (templateName != null && !templateName.trim().isEmpty()) {
                template.setTemplateName(templateName);
            }
            template.setUpdateTime(LocalDateTime.now());
        }
    }

    @Data
    public static class TemplateScheduleGetRequestDTO {
        private Integer pageNum = 1;
        private Integer pageSize = 10;
        @Min(value = 1, message = "号别取值范围为1-3")
        @Max(value = 3, message = "号别取值范围为1-3")
        private Integer slotType;
        @Min(value = 1, message = "时间段取值范围为1-12")
        @Max(value = 12, message = "时间段取值范围为1-12")
        private Integer slotPeriod;
        private Integer weekday;
    }

    @Data
    public static class TemplateScheduleListDTO {
        private Long scheduleId;
        private Long templateId;
        private Integer slotType;
    
        private Integer totalSlots;

        private Integer weekday;
    
        private Integer slotPeriod;
    
        private BigDecimal fee;
    
        private LocalDateTime createTime;
    
        private LocalDateTime updateTime;
    }

    @Data
    public static class TemplateScheduleCreateDTO {
        @NotNull(message = "号别不能为空")
        @Min(value = 1, message = "号别取值范围为1-3")
        @Max(value = 3, message = "号别取值范围为1-3")
        private Integer slotType;
        @NotNull(message = "总号源数不能为空")
        @Min(value = 1, message = "总号源数必须大于0")
        private Integer totalSlots;
        @NotNull(message = "时间段不能为空")
        @Min(value = 1, message = "时间段取值范围为1-12")
        @Max(value = 12, message = "时间段取值范围为1-12")
        private Integer slotPeriod;
        @NotNull(message = "挂号费不能为空")
        @DecimalMin(value = "0.0", message = "挂号费不能为负数")
        private BigDecimal fee;

        @NotNull(message = "星期几不能为空")
        @Min(value = 1, message = "星期几取值范围为1-7")
        @Max(value = 7, message = "星期几取值范围为1-7")
        private Integer weekday;
    }

    @Data
    public static class TemplateScheduleUpdateDTO {
        @Min(value = 1, message = "号别取值范围为1-3")
        @Max(value = 3, message = "号别取值范围为1-3")
        private Integer slotType;
        @Min(value = 1, message = "总号源数必须大于0")
        private Integer totalSlots;
        @Min(value = 1, message = "时间段取值范围为1-12")
        @Max(value = 12, message = "时间段取值范围为1-12")
        private Integer slotPeriod;
        @DecimalMin(value = "0.0", message = "挂号费不能为负数")
        private BigDecimal fee;

        @Min(value = 1, message = "星期几取值范围为1-7")
        @Max(value = 7, message = "星期几取值范围为1-7")
        private Integer weekday;

        public void updateSchedule(Schedule schedule) {
            if (slotType != null) {
                schedule.setSlotType(slotType);
            }
            if (totalSlots != null) {
                schedule.setTotalSlots(totalSlots);
            }
            if (slotPeriod != null) {
                schedule.setSlotPeriod(slotPeriod);
            }
            if (fee != null) {
                schedule.setFee(fee);
            }
            if (weekday != null) {
                schedule.setWeekday(weekday);
            }
            schedule.setUpdateTime(LocalDateTime.now());
        }
    }
}
