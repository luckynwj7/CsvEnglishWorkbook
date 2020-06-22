package com.example.csvenglishworkbook;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

public class WorkbookActivity extends AppCompatActivity {


    private Intent thisActivityGetIntent;

    private String filePullPath;
    private String fileName;
    private File selectedFile;

    private int selectedRow; // 현재 선택된 행을 가리킴

    private CustomSQLiteOpenHelper workbookSQLiteOpenHelper; // SQLite DB관리

    private static boolean workbookActivityOpenFlag; // fileSelectActivity의 생성자 자동 삭제를 방지
    public static boolean GetWorkbookActivityOpenFlag(){
        return workbookActivityOpenFlag;
    }
    public static void SetWorkbookActivityOpenFlag(boolean value){
        workbookActivityOpenFlag = value;
    }

    private TextView fileNameTxtView;
    private TextView viewingWordTxtView;
    private TextView hidingWordTxtView;
    private String realHidingWord; // 진짜로 숨기고 있는 텍스트
    private Button otherFileSelectBtn;
    private TextView noneMemorizeTxtView;
    private TextView memorizeTxtView;

    private ArrayList<Integer> randomIndexArray;

    private int dataTableRowCount;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workbook);
        workbookActivityOpenFlag = true;


        thisActivityGetIntent = getIntent();
        fileName = thisActivityGetIntent.getExtras().getString("fileName");
        filePullPath = thisActivityGetIntent.getExtras().getString("filePullPath");
        selectedFile = new File(filePullPath);

        workbookSQLiteOpenHelper = FileSelectActivity.GetWorkbookSQLiteOpenHelper();
        dataTableRowCount = workbookSQLiteOpenHelper.DataTableRowCount();
        ReadCsvFileAndWriteDB();
        randomIndexArray = GetRandomNoneMemorizeList();

        fileNameTxtView = findViewById(R.id.fileNameTxtView);
        fileNameTxtView.setText(fileName);

        memorizeTxtView = findViewById(R.id.memorizeTxtView);
        noneMemorizeTxtView = findViewById(R.id.noneMemorizeTxtView);
        viewingWordTxtView = findViewById(R.id.viewingWordTxtView);
        hidingWordTxtView = findViewById(R.id.hidingWordTxtView);

        hidingWordTxtView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hidingWordTxtView.setText(realHidingWord);
            }
        });

        otherFileSelectBtn = findViewById(R.id.otherFileSelectBtn);
        otherFileSelectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                workbookActivityOpenFlag = false;
                finish();
            }
        });

        AdjustMemorizeWordCounting();
        ShowWordFromRowIndex(randomIndexArray.get(0));
    }
    private void ReadCsvFileAndWriteDB(){
        // 데이터베이스가 하나도 없을 경우에만 작동
        System.out.println("CSV파일을 READ함");
        if(dataTableRowCount<=0){
            ArrayList<ArrayList<String>> readCsvList = CsvReader.GetSplitDataArrayList(selectedFile);
            CsvReader.SaveArrayListInDataBase(readCsvList, workbookSQLiteOpenHelper); // StringArrayList를 Database에 저장
            dataTableRowCount = workbookSQLiteOpenHelper.DataTableRowCount();
        }
    }

    private ArrayList<Integer> GetRandomNoneMemorizeList(){
        // 작업하지 않은 Row의 배열을 새로 반환함
        ArrayList<Integer> resultList = new ArrayList<Integer>();
        for(int rowIndex=1;rowIndex<=dataTableRowCount;rowIndex++){
            ArrayList<Object> viewingRow = workbookSQLiteOpenHelper.SelectRowAllData(rowIndex);
            if((int)viewingRow.get(3) == 0){
                resultList.add((int)viewingRow.get(0));
            }
        }
        Collections.shuffle(resultList);
        return resultList;
    }

    private void ShowWordFromRowIndex(int rowIndex){
        ArrayList<Object> selectedRow = workbookSQLiteOpenHelper.SelectRowAllData(rowIndex);
        viewingWordTxtView.setText((String)selectedRow.get(1));
        realHidingWord = (String)selectedRow.get(2);
        hidingWordTxtView.setText("");
    }

    private void AdjustMemorizeWordCounting(){
        int memorizeCount = 0;
        int noneMemorizeCount = 0;
        ArrayList<Object> tempArrayList = new ArrayList<Object>();
        for(int rowIndex=1;rowIndex<=dataTableRowCount;rowIndex++){
            tempArrayList=workbookSQLiteOpenHelper.SelectRowAllData(rowIndex);
            if((int)tempArrayList.get(3)==1){
                memorizeCount++; // 외웠으면 1을 읽음
            }
            else if((int)tempArrayList.get(3)==2){
                noneMemorizeCount++; // 못외웠으면 2를 읽음
            }
        }
        memorizeTxtView.setText(Integer.toString(memorizeCount));
        noneMemorizeTxtView.setText(Integer.toString(noneMemorizeCount));
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}