package org.hcmu.hcmupojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

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
}