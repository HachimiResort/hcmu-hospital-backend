package org.hcmu.hcmupojo.dto;

import jakarta.validation.constraints.NotNull;
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
    }

    // 列表展示
    @Data
    public static class DoctorProfileListDTO {
        private Long doctorProfileId;
        private Long userId;
        private String userName; // 关联用户表的用户名
        private String name;
        private Long departmentId;
        private Long locationId;
        private String roomCode;
        private String locationName;
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
        private String name;
        private Long departmentId;
        private Long locationId;
        private String roomCode;
        private String locationName;
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
        @NotNull(message = "用户ID不能为空")
        private Long userId; // 关联用户ID（必传）

        @NotNull(message = "所属科室ID不能为空")
        private Long departmentId; // 所属科室ID（必传）

        @NotNull(message = "职称不能为空")
        private String title;

        @NotNull(message = "专长不能为空")
        private String specialty;

        @NotNull(message = "简介不能为空")
        private String bio;

        private Long locationId;
    }

    // 更新请求
    @Data
    public static class DoctorProfileUpdateDTO {
        private Long departmentId;
        private Long locationId;
        private String title;
        private String specialty;
        private String bio;

        public void updateDoctorProfile(DoctorProfile doctorProfile) {
            if (departmentId != null) doctorProfile.setDepartmentId(departmentId);
            if (locationId != null) doctorProfile.setLocationId(locationId);
            if (title != null && !title.trim().isEmpty()) doctorProfile.setTitle(title);
            if (specialty != null && !specialty.trim().isEmpty()) doctorProfile.setSpecialty(specialty);
            if (bio != null && !bio.trim().isEmpty()) doctorProfile.setBio(bio);
            doctorProfile.setUpdateTime(LocalDateTime.now());
        }
    }

    @Data
    public static class DoctorProfileUpdateSelfDTO {
        private String specialty;
        private String bio;
        public void updateDoctorProfile(DoctorProfile doctorProfile) {
            if (specialty != null && !specialty.trim().isEmpty()) doctorProfile.setSpecialty(specialty);
            if (bio != null && !bio.trim().isEmpty()) doctorProfile.setBio(bio);
            doctorProfile.setUpdateTime(LocalDateTime.now());
        }
    }

    @Data
    public static class DoctorByDeptRequestDTO {
        private Integer pageNum = 1;
        private Integer pageSize = 10;

    }

    @Data
    public static class DoctorScheduleImportDTO {
        @NotNull(message = "排班周内的日期不能为空")
        private java.time.LocalDate scheduleDate;
        @NotNull(message = "模板ID不能为空")
        private Long templateId;
    }
}
