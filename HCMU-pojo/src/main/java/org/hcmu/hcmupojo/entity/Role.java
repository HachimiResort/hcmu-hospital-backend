package org.hcmu.hcmupojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class Role implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "role_id", type = IdType.AUTO)
    private Long roleId;

    /**
     * 角色的名称
     */
    private String name;

    /**
     * 角色的描述
     */
    private String roleInfo;

    /**
     * 逻辑删除
     */
    private Integer isDeleted;

    private Integer type;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Integer isDefault;

}
