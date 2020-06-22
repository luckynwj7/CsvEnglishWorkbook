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

public class CustomSQLiteOpenHelper extends SQLiteOpenHelper {

    public final static String workbookDBName = "workbookDB";
    public final static String fileInformationDBName = "fileInformationDB";

    private String dataTableName;
    private int dbVersion;

    private ArrayList<String> columnList;
    private ArrayList<String> columnType;

    //private SQLiteDatabase writableDB;
    //private SQLiteDatabase readableDB;


    public CustomSQLiteOpenHelper(Context context, String dataTableName, int dbVersion) {
        super(context, dataTableName, null, dbVersion);
        System.out.println(dataTableName + "DB 생성 호출");
        this.dataTableName = dataTableName;
        this.dbVersion = dbVersion;

        //writableDB = this.getWritableDatabase();
        //readableDB = this.getReadableDatabase();

        columnList = new ArrayList<String>();
        columnType = new ArrayList<String>();
        CreateColumn();

    }

    private void CreateColumn(){
        // 워크북 이름에 따라 필요한 컬럼 생성

        columnList.add("RowIndex");
        columnType.add("INTEGER");
        // 기본 키가 되는 속성은 어느 테이블 이름이던 고정으로 달아놓음
        if(dataTableName == workbookDBName){
            columnList.add("ViewingWord");
            columnList.add("HidingWord");
            columnList.add("RememberFlag");

            columnType.add("TEXT");
            columnType.add("TEXT");
            columnType.add("INTEGER");
        }
        else if (dataTableName == fileInformationDBName){
            columnList.add("FileName");
            columnList.add("FilePullPath");

            columnType.add("TEXT");
            columnType.add("TEXT");
        }
        else{
            System.out.println("유효하지 않은 DB이름");
        }
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
                columnList.get(0) + " " + columnType.get(0) + " PRIMARY KEY AUTOINCREMENT" + ",";
        for(int colIndex = 1; colIndex < columnList.size();colIndex++){
            sql += columnList.get(colIndex) + " " + columnType.get(colIndex) + ",";
        }
        sql = sql.substring(0,sql.length()-1); // 마지막 컴마 삭제
        sql += ")";
        SqlExec(db,sql);
    }

    public void ExternalCreateTable(){
        System.out.println("외부에서 DB CreateTable 이벤트 호출");
        SQLiteDatabase writableDB = this.getWritableDatabase();
        CreateTable(writableDB);
    }

    private void DropTable(SQLiteDatabase db){
        String sql = "DROP TABLE IF EXISTS " + dataTableName;
        SqlExec(db,sql);
    }

    public void ExternalDropTable(){
        System.out.println("외부에서 DB DropTable 이벤트 호출");
        SQLiteDatabase writableDB = this.getWritableDatabase();
        DropTable(writableDB);
    }

    public boolean InsertData(String...inputColumn){
        System.out.println("DB INSERT INTO 실행");
        // 맨 처음 인자는 주지 않아야 됨. 키는 삽입되지 아니하며 자동으로 1씩 증가하도록 만듬. 예를 들어 컬럼이 4개라면 맨 앞의 ID를 제외한 3개를 집어넣야 함
        if(inputColumn.length != columnList.size()-1){
            System.out.println("매개변수 갯수가 맞지 않아 거절함");
            return false;
        }

        // 맨 앞에 최대행수를 넣은 배열을 다시 생성함
        String[] convertInputColumn = new String[inputColumn.length+1];
        convertInputColumn[0] = Integer.toString(DataTableRowCount()+1);
        for(int index=0;index<inputColumn.length;index++){
            convertInputColumn[1+index] = inputColumn[index];
        }

        SQLiteDatabase writableDB = this.getWritableDatabase();
        ContentValues contentValues = CreateContentValue(convertInputColumn);
        long result = writableDB.insert(dataTableName, null, contentValues);

        if(result == -1){
            return  false;
        }
        else{
            return true;
        }
    }

    public void UpdateData(Integer rowIndex, String...inputColumn){
        // 맨 처음 인수로는 id를 받음. 이와 일치해야 함
        System.out.println("DB UPDATE 실행");

        if(inputColumn.length != columnList.size()-1){
            System.out.println("매개변수 갯수가 맞지 않아 거절함");
            return;
        }

        // 맨 앞에 기본키를를 넣은 배열을 다시 생성함
        String[] convertInputColumn = new String[inputColumn.length+1];
        convertInputColumn[0] = rowIndex.toString();
        for(int index=0;index<inputColumn.length;index++){
            convertInputColumn[1+index] = inputColumn[index];
        }

        SQLiteDatabase writableDB = this.getWritableDatabase();
        ContentValues contentValues = CreateContentValue(convertInputColumn);
        writableDB.update(dataTableName, contentValues, columnList.get(0) + " = ?", new String[] {convertInputColumn[0]});
    }

    public void DeleteData(Integer rowIndex){
        SQLiteDatabase writableDB = this.getWritableDatabase();
        writableDB.delete(dataTableName, columnList.get(0) + " = ?", new String[] {rowIndex.toString()});
        ReNameRowIndex(rowIndex, writableDB);
    }

    private void ReNameRowIndex(int deleteNum, SQLiteDatabase writableDB){
        // rowIndex를 다시 매겨주는 함수
        int maxRowCount = DataTableRowCount();
        for(int rowIndex=deleteNum;rowIndex<=maxRowCount;rowIndex++){
            ContentValues contentValues = new ContentValues();
            contentValues.put(columnList.get(0), rowIndex);
            writableDB.update(dataTableName, contentValues, columnList.get(0) + " = ?", new String[] {Integer.toString(rowIndex+1)});
        }
    }

    public ArrayList<Object> SelectRowAllData(Integer rowIndex) {
        // 읽기가 가능하게 DB 열기
        ArrayList<Object> resultList = new ArrayList<>();
        SQLiteDatabase readableDB = this.getReadableDatabase();

        // DB에 있는 데이터를 쉽게 처리하기 위해 Cursor를 사용하여 테이블에 있는 데이터 출력
        Cursor cursor = readableDB.rawQuery("SELECT * FROM " + dataTableName + " WHERE " + columnList.get(0) + " = " + rowIndex.toString(), null);
        cursor.moveToFirst();
        for(int index=0;index<columnList.size();index++){
            if(columnType.get(index) == "TEXT"){
                resultList.add(cursor.getString(index));
            }
            else if(columnType.get(index) == "INTEGER"){
                resultList.add(cursor.getInt(index));
            }
        }
        return resultList;
    }

    public int DataTableRowCount(){
        // 전체 행 수를 반환하는 함수
        int result;
        SQLiteDatabase readableDB = this.getReadableDatabase();
        Cursor cursor = readableDB.rawQuery("SELECT COUNT(*) FROM " + dataTableName, null);
        cursor.moveToFirst();
        result = cursor.getInt(0);
        return result;
    }

    public void ShowAllData(){
        // 디버깅 전용 함수
        String result = "";
        SQLiteDatabase readableDB = this.getReadableDatabase();
        Cursor cursor = readableDB.rawQuery("SELECT * FROM " + dataTableName, null);
        while(cursor.moveToNext()){
            for(int index=0;index<columnList.size();index++){
                if(columnType.get(index) == "TEXT"){
                    result += cursor.getString(index);
                    result += ":";
                }
                else if(columnType.get(index) == "INTEGER"){
                    result += cursor.getInt(index);
                    result += ":";
                }
            }
            result+="\n";
        }
        System.out.println(result);
    }

    public boolean IsExistTable(){
        Integer result;
        try{
            result = DataTableRowCount();
            return true;
        }catch (Exception e){
            return false;
        }
    }


    private ContentValues CreateContentValue(String...inputColumn){
        ContentValues contentValues = new ContentValues();
        for(int index = 0; index<columnList.size();index++){
            if(inputColumn[index]!=null){
                if(columnType.get(index) == "TEXT"){
                    contentValues.put(columnList.get(index), inputColumn[index]);
                }
                else if (columnType.get(index) == "INTEGER"){
                    contentValues.put(columnList.get(index), Integer.parseInt(inputColumn[index]));
                }
            }
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
