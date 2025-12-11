package org.hcmu.hcmuserver.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.hcmu.hcmucommon.annotation.AutoLog;
import org.hcmu.hcmucommon.result.Result;
import org.hcmu.hcmupojo.dto.MapDTO;
import org.hcmu.hcmuserver.service.MapService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "地图接口", description = "地图信息相关接口")
@RestController
@RequestMapping("maps")
@Validated
public class MapController {

    @Autowired
    private MapService mapService;

    @AutoLog("获取所有地图")
    @Operation(description = "获取所有地图", summary = "获取所有地图")
    @GetMapping("")
    public Result<List<MapDTO.MapListDTO>> getAllMaps() {
        return mapService.getAllMaps();
    }

    @AutoLog("创建地图")
    @Operation(description = "创建地图", summary = "创建地图")
    @PostMapping("")
    public Result<MapDTO.MapListDTO> createMap(@RequestBody @Valid MapDTO.MapCreateDTO createDTO) {
        return mapService.createMap(createDTO);
    }

    @AutoLog("获取地图详情")
    @Operation(description = "获取地图详情", summary = "获取地图详情")
    @GetMapping("/{mapId}")
    public Result<MapDTO.MapListDTO> getMapById(@PathVariable Long mapId) {
        return mapService.findMapById(mapId);
    }

    @AutoLog("更新地图信息")
    @Operation(description = "更新地图信息", summary = "更新地图信息")
    @PutMapping("/{mapId}")
    public Result<String> updateMap(@PathVariable Long mapId, @RequestBody @Valid MapDTO.MapUpdateDTO updateDTO) {
        return mapService.updateMapById(mapId, updateDTO);
    }

    @AutoLog("删除地图")
    @Operation(description = "删除地图", summary = "删除地图")
    @DeleteMapping("/{mapId}")
    public Result<String> deleteMap(@PathVariable Long mapId) {
        return mapService.deleteMapById(mapId);
    }
}
