package org.hcmu.hcmupojo.entity.relation;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 角色-权限关系表
 * @author Kyy008
 * @since 2025-10-19
 */

@Data
public class RolePermission implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long permissionId;

    private Long roleId;

    private Integer isDeleted;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

}
