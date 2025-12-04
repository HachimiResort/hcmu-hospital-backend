package org.hcmu.hcmupojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

public class AppointmentDashboardVO {

    /**
     * 号源统计响应
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AppointmentStatisticsVO {

        /**
         * 待支付的预约号数量
         */
        private Long pendingPaymentCount;

        /**
         * 已预约的预约号数量
         */
        private Long bookedCount;

        /**
         * 完成就诊的预约号数量
         */
        private Long completedCount;

        /**
         * 已取消的预约号数量
         */
        private Long cancelledCount;

        /**
         * 已爽约的预约号数量
         */
        private Long noShowCount;

        /**
         * 收入统计
         */
        private BigDecimal totalRevenue;
    }

    /**
     * 时段-预约量曲线图
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AppointmentTrendVO {

        private List<TrendDataPoint> trendData;
    }

    /**
     * 曲线图数据点
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrendDataPoint {

        /**
         * 预约数量
         */
        private Long count;

        /**
         * 时段标签
         */
        private String periodLabel;
    }
}