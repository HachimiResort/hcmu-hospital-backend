package org.hcmu.hcmucommon.enumeration;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;



/**
 * 运营规则配置项定义枚举
 *
 * 说明：
 * - 提供：所属规则类型、配置 key、默认值、说明
 * - 建议：前端表单的字段名称、后端解析逻辑统一基于本枚举，避免写死魔法字符串
 */


public enum OpRuleEnum {
    
    /* ====================== 挂号规则 BOOKING ====================== */

    /**
     * 单用户在“全院”范围内每天允许的最大预约次数
     * 用于防止同一人过度占用号源
     */
    BOOKING_MAX_PER_DAY_GLOBAL(
        101,
        OpRuleType.BOOKING,
        "booking.max_per_day_global",
        3,
        "单用户全院每日挂号上限（默认 3 次）"
    ),

    /**
     * 单用户在“同一科室”每天允许的最大预约次数
     * 如既要限制总量，又要防止某科室被同一人刷爆
     */
    BOOKING_MAX_PER_DAY_PER_DEPT(
        102,
        OpRuleType.BOOKING,
        "booking.max_per_day_per_dept",
        2,
        "单用户同科室每日挂号上限（默认 2 次）"
    ),

    /**
     * 可提前预约的最大天数
     * 例如设置为 7：只能预约未来 7 天内的号源
     */
    BOOKING_MAX_FUTURE_DAYS(
        103,
        OpRuleType.BOOKING,
        "booking.max_future_days",
        7,
        "可预约未来天数上限（默认 7 天）"
    ),

    /**
     * 就诊前多少小时停止新预约/改约
     * 防止太临近就诊还在变动号源
     */
    BOOKING_MIN_HOURS_BEFORE_BOOKING_END(
        104,
        OpRuleType.BOOKING,
        "booking.min_hours_before_booking_end",
        2,
        "就诊前停止预约/改约的时间（默认 2 小时）"
    ),

    /**
     * 是否限制同一时间段的多重预约
     * 典型场景：同一用户在同一时间只能保留 1 个有效预约
     * 1 = 限制；0 = 不限制（默认限制）
     */
    BOOKING_LIMIT_SAME_TIMESLOT(
        105,
        OpRuleType.BOOKING,
        "booking.limit_same_timeslot",
        1,
        "是否限制同一时段多重预约（1=限制，默认 1）"
    ),

    /* ====================== 退号规则 CANCEL ====================== */

    /**
     * 就诊前多少小时内退号仍视为“正常/免费退号”
     */
    CANCEL_FREE_CANCEL_HOURS(
        201,
        OpRuleType.CANCEL,
        "cancel.free_cancel_hours",
        24,
        "免费退号时限（默认就诊前 24 小时及以上）"
    ),

    /**
     * 就诊前多少小时内禁止退号
     * 例如 2：就诊前 0-2 小时不允许退号
     */
    CANCEL_FORBID_CANCEL_HOURS(
        202,
        OpRuleType.CANCEL,
        "cancel.forbid_cancel_hours",
        2,
        "禁止退号时限（默认就诊前 2 小时内不可退）"
    ),

    /**
     * 在统计窗口内允许的最大“未到/爽约”次数
     * 超过触发惩罚策略（如加入黑名单）
     */
    CANCEL_NO_SHOW_LIMIT(
        203,
        OpRuleType.CANCEL,
        "cancel.no_show_limit",
        3,
        "爽约次数阈值（默认 3 次）"
    ),

    /**
     * 触发爽约阈值后，限制挂号的天数
     */
    CANCEL_NO_SHOW_PUNISH_DAYS(
        204,
        OpRuleType.CANCEL,
        "cancel.no_show_punish_days",
        7,
        "爽约后限制挂号天数（默认 7 天）"
    ),

    /**
     * 取消预约是否需要填写理由
     */
    CANCEL_NEED_REASON(
        205,
        OpRuleType.CANCEL,
        "cancel.need_reason",
        1,
        "取消预约是否需要填写原因（默认 true）"
    ),

    /* ====================== 候补规则 WAITING_LIST ====================== */

    /**
     * 单个号源允许的候补队列最大长度
     */
    WAITLIST_MAX_QUEUE_LENGTH(
        301,
        OpRuleType.WAITING_LIST,
        "waitlist.max_queue_length",
        20,
        "单号源候补队列长度上限（默认 20 人）"
    ),

    /**
     * 候补中签后锁号时间（分钟）
     * 在该时间内用户未确认则视为放弃，释放给下一位
     */
    WAITLIST_LOCK_MINUTES(
        302,
        OpRuleType.WAITING_LIST,
        "waitlist.lock_minutes",
        15,
        "候补中签锁号时间（默认 15 分钟）"
    ),

    /* ====================== 惩罚 / 黑名单 PUNISH ====================== */

    /**
     * 高频爽约用户加入黑名单后的默认限制天数
     * 可与 CANCEL_NO_SHOW_PUNISH_DAYS 共用或细分
     */
    PUNISH_BLACKLIST_DAYS(
        401,
        OpRuleType.PUNISH,
        "punish.blacklist_days",
        7,
        "黑名单限制挂号天数（默认 7 天）"
    ),

    /**
     * 黑名单判定使用的爽约次数阈值
     * 可以和 CANCEL_NO_SHOW_LIMIT 分离配置
     */
    PUNISH_NO_SHOW_THRESHOLD(
        402,
        OpRuleType.PUNISH,
        "punish.no_show_threshold",
        3,
        "黑名单爽约次数阈值（默认 3 次）"
    ),

    /* ====================== 加号规则 EXTRA ====================== */

    /**
     * 单医生单日允许的最大加号数量
     */
    EXTRA_MAX_EXTRA_PER_DAY(
        501,
        OpRuleType.EXTRA,
        "extra.max_extra_per_day",
        5,
        "单医生单日加号上限（默认 5 个）"
    ),

    /**
     * 加号是否必须填写理由
     * true：强制记录原因，方便审计
     */
    EXTRA_NEED_REASON(
        502,
        OpRuleType.EXTRA,
        "extra.need_reason",
        1,
        "加号是否需要填写原因（默认 true）"
    );


    // ====================== 字段定义 ======================

    private final Integer code;
    private final OpRuleType type;
    private final String key;
    private final Integer defaultValue;
    private final String description;

    OpRuleEnum(Integer code, OpRuleType type, String key, Integer defaultValue, String description) {
        this.code = code;
        this.type = type;
        this.key = key;
        this.defaultValue = defaultValue;
        this.description = description;
    }

    public Integer getCode() {
        return code;
    }

    public OpRuleType getType() {
        return type;
    }

    public String getKey() {
        return key;
    }

    @SuppressWarnings("unchecked")
    public Integer getDefaultValue() {
        return defaultValue;
    }

    public Object getRawDefaultValue() {
        return defaultValue;
    }

    public String getDescription() {
        return description;
    }


    /**
     * 规则类型枚举：
     * 用来对不同业务阶段的规则进行分组管理
     */
    public enum OpRuleType {
        BOOKING,       // 挂号相关
        CANCEL,        // 退号相关
        WAITING_LIST,  // 候补队列
        PUNISH,        // 惩罚/黑名单
        EXTRA,         // 加号相关
    }
}