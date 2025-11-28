package org.hcmu.hcmucommon.enumeration;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

/**
 * @author Codex
 * @description 医生排班申请类型枚举
 */
@Getter
public enum ScheduleRequestTypeEnum {
    SHIFT_CHANGE(1, "调班"),
    LEAVE(2, "休假"),
    EXTRA_SLOT(3, "加号");

    @EnumValue
    private final Integer code;
    private final String desc;

    ScheduleRequestTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
