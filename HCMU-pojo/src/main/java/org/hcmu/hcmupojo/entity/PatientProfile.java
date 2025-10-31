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

    private Integer identityType; // 身份类型

    private String studentTeacherId; // 学生/教师

    private String emergencyContact; // 紧急联系人

    private String emergencyContactPhone; // 紧急联系人电话

    private String medicalHistory; // 病史

    private String allergyHistory; // 过敏史

    private Integer isDeleted; // 逻辑删除（0-未删除，1-已删除）

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}