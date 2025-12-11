package org.hcmu.hcmuserver.service;

import com.github.yulichang.base.MPJBaseService;
import org.hcmu.hcmucommon.result.Result;
import org.hcmu.hcmupojo.dto.MapDTO;
import org.hcmu.hcmupojo.entity.Map;

import java.util.List;

public interface MapService extends MPJBaseService<Map> {
    Result<List<MapDTO.MapListDTO>> getAllMaps();
    Result<MapDTO.MapListDTO> createMap(MapDTO.MapCreateDTO createDTO);
    Result<MapDTO.MapListDTO> findMapById(Long mapId);
    Result<String> updateMapById(Long mapId, MapDTO.MapUpdateDTO updateDTO);
    Result<String> deleteMapById(Long mapId);
}
