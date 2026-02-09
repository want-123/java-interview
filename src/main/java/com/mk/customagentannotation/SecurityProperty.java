package com.mk.customagentannotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: milk
 * @Date: 2025/12/19/16:49
 * @Description:
 */
@Retention(RetentionPolicy.RUNTIME) // 运行时可反射获取（核心）
@Target({})
public @interface SecurityProperty {
    String key();
    String value();
}
