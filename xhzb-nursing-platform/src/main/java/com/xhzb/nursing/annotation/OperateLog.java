package com.xhzb.nursing.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
//@Documented
@Inherited
public @interface OperateLog {
    String value();
    String module();
    boolean recordParam();
}
