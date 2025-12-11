package org.hcmu.hcmupojo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hcmu.hcmupojo.entity.MapEdge;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class MapEdgeDTO {
    @Data
    public static class MapEdgeListDTO {
        private Long edgeId;
        private Long fromPointId;
        private Long toPointId;
        private BigDecimal distance;
    }

    @Data
    public static class MapEdgeCreateDTO {
        @NotNull(message = "起点ID不能为空")
        private Long fromPointId;
        @NotNull(message = "终点ID不能为空")
        private Long toPointId;
        @NotNull(message = "距离不能为空")
        private BigDecimal distance;
    }

    @Data
    public static class MapEdgeUpdateDTO {
        private Long fromPointId;
        private Long toPointId;
        private BigDecimal distance;

        public void updateMapEdge(MapEdge mapEdge) {
            if (fromPointId != null) mapEdge.setFromPointId(fromPointId);
            if (toPointId != null) mapEdge.setToPointId(toPointId);
            if (distance != null) mapEdge.setDistance(distance);
            mapEdge.setUpdateTime(LocalDateTime.now());
        }
    }
}
