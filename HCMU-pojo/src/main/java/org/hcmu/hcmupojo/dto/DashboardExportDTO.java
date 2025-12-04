package org.hcmu.hcmupojo.dto;


import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hcmu.hcmucommon.enumeration.StatisticsTimeRangeEnum;

/**
 * 大屏数据导出 DTO
 */
@Data
public class DashboardExportDTO {

    private StatisticsTimeRangeEnum timeRange =  StatisticsTimeRangeEnum.WEEK;

    private Integer rankLimit = 10;
}
