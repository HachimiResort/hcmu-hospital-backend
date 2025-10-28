package org.hcmu.hcmuserver.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.github.yulichang.base.MPJBaseServiceImpl;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import lombok.extern.slf4j.Slf4j;

import org.hcmu.hcmucommon.result.Result;

import org.hcmu.hcmupojo.dto.DepartmentDTO;
import org.hcmu.hcmupojo.dto.PageDTO;
import org.hcmu.hcmupojo.entity.Department;
import org.hcmu.hcmuserver.mapper.department.DepartmentMapper;
import org.hcmu.hcmuserver.service.DepartmentService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class DepartmentServiceImpl extends MPJBaseServiceImpl<DepartmentMapper, Department> implements DepartmentService {

    @Override
    public Result<DepartmentDTO.DepartmentListDTO> createDepartment(DepartmentDTO.DepartmentCreateDTO createDTO) {
        // 校验科室名称是否存在
        LambdaQueryWrapper<Department> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Department::getName, createDTO.getName())
                .eq(Department::getIsDeleted, 0); // 只校验未删除的
        if (baseMapper.selectCount(wrapper) > 0) {
            return Result.error("科室名称已存在");
        }

        Department department = new Department();
        department.setName(createDTO.getName());
        department.setParentId(createDTO.getParentId());
        department.setDescription(createDTO.getDescription());
        department.setLocation(createDTO.getLocation());
        department.setIsDeleted(0); // 初始未删除
        department.setCreateTime(LocalDateTime.now());
        department.setUpdateTime(LocalDateTime.now());

        baseMapper.insert(department);

        DepartmentDTO.DepartmentListDTO result = new DepartmentDTO.DepartmentListDTO();
        result.setDepartmentId(department.getDepartmentId());
        result.setName(department.getName());
        result.setParentId(department.getParentId());
        result.setDescription(department.getDescription());
        result.setLocation(department.getLocation());
        result.setCreateTime(department.getCreateTime());

        return Result.success(result);
    }

    @Override
    public Result<PageDTO<DepartmentDTO.DepartmentListDTO>> findAllDepartments(DepartmentDTO.DepartmentGetRequestDTO requestDTO) {
        MPJLambdaWrapper<Department> queryWrapper = new MPJLambdaWrapper<>();
        queryWrapper.select(Department::getDepartmentId, Department::getName, Department::getParentId, 
                           Department::getDescription, Department::getLocation, Department::getCreateTime)
                .like(requestDTO.getName() != null, Department::getName, requestDTO.getName())
                .eq(requestDTO.getParentId() != null, Department::getParentId, requestDTO.getParentId())
                .eq(requestDTO.getIsDeleted() != null, Department::getIsDeleted, requestDTO.getIsDeleted())
                .orderByDesc(Department::getCreateTime);
        
        IPage<DepartmentDTO.DepartmentListDTO> page = baseMapper.selectJoinPage(
                new Page<>(requestDTO.getPageNum(), requestDTO.getPageSize()), 
                DepartmentDTO.DepartmentListDTO.class, 
                queryWrapper);
        
        return Result.success(new PageDTO<>(page));
    }

    @Override
    public Result<DepartmentDTO.DepartmentListDTO> findDepartmentById(Long departmentId) {
        Department department = baseMapper.selectById(departmentId);
        if (department == null || department.getIsDeleted() == 1) {
            return Result.error("科室不存在");
        }

        DepartmentDTO.DepartmentListDTO dto = new DepartmentDTO.DepartmentListDTO();
        dto.setDepartmentId(department.getDepartmentId());
        dto.setName(department.getName());
        dto.setParentId(department.getParentId());
        dto.setDescription(department.getDescription());
        dto.setLocation(department.getLocation());
        dto.setCreateTime(department.getCreateTime());

        return Result.success(dto);
    }

    @Override
    public Result<String> updateDepartmentById(Long departmentId, DepartmentDTO.DepartmentUpdateDTO updateDTO) {
        Department department = baseMapper.selectById(departmentId);
        if (department == null || department.getIsDeleted() == 1) {
            return Result.error("科室不存在");
        }

        // 若修改名称，需校验新名称是否重复
        if (updateDTO.getName() != null && !updateDTO.getName().equals(department.getName())) {
            LambdaQueryWrapper<Department> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Department::getName, updateDTO.getName())
                    .eq(Department::getIsDeleted, 0);
            if (baseMapper.selectCount(wrapper) > 0) {
                return Result.error("科室名称已存在");
            }
        }

        updateDTO.updateDepartment(department);
        baseMapper.updateById(department);
        return Result.success("更新成功");
    }

    @Override
    public Result<String> deleteDepartmentById(Long departmentId) {
        Department department = baseMapper.selectById(departmentId);
        if (department == null) {
            return Result.error("科室不存在");
        }

        // 校验是否有子科室
        LambdaQueryWrapper<Department> childWrapper = new LambdaQueryWrapper<>();
        childWrapper.eq(Department::getParentId, departmentId);
        if (baseMapper.selectCount(childWrapper) > 0) {
            return Result.error("该科室存在子科室，无法删除");
        }

        // 使用MyBatis-Plus的逻辑删除
        baseMapper.deleteById(departmentId);
        return Result.success("删除成功");
    }

    // 新增：批量删除实现
    @Override
    public Result<String> batchDeleteDepartments(List<Long> departmentIds) {
        if (CollectionUtils.isEmpty(departmentIds)) {
            return Result.error("请选择需要删除的科室");
        }

        // 校验科室是否存在且未删除
        LambdaQueryWrapper<Department> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Department::getDepartmentId, departmentIds)
                .eq(Department::getIsDeleted, 0);
        long existCount = baseMapper.selectCount(wrapper);
        if (existCount != departmentIds.size()) {
            return Result.error("部分科室不存在或已删除");
        }

        // 校验是否有子科室
        LambdaQueryWrapper<Department> childWrapper = new LambdaQueryWrapper<>();
        childWrapper.in(Department::getParentId, departmentIds)
                .eq(Department::getIsDeleted, 0);
        if (baseMapper.selectCount(childWrapper) > 0) {
            return Result.error("部分科室存在子科室，无法删除");
        }

        // 批量逻辑删除
        Department update = new Department();
        update.setIsDeleted(1);
        update.setUpdateTime(LocalDateTime.now());
        LambdaQueryWrapper<Department> updateWrapper = new LambdaQueryWrapper<>();
        updateWrapper.in(Department::getDepartmentId, departmentIds);
        baseMapper.update(update, updateWrapper);

        return Result.success("批量删除成功");
    }
}
