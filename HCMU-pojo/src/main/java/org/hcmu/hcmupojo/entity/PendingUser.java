package org.hcmu.hcmupojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 待处理用户实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("pending_user")
public class PendingUser implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户id/主键/自增
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户名，非空唯一
     */
    private String userName;

    /**
     * 用户姓名
     */
    private String name;


    /**
     * 逻辑删除
     */
    private Integer isDeleted;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private String email;

    private Long roleId;

    private Integer identityType; // 身份类型

    private String studentTeacherId; // 学生/教师

    private String departmentName; 

    private String title; // 职称

    private String specialty; // 擅长领域

}
