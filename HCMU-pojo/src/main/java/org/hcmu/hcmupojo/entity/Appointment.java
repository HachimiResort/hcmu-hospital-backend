package org.hcmu.hcmupojo.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("appointment")
public class Appointment {
    @TableId(type = IdType.AUTO, value = "appointment_id")
    private Long appointmentId; // 预约id

    @TableField("appointment_no")
    private String appointmentNo; // 预约编号

    @TableField("patient_user_id")
    private Long patientUserId; // 患者的用户id

    @TableField("schedule_id")
    private Long scheduleId; // 排班id

    @TableField("visit_no")
    private Integer visitNo; // 就诊序号

    @TableField("status")
    private Integer status; // 状态

    @TableField("original_fee")
    private BigDecimal originalFee; // 原始费用

    @TableField("actual_fee")
    private BigDecimal actualFee; // 实际费用

    @TableField("payment_time")
    private LocalDateTime paymentTime; // 付款时间

    @TableField("cancellation_time")
    private LocalDateTime cancellationTime; // 取消时间

    @TableField("cancellation_reason")
    private String cancellationReason; // 取消原因


    private LocalDateTime callingTime;
    private LocalDateTime completionTime;

    @TableField("is_deleted")
    private Integer isDeleted; // 逻辑删除

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;
}
