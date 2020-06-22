package com.example.csvenglishworkbook;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import java.io.File;

import java.util.*;
import java.util.ArrayList;

public class WorkbookActivity extends AppCompatActivity {


    private Intent thisActivityGetIntent;

    private String filePullPath;
    private String fileName;
    private File selectedFile;

    private WorkbookSQLiteOpenHelper myWorkbookSQLiteOpenHelper; // SQLite DB관리


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workbook);

        thisActivityGetIntent = getIntent();
        fileName = thisActivityGetIntent.getExtras().getString("fileName");
        filePullPath = thisActivityGetIntent.getExtras().getString("filePullPath");
        selectedFile = new File(filePullPath);

        myWorkbookSQLiteOpenHelper = new WorkbookSQLiteOpenHelper(this,WorkbookSQLiteOpenHelper.workbookDBName,1);

        myWorkbookSQLiteOpenHelper.ExternalDropTable();
        myWorkbookSQLiteOpenHelper.ExternalCreateTable();

        ArrayList<ArrayList<String>> readCsvList = CsvReader.GetSplitDataArrayList(selectedFile);
        CsvReader.SaveArrayListInDataBase(readCsvList, myWorkbookSQLiteOpenHelper); // StringArrayList를 Database에 저장
        myWorkbookSQLiteOpenHelper.ShowAllData();//디버깅전용



    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        myWorkbookSQLiteOpenHelper.ExternalDropTable();
    }
}