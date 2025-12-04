package org.hcmu.hcmuserver.service;

import org.hcmu.hcmucommon.result.Result;
import org.hcmu.hcmupojo.dto.AppointmentDashboardDTO;
import org.hcmu.hcmupojo.vo.AppointmentDashboardVO;

/**
 * 号源可视化大屏服务接口
 */
public interface AppointmentDashboardService {

    /**
     * 获取号源统计
     * 根据时间范围统计各状态的预约数量及总收入
     *
     * @param requestDTO 号源统计请求参数
     * @return 号源统计数据
     */
    Result<AppointmentDashboardVO.AppointmentStatisticsVO> getAppointmentStatistics(AppointmentDashboardDTO.AppointmentStatisticsDTO requestDTO);

    /**
     * 获取时段-预约量曲线图数据
     * 根据时间范围返回不同粒度的预约量趋势数据
     *
     * @param requestDTO 时段-预约量曲线图请求参数
     * @return 时段-预约量曲线图数据
     */
    Result<AppointmentDashboardVO.AppointmentTrendVO> getAppointmentTrend(AppointmentDashboardDTO.AppointmentTrendDTO requestDTO);
}
