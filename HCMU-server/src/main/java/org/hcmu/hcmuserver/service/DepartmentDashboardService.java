package org.hcmu.hcmuserver.service;

import org.hcmu.hcmucommon.result.Result;
import org.hcmu.hcmupojo.dto.DepartmentDashboardDTO;
import org.hcmu.hcmupojo.vo.DepartmentDashboardVO;

/**
 * 科室可视化大屏服务接口
 */
public interface DepartmentDashboardService {

    /**
     * 获取科室负荷统计
     * 统计科室总数及各负荷等级的科室数量
     *
     * @return 科室负荷统计数据
     */
    Result<DepartmentDashboardVO.LoadStatisticsVO> getDepartmentLoadStatistics();

    /**
     * 获取科室预约排行
     * 根据时间范围统计各科室的预约数量并排行
     *
     * @param requestDTO 预约排行请求参数
     * @return 科室预约排行数据
     */
    Result<DepartmentDashboardVO.AppointmentRankVO> getDepartmentAppointmentRank(DepartmentDashboardDTO.AppointmentRankDTO requestDTO);
}
