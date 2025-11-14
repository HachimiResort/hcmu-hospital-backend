package org.hcmu.hcmupojo.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AppointmentDTO {
    @Data
    public static class AppointmentGetRequestDTO {
        private Long scheduleId;
        private Long patientUserId;
        private Long pageNum = 1L;
        private Long pageSize = 20L;
        private Integer isDeleted;
    }

    @Data
    public static class AppointmentListDTO {
        private Long appointmentId;
        private String appointmentNo;
        private Long patientUserId;
        private Long scheduleId;
        private Integer visitNo;
        private Integer status;
        private BigDecimal originalFee;
        private BigDecimal actualFee;
        private LocalDateTime paymentTime;
        private LocalDateTime cancellationTime;
        private String cancellationReason;
        private LocalDateTime createTime;
        private LocalDateTime updateTime;

        // 关联字段 - 患者信息
        private String patientUserName;// 患者姓名
        private String patientPhone;// 患者电话
        private String patientName;// 患者真实姓名

        // 关联字段 - 排班信息
        private java.time.LocalDate scheduleDate;// 出诊日期
        private Integer slotType;// 号别
        private Integer slotPeriod;// 时间段

        // 关联字段 - 医生信息
        private String doctorName;// 医生姓名
        private String doctorTitle;// 医生职称

        // 关联字段 - 科室信息
        private String departmentName;// 科室名称
    }

    @Data
    public static class AppointmentCancelDTO {
        private String reason;  // 取消原因
    }

    @Data
    public static class AppointmentPayDTO {

    }


}
