package com.example.roma.myapplication;

/**
 * Created by lolol on 05.04.2017.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ControlSQL extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "android.db";
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_TABLE = "table2";

    // поля таблицы для хранения ФИО, Должности и Телефона (id формируется автоматически)
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_WORD = "word";
    public static final String COLUMN_TRANLATED = "tran";
    public static final String COLUMN_FAVORITE = "fav";

    // формируем запрос для создания базы данных
    private static final String DATABASE_CREATE = "create table "
    + DATABASE_TABLE + "(" + COLUMN_ID
    + " integer primary key autoincrement, " + COLUMN_WORD
    + " text not null, " + COLUMN_TRANLATED + " text not null, " + COLUMN_FAVORITE  + " text not null" + ");";

    public ControlSQL(Context context) {
        super(context, DATABASE_NAME, null,DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_CREATE);
        ContentValues initialValues = createContentValues("","","");
        db.insert(DATABASE_TABLE, null, initialValues);
    }



    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS table2");
        onCreate(db);
    }

    /**
          * Создаёт новый контакт. Если создан успешно - возвращается
          * номер строки rowId, иначе -1
          */

    public long createNewTable(String WORD, String TRANSLATED,String FAVORITE) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues initialValues = createContentValues(WORD, TRANSLATED,FAVORITE);

        long row = db.insert(DATABASE_TABLE, null, initialValues);
        db.close();

        return row;
    }

    /**
          * Изменение строчки
          */
    public boolean updateTable(long rowId, String WORD, String TRANSLATED, String FAVOTITE) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues updateValues = createContentValues(WORD, TRANSLATED,FAVOTITE);

        return db.update(DATABASE_TABLE, updateValues, COLUMN_ID + "=" + rowId,null) > 0;
    }

    /**
      * Удаление контакта
      */
        public void deleteTable(long rowId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(DATABASE_TABLE, null, null);
        db.close();
    }

    public void deleteStory() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(DATABASE_TABLE, COLUMN_FAVORITE+"=?", new String[]{"false"});
        db.close();
    }

    public void deleteFavorite() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(DATABASE_TABLE, COLUMN_FAVORITE+"=?", new String[]{"true"});
        db.close();
    }

    public void deleteOneTableFromFavorite(String _word) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(DATABASE_TABLE, COLUMN_WORD+"=? AND "+COLUMN_FAVORITE+"=?" , new String[]{_word,"true"});
        db.close();
    }

    public void deleteLastTable() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor crs = getFullTable();
        db.delete(DATABASE_TABLE, COLUMN_ID+"="+Integer.toString(crs.getCount()-1), null);
        db.close();
    }

        /**
      * Получение всех контактов
      */
        public Cursor getFullTable() {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.query(DATABASE_TABLE, new String[] { COLUMN_ID,COLUMN_WORD, COLUMN_TRANLATED,COLUMN_FAVORITE },COLUMN_FAVORITE+"=?",new String[]{"false"}, null, null, null);

        }

    public Cursor getFavoriteTable() {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.query(DATABASE_TABLE, new String[] { COLUMN_ID,COLUMN_WORD, COLUMN_TRANLATED,COLUMN_FAVORITE },COLUMN_FAVORITE+"=?",new String[]{"true"}, null, null, null);

    }

    public boolean FindWordInDB (String _word){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor crs = db.query(DATABASE_TABLE, new String[] { COLUMN_ID,COLUMN_WORD, COLUMN_TRANLATED,COLUMN_FAVORITE },COLUMN_WORD+"=? AND "+COLUMN_FAVORITE+"=?",new String[]{_word,"true"}, null, null, null);
       if (crs.getCount()>0)
        return true;
        else return false;
    }
    /**
      * Получаем конкретный контакт
      */
        public Cursor getTable(long rowId) throws SQLException {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor mCursor = db.query(true, DATABASE_TABLE,new String[] { COLUMN_ID, COLUMN_WORD, COLUMN_TRANLATED,COLUMN_FAVORITE }, COLUMN_ID + "=" + rowId, null,null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }



    private ContentValues createContentValues(String WORD, String TRANSLATED, String FAVORITE) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_WORD, WORD);
        values.put(COLUMN_TRANLATED, TRANSLATED);
        values.put(COLUMN_FAVORITE,FAVORITE);
        return values;
    }
}
