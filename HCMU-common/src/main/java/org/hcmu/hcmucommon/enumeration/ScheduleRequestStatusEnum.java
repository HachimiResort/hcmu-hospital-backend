package org.hcmu.hcmucommon.enumeration;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

/**
 * 医生排班申请状态
 */
@Getter
public enum ScheduleRequestStatusEnum {
    PENDING(1, "待审批"),
    APPROVED(2, "已同意"),
    REJECTED(3, "已拒绝"),
    CANCELLED(4, "已撤销");

    @EnumValue
    private final Integer code;
    private final String desc;

    ScheduleRequestStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
