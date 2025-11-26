package org.hcmu.hcmucommon.enumeration;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

/**
 * @author Kyy008
 * @description 候补名单枚举
 * @date 2025-11-24
 */
@Getter
public enum WaitListEnum {
    WAITING(1, "候补中"),
    NOTIFIED(2, "已通知"),
    BOOKED(3, "已预约"),
    EXPIRED(4, "已过期"),
    CANCELLED(5, "已取消");

    @EnumValue
    private final Integer code;
    private final String desc;

    WaitListEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}