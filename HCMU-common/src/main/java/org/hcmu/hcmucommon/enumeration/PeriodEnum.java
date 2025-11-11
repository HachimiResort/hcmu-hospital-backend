package org.hcmu.hcmucommon.enumeration;

import com.baomidou.mybatisplus.annotation.EnumValue;

/**
 * 出诊时段枚举
 * @Author Kyy008
 * @Date 2025-11-01
 */
public enum PeriodEnum {

    MORNING1(1,"上午 8:00-8:30"),
    MORNING2(2,"上午 8:30-9:00"),
    MORNING3(3,"上午 9:00-9:30"),
    MORNING4(4,"上午 9:30-10:00"),
    MORNING5(5,"上午 10:00-10:30"),
    MORNING6(6,"上午 10:30-11:00"),

    AFTERNOON1(7,"下午 13:30-14:00"),
    AFTERNOON2(8,"下午 14:00-14:30"),
    AFTERNOON3(9,"下午 14:30-15:00"),
    AFTERNOON4(10,"下午 15:00-15:30"),
    AFTERNOON5(11,"下午 15:30-16:00"),
    AFTERNOON6(12,"下午 16:00-16:30");
    @EnumValue
    private Integer code;

    private String desc;

    PeriodEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }
    public String getDesc() {
        return desc;
    }

    public static PeriodEnum getEnumByCode(Integer code) {
        for (PeriodEnum e : PeriodEnum.values()) {
            if (e.getCode().equals(code)) {
                return e;
            }
        }
        return null;
    }

    public static PeriodEnum getEnumByDesc(String desc) {
        for (PeriodEnum e : PeriodEnum.values()) {
            if (e.getDesc().equals(desc)) {
                return e;
            }
        }
        return null;
    }
}
