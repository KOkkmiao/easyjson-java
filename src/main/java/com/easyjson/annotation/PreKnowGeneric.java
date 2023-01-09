package com.easyjson.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.CLASS;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @Author: author
 * @Description:
 * @Date: 2023/1/8 7:51 下午
 * @Version: 1.0
 */
@Target(ElementType.FIELD)
@Retention(CLASS)
public @interface PreKnowGeneric {
    Class[] map() default {};
    Class list() default Void.class;
}
