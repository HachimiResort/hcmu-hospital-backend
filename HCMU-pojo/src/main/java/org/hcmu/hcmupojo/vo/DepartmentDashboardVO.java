package org.hcmu.hcmupojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 科室可视化大屏数据 VO
 */
public class DepartmentDashboardVO {

    /**
     * 科室负荷统计
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoadStatisticsVO {
        /**
         * 科室总数
         */
        private Integer totalDepartments;

        /**
         * 高负荷科室数量
         */
        private Integer highLoadDepartments;

        /**
         * 中负荷科室数量
         */
        private Integer mediumLoadDepartments;

        /**
         * 低负荷科室数量
         */
        private Integer lowLoadDepartments;

        /**
         * 空闲科室数量
         */
        private Integer idleDepartments;

        /**
         * 高负荷科室列表
         */
        private List<DepartmentInfoVO> highLoadDepartmentList;

        /**
         * 中负荷科室列表
         */
        private List<DepartmentInfoVO> mediumLoadDepartmentList;

        /**
         * 低负荷科室列表
         */
        private List<DepartmentInfoVO> lowLoadDepartmentList;

        /**
         * 空闲科室列表
         */
        private List<DepartmentInfoVO> idleDepartmentList;
    }

    /**
     * 科室信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DepartmentInfoVO {

        private Long departmentId;

        private String departmentName;
    }

    /**
     * 科室预约排行榜项
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AppointmentRankItemVO {

        private Integer rank;

        private Long departmentId;

        private String departmentName;

        private Long appointmentCount;
    }

    /**
     * 科室预约排行榜响应
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AppointmentRankVO {

        private List<AppointmentRankItemVO> rankList;
    }
}
