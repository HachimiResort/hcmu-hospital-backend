package org.hcmu.hcmuserver.service;

import org.hcmu.hcmucommon.result.Result;
import org.hcmu.hcmupojo.dto.PageDTO;
import org.hcmu.hcmupojo.dto.WaitlistDTO;

public interface WaitlistService {
    Result<WaitlistDTO.WaitlistDetailDTO> createWaitlist(WaitlistDTO.WaitlistCreateDTO createDTO);

    Result<WaitlistDTO.WaitlistDetailDTO> patientJoinWaitlist(WaitlistDTO.PatientJoinDTO joinDTO);

    Result<PageDTO<WaitlistDTO.WaitlistListDTO>> getWaitlists(WaitlistDTO.WaitlistGetRequestDTO requestDTO);

    Result<WaitlistDTO.WaitlistDetailDTO> getWaitlistById(Long waitlistId);

    Result<String> updateWaitlistById(Long waitlistId, WaitlistDTO.WaitlistUpdateDTO updateDTO);

    Result<String> deleteWaitlistById(Long waitlistId);

    /**
     * 通知下一个候补患者
     * @param scheduleId 排班ID
     * @return 是否成功通知候补（true表示有候补并通知成功，false表示没有候补）
     */
    boolean notifyNextWaitlist(Long scheduleId);

    /**
     * 候补患者支付接口
     * @param waitlistId 候补ID
     * @return 预约信息
     */
    Result<org.hcmu.hcmupojo.dto.AppointmentDTO.AppointmentListDTO> payWaitlist(Long waitlistId);

    /**
     * 患者取消候补
     * @param waitlistId 候补ID
     * @return 操作结果
     */
    Result<String> cancelWaitlist(Long waitlistId);
}
