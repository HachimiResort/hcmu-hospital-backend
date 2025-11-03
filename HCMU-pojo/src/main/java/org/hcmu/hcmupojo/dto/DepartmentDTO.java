package org.hcmu.hcmupojo.dto;

import jakarta.validation.constraints.NotNull;
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
        @NotNull(message = "科室名不能为空")
        private String name; // 模糊查询科室名称
        @NotNull(message = "父科室名不能为空")
        private Long parentId; // 按父科室筛选
    }

    // 列表展示
    @Data
    public static class DepartmentListDTO {
        @NotNull(message = "科室ID不能为空")
        private Long departmentId;
        @NotNull(message = "科室名不能为空")
        private String name;
        @NotNull(message = "上级科室id不能为空")
        private Long parentId;
        @NotNull(message = "描述不能为空")
        private String description;
        @NotNull(message = "地点不能为空不能为空")
        private String location;
        @NotNull(message = "创建时间不能为空")
        private LocalDateTime createTime;
    }

    // 创建请求
    @Data
    public static class DepartmentCreateDTO {
        @NotNull(message = "科室名不能为空")
        private String name; // 非空
        @NotNull(message = "不能为空")
        private Long parentId = 0L; // 默认为顶级科室
        @NotNull(message = "描述不能为空")
        private String description;
        @NotNull(message = "地点不能为空")
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
