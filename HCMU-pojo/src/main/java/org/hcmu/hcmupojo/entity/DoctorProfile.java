package org.hcmu.hcmupojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("doctor_profile")
public class DoctorProfile {
    @TableId(type = IdType.AUTO, value = "doctor_profile_id")
    private Long doctorProfileId; // 医生档案ID，主键自增

    @TableField("user_id")
    private Long userId; // 关联的用户ID

    @TableField("department_id")
    private Long departmentId; // 所属科室ID

    @TableField("title")
    private String title; // 职称

    @TableField("specialty")
    private String specialty; // 擅长领域

    @TableField("bio")
    private String bio; // 医生简介

    @TableLogic
    @TableField("is_deleted")
    private Integer isDeleted; // 逻辑删除（0-未删除，1-已删除）

    @TableField(value = "create_time", fill = com.baomidou.mybatisplus.annotation.FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = com.baomidou.mybatisplus.annotation.FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}