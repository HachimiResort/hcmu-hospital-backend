package org.hcmu.hcmupojo.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hcmu.hcmucommon.enumeration.StatisticsTimeRangeEnum;

/**
 * 医生大屏数据 DTO
 */
public class DoctorDashboardDTO {


    @Data
    public static class DoctorVisitRankDTO {

        @NotNull(message = "时间范围不能为空")
        private StatisticsTimeRangeEnum timeRange;

        @NotNull(message = "排行数量不能为空")
        @Min(value = 1, message = "排行数量至少为1")
        @Max(value = 50, message = "排行数量最多为50")
        private Integer limit;
    }

    @Data
    public static class DoctorIncomeRankDTO {

        @NotNull(message = "时间范围不能为空")
        private StatisticsTimeRangeEnum timeRange;

        @NotNull(message = "排行数量不能为空")
        @Min(value = 1, message = "排行数量至少为1")
        @Max(value = 50, message = "排行数量最多为50")
        private Integer limit;
    }

    @Data
    public static class DoctorAppointmentRateDTO {

        @NotNull(message = "时间范围不能为空")
        private StatisticsTimeRangeEnum timeRange;
    }
}
