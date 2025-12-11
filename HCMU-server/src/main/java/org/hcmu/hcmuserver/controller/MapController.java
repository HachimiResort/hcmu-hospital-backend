package org.hcmu.hcmuserver.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.hcmu.hcmucommon.annotation.AutoLog;
import org.hcmu.hcmucommon.result.Result;
import org.hcmu.hcmupojo.dto.MapDTO;
import org.hcmu.hcmupojo.dto.MapEdgeDTO;
import org.hcmu.hcmupojo.dto.MapPointDTO;
import org.hcmu.hcmuserver.service.MapService;
import org.hcmu.hcmuserver.service.MapEdgeService;
import org.hcmu.hcmuserver.service.MapPointService;
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

    @Autowired
    private MapPointService mapPointService;

    @Autowired
    private MapEdgeService mapEdgeService;

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

    @AutoLog("获取所有地图点")
    @Operation(description = "获取所有地图点", summary = "获取所有地图点")
    @GetMapping("/points")
    public Result<List<MapPointDTO.MapPointListDTO>> getAllMapPoints() {
        return mapPointService.getAllMapPoints();
    }

    @AutoLog("创建地图点")
    @Operation(description = "创建地图点", summary = "创建地图点")
    @PostMapping("/points")
    public Result<MapPointDTO.MapPointListDTO> createMapPoint(@RequestBody @Valid MapPointDTO.MapPointCreateDTO createDTO) {
        return mapPointService.createMapPoint(createDTO);
    }

    @AutoLog("获取地图点详情")
    @Operation(description = "获取地图点详情", summary = "获取地图点详情")
    @GetMapping("/points/{pointId}")
    public Result<MapPointDTO.MapPointListDTO> getMapPointById(@PathVariable Long pointId) {
        return mapPointService.findMapPointById(pointId);
    }

    @AutoLog("更新地图点信息")
    @Operation(description = "更新地图点信息", summary = "更新地图点信息")
    @PutMapping("/points/{pointId}")
    public Result<String> updateMapPoint(@PathVariable Long pointId, @RequestBody @Valid MapPointDTO.MapPointUpdateDTO updateDTO) {
        return mapPointService.updateMapPointById(pointId, updateDTO);
    }

    @AutoLog("删除地图点")
    @Operation(description = "删除地图点", summary = "删除地图点")
    @DeleteMapping("/points/{pointId}")
    public Result<String> deleteMapPoint(@PathVariable Long pointId) {
        return mapPointService.deleteMapPointById(pointId);
    }

    @AutoLog("获取所有地图边")
    @Operation(description = "获取所有地图边", summary = "获取所有地图边")
    @GetMapping("/edges")
    public Result<List<MapEdgeDTO.MapEdgeListDTO>> getAllMapEdges() {
        return mapEdgeService.getAllMapEdges();
    }

    @AutoLog("创建地图边")
    @Operation(description = "创建地图边", summary = "创建地图边")
    @PostMapping("/edges")
    public Result<MapEdgeDTO.MapEdgeListDTO> createMapEdge(@RequestBody @Valid MapEdgeDTO.MapEdgeCreateDTO createDTO) {
        return mapEdgeService.createMapEdge(createDTO);
    }

    @AutoLog("获取地图边详情")
    @Operation(description = "获取地图边详情", summary = "获取地图边详情")
    @GetMapping("/edges/{edgeId}")
    public Result<MapEdgeDTO.MapEdgeListDTO> getMapEdgeById(@PathVariable Long edgeId) {
        return mapEdgeService.findMapEdgeById(edgeId);
    }

    @AutoLog("更新地图边信息")
    @Operation(description = "更新地图边信息", summary = "更新地图边信息")
    @PutMapping("/edges/{edgeId}")
    public Result<String> updateMapEdge(@PathVariable Long edgeId, @RequestBody @Valid MapEdgeDTO.MapEdgeUpdateDTO updateDTO) {
        return mapEdgeService.updateMapEdgeById(edgeId, updateDTO);
    }

    @AutoLog("删除地图边")
    @Operation(description = "删除地图边", summary = "删除地图边")
    @DeleteMapping("/edges/{edgeId}")
    public Result<String> deleteMapEdge(@PathVariable Long edgeId) {
        return mapEdgeService.deleteMapEdgeById(edgeId);
    }
}
