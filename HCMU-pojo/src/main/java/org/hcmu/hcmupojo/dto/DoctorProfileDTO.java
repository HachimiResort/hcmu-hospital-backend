package org.hcmu.hcmupojo.dto;

import lombok.Data;
import org.hcmu.hcmupojo.entity.DoctorProfile;

import java.time.LocalDateTime;

@Data
public class DoctorProfileDTO {

    // 分页查询请求
    @Data
    public static class DoctorProfileGetRequestDTO {
        private Integer pageNum = 1;
        private Integer pageSize = 10;
        private Long departmentId; // 按科室筛选
        private String title; // 按职称筛选
        private Integer isDeleted; // 逻辑删除状态
    }

    // 列表展示
    @Data
    public static class DoctorProfileListDTO {
        private Long doctorProfileId;
        private Long userId;
        private String userName; // 关联用户表的用户名
        private Long departmentId;
        private String departmentName; // 关联科室表的科室名称
        private String title;
        private String specialty;
        private LocalDateTime createTime;
    }

    // 详情展示
    @Data
    public static class DoctorProfileDetailDTO {
        private Long doctorProfileId;
        private Long userId;
        private String userName;
        private Long departmentId;
        private String departmentName;
        private String title;
        private String specialty;
        private String bio;
        private LocalDateTime createTime;
        private LocalDateTime updateTime;
    }

    // 创建请求
    @Data
    public static class DoctorProfileCreateDTO {
        private Long userId; // 关联用户ID（必传）
        private Long departmentId; // 所属科室ID（必传）
        private String title;
        private String specialty;
        private String bio;
    }

    // 更新请求
    @Data
    public static class DoctorProfileUpdateDTO {
        private Long departmentId;
        private String title;
        private String specialty;
        private String bio;

        public void updateDoctorProfile(DoctorProfile doctorProfile) {
            if (departmentId != null) doctorProfile.setDepartmentId(departmentId);
            if (title != null && !title.trim().isEmpty()) doctorProfile.setTitle(title);
            if (specialty != null && !specialty.trim().isEmpty()) doctorProfile.setSpecialty(specialty);
            if (bio != null && !bio.trim().isEmpty()) doctorProfile.setBio(bio);
            doctorProfile.setUpdateTime(LocalDateTime.now());
        }
    }
}