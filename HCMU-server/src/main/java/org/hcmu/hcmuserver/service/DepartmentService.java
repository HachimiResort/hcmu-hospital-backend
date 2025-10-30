package org.hcmu.hcmuserver.service;

import com.github.yulichang.base.MPJBaseService;
import org.hcmu.hcmucommon.result.Result;
import org.hcmu.hcmupojo.dto.DepartmentDTO;
import org.hcmu.hcmupojo.dto.PageDTO;
import org.hcmu.hcmupojo.entity.Department;

import java.util.List;

public interface DepartmentService extends MPJBaseService<Department> {
    Result<DepartmentDTO.DepartmentListDTO> createDepartment(DepartmentDTO.DepartmentCreateDTO createDTO);
    Result<PageDTO<DepartmentDTO.DepartmentListDTO>> findAllDepartments(DepartmentDTO.DepartmentGetRequestDTO requestDTO);
    Result<DepartmentDTO.DepartmentListDTO> findDepartmentById(Long departmentId);
    Result<String> updateDepartmentById(Long departmentId, DepartmentDTO.DepartmentUpdateDTO updateDTO);
    Result<String> deleteDepartmentById(Long departmentId);
    Result<String> batchDeleteDepartments(List<Long> departmentIds); // 新增批量删除方法
}