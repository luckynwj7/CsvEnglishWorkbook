package com.example.csvenglishworkbook;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

import java.util.ArrayList;
import java.util.Collections;

public class WorkbookActivity extends AppCompatActivity {

    private static WorkbookActivity workbookActivity; //자기자신객체
    public static WorkbookActivity GetWorkActivity(){
        return workbookActivity;
    }


    private Intent thisActivityGetIntent;

    private String filePullPath;
    private String fileName;
    private File selectedFile;

    private int selectedRowIndex; // 현재 선택된 행을 가리킴

    private CustomSQLiteOpenHelper workbookSQLiteOpenHelper; // SQLite DB관리

    private InsertDataDialogManager myInsertDataDialog; // insert시에 작동하는 다이얼로그

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

    private TextView currentJobStatusTxtView;
    private TextView allJobStatusTxtView;

    private TextView noneMemorizeTxtView;
    private TextView memorizeTxtView;
    private Button noneMemorizeBtn;
    private Button memorizeBtn;

    private ArrayList<Integer> randomIndexArray;
    private int currentViewIndex;
    private AlertDialogManager updateAlertDialog;

    private int dataTableRowCount;

    private boolean maxJobFlag; //모든 작업을 끝마쳤을 경우 외움/못외움을 진행하지 못하도록 함


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workbook);
        workbookActivityOpenFlag = true;
        maxJobFlag=false;
        workbookActivity = this;


        thisActivityGetIntent = getIntent();
        fileName = thisActivityGetIntent.getExtras().getString("fileName");
        filePullPath = thisActivityGetIntent.getExtras().getString("filePullPath");
        selectedFile = new File(filePullPath);

        workbookSQLiteOpenHelper = FileSelectActivity.GetWorkbookSQLiteOpenHelper();
        dataTableRowCount = workbookSQLiteOpenHelper.DataTableRowCount();
        ReadCsvFileAndWriteDB();

        myInsertDataDialog = new InsertDataDialogManager(this); // insert 다이얼로그 할당
        updateAlertDialog = new AlertDialogManager(this); // update 다이얼로그 할당


        fileNameTxtView = findViewById(R.id.fileNameTxtView);
        fileNameTxtView.setText(fileName);

        currentJobStatusTxtView = findViewById(R.id.currentJobStatusTxtView);
        currentJobStatusTxtView.setText(Integer.toString(GetCompleteJobCounting()));

        allJobStatusTxtView = findViewById(R.id.allJobStatusTxtView);
        allJobStatusTxtView.setText(Integer.toString(dataTableRowCount));

        memorizeTxtView = findViewById(R.id.memorizeTxtView);
        noneMemorizeTxtView = findViewById(R.id.noneMemorizeTxtView);
        viewingWordTxtView = findViewById(R.id.viewingWordTxtView);
        registerForContextMenu(viewingWordTxtView);
        hidingWordTxtView = findViewById(R.id.hidingWordTxtView);
        registerForContextMenu(hidingWordTxtView);

        hidingWordTxtView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hidingWordTxtView.setText(realHidingWord);
            }
        });

        noneMemorizeBtn = findViewById(R.id.noneMemorizeBtn);
        noneMemorizeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 못외운 상태는 2를 저장함
                if(maxJobFlag){
                    RandomGetAndStart();
                    return;
                }
                else if(dataTableRowCount<=0){
                    Toast.makeText(getApplicationContext(),"진행할 데이터가 없습니다.", Toast.LENGTH_SHORT).show();
                    return;
                }
                workbookSQLiteOpenHelper.UpdateData(randomIndexArray.get(currentViewIndex),null,null,"2");
                int currentCount = Integer.parseInt(noneMemorizeTxtView.getText().toString());
                noneMemorizeTxtView.setText(Integer.toString(currentCount+1));
                ViewNextCount();
            }
        });

        memorizeBtn = findViewById(R.id.memorizeBtn);
        memorizeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            // 외운 상태는 1을 저장함
            public void onClick(View view) {
                if(maxJobFlag){
                    RandomGetAndStart();
                    return;
                }
                else if(dataTableRowCount<=0){
                    Toast.makeText(getApplicationContext(),"진행할 데이터가 없습니다.", Toast.LENGTH_SHORT).show();
                    return;
                }
                workbookSQLiteOpenHelper.UpdateData(randomIndexArray.get(currentViewIndex),null,null,"1");
                int currentCount = Integer.parseInt(memorizeTxtView.getText().toString());
                memorizeTxtView.setText(Integer.toString(currentCount+1));
                ViewNextCount();
            }
        });



    }

    @Override
    protected void onStart() {
        super.onStart();
        selectedRowIndex=1;
        AdjustMemorizeWordCounting(); // 외운 횟수 및 못외운 횟수를 조정하고 시작함
        ViewingStartConditionCheckTableCountEmpty(); // 보여줄 행이 있을지에 따라 결정하고 시작
    }

    private void RandomGetAndStart(){
        // 랜덤한 Row를 받아서 실행하게 하는 함수. 랜덤 배열을 0부터 시작시킴
        randomIndexArray = GetRandomNoneMemorizeList(); // 랜덤 배열을 새로 저장함
        // 첫 시작화면을 띄움
        if(randomIndexArray.size()<=0){
            maxJobFlag=true;
            ShowWordFromRowIndex(selectedRowIndex);
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("알림");
            builder.setMessage("더 이상 할 작업이 없습니다. 외움/못외움 상태를 초기화하겠습니까?");
            builder.setPositiveButton("예",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            WordStatusInitialize();
                        }
                    });
            builder.setNegativeButton("다른 파일 확인",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            OtherFileSelectClick();
                        }
                    });
            builder.show();
        }
        else{
            currentJobStatusTxtView.setText(Integer.toString(dataTableRowCount-randomIndexArray.size())); // 진행상황의 갯수를 맞춰줌
            currentViewIndex = 0;
            ShowWordFromRowIndex(randomIndexArray.get(currentViewIndex));
        }

    }

    private void ReadCsvFileAndWriteDB(){
        // 데이터베이스가 하나도 없을 경우에만 작동
        System.out.println("CSV파일을 READ함");
        if(FileSelectActivity.GetIsNeedReadCsv()){
            System.out.println("CSV파일을 새롭게 READ함");
            ArrayList<ArrayList<String>> readCsvList = CsvReader.GetSplitDataArrayList(selectedFile);
            if(readCsvList!=null){
                CsvReader.SaveArrayListInDataBase(readCsvList, workbookSQLiteOpenHelper); // StringArrayList를 Database에 저장
            }
            dataTableRowCount = workbookSQLiteOpenHelper.DataTableRowCount();
            FileSelectActivity.SetIsNeedReadCsv(false);
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

    private int GetCompleteJobCounting(){
        int result=0;
        for(int rowIndex=1;rowIndex<=dataTableRowCount;rowIndex++){
            ArrayList<Object> viewingRow = workbookSQLiteOpenHelper.SelectRowAllData(rowIndex);
            if((int)viewingRow.get(3) != 0){
                result++;
            }
        }
        return result;
    }

    private void ShowWordFromRowIndex(int rowIndex){
        ArrayList<Object> selectedRow = workbookSQLiteOpenHelper.SelectRowAllData(rowIndex);
        selectedRowIndex = (int)(selectedRow.get(0));
        viewingWordTxtView.setText((String)selectedRow.get(1));
        realHidingWord = (String)selectedRow.get(2);
        hidingWordTxtView.setText("");
    }

    private void AdjustMemorizeWordCounting(){
        // 첫 시작할 때 외운 횟수 및 못외운 횟수를 조정해주는 함수
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

    private void ViewNextCount(){
        currentViewIndex++;
        int currentJobCount = Integer.parseInt(currentJobStatusTxtView.getText().toString());
        currentJobStatusTxtView.setText(Integer.toString(currentJobCount+1)); // 작업상황을 하나 늘림
        if(randomIndexArray.size()<=currentViewIndex){
            NewStatusStart();
            return;
        }
        ShowWordFromRowIndex(randomIndexArray.get(currentViewIndex));
    }

    private void NewStatusStart(){
        //작업을 완료했을 때 다시 시작하도록 설정하는 함수. 못외운 Row들을 미작업 상태로 돌려버림

        if(allJobStatusTxtView.getText().toString().equals(memorizeTxtView.getText().toString())){
            RandomGetAndStart();
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("알림");
        builder.setMessage("모든 단어를 확인했습니다. 못외운 단어들을 다시 확인할까요?");
        builder.setPositiveButton("예",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        ArrayList<Object> tempArrayList = new ArrayList<Object>();
                        for(int rowIndex=1;rowIndex<=dataTableRowCount;rowIndex++){
                            tempArrayList=workbookSQLiteOpenHelper.SelectRowAllData(rowIndex);
                            if((int)tempArrayList.get(3)==2){
                                workbookSQLiteOpenHelper.UpdateData(rowIndex,null,null,"0");
                            }
                        }
                        noneMemorizeTxtView.setText("0");
                        System.out.println("RememberFlag 중 2를 0으로 전부 초기화시킴");
                        RandomGetAndStart();
                    }
                });
        builder.setNegativeButton("아니오",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
        builder.show();
    }

    private void WordStatusInitialize(){
        maxJobFlag=false;
        for(int rowIndex=1;rowIndex<=dataTableRowCount;rowIndex++){
            workbookSQLiteOpenHelper.UpdateData(rowIndex,null,null,"0");
        }
        memorizeTxtView.setText(("0"));
        noneMemorizeTxtView.setText(("0"));
        currentJobStatusTxtView.setText(("0"));
        RandomGetAndStart();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.workbook_action_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case R.id.wordInsertItem:
                WordInsertClick();
                return true;
            case R.id.wordDeleteItem:
                WordDeleteClick();
                return true;
            case R.id.mixStartItem:
                MixStartClick();
                return true;
            case R.id.wordStatusInitializeItem:
                WordStatusInitializeClick();
                return true;
            case R.id.fullSpreadItem:
                FullSpreadClick();
                return true;
            case R.id.otherFileSelectItem:
                OtherFileSelectClick();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void WordInsertClick(){
        myInsertDataDialog.Show();
    }


    /// Insert작업
    public static void NewDataInsert(String viewingWord, String hidingWord){
        // InsertDialog로부터 전달받기 위한 함수
        FileSelectActivity.GetWorkbookSQLiteOpenHelper().InsertData(viewingWord,hidingWord,"0");
        ThisActivityInsertJobRestart(WorkbookActivity.GetWorkActivity());
    }
    private static void ThisActivityInsertJobRestart(WorkbookActivity activity){
        System.out.println("액티비티 REFRESH");
        activity.onStart();
        activity.AllJobCountingPlus();
    }
    private void AllJobCountingPlus(){
        dataTableRowCount++;
        allJobStatusTxtView.setText(Integer.toString(dataTableRowCount));
        if(randomIndexArray==null){
            randomIndexArray = new ArrayList<Integer>();
            RandomGetAndStart();
        }
        randomIndexArray.add(dataTableRowCount);
        ShowWordFromRowIndex(dataTableRowCount);
    }
    ///

    private void WordDeleteClick(){
        if(dataTableRowCount<=0){
            Toast.makeText(getApplicationContext(),"삭제할 단어가 없습니다.",Toast.LENGTH_SHORT).show();
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("주의");
        builder.setMessage("정말로 보고있는 데이터를 삭제하시겠습니까?");
        builder.setPositiveButton("예",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        workbookSQLiteOpenHelper.DeleteData(selectedRowIndex);
                        // 전체 데이터 테이블 갯수도 낮춰놓음
                        dataTableRowCount--;
                        allJobStatusTxtView.setText(Integer.toString(dataTableRowCount));
                        ViewingStartConditionCheckTableCountEmpty();
                    }
                });
        builder.setNegativeButton("아니오",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
        builder.show();
    }
    private void MixStartClick(){
        ViewingStartConditionCheckTableCountEmpty();
    }
    private void WordStatusInitializeClick(){
        if(dataTableRowCount<=0){
            Toast.makeText(getApplicationContext(),"작업할 데이터가 없습니다.",Toast.LENGTH_SHORT).show();
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("주의");
        builder.setMessage("외움/못외움 상태 초기화를 하시겠습니까? 이 작업은 되돌릴 수 없습니다.");
        builder.setPositiveButton("예",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        WordStatusInitialize();
                    }
                });
        builder.setNegativeButton("아니오",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
        builder.show();
    }

    private void FullSpreadClick(){
        Toast.makeText(this,"풀스크린은 미구현기능",Toast.LENGTH_SHORT).show();
    }

    private void OtherFileSelectClick(){
        workbookActivityOpenFlag = false;
        CsvWriter.WriteScvFile(filePullPath, workbookSQLiteOpenHelper);
        finish();
    }

    // 수정 관련 컨텍스트 메뉴
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater updateMenuInflater = getMenuInflater();
        if(v==viewingWordTxtView){
            updateMenuInflater.inflate(R.menu.viewing_word_click_menu,menu);
        }
        else if(v==hidingWordTxtView){
            updateMenuInflater.inflate(R.menu.hiding_word_click_menu, menu);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch(item.getItemId())
        {
            case R.id.updateViewingWordItem:
                updateAlertDialog.SetInputText(viewingWordTxtView.getText().toString());
                updateAlertDialog.SetOkBtnClickFunc(ViewingWordUpdateClickListener());
                updateAlertDialog.ShowAlertDialog();
                break;
            case R.id.updateHidingWordItem:
                updateAlertDialog.SetInputText(realHidingWord);
                updateAlertDialog.SetOkBtnClickFunc(HidingWordUpdateClickListener());
                updateAlertDialog.ShowAlertDialog();
                break;
        }
        return true;
    }

    private DialogInterface.OnClickListener ViewingWordUpdateClickListener() {
        //Viewing 수정
        return new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String inputText = updateAlertDialog.GetInputTxtText();
                viewingWordTxtView.setText(inputText);
                workbookSQLiteOpenHelper.UpdateData(selectedRowIndex,inputText,null,null);
            }
        };
    }

    private DialogInterface.OnClickListener HidingWordUpdateClickListener() {
        //Hiding 수정
        return new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String inputText = updateAlertDialog.GetInputTxtText();
                hidingWordTxtView.setText(updateAlertDialog.GetInputTxtText());
                workbookSQLiteOpenHelper.UpdateData(selectedRowIndex,null,inputText,null);
                realHidingWord = inputText;
            }
        };
    }




    private void ViewingStartConditionCheckTableCountEmpty(){
        // 테이블의 행이 있냐 없냐에 따라 진행되는 Viewing을 결정함
        if(dataTableRowCount<=0){
            // 테이블 카운트가 0이라면 작동하는 함수
            System.out.println("빈 행 작업이 일어났음.");
            allJobStatusTxtView.setText("0");
            currentJobStatusTxtView.setText("0");
            noneMemorizeTxtView.setText("0");
            memorizeTxtView.setText("0");
            viewingWordTxtView.setText("");
            hidingWordTxtView.setText("");
            currentViewIndex=0;
            maxJobFlag=false;
            selectedRowIndex=0;
        }
        else{
            RandomGetAndStart();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}