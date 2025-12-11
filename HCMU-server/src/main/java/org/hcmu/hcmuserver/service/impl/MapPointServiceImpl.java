package org.hcmu.hcmuserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.yulichang.base.MPJBaseServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.hcmu.hcmucommon.result.Result;
import org.hcmu.hcmupojo.dto.MapPointDTO;
import org.hcmu.hcmupojo.entity.MapPoint;
import org.hcmu.hcmuserver.mapper.map.MapPointMapper;
import org.hcmu.hcmuserver.service.MapPointService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MapPointServiceImpl extends MPJBaseServiceImpl<MapPointMapper, MapPoint> implements MapPointService {

    @Override
    public Result<List<MapPointDTO.MapPointListDTO>> getAllMapPoints() {
        LambdaQueryWrapper<MapPoint> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(MapPoint::getPointId, MapPoint::getMapId, MapPoint::getX, MapPoint::getY,
                MapPoint::getType, MapPoint::getPointName, MapPoint::getRoomCode);

        List<MapPoint> mapPoints = baseMapper.selectList(wrapper);

        List<MapPointDTO.MapPointListDTO> result = mapPoints.stream().map(mapPoint -> {
            MapPointDTO.MapPointListDTO dto = new MapPointDTO.MapPointListDTO();
            dto.setPointId(mapPoint.getPointId());
            dto.setMapId(mapPoint.getMapId());
            dto.setX(mapPoint.getX());
            dto.setY(mapPoint.getY());
            dto.setType(mapPoint.getType());
            dto.setPointName(mapPoint.getPointName());
            dto.setRoomCode(mapPoint.getRoomCode());
            return dto;
        }).collect(Collectors.toList());

        return Result.success(result);
    }

    @Override
    public Result<MapPointDTO.MapPointListDTO> createMapPoint(MapPointDTO.MapPointCreateDTO createDTO) {
        MapPoint mapPoint = new MapPoint();
        mapPoint.setMapId(createDTO.getMapId());
        mapPoint.setX(createDTO.getX());
        mapPoint.setY(createDTO.getY());
        mapPoint.setType(createDTO.getType());
        mapPoint.setPointName(createDTO.getPointName());
        mapPoint.setRoomCode(createDTO.getRoomCode());
        mapPoint.setIsDeleted(0);
        mapPoint.setCreateTime(LocalDateTime.now());
        mapPoint.setUpdateTime(LocalDateTime.now());

        baseMapper.insert(mapPoint);

        MapPointDTO.MapPointListDTO result = new MapPointDTO.MapPointListDTO();
        result.setPointId(mapPoint.getPointId());
        result.setMapId(mapPoint.getMapId());
        result.setX(mapPoint.getX());
        result.setY(mapPoint.getY());
        result.setType(mapPoint.getType());
        result.setPointName(mapPoint.getPointName());
        result.setRoomCode(mapPoint.getRoomCode());

        return Result.success(result);
    }

    @Override
    public Result<MapPointDTO.MapPointListDTO> findMapPointById(Long pointId) {
        MapPoint mapPoint = baseMapper.selectById(pointId);
        if (mapPoint == null || mapPoint.getIsDeleted() == 1) {
            return Result.error("地图点不存在");
        }

        MapPointDTO.MapPointListDTO dto = new MapPointDTO.MapPointListDTO();
        dto.setPointId(mapPoint.getPointId());
        dto.setMapId(mapPoint.getMapId());
        dto.setX(mapPoint.getX());
        dto.setY(mapPoint.getY());
        dto.setType(mapPoint.getType());
        dto.setPointName(mapPoint.getPointName());
        dto.setRoomCode(mapPoint.getRoomCode());

        return Result.success(dto);
    }

    @Override
    public Result<String> updateMapPointById(Long pointId, MapPointDTO.MapPointUpdateDTO updateDTO) {
        MapPoint mapPoint = baseMapper.selectById(pointId);
        if (mapPoint == null) {
            return Result.error("地图点不存在");
        }

        updateDTO.updateMapPoint(mapPoint);
        baseMapper.updateById(mapPoint);
        return Result.success("更新成功");
    }

    @Override
    public Result<String> deleteMapPointById(Long pointId) {
        MapPoint mapPoint = baseMapper.selectById(pointId);
        if (mapPoint == null) {
            return Result.error("地图点不存在");
        }

        baseMapper.deleteById(pointId);
        return Result.success("删除成功");
    }
}
