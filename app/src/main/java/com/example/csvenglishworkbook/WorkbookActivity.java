package com.example.csvenglishworkbook;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import java.io.File;

import java.util.ArrayList;

public class WorkbookActivity extends AppCompatActivity {


    private Intent thisActivityGetIntent;

    private String filePullPath;
    private String fileName;
    private File selectedFile;

    private CustomSQLiteOpenHelper workbookSQLiteOpenHelper; // SQLite DB관리


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workbook);

        thisActivityGetIntent = getIntent();
        fileName = thisActivityGetIntent.getExtras().getString("fileName");
        filePullPath = thisActivityGetIntent.getExtras().getString("filePullPath");
        selectedFile = new File(filePullPath);

        workbookSQLiteOpenHelper = new CustomSQLiteOpenHelper(this, CustomSQLiteOpenHelper.workbookDBName,1);

        workbookSQLiteOpenHelper.ExternalDropTable();
        workbookSQLiteOpenHelper.ExternalCreateTable();

        ArrayList<ArrayList<String>> readCsvList = CsvReader.GetSplitDataArrayList(selectedFile);
        CsvReader.SaveArrayListInDataBase(readCsvList, workbookSQLiteOpenHelper); // StringArrayList를 Database에 저장
        workbookSQLiteOpenHelper.ShowAllData();//디버깅전용



    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        workbookSQLiteOpenHelper.ExternalDropTable();
    }
}