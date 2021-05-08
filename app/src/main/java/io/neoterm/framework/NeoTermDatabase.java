package io.neoterm.framework;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import io.neoterm.App;
import io.neoterm.framework.database.*;
import io.neoterm.framework.database.bean.TableInfo;
import io.neoterm.framework.reflection.Reflect;
import io.neoterm.utils.NLog;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

/**
 * @author Lody, Kiva
 * <p>
 * 基于<b>DTO(DataToObject)</b>映射的数据库操纵模型.
 * 通过少量可选的注解,即可构造数据模型.
 * 增删查改异常轻松.
 * @version 1.4
 */
public class NeoTermDatabase {

  /**
   * 缓存创建的数据库,以便防止数据库冲突.
   */
  private static final Map<String, NeoTermDatabase> DAO_MAP = new HashMap<>();

  /**
   * 数据库配置
   */
  private NeoTermSQLiteConfig neoTermSQLiteConfig;
  /**
   * 内部操纵的数据库执行类
   */
  private SQLiteDatabase db;

  /**
   * 默认构造器
   *
   * @param config
   */
  private NeoTermDatabase(NeoTermSQLiteConfig config) {

    this.neoTermSQLiteConfig = config;
    String saveDir = config.getSaveDir();
    if (saveDir != null
      && saveDir.trim().length() > 0) {
      this.db = createDataBaseFileOnSDCard(saveDir,
        config.getDatabaseName());
    } else {
      this.db = new SQLiteDataBaseHelper(App.Companion.get()
        .getApplicationContext()
        .getApplicationContext(), config)
        .getWritableDatabase();
    }

  }

  /**
   * 根据配置取得用于操纵数据库的WeLikeDao实例
   *
   * @param config
   * @return
   */
  public static NeoTermDatabase instance(NeoTermSQLiteConfig config) {
    if (config.getDatabaseName() == null) {
      throw new IllegalArgumentException("DBName is null in SqLiteConfig.");
    }
    NeoTermDatabase dao = DAO_MAP.get(config.getDatabaseName());
    if (dao == null) {
      dao = new NeoTermDatabase(config);
      synchronized (DAO_MAP) {
        DAO_MAP.put(config.getDatabaseName(), dao);
      }
    } else {//更换配置
      dao.applyConfig(config);
    }

    return dao;
  }

  /**
   * 根据默认配置取得操纵数据库的WeLikeDao实例
   *
   * @return
   */
  public static NeoTermDatabase instance() {
    return instance(NeoTermSQLiteConfig.DEFAULT_CONFIG);
  }

  /**
   * 取得操纵数据库的WeLikeDao实例
   *
   * @param dbName
   * @return
   */
  public static NeoTermDatabase instance(String dbName) {
    NeoTermSQLiteConfig config = new NeoTermSQLiteConfig();
    config.setDatabaseName(dbName);
    return instance(config);
  }

  /**
   * 取得操纵数据库的WeLikeDao实例
   *
   * @param dbVersion
   * @return
   */
  public static NeoTermDatabase instance(int dbVersion) {
    NeoTermSQLiteConfig config = new NeoTermSQLiteConfig();
    config.setDatabaseVersion(dbVersion);
    return instance(config);
  }

  /**
   * 取得操纵数据库的WeLikeDao实例
   *
   * @param listener
   * @return
   */
  public static NeoTermDatabase instance(OnDatabaseUpgradedListener listener) {
    NeoTermSQLiteConfig config = new NeoTermSQLiteConfig();
    config.setOnDatabaseUpgradedListener(listener);
    return instance(config);
  }

  /**
   * 取得操纵数据库的WeLikeDao实例
   *
   * @param dbName
   * @param dbVersion
   * @return
   */
  public static NeoTermDatabase instance(String dbName, int dbVersion) {
    NeoTermSQLiteConfig config = new NeoTermSQLiteConfig();
    config.setDatabaseName(dbName);
    config.setDatabaseVersion(dbVersion);
    return instance(config);
  }

  /**
   * 取得操纵数据库的WeLikeDao实例
   *
   * @param dbName
   * @param dbVersion
   * @param listener
   * @return
   */
  public static NeoTermDatabase instance(String dbName, int dbVersion, OnDatabaseUpgradedListener listener) {
    NeoTermSQLiteConfig config = new NeoTermSQLiteConfig();
    config.setDatabaseName(dbName);
    config.setDatabaseVersion(dbVersion);
    config.setOnDatabaseUpgradedListener(listener);
    return instance(config);
  }

  /**
   * 配置为新的参数(不改变数据库名).
   *
   * @param config
   */
  private void applyConfig(NeoTermSQLiteConfig config) {
    this.neoTermSQLiteConfig.debugMode = config.debugMode;
    this.neoTermSQLiteConfig.setOnDatabaseUpgradedListener(config.getOnDatabaseUpgradedListener());
  }

  public void release() {
    DAO_MAP.clear();
    if (neoTermSQLiteConfig.debugMode) {
      NLog.INSTANCE.d("缓存的DAO已经全部清除,将不占用内存.");
    }
  }


  /**
   * 在SD卡的指定目录上创建数据库文件
   *
   * @param sdcardPath sd卡路径
   * @param dbFileName 数据库文件名
   * @return
   */
  private SQLiteDatabase createDataBaseFileOnSDCard(String sdcardPath,
                                                    String dbFileName) {
    File dbFile = new File(sdcardPath, dbFileName);
    if (!dbFile.exists()) {
      try {
        if (dbFile.createNewFile()) {
          return SQLiteDatabase.openOrCreateDatabase(dbFile, null);
        }
      } catch (IOException e) {
        throw new RuntimeException("无法在 " + dbFile.getAbsolutePath() + "创建DB文件.");
      }
    } else {
      //数据库文件已经存在,无需再次创建.
      return SQLiteDatabase.openOrCreateDatabase(dbFile, null);
    }
    return null;
  }

  /**
   * 如果表不存在,需要创建它.
   *
   * @param clazz
   */
  private void createTableIfNeed(Class<?> clazz) {
    TableInfo tableInfo = TableHelper.from(clazz);
    if (tableInfo.isCreate) {
      return;
    }
    if (!isTableExist(tableInfo)) {
      String sql = SQLStatementHelper.createTable(tableInfo);
      if (neoTermSQLiteConfig.debugMode) {
        NLog.INSTANCE.w(sql);
      }
      db.execSQL(sql);
      Method afterTableCreateMethod = tableInfo.afterTableCreateMethod;
      if (afterTableCreateMethod != null) {
        //如果afterTableMethod存在,就调用它
        try {
          afterTableCreateMethod.invoke(null, this);
        } catch (Throwable ignore) {
          ignore.printStackTrace();
        }
      }
    }
  }

  /**
   * 判断表是否存在?
   *
   * @param table 需要盘的的表
   * @return
   */
  private boolean isTableExist(TableInfo table) {
    String sql = "SELECT COUNT(*) AS c FROM sqlite_master WHERE type ='table' AND name ='"
      + table.tableName + "' ";
    try (Cursor cursor = db.rawQuery(sql, null)) {
      if (cursor != null && cursor.moveToNext()) {
        int count = cursor.getInt(0);
        if (count > 0) {
          return true;
        }
      }
    } catch (Throwable ignore) {
      ignore.printStackTrace();
    }

    return false;
  }

  /**
   * 删除全部的表
   */
  public void dropAllTable() {
    try (Cursor cursor = db.rawQuery(
      "SELECT name FROM sqlite_master WHERE type ='table'", null)) {
      if (cursor != null) {
        cursor.moveToFirst();
        while (cursor.moveToNext()) {
          try {
            dropTable(cursor.getString(0));
          } catch (SQLException ignore) {
            ignore.printStackTrace();
          }
        }
      }
    }
  }

  /**
   * 取得数据库中的表的数量
   *
   * @return 表的数量
   */
  public int tableCount() {
    try (Cursor cursor = db.rawQuery(
      "SELECT name FROM sqlite_master WHERE type ='table'", null)) {
      return cursor == null ? 0 : cursor.getCount();
    }
  }

  /**
   * 取得数据库中的所有表名组成的List.
   *
   * @return
   */
  public List<String> getTableList() {
    try (Cursor cursor = db.rawQuery(
      "SELECT name FROM sqlite_master WHERE type ='table'", null)) {
      List<String> tableList = new ArrayList<>();
      if (cursor != null) {
        cursor.moveToFirst();
        while (cursor.moveToNext()) {
          tableList.add(cursor.getString(0));
        }
      }
      return tableList;
    }
  }

  /**
   * 删除一张表
   *
   * @param beanClass 表所对应的类
   */
  public void dropTable(Class<?> beanClass) {
    TableInfo tableInfo = TableHelper.from(beanClass);
    dropTable(tableInfo.tableName);
    tableInfo.isCreate = false;
  }

  /**
   * 删除一张表
   *
   * @param tableName 表名
   */
  public void dropTable(String tableName) {
    String statement = "DROP TABLE IF EXISTS " + tableName;
    if (neoTermSQLiteConfig.debugMode) {
      NLog.INSTANCE.w(statement);
    }
    db.execSQL(statement);
    TableInfo tableInfo = TableHelper.findTableInfoByName(tableName);
    if (tableInfo != null) {
      tableInfo.isCreate = false;
    }
  }

  /**
   * 存储一个Bean.
   *
   * @param bean
   * @return
   */
  public <T> NeoTermDatabase saveBean(T bean) {
    createTableIfNeed(bean.getClass());
    String statement = SQLStatementHelper.insertIntoTable(bean);
    if (neoTermSQLiteConfig.debugMode) {
      NLog.INSTANCE.w(statement);
    }
    db.execSQL(statement);
    return this;

  }

  /**
   * 存储多个Bean.
   *
   * @param beans
   * @return
   */
  public NeoTermDatabase saveBeans(Object[] beans) {
    for (Object o : beans) {
      saveBean(o);
    }

    return this;
  }

  /**
   * 存储多个Bean.
   *
   * @param beans
   * @return
   */
  public <T> NeoTermDatabase saveBeans(List<T> beans) {
    for (Object o : beans) {
      saveBean(o);
    }

    return this;
  }

  /**
   * 寻找Bean对应的全部数据
   *
   * @param clazz
   * @param <T>
   * @return
   */
  public <T> List<T> findAll(Class<?> clazz) {
    createTableIfNeed(clazz);
    TableInfo tableInfo = TableHelper.from(clazz);
    String statement = SQLStatementHelper.selectTable(tableInfo.tableName);
    if (neoTermSQLiteConfig.debugMode) {
      NLog.INSTANCE.w(statement);
    }
    List<T> list = new ArrayList<T>();
    try (Cursor cursor = db.rawQuery(statement, null)) {
      if (cursor == null) {
        // DO NOT RETURN NULL
        // null checks are ugly!
        return Collections.emptyList();
      }

      while (cursor.moveToNext()) {
        T object = Reflect.on(clazz).create().get();

        if (tableInfo.containID) {
          DatabaseDataType dataType = SQLTypeParser.getDataType(tableInfo.primaryField);
          String idFieldName = tableInfo.primaryField.getName();
          ValueHelper.setKeyValue(cursor, object, tableInfo.primaryField, dataType, cursor.getColumnIndex(idFieldName));
        }

        for (Field field : tableInfo.fieldToDataTypeMap.keySet()) {
          DatabaseDataType dataType = tableInfo.fieldToDataTypeMap.get(field);
          ValueHelper.setKeyValue(cursor, object, field, dataType, cursor.getColumnIndex(field.getName()));
        }
        list.add(object);
      }
      return list;
    }
  }

  /**
   * 根据where语句寻找Bean
   *
   * @param clazz
   * @param <T>
   * @return
   */
  public <T> List<T> findBeanByWhere(Class<?> clazz, String where) {
    createTableIfNeed(clazz);
    TableInfo tableInfo = TableHelper.from(clazz);
    String statement = SQLStatementHelper.findByWhere(tableInfo, where);
    if (neoTermSQLiteConfig.debugMode) {
      NLog.INSTANCE.w(statement);
    }
    List<T> list = new ArrayList<>();
    try (Cursor cursor = db.rawQuery(statement, null)) {
      if (cursor == null) {
        // DO NOT RETURN NULL
        // null checks are ugly!
        return Collections.emptyList();
      }

      while (cursor.moveToNext()) {
        T object = Reflect.on(clazz).create().get();
        if (tableInfo.containID) {
          DatabaseDataType dataType = SQLTypeParser.getDataType(tableInfo.primaryField);
          String idFieldName = tableInfo.primaryField.getName();
          ValueHelper.setKeyValue(cursor, object, tableInfo.primaryField, dataType, cursor.getColumnIndex(idFieldName));
        }
        for (Field field : tableInfo.fieldToDataTypeMap.keySet()) {
          DatabaseDataType dataType = tableInfo.fieldToDataTypeMap.get(field);
          ValueHelper.setKeyValue(cursor, object, field, dataType, cursor.getColumnIndex(field.getName()));
        }
        list.add(object);
      }
      return list;
    }
  }

  public <T> T findOneBeanByWhere(Class<?> clazz, String where) {
    List<T> list = findBeanByWhere(clazz, where);
    if (!list.isEmpty()) {
      return list.get(0);
    }
    return null;
  }

  /**
   * 根据where语句删除Bean
   *
   * @param clazz
   * @return
   */
  public NeoTermDatabase deleteBeanByWhere(Class<?> clazz, String where) {
    createTableIfNeed(clazz);
    TableInfo tableInfo = TableHelper.from(clazz);
    String statement = SQLStatementHelper.deleteByWhere(tableInfo, where);
    if (neoTermSQLiteConfig.debugMode) {
      NLog.INSTANCE.w(statement);
    }
    try {
      db.execSQL(statement);
    } catch (SQLException ignore) {
      ignore.printStackTrace();
    }

    return this;
  }

  /**
   * 删除指定ID的bean
   *
   * @param tableClass
   * @param id
   * @return 删除的Bean
   */
  public NeoTermDatabase deleteBeanByID(Class<?> tableClass, Object id) {
    createTableIfNeed(tableClass);
    TableInfo tableInfo = TableHelper.from(tableClass);
    DatabaseDataType dataType = SQLTypeParser.getDataType(id.getClass());
    if (dataType != null && tableInfo.primaryField != null) {
      //判断ID类型是否与数据类型匹配
      boolean match = SQLTypeParser.matchType(tableInfo.primaryField, dataType);
      if (!match) {//不匹配,抛出异常
        throw new IllegalArgumentException("类型 " + id.getClass().getName() + " 不是主键的类型,主键的类型应该为 " + tableInfo.primaryField.getType().getName());
      }
    }
    String idValue = ValueHelper.valueToString(dataType, id);
    String statement = SQLStatementHelper.deleteByWhere(tableInfo, tableInfo.primaryField == null ? "_id" : tableInfo.primaryField.getName() + " = " + idValue);
    if (neoTermSQLiteConfig.debugMode) {
      NLog.INSTANCE.w(statement);
    }

    try {
      db.execSQL(statement);
    } catch (SQLException ignore) {
      ignore.printStackTrace();
      //删除失败
    }
    return this;

  }

  /**
   * 根据给定的where更新数据
   *
   * @param tableClass
   * @param where
   * @param bean
   * @return
   */
  public NeoTermDatabase updateByWhere(Class<?> tableClass, String where, Object bean) {
    createTableIfNeed(tableClass);
    TableInfo tableInfo = TableHelper.from(tableClass);
    String statement = SQLStatementHelper.updateByWhere(tableInfo, bean, where);
    if (neoTermSQLiteConfig.debugMode) {
      NLog.INSTANCE.d(statement);
    }
    db.execSQL(statement);
    return this;
  }

  /**
   * 根据给定的id更新数据
   *
   * @param tableClass
   * @param id
   * @param bean
   * @return
   */
  public NeoTermDatabase updateByID(Class<?> tableClass, Object id, Object bean) {
    createTableIfNeed(tableClass);
    TableInfo tableInfo = TableHelper.from(tableClass);
    StringBuilder subStatement = new StringBuilder();
    if (tableInfo.containID) {
      subStatement.append(tableInfo.primaryField.getName()).append(" = ").append(ValueHelper.valueToString(SQLTypeParser.getDataType(tableInfo.primaryField), id));
    } else {
      subStatement.append("_id = ").append((int) id);
    }
    updateByWhere(tableClass, subStatement.toString(), bean);

    return this;
  }

  /**
   * 根据ID查找Bean
   *
   * @param tableClass
   * @param id
   * @param <T>
   * @return
   */
  public <T> T findBeanByID(Class<?> tableClass, Object id) {
    createTableIfNeed(tableClass);
    TableInfo tableInfo = TableHelper.from(tableClass);
    DatabaseDataType dataType = SQLTypeParser.getDataType(id.getClass());
    if (dataType == null) {
      return null;
    }
    // 判断ID类型是否与数据类型匹配
    boolean match = SQLTypeParser.matchType(tableInfo.primaryField, dataType) || tableInfo.primaryField == null;
    if (!match) {// 不匹配,抛出异常
      throw new IllegalArgumentException("Type " + id.getClass().getName() + " is not the primary key, expecting " + tableInfo.primaryField.getType().getName());
    }
    String idValue = ValueHelper.valueToString(dataType, id);
    String statement = SQLStatementHelper.findByWhere(tableInfo, tableInfo.primaryField == null ? "_id" : tableInfo.primaryField.getName() + " = " + idValue);
    if (neoTermSQLiteConfig.debugMode) {
      NLog.INSTANCE.w(statement);
    }

    try (Cursor cursor = db.rawQuery(statement, null)) {
      if (cursor != null && cursor.getCount() > 0) {
        cursor.moveToFirst();
        T bean = Reflect.on(tableClass).create().get();
        for (Field field : tableInfo.fieldToDataTypeMap.keySet()) {
          DatabaseDataType fieldType = tableInfo.fieldToDataTypeMap.get(field);
          ValueHelper.setKeyValue(cursor, bean, field, fieldType, cursor.getColumnIndex(field.getName()));
        }
        try {
          Reflect.on(bean).set(tableInfo.containID ? tableInfo.primaryField.getName() : "_id", id);
        } catch (Throwable ignore) {
          // 我们允许Bean没有id字段,因此此异常可以忽略
        }
        return bean;
      }
      return null;
    }
  }

  /**
   * 通过 VACUUM 命令压缩数据库
   */
  public void vacuum() {
    db.execSQL("VACUUM");
  }

  /**
   * 调用本方法会释放当前数据库占用的内存,
   * 调用后请确保你不会在接下来的代码中继续用到本实例.
   */
  public void destroy() {
    DAO_MAP.remove(this);
    this.neoTermSQLiteConfig = null;
    this.db = null;
  }

  /**
   * 取得内部操纵的SqliteDatabase.
   *
   * @return
   */
  public SQLiteDatabase getDatabase() {
    return db;
  }

  /**
   * 内部数据库监听器,负责派发接口.
   */
  private class SQLiteDataBaseHelper extends SQLiteOpenHelper {
    private final OnDatabaseUpgradedListener onDatabaseUpgradedListener;
    private final boolean defaultDropAllTables;

    public SQLiteDataBaseHelper(Context context, NeoTermSQLiteConfig config) {
      super(context, config.getDatabaseName(), null, config.getDatabaseVersion());
      this.onDatabaseUpgradedListener = config.getOnDatabaseUpgradedListener();
      this.defaultDropAllTables = config.isDefaultDropAllTables();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
      if (onDatabaseUpgradedListener != null) {
        onDatabaseUpgradedListener.onDatabaseUpgraded(db, oldVersion, newVersion);

      } else if (defaultDropAllTables) { // 干掉所有的表
        dropAllTable();
      }
    }
  }
}
