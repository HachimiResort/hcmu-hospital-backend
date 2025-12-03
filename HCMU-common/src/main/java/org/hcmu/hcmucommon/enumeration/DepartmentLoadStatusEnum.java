package org.hcmu.hcmucommon.enumeration;

import lombok.Getter;

/**
 * 科室负荷状态枚举
 */
@Getter
public enum DepartmentLoadStatusEnum {
    HIGH("高负荷", 0.0, 0.2),
    MEDIUM("中负荷", 0.2, 0.4),
    LOW("低负荷", 0.4, 0.6),
    IDLE("空闲", 0.6, 1.0);

    private final String description;
    private final Double minRate;
    private final Double maxRate;

    DepartmentLoadStatusEnum(String description, Double minRate, Double maxRate) {
        this.description = description;
        this.minRate = minRate;
        this.maxRate = maxRate;
    }

    /**
     * 根据可用率获取负荷状态
     *
     * @param availabilityRate 可用率（可用号源/总号源）
     * @return 负荷状态
     */
    public static DepartmentLoadStatusEnum getByAvailabilityRate(Double availabilityRate) {
        if (availabilityRate == null || availabilityRate < 0) {
            return IDLE;
        }

        for (DepartmentLoadStatusEnum status : values()) {
            if (availabilityRate > status.minRate && availabilityRate <= status.maxRate) {
                return status;
            }
        }

        return IDLE;
    }
}
