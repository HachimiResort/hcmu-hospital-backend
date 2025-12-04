package org.hcmu.hcmucommon.enumeration;

import com.baomidou.mybatisplus.annotation.EnumValue;

/**
 * redis存储键名称枚举
 * @Author Kyy008
 * @Date 2025-10-23
 */
public enum RedisEnum {

    LOGIN(1, "login:"),
    REGISTER(2, "register:"),
    REBIND(3, "rebind:"),
    FORGET(4, "forget:"),
    CHECK_IN_TOKEN(5, "checkin-token:");

    @EnumValue
    private Integer code;
    private String desc;

    RedisEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }


}
