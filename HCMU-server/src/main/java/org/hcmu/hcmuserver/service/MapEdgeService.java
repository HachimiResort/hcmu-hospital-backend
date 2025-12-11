package org.hcmu.hcmuserver.service;

import com.github.yulichang.base.MPJBaseService;
import org.hcmu.hcmucommon.result.Result;
import org.hcmu.hcmupojo.dto.MapEdgeDTO;
import org.hcmu.hcmupojo.entity.MapEdge;

import java.util.List;

public interface MapEdgeService extends MPJBaseService<MapEdge> {
    Result<List<MapEdgeDTO.MapEdgeListDTO>> getAllMapEdges();
    Result<MapEdgeDTO.MapEdgeListDTO> createMapEdge(MapEdgeDTO.MapEdgeCreateDTO createDTO);
    Result<MapEdgeDTO.MapEdgeListDTO> findMapEdgeById(Long edgeId);
    Result<String> updateMapEdgeById(Long edgeId, MapEdgeDTO.MapEdgeUpdateDTO updateDTO);
    Result<String> deleteMapEdgeById(Long edgeId);
}
