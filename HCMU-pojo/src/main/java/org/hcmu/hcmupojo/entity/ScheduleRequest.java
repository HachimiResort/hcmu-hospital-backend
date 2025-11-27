package org.hcmu.hcmupojo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleRequest {

    @TableId(type = IdType.AUTO, value = "request_id")
    private Long requestId;

    private Long doctorUserId;

    private Long scheduleId;

    /**
     * 申请类型：1-调班，2-休假，3-加号
     */
    private Integer requestType;

    /**
     * 调班目标日期
     */
    private LocalDate targetDate;

    /**
     * 调班目标时段
     */
    private Integer targetSlotPeriod;

    /**
     * 调班目标号别
     */
    private Integer targetSlotType;

    /**
     * 加号数量
     */
    private Integer extraSlots;

    private String reason;

    /**
     * 状态：1-待审批，2-已同意，3-已拒绝，4-已撤销
     */
    private Integer status;

    private Long approverUserId;

    private String approveRemark;

    private LocalDateTime approveTime;

    private Integer isDeleted;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
