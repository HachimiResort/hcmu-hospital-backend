package org.hcmu.hcmupojo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hcmu.hcmupojo.entity.PatientProfile;

import java.time.LocalDateTime;

@Data
public class PatientProfileDTO {

    // 分页查询请求
    @Data
    public static class PatientProfileGetRequestDTO {
        private Integer pageNum = 1;
        private Integer pageSize = 10;
        private Integer identityType; // 按身份类型筛选
        private Integer isDeleted; // 逻辑删除状态
    }

    // 列表展示
    @Data
    public static class PatientProfileListDTO {
        private Long patientProfileId;
        private Long userId;
        private String userName; // 关联用户表的用户名
        private Integer identityType;
        private String studentTeacherId;
        private String emergencyContact;
        private LocalDateTime createTime;
    }

    // 详情展示
    @Data
    public static class PatientProfileDetailDTO {
        private Long patientProfileId;
        private Long userId;
        private String userName;
        private Integer identityType;
        private String studentTeacherId;
        private String emergencyContact;
        private String emergencyContactPhone;
        private String medicalHistory;
        private String allergyHistory;
        private LocalDateTime createTime;
        private LocalDateTime updateTime;
    }

    // 创建请求
    @Data
    public static class PatientProfileCreateDTO {
        @NotNull(message = "用户ID不能为空")
        private Long userId; // 关联用户ID（必传）

        @NotNull(message = "身份类型不能为空")
        private Integer identityType;

        private String studentTeacherId;
        private String emergencyContact;
        private String emergencyContactPhone;
        private String medicalHistory;
        private String allergyHistory;
    }

    // 更新请求
    @Data
    public static class PatientProfileUpdateDTO {
        private Integer identityType;
        private String studentTeacherId;
        private String emergencyContact;
        private String emergencyContactPhone;
        private String medicalHistory;
        private String allergyHistory;

        public void updatePatientProfile(PatientProfile patientProfile) {
            if (identityType != null) patientProfile.setIdentityType(identityType);
            if (studentTeacherId != null && !studentTeacherId.trim().isEmpty()) patientProfile.setStudentTeacherId(studentTeacherId);
            if (emergencyContact != null && !emergencyContact.trim().isEmpty()) patientProfile.setEmergencyContact(emergencyContact);
            if (emergencyContactPhone != null && !emergencyContactPhone.trim().isEmpty()) patientProfile.setEmergencyContactPhone(emergencyContactPhone);
            if (medicalHistory != null && !medicalHistory.trim().isEmpty()) patientProfile.setMedicalHistory(medicalHistory);
            if (allergyHistory != null && !allergyHistory.trim().isEmpty()) patientProfile.setAllergyHistory(allergyHistory);
            patientProfile.setUpdateTime(LocalDateTime.now());
        }
    }

        // 更新请求（患者自己修改）
    @Data
    public static class PatientProfileUpdateSelfDTO {
        private String emergencyContact;
        private String emergencyContactPhone;
        private String medicalHistory;
        private String allergyHistory;

        public void updatePatientProfile(PatientProfile patientProfile) {
            if (emergencyContact != null && !emergencyContact.trim().isEmpty()) patientProfile.setEmergencyContact(emergencyContact);
            if (emergencyContactPhone != null && !emergencyContactPhone.trim().isEmpty()) patientProfile.setEmergencyContactPhone(emergencyContactPhone);
            if (medicalHistory != null && !medicalHistory.trim().isEmpty()) patientProfile.setMedicalHistory(medicalHistory);
            if (allergyHistory != null && !allergyHistory.trim().isEmpty()) patientProfile.setAllergyHistory(allergyHistory);
            patientProfile.setUpdateTime(LocalDateTime.now());
        }
    }
}