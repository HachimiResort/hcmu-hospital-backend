package org.hcmu.hcmupojo.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import lombok.Data;
import org.hcmu.hcmupojo.entity.Schedule;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ScheduleDTO {
    // 分页查询请求
    @Data
    public static class ScheduleGetRequestDTO {
        private Integer pageNum = 1;
        private Integer pageSize = 10;
        private Long doctorUserId;
        private LocalDate scheduleStartDate; // 查询起始日期
        private LocalDate scheduleEndDate;   // 查询终止日期 
        @Min(value = 1, message = "号别取值范围为1-3")
        @Max(value = 3, message = "号别取值范围为1-3")
        private Integer slotType; 
        @Min(value = 1, message = "时间段取值范围为1-12")
        @Max(value = 12, message = "时间段取值范围为1-12")
        private Integer slotPeriod; 
        private Integer status; 
    }

    // 列表展示
    @Data
    public static class ScheduleListDTO {
        @NotNull(message = "排班ID不能为空")
        private Long scheduleId;
        @NotNull(message = "医生用户ID不能为空")
        private Long doctorUserId;
        @NotNull(message = "出诊日期不能为空")
        private LocalDate scheduleDate;
        @NotNull(message = "号别不能为空")
        @Min(value = 1, message = "号别取值范围为1-3")
        @Max(value = 3, message = "号别取值范围为1-3")
        private Integer slotType;
        @NotNull(message = "时间段不能为空")
        @Min(value = 1, message = "时间段取值范围为1-12")
        @Max(value = 12, message = "时间段取值范围为1-12")
        private Integer slotPeriod;
        @NotNull(message = "总号源数不能为空")
        private Integer totalSlots;
        @NotNull(message = "可用号源数不能为空")
        private Integer availableSlots;
        @NotNull(message = "挂号费不能为空")
        private BigDecimal fee;
        @NotNull(message = "排班状态不能为空")
        private Integer status;
        @NotNull(message = "创建时间不能为空")
        private LocalDateTime createTime;
    }

    // 创建请求
    @Data
    public static class ScheduleCreateDTO {
        @NotNull(message = "医生用户ID不能为空")
        private Long doctorUserId;
        @NotNull(message = "出诊日期不能为空")
        private LocalDate scheduleDate;
        @NotNull(message = "号别不能为空")
        @Min(value = 1, message = "号别取值范围为1-3")
        @Max(value = 3, message = "号别取值范围为1-3")
        private Integer slotType;
        @NotNull(message = "时间段不能为空")
        @Min(value = 1, message = "时间段取值范围为1-12")
        @Max(value = 12, message = "时间段取值范围为1-12")
        private Integer slotPeriod;
        @NotNull(message = "总号源数不能为空")
        @Min(value = 1, message = "总号源数必须大于0")
        private Integer totalSlots;
        @NotNull(message = "挂号费不能为空")
        @DecimalMin(value = "0.0", message = "挂号费不能为负数")
        private BigDecimal fee;
        @NotNull(message = "排班状态不能为空")
        private Integer status = 1; // 默认为开放状态
    }

    // 更新请求
    @Data
    public static class ScheduleUpdateDTO {
        private Long doctorUserId;
        private LocalDate scheduleDate;
        @Min(value = 1, message = "号别取值范围为1-3")
        @Max(value = 3, message = "号别取值范围为1-3")
        private Integer slotType;
        @Min(value = 1, message = "时间段取值范围为1-12")
        @Max(value = 12, message = "时间段取值范围为1-12")
        private Integer slotPeriod;
        @Min(value = 1, message = "总号源数必须大于0")
        private Integer totalSlots;
        @Min(value = 0, message = "可用号源数不能为负数")
        private Integer availableSlots;
        @DecimalMin(value = "0.0", message = "挂号费不能为负数")
        private BigDecimal fee;
        private Integer status;

        public void updateSchedule(Schedule schedule) {
            if (doctorUserId != null && !doctorUserId.toString().trim().isEmpty()) schedule.setDoctorUserId(doctorUserId);
            if (scheduleDate != null && !scheduleDate.toString().trim().isEmpty()) schedule.setScheduleDate(scheduleDate);
            if (slotType != null && !slotType.toString().trim().isEmpty()) schedule.setSlotType(slotType);
            if (slotPeriod != null && !slotPeriod.toString().trim().isEmpty()) schedule.setSlotPeriod(slotPeriod);
            if (totalSlots != null && !totalSlots.toString().trim().isEmpty()) schedule.setTotalSlots(totalSlots);
            if (availableSlots != null && !availableSlots.toString().trim().isEmpty()) schedule.setAvailableSlots(availableSlots);
            if (fee != null && !fee.toString().trim().isEmpty()) schedule.setFee(fee);
            if (status != null && !status.toString().trim().isEmpty()) schedule.setStatus(status);
            schedule.setUpdateTime(LocalDateTime.now());
        }
    }

    // 复制排班请求
    @Data
    public static class ScheduleCopyDTO {
        @NotNull(message = "医生用户ID不能为空")
        private Long doctorUserId;
        @NotNull(message = "目标日期不能为空")
        private LocalDate targetDate;
    }

    // 排班患者信息DTO
    @Data
    public static class SchedulePatientDTO {
        private Long userId;
        private String userName;
        private String name;
        private String sex;
        private String email;
        private String phone;
    }
}
