package org.hcmu.hcmupojo.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hcmu.hcmucommon.enumeration.StatisticsTimeRangeEnum;

/**
 * 科室大屏数据 DTO
 */
public class DepartmentDashboardDTO {

    /**
     * 科室预约排行请求
     */
    @Data
    public static class AppointmentRankDTO {
     
        @NotNull(message = "时间范围不能为空")
        private StatisticsTimeRangeEnum timeRange;

        /**
         * 返回排行数量
         */
        @NotNull(message = "排行数量不能为空")
        @Min(value = 1, message = "排行数量至少为1")
        @Max(value = 30, message = "排行数量最多为30")
        private Integer limit;
    }
}