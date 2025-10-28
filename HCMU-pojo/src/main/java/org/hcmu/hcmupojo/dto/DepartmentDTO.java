package org.hcmu.hcmupojo.dto;

import lombok.Data;
import org.hcmu.hcmupojo.entity.Department;

import java.time.LocalDateTime;

@Data
public class DepartmentDTO {
    // 分页查询请求
    @Data
    public static class DepartmentGetRequestDTO {
        private Integer pageNum = 1;
        private Integer pageSize = 10;
        private String name; // 模糊查询科室名称
        private Long parentId; // 按父科室筛选
        private Integer isDeleted; // 逻辑删除状态
    }

    // 列表展示
    @Data
    public static class DepartmentListDTO {
        private Long departmentId;
        private String name;
        private Long parentId;
        private String description;
        private String location;
        private LocalDateTime createTime;
    }

    // 创建请求
    @Data
    public static class DepartmentCreateDTO {
        private String name; // 非空
        private Long parentId = 0L; // 默认为顶级科室
        private String description;
        private String location;
    }

    // 更新请求
    @Data
    public static class DepartmentUpdateDTO {
        private String name;
        private Long parentId;
        private String description;
        private String location;

        public void updateDepartment(Department department) {
            if (name != null && !name.trim().isEmpty()) department.setName(name);
            if (parentId != null) department.setParentId(parentId);
            if (description != null && !description.trim().isEmpty()) department.setDescription(description);
            if (location != null && !location.trim().isEmpty()) department.setLocation(location);
            department.setUpdateTime(LocalDateTime.now());
        }
    }
}
