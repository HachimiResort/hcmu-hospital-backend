package org.hcmu.hcmucommon.annotation;

import java.lang.annotation.*;

/**
 *
 * 用于记录日志的注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AutoLog {
    String value() default "";
}
