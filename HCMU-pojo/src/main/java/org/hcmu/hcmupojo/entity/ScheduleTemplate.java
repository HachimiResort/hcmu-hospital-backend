package org.hcmu.hcmupojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ScheduleTemplate {
    @TableId(type = IdType.AUTO, value = "template_id")
    private Long templateId; // 排班模板ID

    private String templateName; // 模板名称

    private Integer isDeleted; // 逻辑删除（0-未删除，1-已删除）
    
    private LocalDateTime createTime; // 创建时间
    
    private LocalDateTime updateTime; // 更新时间
    
   
}
