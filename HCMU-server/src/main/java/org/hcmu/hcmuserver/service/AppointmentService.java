package org.hcmu.hcmuserver.service;

import com.github.yulichang.base.MPJBaseService;
import org.hcmu.hcmucommon.result.Result;
import org.hcmu.hcmupojo.dto.AppointmentDTO;
import org.hcmu.hcmupojo.dto.PageDTO;

import java.util.List;

public interface AppointmentService{
    Result<PageDTO<AppointmentDTO.AppointmentListDTO>> getAppointments(AppointmentDTO.AppointmentGetRequestDTO appointmentGetRequsetDTO);

    public Result<AppointmentDTO.AppointmentDetailDTO> getAppointmentById(Long appointmentId);//根据id查找

    public Result<PageDTO<AppointmentDTO.AppointmentDetailDTO>> getAppointmentsByPatientUserId(Long userId);//根据患者用户id查找
}
