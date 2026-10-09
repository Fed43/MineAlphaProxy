package com.minealphaproxy.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface MineAlphaPluginInfo {
    String id();
    String name() default "";
    String version() default "1.0";
    String author() default "";
    String description() default "";
    String[] depends() default {};
    String[] softDepends() default {};
}