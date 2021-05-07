package io.neoterm.framework.database;

/**
 * @author kiva
 */
public enum DatabaseDataType {
  /**
   * int类型
   */
  INTEGER,
  /**
   * String类型
   */
  TEXT,
  /**
   * float类型
   */
  FLOAT,
  /**
   * long类型
   */
  BIGINT,
  /**
   * double类型
   */
  DOUBLE;

  boolean nullable = true;

  /**
   * 数据类型是否允许为null
   */
  public DatabaseDataType nullable(boolean nullable) {
    this.nullable = nullable;
    return this;
  }

}
