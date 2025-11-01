package org.hcmu.hcmucommon.enumeration;

import com.baomidou.mybatisplus.annotation.EnumValue;

public enum SlotTypeEnum {
    GENERAL(1,"普通号"),
    EXPERT(2,"专家号"),
    SPECIAL(3,"特需号");

    @EnumValue
    private Integer code;

    private String desc;

    SlotTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }
    public String getDesc() {
        return desc;
    }

    public static SlotTypeEnum getEnumByCode(Integer code) {
        for (SlotTypeEnum e : SlotTypeEnum.values()) {
            if (e.getCode().equals(code)) {
                return e;
            }
        }
        return null;
    }

    public static SlotTypeEnum getEnumByDesc(String desc) {
        for (SlotTypeEnum e : SlotTypeEnum.values()) {
            if (e.getDesc().equals(desc)) {
                return e;
            }
        }
        return null;
    }
}
