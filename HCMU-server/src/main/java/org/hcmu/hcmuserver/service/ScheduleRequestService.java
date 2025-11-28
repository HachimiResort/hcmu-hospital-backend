package org.hcmu.hcmuserver.service;

import org.hcmu.hcmucommon.result.Result;
import org.hcmu.hcmupojo.dto.PageDTO;
import org.hcmu.hcmupojo.dto.ScheduleRequestDTO;

public interface ScheduleRequestService {

    Result<ScheduleRequestDTO.ScheduleRequestDetailDTO> createScheduleRequest(ScheduleRequestDTO.ScheduleRequestCreateDTO createDTO);

    Result<PageDTO<ScheduleRequestDTO.ScheduleRequestListDTO>> getScheduleRequests(ScheduleRequestDTO.ScheduleRequestGetRequestDTO requestDTO);

    Result<ScheduleRequestDTO.ScheduleRequestDetailDTO> getScheduleRequestById(Long requestId);

    Result<String> updateScheduleRequestById(Long requestId, ScheduleRequestDTO.ScheduleRequestUpdateDTO updateDTO);

    Result<String> deleteScheduleRequestById(Long requestId);

    /**
     * 审批排班申请（同意或拒绝）
     */
    Result<String> handleScheduleRequest(Long requestId, ScheduleRequestDTO.ScheduleRequestHandleDTO handleDTO);

    /**
     * 医生撤销自己的申请
     */
    Result<String> cancelScheduleRequest(Long requestId);
}
