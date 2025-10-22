package org.hcmu.hcmucommon.annotation;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * @Author: Kyy008
 * @Description: 幂等注解
 * @Date: 2025/10/23
 **/
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Idempotent {

    /**
     * 幂等的超时时间，默认为 2 秒
     */
    int timeout() default 2;

    /**
     * 时间单位，默认为 SECONDS 秒
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;

    /**
     * redis锁前缀
     */
    String keyPrefix() default "idempotent:";

    /**
     * 提示信息，正在执行中的提示
     */
    String message() default "重复请求，请稍后重试";
}
