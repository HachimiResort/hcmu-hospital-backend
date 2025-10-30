package org.hcmu.hcmucommon.enumeration;

import com.baomidou.mybatisplus.annotation.EnumValue;

public enum RoleTypeEnum {
    SYS(1,"系统角色"),
    DOCTOR(2,"医生角色"),
    PATIENT(3,"患者角色");

    @EnumValue
    private Integer code;

    private String desc;

    RoleTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }
    public String getDesc() {
        return desc;
    }

    public static RoleTypeEnum getEnumByCode(Integer code) {
        for (RoleTypeEnum e : RoleTypeEnum.values()) {
            if (e.getCode().equals(code)) {
                return e;
            }
        }
        return null;
    }

    public static RoleTypeEnum getEnumByDesc(String desc) {
        for (RoleTypeEnum e : RoleTypeEnum.values()) {
            if (e.getDesc().equals(desc)) {
                return e;
            }
        }
        return null;
    }
}
