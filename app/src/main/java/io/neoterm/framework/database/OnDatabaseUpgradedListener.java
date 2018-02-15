package io.neoterm.framework.database;

import android.database.sqlite.SQLiteDatabase;

/**
 * @author kiva
 */
public interface OnDatabaseUpgradedListener {
    /**
     * @param db         数据库
     * @param oldVersion 旧版本
     * @param newVersion 新版本
     */
    void onDatabaseUpgraded(SQLiteDatabase db, int oldVersion, int newVersion);
}