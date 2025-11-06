package org.hcmu.hcmupojo.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AppointmentDTO {
    @Data
    public static class AppointmentGetRequestDTO {
        private Long scheduleId;//按排班号查询
        private Long patientUserId;//按患者id查询
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

        // 关联字段
        private String patientUserName;// 患者姓名
        private String patientPhone;// 患者电话
        private String patientName;// 患者真实姓名
    }


}
