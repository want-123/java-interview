package com.mk.customagentannotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: milk
 * @Date: 2025/12/19/16:40
 * @Description:
 */
@Target(ElementType.TYPE) // 标注在接口/类上
@Retention(RetentionPolicy.RUNTIME) // 运行时可反射获取
public @interface AgentAnnotation {
    long id();
}
