package org.hcmu.hcmuserver.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.github.yulichang.base.MPJBaseServiceImpl;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.hcmu.hcmucommon.result.Result;
import org.hcmu.hcmupojo.dto.LogDTO;
import org.hcmu.hcmupojo.dto.PageDTO;
import org.hcmu.hcmupojo.entity.Log;
import org.hcmu.hcmupojo.entity.User;
import org.hcmu.hcmuserver.mapper.user.LogMapper;
import org.hcmu.hcmuserver.service.LogService;
import org.springframework.stereotype.Service;

@Service
public class LogServiceImpl extends MPJBaseServiceImpl<LogMapper, Log> implements LogService {

    @Override
    public Result<PageDTO<LogDTO.LogListDTO>> findLogs(LogDTO.LogGetRequestDTO logGetRequestDTO) {
        MPJLambdaWrapper<Log> queryWrapper = new MPJLambdaWrapper<Log>();
        queryWrapper.select(Log::getLogId, Log::getOperation, Log::getUserId, Log::getCreateTime, Log::getIp)
                    .select(User::getUserName, User::getUserName)
                    .leftJoin(User.class, User::getUserId, Log::getUserId)
                    .like(logGetRequestDTO.getUserName() != null, User::getUserName, logGetRequestDTO.getUserName())
                    .like(logGetRequestDTO.getUserName() != null, User::getUserName, logGetRequestDTO.getUserName())
                    .like(logGetRequestDTO.getOperation() != null, Log::getOperation, logGetRequestDTO.getOperation())
                    .like(logGetRequestDTO.getIp() != null, Log::getIp, logGetRequestDTO.getIp())
                    .orderByDesc(Log::getCreateTime);
        IPage<LogDTO.LogListDTO> page = baseMapper.selectJoinPage(new Page<>(logGetRequestDTO.getPageNum(), logGetRequestDTO.getPageSize()), LogDTO.LogListDTO.class, queryWrapper);
        return Result.success(new PageDTO<LogDTO.LogListDTO>(page));
    }
}
