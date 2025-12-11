package org.hcmu.hcmupojo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hcmu.hcmupojo.entity.MapPoint;

import java.time.LocalDateTime;

@Data
public class MapPointDTO {
    @Data
    public static class MapPointListDTO {
        private Long pointId;
        private Long mapId;
        private Integer x;
        private Integer y;
        private Integer type;
        private String pointName;
        private String roomCode;
    }

    @Data
    public static class MapPointCreateDTO {
        @NotNull(message = "地图ID不能为空")
        private Long mapId;
        @NotNull(message = "X坐标不能为空")
        private Integer x;
        @NotNull(message = "Y坐标不能为空")
        private Integer y;
        @NotNull(message = "点类型不能为空")
        private Integer type;
        private String pointName;
        private String roomCode;
    }

    @Data
    public static class MapPointUpdateDTO {
        private Long mapId;
        private Integer x;
        private Integer y;
        private Integer type;
        private String pointName;
        private String roomCode;

        public void updateMapPoint(MapPoint mapPoint) {
            if (mapId != null) mapPoint.setMapId(mapId);
            if (x != null) mapPoint.setX(x);
            if (y != null) mapPoint.setY(y);
            if (type != null) mapPoint.setType(type);
            if (pointName != null && !pointName.trim().isEmpty()) mapPoint.setPointName(pointName);
            if (roomCode != null && !roomCode.trim().isEmpty()) mapPoint.setRoomCode(roomCode);
            mapPoint.setUpdateTime(LocalDateTime.now());
        }
    }
}
