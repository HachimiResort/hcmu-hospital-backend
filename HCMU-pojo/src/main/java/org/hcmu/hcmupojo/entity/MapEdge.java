package org.hcmu.hcmupojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MapEdge {
    @TableId(type = IdType.AUTO, value = "edge_id")
    private Long edgeId;
    @TableField("from_point_id")
    private Long fromPointId;
    @TableField("to_point_id")
    private Long toPointId;
    @TableLogic
    @TableField("is_deleted")
    private Integer isDeleted;
    @TableField("create_time")
    private LocalDateTime createTime;
    @TableField("update_time")
    private LocalDateTime updateTime;
}
