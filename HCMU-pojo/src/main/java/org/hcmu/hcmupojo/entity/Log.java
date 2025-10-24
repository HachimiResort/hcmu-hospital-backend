package org.hcmu.hcmupojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 *
 * @author Kyy008
 * @since 2025-10-24
 */

@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
@TableName("log")
public class Log {

    private static final long serialVersionUID = 1L;

    /**
     * 日志主键
     */
    @TableId(value = "log_id", type = IdType.AUTO)
    private Long logId;

    /**
     * 操作人id
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 操作人ip
     */
    private String ip;

    /**
     * 操作名称
     */
    private String operation;
    
    /**
     * 操作时间
     */
    private LocalDateTime createTime;

    /**
     * 修改时间
     */
    private LocalDateTime updateTime;

    /**
     * 逻辑删除
     */
    private Integer isDeleted;

    public Log(String operation, Long userId, String ip) {
        this.operation = operation;
        this.userId = userId;
        this.ip = ip;
    }
}
