package org.hcmu.hcmupojo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("patient_profile")
public class PatientProfile {
    @TableId(type = IdType.AUTO, value = "patient_profile_id")
    private Long patientProfileId; // 患者档案ID，主键自增

    @TableField("user_id")
    private Long userId; // 关联的用户ID

    @TableField("identity_type")
    private Integer identityType; // 身份类型

    @TableField("student_teacher")
    private String studentTeacher; // 学生/教师

    @TableField("emergency_contact")
    private String emergencyContact; // 紧急联系人

    @TableField("emergency_contact_phone")
    private String emergencyContactPhone; // 紧急联系人电话

    @TableField("medical_history")
    private String medicalHistory; // 病史

    @TableField("allergy_history")
    private String allergyHistory; // 过敏史

    @TableLogic
    @TableField("is_deleted")
    private Integer isDeleted; // 逻辑删除（0-未删除，1-已删除）

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}