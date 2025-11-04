package org.hcmu.hcmupojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class Schedule {
    @TableId(type = IdType.AUTO, value = "schedule_id")
    private Long scheduleId; // 排班ID，主键自增
    
    @TableField("doctor_user_id")
    private Long doctorUserId; // 医生用户ID

    @TableField("schedule_date")
    private LocalDate scheduleDate; // 出诊日期
    
    @TableField("slot_type")
    private Integer slotType; // 号别
    
    @TableField("total_slots")
    private Integer totalSlots; // 总号源数

     @TableField("slot_period")
    private Integer slotPeriod; // 时间段
    
    @TableField("available_slots")
    private Integer availableSlots; // 可用号源数
    
    @TableField("fee")
    private BigDecimal fee; // 挂号费（原价）
    
    @TableField("status")
    private Integer status; // 排班状态（1-开放...）
    
    @TableLogic
    @TableField("is_deleted")
    private Integer isDeleted; // 逻辑删除（0-未删除，1-已删除）
    
    @TableField("create_time")
    private LocalDateTime createTime; // 创建时间
    
    @TableField("update_time")
    private LocalDateTime updateTime; // 更新时间
    
   
}
