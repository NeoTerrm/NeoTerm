package io.neoterm.framework.database;

import java.io.Serializable;

/**
 * @author kiva
 */
public class NeoTermSQLiteConfig implements Serializable {

  private static final long serialVersionUID = -4069725570156436316L;
  //==============================================================
  //                          常量
  //==============================================================
  public static String DEFAULT_DB_NAME = "we_like.db";
  public static NeoTermSQLiteConfig DEFAULT_CONFIG = new NeoTermSQLiteConfig();

  //==============================================================
  //                          字段
  //==============================================================
  /**
   * 是否为DEBUG模式
   */
  public boolean debugMode = false;
  /**
   * 数据库名
   */
  private String dbName = DEFAULT_DB_NAME;
  /**
   * 数据库升级监听器
   */
  private OnDatabaseUpgradedListener onDatabaseUpgradedListener;
  private boolean defaultDropAllTables = false;
  private String saveDir;
  private int dbVersion = 1;

  /**
   * 取得数据库的名称
   *
   * @return
   */
  public String getDatabaseName() {
    return dbName;
  }

  /**
   * 设置数据库的名称
   *
   * @param dbName
   */
  public void setDatabaseName(String dbName) {
    this.dbName = dbName;
  }

  /**
   * 取得数据库升级监听器
   *
   * @return
   */
  public OnDatabaseUpgradedListener getOnDatabaseUpgradedListener() {
    return onDatabaseUpgradedListener;
  }

  /**
   * 设置数据库升级监听器
   *
   * @param onDatabaseUpgradedListener
   */
  public void setOnDatabaseUpgradedListener(OnDatabaseUpgradedListener onDatabaseUpgradedListener) {
    this.onDatabaseUpgradedListener = onDatabaseUpgradedListener;
  }

  /**
   * 取得数据库保存目录
   *
   * @return
   */
  public String getSaveDir() {
    return saveDir;
  }

  /**
   * 设置数据库的保存目录
   *
   * @param saveDir
   */
  public void setSaveDir(String saveDir) {
    this.saveDir = saveDir;
  }

  /**
   * 获取DB的版本号
   *
   * @return
   */
  public int getDatabaseVersion() {
    return dbVersion;
  }

  /**
   * 设置DB的版本号
   *
   * @param dbVersion
   */
  public void setDatabaseVersion(int dbVersion) {
    this.dbVersion = dbVersion;
  }

  /**
   * App 更新时是否默认删除所有存在的表
   *
   * @return
   */
  public boolean isDefaultDropAllTables() {
    return defaultDropAllTables;
  }

  /**
   * 设置 App 更新时是否默认删除所有存在的表
   *
   * @param defaultDropAllTables
   */
  public void setDefaultDropAllTables(boolean defaultDropAllTables) {
    this.defaultDropAllTables = defaultDropAllTables;
  }
}
