package org.hcmu.hcmuserver.service;

import org.hcmu.hcmucommon.result.Result;
import org.hcmu.hcmupojo.dto.PageDTO;
import org.hcmu.hcmupojo.dto.WaitlistDTO;

public interface WaitlistService {
    public Result<PageDTO<WaitlistDTO.WaitlistListDTO>> getWaitlists(WaitlistDTO.WaitlistGetRequestDTO requestDTO);

    public Result<WaitlistDTO.WaitlistDetailDTO> getWaitlistById(Long waitlistId);

}
