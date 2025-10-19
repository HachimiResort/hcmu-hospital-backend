package org.hcmu.hcmupojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 管理端员工实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("user")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户id/主键/自增
     */
    @TableId(value = "user_id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户姓名
     */
    private String name;

    /**
     * 用户名，非空唯一
     */
    private String userName;

    private String password;

    private String sex;

    /**
     * 用户状态，0：禁用，1：正常
     */
    private Integer status;

    /**
     * 逻辑删除
     */
    private Integer isDeleted;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

}
