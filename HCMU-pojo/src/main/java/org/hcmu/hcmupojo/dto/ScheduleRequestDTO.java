package org.hcmu.hcmupojo.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hcmu.hcmupojo.entity.ScheduleRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ScheduleRequestDTO {

    @Data
    public static class ScheduleRequestGetRequestDTO {
        private Integer pageNum = 1;
        private Integer pageSize = 10;
        private Long doctorUserId;
        private Long scheduleId;
        private Integer requestType;
        private Integer status;
    }

    @Data
    public static class ScheduleRequestListDTO {
        private Long requestId;
        private Long doctorUserId;
        private Long scheduleId;
        private Integer requestType;
        private Integer status;
        private LocalDate targetDate;
        private Integer targetSlotPeriod;
        private Integer targetSlotType;
        private Integer extraSlots;
        private String reason;
        private Long approverUserId;
        private String approveRemark;
        private LocalDateTime approveTime;
        private LocalDateTime createTime;

        // 关联字段
        private String doctorName;
        private LocalDate scheduleDate;
        private Integer slotPeriod;
        private Integer slotType;
    }

    @Data
    public static class ScheduleRequestDetailDTO {
        private Long requestId;
        private Long doctorUserId;
        private Long scheduleId;
        private Integer requestType;
        private Integer status;
        private LocalDate targetDate;
        private Integer targetSlotPeriod;
        private Integer targetSlotType;
        private Integer extraSlots;
        private String reason;
        private Long approverUserId;
        private String approveRemark;
        private LocalDateTime approveTime;
        private LocalDateTime createTime;
        private LocalDateTime updateTime;

        // 关联字段
        private String doctorName;
        private LocalDate scheduleDate;
        private Integer slotPeriod;
        private Integer slotType;
    }

    @Data
    public static class ScheduleRequestCreateDTO {
        @NotNull(message = "医生用户ID不能为空")
        private Long doctorUserId;

        @NotNull(message = "排班ID不能为空")
        private Long scheduleId;

        @NotNull(message = "申请类型不能为空")
        private Integer requestType;

        private LocalDate targetDate;
        private Integer targetSlotPeriod;
        private Integer targetSlotType;

        @Min(value = 1, message = "加号数量必须大于0")
        private Integer extraSlots;

        private String reason;
    }

    @Data
    public static class ScheduleRequestUpdateDTO {
        private LocalDate targetDate;
        private Integer targetSlotPeriod;
        private Integer targetSlotType;
        @Min(value = 1, message = "加号数量必须大于0")
        private Integer extraSlots;
        private String reason;

        public void updateRequest(ScheduleRequest request) {
            if (targetDate != null) {
                request.setTargetDate(targetDate);
            }
            if (targetSlotPeriod != null) {
                request.setTargetSlotPeriod(targetSlotPeriod);
            }
            if (targetSlotType != null) {
                request.setTargetSlotType(targetSlotType);
            }
            if (extraSlots != null) {
                request.setExtraSlots(extraSlots);
            }
            if (reason != null) {
                request.setReason(reason);
            }
            request.setUpdateTime(LocalDateTime.now());
        }
    }

    @Data
    public static class ScheduleRequestHandleDTO {
        @NotNull(message = "审批结果不能为空")
        private Boolean approved;
        private String approveRemark;
    }
}
