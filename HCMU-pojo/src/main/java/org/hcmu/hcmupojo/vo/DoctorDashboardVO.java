package org.hcmu.hcmupojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

public class DoctorDashboardVO {


    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DoctorVisitRankVO {

        private List<DoctorVisitRankItemVO> rankList;
    }

    /**
     * 医生就诊量排行榜项
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DoctorVisitRankItemVO {

        private Integer rank;

        private Long doctorUserId;

        private String doctorName;

        private Long visitCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DoctorIncomeRankVO {

        private List<DoctorIncomeRankItemVO> rankList;
    }

    /**
     * 医生收入排行榜项
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DoctorIncomeRankItemVO {

        private Integer rank;

        private Long doctorUserId;

        private String doctorName;

        private BigDecimal totalIncome;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DoctorAppointmentRateVO {

        private List<DoctorAppointmentRateItemVO> rateList;
    }

    /**
     * 医生预约率统计项
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DoctorAppointmentRateItemVO {

        private Long doctorUserId;

        private String doctorName;

        private Long completedCount;

        private Long cancelledCount;

        private Long noShowCount;

        private BigDecimal completedRate;

        private BigDecimal cancelledRate;

        private BigDecimal noShowRate;
    }
}
