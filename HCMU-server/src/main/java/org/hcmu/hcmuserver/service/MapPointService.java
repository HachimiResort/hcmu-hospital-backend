package org.hcmu.hcmuserver.service;

import com.github.yulichang.base.MPJBaseService;
import org.hcmu.hcmucommon.result.Result;
import org.hcmu.hcmupojo.dto.MapPointDTO;
import org.hcmu.hcmupojo.entity.MapPoint;

import java.util.List;

public interface MapPointService extends MPJBaseService<MapPoint> {
    Result<List<MapPointDTO.MapPointListDTO>> getAllMapPoints();
    Result<MapPointDTO.MapPointListDTO> createMapPoint(MapPointDTO.MapPointCreateDTO createDTO);
    Result<MapPointDTO.MapPointListDTO> findMapPointById(Long pointId);
    Result<String> updateMapPointById(Long pointId, MapPointDTO.MapPointUpdateDTO updateDTO);
    Result<String> deleteMapPointById(Long pointId);
}
