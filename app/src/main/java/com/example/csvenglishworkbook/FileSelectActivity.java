package com.example.csvenglishworkbook;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.DownloadManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.Console;
import java.io.File;

import java.util.*;
import java.util.ArrayList;


public class FileSelectActivity extends AppCompatActivity {

    private Intent fileExplorerActivityIntent;
    private Intent workbookActivityIntent;

    private Button fileSearchBtn;
    private Button activityExitBtn;
    private  Button workbookStartBtn;
    private TextView selectedFileNameTxtView;

    private String selectedFilePath;

    // SQLite DB관리
    private static CustomSQLiteOpenHelper workbookSQLiteOpenHelper;
    public static CustomSQLiteOpenHelper GetWorkbookSQLiteOpenHelper(){
        return workbookSQLiteOpenHelper;
    }
    private static CustomSQLiteOpenHelper fileInformationSQLiteOpenHelper;
    public static CustomSQLiteOpenHelper GetFileInformationSQLiteOpenHelper(){
        return fileInformationSQLiteOpenHelper;
    }
    private static CustomSQLiteOpenHelper audioOptionsSQLiteOpenHelper;
    public static CustomSQLiteOpenHelper GetAudioOptionsSQLiteOpenHelper(){
        return audioOptionsSQLiteOpenHelper;
    }

    private static boolean isNeedReadCsv; // 데이터 테이블을 갱신할 때, csv파일을 새로 읽을 필요가 있는지 검사
    public static boolean GetIsNeedReadCsv(){
        return isNeedReadCsv;
    }
    public static void SetIsNeedReadCsv(boolean value){
        isNeedReadCsv = value;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_select);

        RequestMemoryPermission();

        fileExplorerActivityIntent = new Intent(this,FileExplorerActivity.class);
        workbookActivityIntent = new Intent(this, WorkbookActivity.class);

        fileSearchBtn = findViewById(R.id.fileSearchBtn);
        activityExitBtn = findViewById(R.id.activityExitBtn);
        workbookStartBtn = findViewById(R.id.workbookStartBtn);
        selectedFileNameTxtView = findViewById(R.id.selectedFileNameTxtView);

        workbookSQLiteOpenHelper = new CustomSQLiteOpenHelper(this,CustomSQLiteOpenHelper.workbookDBName, 1);
        fileInformationSQLiteOpenHelper = new CustomSQLiteOpenHelper(this,CustomSQLiteOpenHelper.fileInformationDBName, 1);
        audioOptionsSQLiteOpenHelper = new CustomSQLiteOpenHelper(this, CustomSQLiteOpenHelper.audioOptionsDBName,1);
        SetInitializeAudioOptionDB(); // 오디오 옵션 DB를 만듬



        fileSearchBtn.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View view) {
                onPause();
                startActivityForResult(fileExplorerActivityIntent, 1);
            }
        });

        activityExitBtn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        workbookStartBtn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(selectedFilePath == null){
                    Toast.makeText(getApplicationContext(), "선택된 파일이 없습니다.", Toast.LENGTH_SHORT).show();
                    return;
                }
                else{
                    onPause();
                    RenewalDb();
                    workbookActivityIntent.putExtra("fileName", selectedFileNameTxtView.getText());
                    workbookActivityIntent.putExtra("filePullPath",selectedFilePath);
                    startActivity(workbookActivityIntent);
                }
            }
        });



        WorkbookActivity.SetWorkbookActivityOpenFlag(false); // 이 액티비티 생성자가 자동 삭제되는 것을 방지
        try{
            if(fileInformationSQLiteOpenHelper.IsExistTable()){
                isNeedReadCsv = false;
                ArrayList<Object> intentDataList = fileInformationSQLiteOpenHelper.SelectRowAllData(1);
                onPause();
                workbookActivityIntent.putExtra("fileName", intentDataList.get(1).toString());
                workbookActivityIntent.putExtra("filePullPath",intentDataList.get(2).toString());
                startActivity(workbookActivityIntent);
            }
        }
        catch (Exception e){
            System.out.println("DB생성 관련 예외처리");
        }


    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==1){
            if(resultCode==RESULT_OK){
                selectedFileNameTxtView.setText(data.getStringExtra("fileName"));
                selectedFilePath = data.getStringExtra("filePullPath");


            }
            else{
                System.out.println("실패했음");
            }
        }
    }

    private void RequestMemoryPermission(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        if(fileInformationSQLiteOpenHelper.IsExistTable() && WorkbookActivity.GetWorkbookActivityOpenFlag()){
            // 선택된 데이터가 있다면 종료함
            System.out.println("이 액티비티가 다시 재개되었음");
            finish();
        }
    }

    public FileSelectActivity(){
        System.out.println("액티비티 1 생성");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed(){
        //super.onBackPressed();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("알림");
        builder.setMessage("종료하시겠습니까?");
        builder.setPositiveButton("예",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
        builder.setNegativeButton("아니오",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
        builder.show();
    }

    private void RenewalDb(){
        // 테이블 새로 갱신
        System.out.println("파일 정보 테이블 갱신");
        fileInformationSQLiteOpenHelper.ExternalDropTable();
        fileInformationSQLiteOpenHelper.ExternalCreateTable();
        isNeedReadCsv=true;
        fileInformationSQLiteOpenHelper.InsertData(selectedFileNameTxtView.getText().toString(), selectedFilePath);
        fileInformationSQLiteOpenHelper.ShowAllData(); // 디버깅전용

        workbookSQLiteOpenHelper.ExternalDropTable();
        workbookSQLiteOpenHelper.ExternalCreateTable();
    }

    private void SetInitializeAudioOptionDB(){
        if(!audioOptionsSQLiteOpenHelper.IsExistTable()){
            // 관련 DB가 존재하지 않는 경우에만 적용
            System.out.println("오디오 옵션 DB를 새로 만듬");
            audioOptionsSQLiteOpenHelper.ExternalCreateTable();
            audioOptionsSQLiteOpenHelper.InsertData("0","1.0"); // 기본값 삽입
        }
        else{
            System.out.println("오디오 옵션를 가지고 있음. 갯수는 : " + audioOptionsSQLiteOpenHelper.DataTableRowCount());
            //audioOptionsSQLiteOpenHelper.ShowAllData(); // 디버깅전용
            if(audioOptionsSQLiteOpenHelper.DataTableRowCount()>1){
                // 테이블에 있는 row의 갯수가 두 개 이상이면 안됨
                System.out.println("오디오 옵션의 갯수 오류가 있으므로 테이블 재생성");
                audioOptionsSQLiteOpenHelper.ExternalDropTable();
                audioOptionsSQLiteOpenHelper.ExternalCreateTable();
                audioOptionsSQLiteOpenHelper.InsertData("0","1.0"); // 기본값 삽입
            }
        }



    }
}