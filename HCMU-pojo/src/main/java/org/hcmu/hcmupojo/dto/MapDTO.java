package org.hcmu.hcmupojo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hcmu.hcmupojo.entity.Map;

import java.time.LocalDateTime;

@Data
public class MapDTO {
    @Data
    public static class MapListDTO {
        private Long mapId;
        private String mapName;
        private String imageBase64;
    }

    @Data
    public static class MapCreateDTO {
        @NotNull(message = "地图名称不能为空")
        private String mapName;
        @NotNull(message = "地图图片不能为空")
        private String imageBase64;
    }

    @Data
    public static class MapUpdateDTO {
        private String mapName;
        private String imageBase64;

        public void updateMap(Map map) {
            if (mapName != null && !mapName.trim().isEmpty()) map.setMapName(mapName);
            if (imageBase64 != null && !imageBase64.trim().isEmpty()) map.setImageBase64(imageBase64);
            map.setUpdateTime(LocalDateTime.now());
        }
    }
}
