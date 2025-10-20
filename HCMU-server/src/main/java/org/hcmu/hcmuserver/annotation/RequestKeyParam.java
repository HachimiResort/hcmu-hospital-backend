package org.hcmu.hcmuserver.annotation;

import java.lang.annotation.*;

/**
 *
 * @description 加上这个注解可以将参数设置为key(该key的作用是用来区分不同的请求)
 */
@Target({ElementType.METHOD, ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface RequestKeyParam {

}
