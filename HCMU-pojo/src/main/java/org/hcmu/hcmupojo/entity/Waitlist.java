package org.hcmu.hcmupojo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("waitlist")
public class Waitlist {

    @TableId(type = IdType.AUTO, value = "waitlist_id")
    private Long waitlistId;

    @TableField("patient_user_id")
    private Long patientUserId;

    @TableField("schedule_id")
    private Long scheduleId;

    @TableField("status")
    private Integer status;

    @TableField("notified_time")
    private LocalDateTime notifiedTime;

    @TableField("lock_expire_time")
    private LocalDateTime lockExpireTime;

    @TableField("is_deleted")
    private Integer isDeleted;

    @TableField(value = "create_time")
    private LocalDateTime createTime;

    @TableField(value = "update_time")
    private LocalDateTime updateTime;
}
