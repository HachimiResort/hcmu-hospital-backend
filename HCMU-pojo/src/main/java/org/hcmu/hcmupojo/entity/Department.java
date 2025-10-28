package org.hcmu.hcmupojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Department {
    @TableId(type = IdType.AUTO, value = "department_id")
    private Long departmentId; // 科室ID，主键自增
    @TableField("name")
    private String name; // 科室名称
    @TableField("parent_id")
    private Long parentId = 0L; // 父科室ID（0表示顶级科室）
    @TableField("description")
    private String description; // 科室介绍
    @TableField("location")
    private String location; // 科室位置
    @TableLogic
    @TableField("is_deleted")
    private Integer isDeleted; // 逻辑删除（0-未删除，1-已删除）
    @TableField("create_time")
    private LocalDateTime createTime;
    @TableField("update_time")
    private LocalDateTime updateTime;
}
