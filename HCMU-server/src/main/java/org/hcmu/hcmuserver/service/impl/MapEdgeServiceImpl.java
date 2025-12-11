package org.hcmu.hcmuserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.yulichang.base.MPJBaseServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.hcmu.hcmucommon.result.Result;
import org.hcmu.hcmupojo.dto.MapEdgeDTO;
import org.hcmu.hcmupojo.entity.MapEdge;
import org.hcmu.hcmuserver.mapper.map.MapEdgeMapper;
import org.hcmu.hcmuserver.service.MapEdgeService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MapEdgeServiceImpl extends MPJBaseServiceImpl<MapEdgeMapper, MapEdge> implements MapEdgeService {

    @Override
    public Result<List<MapEdgeDTO.MapEdgeListDTO>> getAllMapEdges() {
        LambdaQueryWrapper<MapEdge> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(MapEdge::getEdgeId, MapEdge::getFromPointId, MapEdge::getToPointId, MapEdge::getDistance);

        List<MapEdge> mapEdges = baseMapper.selectList(wrapper);

        List<MapEdgeDTO.MapEdgeListDTO> result = mapEdges.stream().map(mapEdge -> {
            MapEdgeDTO.MapEdgeListDTO dto = new MapEdgeDTO.MapEdgeListDTO();
            dto.setEdgeId(mapEdge.getEdgeId());
            dto.setFromPointId(mapEdge.getFromPointId());
            dto.setToPointId(mapEdge.getToPointId());
            dto.setDistance(mapEdge.getDistance());
            return dto;
        }).collect(Collectors.toList());

        return Result.success(result);
    }

    @Override
    public Result<MapEdgeDTO.MapEdgeListDTO> createMapEdge(MapEdgeDTO.MapEdgeCreateDTO createDTO) {
        MapEdge mapEdge = new MapEdge();
        mapEdge.setFromPointId(createDTO.getFromPointId());
        mapEdge.setToPointId(createDTO.getToPointId());
        mapEdge.setDistance(createDTO.getDistance());
        mapEdge.setIsDeleted(0);
        mapEdge.setCreateTime(LocalDateTime.now());
        mapEdge.setUpdateTime(LocalDateTime.now());

        baseMapper.insert(mapEdge);

        MapEdgeDTO.MapEdgeListDTO result = new MapEdgeDTO.MapEdgeListDTO();
        result.setEdgeId(mapEdge.getEdgeId());
        result.setFromPointId(mapEdge.getFromPointId());
        result.setToPointId(mapEdge.getToPointId());
        result.setDistance(mapEdge.getDistance());

        return Result.success(result);
    }

    @Override
    public Result<MapEdgeDTO.MapEdgeListDTO> findMapEdgeById(Long edgeId) {
        MapEdge mapEdge = baseMapper.selectById(edgeId);
        if (mapEdge == null || mapEdge.getIsDeleted() == 1) {
            return Result.error("地图边不存在");
        }

        MapEdgeDTO.MapEdgeListDTO dto = new MapEdgeDTO.MapEdgeListDTO();
        dto.setEdgeId(mapEdge.getEdgeId());
        dto.setFromPointId(mapEdge.getFromPointId());
        dto.setToPointId(mapEdge.getToPointId());
        dto.setDistance(mapEdge.getDistance());

        return Result.success(dto);
    }

    @Override
    public Result<String> updateMapEdgeById(Long edgeId, MapEdgeDTO.MapEdgeUpdateDTO updateDTO) {
        MapEdge mapEdge = baseMapper.selectById(edgeId);
        if (mapEdge == null) {
            return Result.error("地图边不存在");
        }

        updateDTO.updateMapEdge(mapEdge);
        baseMapper.updateById(mapEdge);
        return Result.success("更新成功");
    }

    @Override
    public Result<String> deleteMapEdgeById(Long edgeId) {
        MapEdge mapEdge = baseMapper.selectById(edgeId);
        if (mapEdge == null) {
            return Result.error("地图边不存在");
        }

        baseMapper.deleteById(edgeId);
        return Result.success("删除成功");
    }
}
