package org.hcmu.hcmuserver.service;

import org.hcmu.hcmucommon.result.Result;
import org.hcmu.hcmupojo.dto.DoctorDashboardDTO;
import org.hcmu.hcmupojo.vo.DoctorDashboardVO;

/**
 * 医生大屏服务接口
 */
public interface DoctorDashboardService {

    /**
     * 获取医生就诊量排行
     * 根据时间范围统计医生已完成的就诊量并排序
     *
     * @param requestDTO 医生就诊量排行请求参数
     * @return 医生就诊量排行数据
     */
    Result<DoctorDashboardVO.DoctorVisitRankVO> getDoctorVisitRank(DoctorDashboardDTO.DoctorVisitRankDTO requestDTO);

    /**
     * 获取医生收入排行
     * 根据时间范围统计医生的总收入并排序
     *
     * @param requestDTO 医生收入排行请求参数
     * @return 医生收入排行数据
     */
    Result<DoctorDashboardVO.DoctorIncomeRankVO> getDoctorIncomeRank(DoctorDashboardDTO.DoctorIncomeRankDTO requestDTO);

    /**
     * 获取医生预约率统计
     * 根据时间范围统计医生的就诊率、取消率、爽约率
     *
     * @param requestDTO 医生预约率统计请求参数
     * @return 医生预约率统计数据
     */
    Result<DoctorDashboardVO.DoctorAppointmentRateVO> getDoctorAppointmentRate(DoctorDashboardDTO.DoctorAppointmentRateDTO requestDTO);
}
