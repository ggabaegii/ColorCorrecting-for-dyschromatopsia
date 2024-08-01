package com.example.cvd;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

public class PhotoBookDB extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "PhotoBook.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME = "photo_table";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_TITLE = "title";
    private static final String COLUMN_URI = "uri";
    private Context context;

    public PhotoBookDB(Context context) {

        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_NAME + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_TITLE + " TEXT, " +
                COLUMN_URI + " TEXT)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public void addPhoto(String title, String uri) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, title);
        values.put(COLUMN_URI, uri);
        db.insert(TABLE_NAME, null, values);
    }

    public boolean isTitleExists(String title) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + COLUMN_TITLE + " = ?", new String[]{title});
        boolean exists = (cursor.getCount() > 0);
        cursor.close();
        return exists;
    }


    public Cursor readAllData() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
    }

    public Cursor readAllDataTitle() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_NAME + " ORDER BY " + COLUMN_TITLE, null);
    }

    public void deleteData(String title) {
        SQLiteDatabase db = this.getWritableDatabase();
        long result = db.delete(TABLE_NAME, "title=?", new String[]{title});
        if (result == -1) {
            Toast.makeText(context, "삭제 실패", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "삭제 완료", Toast.LENGTH_SHORT).show();
        }
    }
    // 제목을 업데이트하는 메서드 추가
    public void updateData(String oldTitle, String newTitle) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_TITLE, newTitle);

        long result = db.update(TABLE_NAME, cv, "title=?", new String[]{oldTitle});
        if (result == -1) {
            Toast.makeText(context, "업데이트 실패", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "업데이트 완료", Toast.LENGTH_SHORT).show();
        }
    }
}
