package org.hcmu.hcmupojo.dto;

import lombok.Data;

import java.time.LocalDateTime;

public class AppointmentDTO {
    @Data
    public static class AppointmentGetRequsetDTO {
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
        private LocalDateTime creatTime;
    }
    @Data
    public static class AppointmentDetailDTO {
        private Long appointmentId;
        private String appointmentNo;
        private Long patientUserId;
        private String patientUserName;
        private Long doctorId;
        private String doctorUserName;
        private Long departmentId;
        private String departmentName;
        private LocalDateTime appointmentTime;
        private String timeSlot;
        private Integer status;
        private String symptoms;
        private String remark;
        private LocalDateTime createTime;
        private LocalDateTime updateTime;
    }

}
