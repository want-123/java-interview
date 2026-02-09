package com.mk.customagentannotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: milk
 * @Date: 2025/12/19/16:43
 * @Description:
 */
@Target(ElementType.METHOD) // 标注在接口/类上
@Retention(RetentionPolicy.RUNTIME) // 运行时可反射获取
public @interface ToolsSecurity {
    SecurityProperty[] properties() default {};
}
