package org.hcmu.hcmupojo.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class WaitlistDTO {

    // 查询请求 DTO
    @Data
    public static class WaitlistGetRequestDTO {
        private Long patientUserId;      // 按患者ID查询
        private Long scheduleId;         // 按排班ID查询
        private Integer status;          // 按状态查询
        private Long pageNum = 1L;
        private Long pageSize = 20L;
        private Integer isDeleted;
    }

    // 列表返回 DTO
    @Data
    public static class WaitlistListDTO {
        private Long waitlistId;
        private Long patientUserId;
        private Long scheduleId;
        private Integer status;
        private LocalDateTime createTime;

        // 关联字段
        private String patientUserName;    // 患者姓名
        private String patientPhone;       // 患者电话
    }

    // 详情返回 DTO
    @Data
    public static class WaitlistDetailDTO {
        private Long waitlistId;
        private Long patientUserId;
        private Long scheduleId;
        private Integer status;
        private LocalDateTime notifiedTime;
        private LocalDateTime lockExpireTime;
        private LocalDateTime createTime;
        private LocalDateTime updateTime;

        // 患者信息
        private String patientUserName;    // 患者姓名
        private String patientPhone;       // 患者电话

    }
}
