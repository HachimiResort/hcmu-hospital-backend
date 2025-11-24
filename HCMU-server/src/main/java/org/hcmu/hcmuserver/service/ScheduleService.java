package org.hcmu.hcmuserver.service;

import com.github.yulichang.base.MPJBaseService;
import org.hcmu.hcmucommon.result.Result;
import org.hcmu.hcmupojo.dto.AppointmentDTO;
import org.hcmu.hcmupojo.dto.ScheduleDTO;
import org.hcmu.hcmupojo.dto.PageDTO;
import org.hcmu.hcmupojo.entity.DoctorSchedule;

import java.util.List;

public interface ScheduleService extends MPJBaseService<DoctorSchedule> {
    Result<ScheduleDTO.ScheduleListDTO> createSchedule(ScheduleDTO.ScheduleCreateDTO createDTO);
    Result<PageDTO<ScheduleDTO.ScheduleListDTO>> findAllSchedules(ScheduleDTO.ScheduleGetRequestDTO requestDTO);
    Result<ScheduleDTO.ScheduleListDTO> findScheduleById(Long scheduleId);
    Result<String> updateScheduleById(Long scheduleId, ScheduleDTO.ScheduleUpdateDTO updateDTO);
    Result<String> deleteScheduleById(Long scheduleId);
    Result<String> batchDeleteSchedules(List<Long> scheduleIds); // 新增批量删除方法
    Result<String> copySchedule(ScheduleDTO.ScheduleCopyDTO copyDTO); // 新增复制排班方法
    Result<AppointmentDTO.AppointmentListDTO> appointSchedule(Long scheduleId);
    Result<AppointmentDTO.AppointmentListDTO> appointSchedule(Long scheduleId, Long patientUserId); // 重载方法，指定患者ID
    Result<List<ScheduleDTO.ScheduleListDTO>> getDoctorSchedules(Long doctorUserId); // 新增查询医生排班方法
    Result<List<ScheduleDTO.SchedulePatientDTO>> getSchedulePatients(Long doctorUserId, Long scheduleId); // 新增查询排班患者方法
}
