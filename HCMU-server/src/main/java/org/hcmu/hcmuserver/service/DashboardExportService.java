package org.hcmu.hcmuserver.service;

import org.hcmu.hcmucommon.result.Result;
import org.hcmu.hcmupojo.dto.DashboardExportDTO;

/**
 * 大屏数据导出服务
 */
public interface DashboardExportService {

    /**
     * 导出大屏数据为Excel并上传到云端
     *
     * @param exportDTO 导出参数
     * @return 云端文件地址
     */
    Result<String> exportDashboardData(DashboardExportDTO exportDTO);
}