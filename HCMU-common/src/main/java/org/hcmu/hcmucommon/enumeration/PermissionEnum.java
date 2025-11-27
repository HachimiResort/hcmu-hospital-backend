package org.hcmu.hcmucommon.enumeration;

import com.baomidou.mybatisplus.annotation.EnumValue;

/**
 * 权限枚举
 * @Author Kyy008
 * @Date 2025-10-19
 */
public enum PermissionEnum {
    /*
    人事管理
     */
    PERSON_MG_PAGE(1, "personMGPage"),
    ADD_MB(2, "addMB"),
    DEL_MB(3, "delMB"),
    ALT_MB(4, "altMB"),
    CHECK_MB(5, "checkMB"),
    IMPORT_MB(6, "importMB"),

    /*
     * 角色管理
     */
    MASTER_ROLE(7, "masterRole"),

    /*
     * 日志管理
     */
    LOG_PAGE(8, "logPage"),

    /*
     * 科室管理
     */
    DEPART_MG_PAGE(9, "departMGPage"),
    ADD_DEPART(10, "addDepart"),
    DEL_DEPART(11, "delDepart"),
    ALT_DEPART(12, "altDepart"),
    CHECK_DEPART(13, "checkDepart"),

    /*
     *医生档案管理
     */
    DOCTOR_MG_PAGE(14, "doctorMGPage"),
    ADD_DOCTOR(15, "addDoctor"),
    ALT_DOCTOR(16, "altDoctor"),
    CHECK_DOCTOR(17, "checkDoctor"),

    /*
     * 患者档案管理
     */
    PATIENT_MG_PAGE(18, "patientMGPage"),
    ADD_PATIENT(19, "addPatient"),
    ALT_PATIENT(20, "altPatient"),
    CHECK_PATIENT(21, "checkPatient"),

    /*
     * 医生排班管理
     */
    SCHEDULE_MG_PAGE(22, "scheduleMGPage"),
    ADD_SCHEDULE(23, "addSchedule"),
    ALT_SCHEDULE(24, "altSchedule"),
    DEL_SCHEDULE(25, "delSchedule"),
    CHECK_SCHEDULE(26, "checkSchedule"),
    /*
     *预约记录管理
     */
    APPOINTMENT_MG_PAGE(27, "appointmentMGPage"),
    ADD_APPOINTMENT(28, "addAppointment"),
    ALT_APPOINTMENT(29, "altAppointment"),
    CHECK_APPOINTMENT(30,"checkAppointment"),


    /*
     * 运营规则管理
     */
    RULE_MG_PAGE(31, "ruleMGPage"),
    CHECK_RULE(32, "checkRule"),
    ALT_RULE(33, "altRule"),


    

    /*
     * 医生页面
     */
    DOCTOR_WORK_PAGE(62, "doctorWorkPage"),

    /*
     * 排班模板管理
     */
    TEMPLATE_MG_PAGE(34, "templateMGPage"),
    ADD_TEMPLATE(35, "addtemplate"),
    ALT_TEMPLATE(36, "alttemplate"),
    DEL_TEMPLATE(37, "deltemplate"),
    CHECK_TEMPLATE(38, "checktemplate"),

    /*
     * 候补管理
     */
    WAITLIST_MG_PAGE(39, "waitlistMGPage"),
    ADD_WAITLIST(40, "addwaitlist"),
    ALT_WAITLIST(41, "altwaitlist"),
    DEL_WAITLIST(42, "delwaitlist"),
    CHECK_WAITLIST(43, "checkwaitlist"),

    APPROVE_SCHEDULE_REQUEST(44, "approveScheduleRequest");

    PermissionEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public Integer getCode() {
        return this.code;
    }

    @EnumValue
    private final Integer code;

    private final String name;
}
