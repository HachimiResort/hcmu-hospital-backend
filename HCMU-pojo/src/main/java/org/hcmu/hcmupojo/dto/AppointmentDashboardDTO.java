package org.hcmu.hcmupojo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hcmu.hcmucommon.enumeration.StatisticsTimeRangeEnum;

/**
 * 号源大屏数据 DTO
 */
public class AppointmentDashboardDTO {

    /**
     * 号源统计请求
     */
    @Data
    public static class AppointmentStatisticsDTO {

        @NotNull(message = "时间范围不能为空")
        private StatisticsTimeRangeEnum timeRange;
    }
}