package org.hcmu.hcmuserver.service;

import org.hcmu.hcmucommon.result.Result;
import org.hcmu.hcmupojo.dto.PatientProfileDTO;
import org.hcmu.hcmupojo.dto.PageDTO;

import java.util.List;

public interface PatientProfileService {
    // 创建患者档案
    Result<PatientProfileDTO.PatientProfileListDTO> createPatientProfile(PatientProfileDTO.PatientProfileCreateDTO createDTO);

    // 分页查询患者档案
    Result<PageDTO<PatientProfileDTO.PatientProfileListDTO>> getPatientProfiles(PatientProfileDTO.PatientProfileGetRequestDTO requestDTO);

    // 根据用户ID获取患者档案详情
    Result<PatientProfileDTO.PatientProfileDetailDTO> getPatientProfileByUserId(Long userId);

    // 获取所有患者的详细档案
    Result<List<PatientProfileDTO.PatientProfileDetailDTO>> getAllPatients();

    // 根据用户ID更新患者档案
    Result<String> updatePatientProfileByUserId(Long userId, PatientProfileDTO.PatientProfileUpdateDTO updateDTO);

    // 根据用户ID更新患者档案（患者自己修改）
    Result<String> updateSelfPatientProfile(PatientProfileDTO.PatientProfileUpdateSelfDTO updateDTO);

    // 逻辑删除患者档案
    Result<String> deletePatientProfile(Long patientProfileId);

    // 批量删除患者档案
    Result<String> batchDeletePatientProfiles(List<Long> patientProfileIds);
}