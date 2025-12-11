package org.hcmu.hcmuserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.yulichang.base.MPJBaseServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.hcmu.hcmucommon.result.Result;
import org.hcmu.hcmupojo.dto.MapDTO;
import org.hcmu.hcmupojo.entity.Map;
import org.hcmu.hcmuserver.mapper.map.MapMapper;
import org.hcmu.hcmuserver.service.MapService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MapServiceImpl extends MPJBaseServiceImpl<MapMapper, Map> implements MapService {

    @Override
    public Result<List<MapDTO.MapListDTO>> getAllMaps() {
        LambdaQueryWrapper<Map> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(Map::getMapId, Map::getMapName, Map::getImageBase64);

        List<Map> maps = baseMapper.selectList(wrapper);

        List<MapDTO.MapListDTO> result = maps.stream().map(map -> {
            MapDTO.MapListDTO dto = new MapDTO.MapListDTO();
            dto.setMapId(map.getMapId());
            dto.setMapName(map.getMapName());
            dto.setImageBase64(map.getImageBase64());
            return dto;
        }).collect(Collectors.toList());

        return Result.success(result);
    }

    @Override
    public Result<MapDTO.MapListDTO> createMap(MapDTO.MapCreateDTO createDTO) {
        LambdaQueryWrapper<Map> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Map::getMapName, createDTO.getMapName());
        if (baseMapper.selectCount(wrapper) > 0) {
            return Result.error("地图名称已存在");
        }

        Map map = new Map();
        map.setMapName(createDTO.getMapName());
        map.setImageBase64(createDTO.getImageBase64());
        map.setIsDeleted(0);
        map.setCreateTime(LocalDateTime.now());
        map.setUpdateTime(LocalDateTime.now());

        baseMapper.insert(map);

        MapDTO.MapListDTO result = new MapDTO.MapListDTO();
        result.setMapId(map.getMapId());
        result.setMapName(map.getMapName());
        result.setImageBase64(map.getImageBase64());

        return Result.success(result);
    }

    @Override
    public Result<MapDTO.MapListDTO> findMapById(Long mapId) {
        Map map = baseMapper.selectById(mapId);
        if (map == null || map.getIsDeleted() == 1) {
            return Result.error("地图不存在");
        }

        MapDTO.MapListDTO dto = new MapDTO.MapListDTO();
        dto.setMapId(map.getMapId());
        dto.setMapName(map.getMapName());
        dto.setImageBase64(map.getImageBase64());

        return Result.success(dto);
    }

    @Override
    public Result<String> updateMapById(Long mapId, MapDTO.MapUpdateDTO updateDTO) {
        Map map = baseMapper.selectById(mapId);
        if (map == null) {
            return Result.error("地图不存在");
        }

        if (updateDTO.getMapName() != null && !updateDTO.getMapName().equals(map.getMapName())) {
            LambdaQueryWrapper<Map> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Map::getMapName, updateDTO.getMapName());
            if (baseMapper.selectCount(wrapper) > 0) {
                return Result.error("地图名称已存在");
            }
        }

        updateDTO.updateMap(map);
        baseMapper.updateById(map);
        return Result.success("更新成功");
    }

    @Override
    public Result<String> deleteMapById(Long mapId) {
        Map map = baseMapper.selectById(mapId);
        if (map == null) {
            return Result.error("地图不存在");
        }

        baseMapper.deleteById(mapId);
        return Result.success("删除成功");
    }
}
