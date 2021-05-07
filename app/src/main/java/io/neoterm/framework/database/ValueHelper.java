package io.neoterm.framework.database;

import android.database.Cursor;

import java.lang.reflect.Field;

/**
 * @author kiva
 */
public class ValueHelper {

  /**
   * 根据数据类型将数据库中的值写入到相应的字段.
   *
   * @param cursor   游标
   * @param object   赋值对象
   * @param field    赋值字段
   * @param dataType 数据类型
   */
  public static void setKeyValue(Cursor cursor, Object object, Field field, DatabaseDataType dataType, int index) {
    switch (dataType) {
      case INTEGER:
        try {
          field.set(object, cursor.getInt(index));
        } catch (Throwable e) {
          try {
            //支持Boolean类型
            //因为Boolean默认当Integer处理
            field.set(object, cursor.getInt(index) != 0);
          } catch (IllegalAccessException ignored) {
          }
        }
        break;
      case TEXT:
        try {
          field.set(object, cursor.getString(index));
        } catch (IllegalAccessException e) {
        }
        break;
      case FLOAT:
        try {
          field.set(object, cursor.getFloat(index));
        } catch (IllegalAccessException e) {
        }
        break;
      case BIGINT:
        try {
          field.set(object, cursor.getLong(index));
        } catch (IllegalAccessException e) {
        }
        break;
      case DOUBLE:
        try {
          field.set(object, cursor.getDouble(index));
        } catch (IllegalAccessException e) {
        }
        break;

    }
  }

  /**
   * 根据数据类型从字段中提取值并转换为String
   *
   * @param dataType
   * @param field
   * @param o
   * @return
   * @throws IllegalAccessException 无法转换时抛出的异常
   */
  public static String valueToString(DatabaseDataType dataType, Field field, Object o) throws IllegalAccessException {
    switch (dataType) {
      case INTEGER:
        Object f = field.get(o);
        if (f instanceof Boolean) {
          return String.valueOf(((boolean) field.get(o)) ? 1 : 0);
        } else {
          return String.valueOf((int) field.get(o));
        }
      case TEXT:
        return "\"" + field.get(o) + "" + "\"";
      case DOUBLE:
        return String.valueOf((double) field.get(o));
      case FLOAT:
        return String.valueOf((float) field.get(o));
      case BIGINT:
        return String.valueOf((long) field.get(o));
    }
    return null;
  }

  /**
   * 根据数据类型将对象转换为String
   *
   * @param dataType
   * @param o
   * @return
   */
  public static String valueToString(DatabaseDataType dataType, Object o) {
    switch (dataType) {
      case INTEGER:
        if (o instanceof Boolean) {
          return ((boolean) o) ? "1" : "0";
        } else {
          return String.valueOf((int) o);
        }
      case TEXT:
        return "\"" + o + "\"";
      case DOUBLE:
        return String.valueOf((double) o);
      case FLOAT:
        return String.valueOf((float) o);
      case BIGINT:
        return String.valueOf((long) o);
    }
    return null;
  }
}
