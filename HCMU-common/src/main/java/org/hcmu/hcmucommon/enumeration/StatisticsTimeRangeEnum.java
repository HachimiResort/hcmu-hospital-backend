package org.hcmu.hcmucommon.enumeration;

import lombok.Getter;

/**
 * 统计时间范围枚举
 */
@Getter
public enum StatisticsTimeRangeEnum {
    ALL("全部", null),
    MONTH("本月", 30),
    WEEK("本周", 7),
    DAY("本日", 1);

    private final String description;
    private final Integer days;

    StatisticsTimeRangeEnum(String description, Integer days) {
        this.description = description;
        this.days = days;
    }
}