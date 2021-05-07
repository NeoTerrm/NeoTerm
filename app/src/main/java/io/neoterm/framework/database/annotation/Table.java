package io.neoterm.framework.database.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author kiva
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Table {
  /**
   * @return 表名
   */
  String name() default "";

  /**
   * @return 在表创建后需要回调的方法
   */
  String afterTableCreate() default "";
}
