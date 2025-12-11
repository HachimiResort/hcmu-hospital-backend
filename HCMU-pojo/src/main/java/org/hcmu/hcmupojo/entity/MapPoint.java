package org.hcmu.hcmupojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MapPoint {
    @TableId(type = IdType.AUTO, value = "point_id")
    private Long pointId;
    @TableField("map_id")
    private Long mapId;
    @TableField("x")
    private Integer x;
    @TableField("y")
    private Integer y;
    @TableField("type")
    private Integer type;
    @TableField("point_name")
    private String pointName;
    @TableLogic
    @TableField("is_deleted")
    private Integer isDeleted;
    @TableField("create_time")
    private LocalDateTime createTime;
    @TableField("update_time")
    private LocalDateTime updateTime;
}
