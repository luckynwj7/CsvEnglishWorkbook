package com.example.csvenglishworkbook;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import java.io.File;

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

        myWorkbookSQLiteOpenHelper = new WorkbookSQLiteOpenHelper(this);

        myWorkbookSQLiteOpenHelper.ExternalDropTable();
        myWorkbookSQLiteOpenHelper.ExternalCreateTable();

        myWorkbookSQLiteOpenHelper.InsertData("a","b",1);
        myWorkbookSQLiteOpenHelper.InsertData("c","d",2);
        myWorkbookSQLiteOpenHelper.UpdateData(1,"e","f",0);
        String result = myWorkbookSQLiteOpenHelper.getResult();
        System.out.println(result);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        myWorkbookSQLiteOpenHelper.ExternalDropTable();
    }
}