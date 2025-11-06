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
    ADD_DOCTOR(14, "addDoctor"),
    ALT_DOCTOR(15, "altDoctor"),
    DEL_DOCTOR(16, "delDoctor"),
    CHECK_DOCTOR(17, "checkDoctor"),

    /*
     * 患者档案管理
     */
    ADD_PATIENT(18, "addPatient"),
    ALT_PATIENT(19, "altPatient"),
    DEL_PATIENT(20, "delPatient"),
    CHECK_PATIENT(21, "checkPatient"),

    /*
     * 医生排班管理
     */
    ADD_SCHEDULE(22, "addSchedule"),
    ALT_SCHEDULE(23, "altSchedule"),
    DEL_SCHEDULE(24, "delSchedule"),
    CHECK_SCHEDULE(25, "checkSchedule"),
    /*
     *预约记录管理
     */
    CHECK_APPOINTMENT(26,"checkAppointment"),

    /*
     * 运营规则管理
     */
    CHECK_RULE(27, "checkRule"),
    ALT_RULE(28, "altRule");
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
