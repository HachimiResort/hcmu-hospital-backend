package org.hcmu.hcmuserver.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.github.yulichang.base.MPJBaseService;
import org.hcmu.hcmucommon.result.Result;
import org.hcmu.hcmupojo.dto.LogDTO;
import org.hcmu.hcmupojo.entity.Log;

public interface LogService extends MPJBaseService<Log> {

    /**
     * 获取日志
     * @param logGetRequestDTO 日志请求信息
     * @return BaseResponse 返回信息
     */
    Result<PageDTO<LogDTO.LogListDTO>> findLogs(LogDTO.LogGetRequestDTO logGetRequestDTO);
}
