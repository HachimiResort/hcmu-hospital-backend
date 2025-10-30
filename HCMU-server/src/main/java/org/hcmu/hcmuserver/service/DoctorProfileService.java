package org.hcmu.hcmuserver.service;

import org.hcmu.hcmucommon.result.Result;
import org.hcmu.hcmupojo.dto.DoctorProfileDTO;
import org.hcmu.hcmupojo.dto.PageDTO;

import java.util.List;

public interface DoctorProfileService {
    // 创建医生档案
    Result<DoctorProfileDTO.DoctorProfileListDTO> createDoctorProfile(DoctorProfileDTO.DoctorProfileCreateDTO createDTO);

    // 分页查询医生档案
    Result<PageDTO<DoctorProfileDTO.DoctorProfileListDTO>> getDoctorProfiles(DoctorProfileDTO.DoctorProfileGetRequestDTO requestDTO);

    // 根据用户ID获取医生档案详情
    Result<DoctorProfileDTO.DoctorProfileDetailDTO> getDoctorProfileByUserId(Long userId);

    // 更新医生档案
    Result<String> updateDoctorProfile(Long doctorProfileId, DoctorProfileDTO.DoctorProfileUpdateDTO updateDTO);

    // 逻辑删除医生档案
    Result<String> deleteDoctorProfile(Long doctorProfileId);

    // 批量删除医生档案
    Result<String> batchDeleteDoctorProfiles(List<Long> doctorProfileIds);
}