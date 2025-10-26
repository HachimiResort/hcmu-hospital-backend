package org.hcmu.hcmupojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import org.hcmu.hcmucommon.enumeration.PermissionEnum;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class Permission implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "permission_id", type = IdType.AUTO)
    private Long permissionId;

    private PermissionEnum keyValue;

    private Integer type;

    /**
     * 权限描述
     */
    private String name;

    /**
     * 逻辑删除
     */
    private Integer isDeleted;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
