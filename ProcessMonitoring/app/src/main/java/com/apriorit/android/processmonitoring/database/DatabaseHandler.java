package com.apriorit.android.processmonitoring.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of CRUD operations
 * Creating database and table for Blacklist
 */
public class DatabaseHandler extends SQLiteOpenHelper implements IDatabaseHandler {

    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_NAME = "Blacklist";
    private static final String TABLE_BLACKLIST = "blacklist";
    private static final String KEY_ID = "id";
    private static final String KEY_PACKAGE_NAME = "package_name";
    private static final String KEY_APP_NAME = "app_name";
    private static final String KEY_BLOCKED = "blocked";
    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_BLACKLIST_TABLE = "CREATE TABLE " + TABLE_BLACKLIST + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_PACKAGE_NAME + " TEXT NOT NULL UNIQUE ,"
                + KEY_APP_NAME + " TEXT, " + KEY_BLOCKED + " INTEGER " +  ")";
        db.execSQL(CREATE_BLACKLIST_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BLACKLIST);

        onCreate(db);
    }

    @Override
    public void addApplicationData(AppData app) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_PACKAGE_NAME , app.getPackageName());
        values.put(KEY_APP_NAME , app.getAppName());
        values.put(KEY_BLOCKED , app.isBlocked());
        db.insertWithOnConflict(TABLE_BLACKLIST, null, values, SQLiteDatabase.CONFLICT_IGNORE);
        db.close();
    }

    @Override
    public AppData getApplication(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_BLACKLIST, new String[] { KEY_ID,
                        KEY_PACKAGE_NAME, KEY_APP_NAME, KEY_BLOCKED }, KEY_ID + "=?",
                new String[] { String.valueOf(id) }, null, null, null, null);

        if (cursor != null){
            cursor.moveToFirst();
        }

        AppData app = new AppData(Integer.parseInt(cursor.getString(0)), cursor.getString(1), cursor.getString(2), cursor.getInt(3));

        return app;
    }

    @Override
    public List<AppData> getAllApps() {
        List<AppData> appList = new ArrayList<AppData>();
        String selectQuery = "SELECT  * FROM " + TABLE_BLACKLIST;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                AppData app = new AppData();
                app.setID(Integer.parseInt(cursor.getString(0)));
                app.setPackageName(cursor.getString(1));
                app.setAppName(cursor.getString(2));
                app.setAccess(cursor.getInt(3));
                appList.add(app);
            } while (cursor.moveToNext());
        }

        return appList;
    }

    @Override
    public int updateApplication(AppData app) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_PACKAGE_NAME , app.getPackageName());
        values.put(KEY_APP_NAME , app.getAppName());
        values.put(KEY_BLOCKED , app.isBlocked());

        return db.update(TABLE_BLACKLIST, values, KEY_ID + " = ?",
                new String[] { String.valueOf(app.getID()) });
    }

    @Override
    public void deleteApplicationData(AppData app) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_BLACKLIST, KEY_ID + " = ?", new String[] { String.valueOf(app.getID()) });
        db.close();
    }

    @Override
    public void deleteAll() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_BLACKLIST, null, null);
        db.close();
    }
    public void clearBlacklist() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from "+ TABLE_BLACKLIST);
    }
}