package io.neoterm.framework.database.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author kiva
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ID {
  /**
   * 只对Integer类型的ID字段有效
   *
   * @return 是否为自增长
   */
  boolean autoIncrement() default false;
}
