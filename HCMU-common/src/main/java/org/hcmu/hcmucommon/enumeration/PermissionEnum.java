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

    LOG_PAGE(8, "logPage"),

    /*
     * 科室管理
     */
    DEPART_MG_PAGE(9, "departMGPage"),
    ADD_DEPART(10, "addDepart"),
    DEL_DEPART(11, "delDepart"),
    ALT_DEPART(12, "altDepart"),
    CHECK_DEPART(13, "checkDepart");


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
