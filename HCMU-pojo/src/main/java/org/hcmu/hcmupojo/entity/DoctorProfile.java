package org.hcmu.hcmupojo.entity;

import com.baomidou.mybatisplus.annotation.*;
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

    @TableField("location_id")
    private Long locationId; // 关联map_point的point_id

    @TableField("title")
    private String title; // 职称

    @TableField("specialty")
    private String specialty; // 擅长领域

    @TableField("bio")
    private String bio; // 医生简介

    @TableLogic
    @TableField("is_deleted")
    private Integer isDeleted; // 逻辑删除（0-未删除，1-已删除）

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
