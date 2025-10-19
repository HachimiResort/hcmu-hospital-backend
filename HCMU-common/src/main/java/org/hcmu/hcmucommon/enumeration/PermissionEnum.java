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
    GENERATE_CODE(6, "generateCode"),

    /*
     * 角色管理
     */
    MASTER_ROLE(42, "masterRole"),

    /*
     * 字段管理
     */
    ADD_FIELD(45, "addField");

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
