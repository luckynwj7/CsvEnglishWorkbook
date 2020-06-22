package com.example.csvenglishworkbook;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.*;
import java.util.ArrayList;

import android.icu.lang.UCharacter;

import androidx.annotation.Nullable;

public class WorkbookSQLiteOpenHelper extends SQLiteOpenHelper {
    private static final int dbVersion = 1;
    private static final String dataTableName = "workbook";

    private static final String columnPrimary = "WordId";
    private static final String column2 = "ViewingWord";
    private static final String column3 = "HidingWord";
    private static final String column4 = "RememberFlag";

    private SQLiteDatabase writableDB;
    private SQLiteDatabase readableDB;


    public WorkbookSQLiteOpenHelper(Context context) {
        super(context, dataTableName, null, dbVersion);
        System.out.println("DB 생성 호출");

        writableDB = this.getWritableDatabase();
        readableDB = this.getReadableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        System.out.println("DB OnCreate 이벤트 호출");
        CreateTable(sqLiteDatabase);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        DropTable(sqLiteDatabase);
    }

    private void CreateTable(SQLiteDatabase db)
    {
        String sql = "CREATE TABLE IF NOT EXISTS " + dataTableName + "(" +
                columnPrimary + " INTEGER PRIMARY KEY AUTOINCREMENT" + "," +
                column2 + " TEXT" + "," +
                column3 + " TEXT" + "," +
                column4 + " INTEGER" +
                ")";
        SqlExec(db,sql);
    }

    public void ExternalCreateTable(){
        System.out.println("외부에서 DB CreateTable 이벤트 호출");
        CreateTable(writableDB);
    }

    private void DropTable(SQLiteDatabase db){
        String sql = "DROP TABLE IF EXISTS " + dataTableName;
        SqlExec(db,sql);
    }

    public void ExternalDropTable(){
        System.out.println("외부에서 DB DropTable 이벤트 호출");
        DropTable(writableDB);
    }

    public boolean InsertData(@Nullable String viewingWord, @Nullable String hidingWord, @Nullable Integer rememberFlag){
        System.out.println("DB INSERT INTO 실행");
        ContentValues contentValues = CreateContentValue(null, viewingWord, hidingWord, rememberFlag);
        long result = writableDB.insert(dataTableName, null, contentValues);

        if(result == -1){
            return  false;
        }
        else{
            return true;
        }
    }

    public void UpdateData(Integer wordId, @Nullable String viewingWord, @Nullable String hidingWord, @Nullable Integer rememberFlag){
        System.out.println("DB UPDATE 실행");
        ContentValues contentValues = CreateContentValue(null, viewingWord, hidingWord, rememberFlag);

        writableDB.update(dataTableName, contentValues, columnPrimary + " = ?", new String[] {wordId.toString()});
    }

    public void DeleteData(Integer wordId){
        writableDB.delete(dataTableName, columnPrimary + " = ?", new String[] {wordId.toString()});
    }

    public ArrayList<Object> SelectRowAllData(Integer wordId) {
        // 읽기가 가능하게 DB 열기
        ArrayList<Object> resultList = new ArrayList<>();

        // DB에 있는 데이터를 쉽게 처리하기 위해 Cursor를 사용하여 테이블에 있는 데이터 출력
        Cursor cursor = readableDB.rawQuery("SELECT * FROM " + dataTableName + " WHERE " + columnPrimary + " = " + wordId.toString(), null);
        cursor.moveToFirst();
        resultList.add(cursor.getInt(0));
        resultList.add(cursor.getString(1));
        resultList.add(cursor.getString(2));
        resultList.add(cursor.getInt(3));

        return resultList;
    }

    public int DataTableRowCount(){
        // 전채 행 수를 반환하는 함수
        int result;
        Cursor cursor = readableDB.rawQuery("SELECT COUNT(*) FROM " + dataTableName, null);
        cursor.moveToFirst();
        result = cursor.getInt(0);
        return result;
    }

    public void ShowAllData(){
        // 디버깅 전용 함수
        Cursor cursor = readableDB.rawQuery("SELECT * FROM " + dataTableName, null);
        while(cursor.moveToNext()){
            System.out.println(cursor.getInt(0) + ":" + cursor.getString(1) + ":" + cursor.getString(2) + ":" + cursor.getInt(3));
        }
    }


    private ContentValues CreateContentValue(@Nullable Integer wordId, @Nullable String viewingWord, @Nullable String hidingWord, @Nullable Integer rememberFlag){
        ContentValues contentValues = new ContentValues();
        if(wordId != null){
            contentValues.put(columnPrimary, viewingWord);
        }
        if(viewingWord != null){
            contentValues.put(column2, viewingWord);
        }
        if(hidingWord != null){
            contentValues.put(column3, hidingWord);
        }
        if(rememberFlag != null){
            contentValues.put(column4, rememberFlag);
        }
        return contentValues;
    }

    private void SqlExec(SQLiteDatabase db, String sql){
        try
        {
            db.execSQL(sql);
        }
        catch (SQLException e)
        {
            System.out.println("DB EXECUTE 실패");
        }
    }
}
