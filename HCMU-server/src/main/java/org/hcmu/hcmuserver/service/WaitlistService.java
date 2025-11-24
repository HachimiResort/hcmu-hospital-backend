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
}
