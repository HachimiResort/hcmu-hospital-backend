package org.hcmu.hcmupojo.dto;

import jakarta.validation.constraints.NotNull;
import org.hcmu.hcmucommon.enumeration.WaitListEnum;
import lombok.Data;
import org.hcmu.hcmupojo.entity.Waitlist;

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
        private Integer pageNum = 1;
        private Integer pageSize = 10;
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

    // 创建 DTO
    @Data
    public static class WaitlistCreateDTO {
        @NotNull(message = "患者ID不能为空")
        private Long patientUserId;

        @NotNull(message = "排班ID不能为空")
        private Long scheduleId;

        @NotNull(message = "排班状态不能为空")
        private Integer status = WaitListEnum.WAITING.getCode();
    }

    // 更新 DTO
    @Data
    public static class WaitlistUpdateDTO {
        private Integer status;
        private LocalDateTime notifiedTime;
        private LocalDateTime lockExpireTime;

        public void updateWaitlist(Waitlist waitlist) {
            if (status != null) {
                waitlist.setStatus(status);
            }
            if (notifiedTime != null) {
                waitlist.setNotifiedTime(notifiedTime);
            }
            if (lockExpireTime != null) {
                waitlist.setLockExpireTime(lockExpireTime);
            }
            waitlist.setUpdateTime(LocalDateTime.now());
        }
    }
}
