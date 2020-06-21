package com.example.csvenglishworkbook;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.DownloadManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.Console;
import java.io.File;

public class FileSelectActivity extends AppCompatActivity {

    private Intent fileExplorerActivityIntent;
    private Intent workbookActivityIntent;

    private Button fileSearchBtn;
    private Button activityExitBtn;
    private  Button workbookStartBtn;
    private TextView selectedFileNameTxtView;

    private String selectedFilePath;

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
                    workbookActivityIntent.putExtra("filePullPath",selectedFilePath);
                    workbookActivityIntent.putExtra("fileName", selectedFileNameTxtView.getText());
                    startActivity(workbookActivityIntent);
                }
            }
        });

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




    public FileSelectActivity(){
        System.out.println("액티비티 1 생성");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.out.println("액티비티 1 소멸");
    }
}