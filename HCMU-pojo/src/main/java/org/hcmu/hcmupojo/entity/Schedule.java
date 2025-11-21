package org.hcmu.hcmupojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Schedule {
    @TableId(type = IdType.AUTO, value = "schedule_id")
    private Long scheduleId; // 排班ID，主键自增
    
    private Long templateId; // 排班模板ID

    private Integer slotType; // 号别
    
    private Integer totalSlots; // 总号源数

    private Integer slotPeriod; // 时间段

    private Integer weekday; // 星期几（1-星期一，2-星期二，...，7-星期日）
    
    private BigDecimal fee; // 挂号费（原价）


    private Integer isDeleted; // 逻辑删除（0-未删除，1-已删除）
    
    private LocalDateTime createTime; // 创建时间
    
    private LocalDateTime updateTime; // 更新时间
    
   
}
