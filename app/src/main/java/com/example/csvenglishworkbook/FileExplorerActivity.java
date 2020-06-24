package com.example.csvenglishworkbook;

import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class FileExplorerActivity extends AppCompatActivity {
    private Button prevDirectoryBtn;
    private Button newCsvFileBtn;
    private TextView currentPathTxt;
    private Intent thisActivityIntent;

    private ListView fileListView;

    private ArrayAdapter<String> fileListAdapter;
    private ArrayList<String> fileArrayList;

    private String currentPath;

    private int currentExistFileCount;

    private AlertDialogManager alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_explorer);
        this.setTitle(getString(R.string.app_name)+" - 파일 탐색기");

        thisActivityIntent = new Intent();

        prevDirectoryBtn = findViewById(R.id.prevDirectoryBtn);
        newCsvFileBtn = findViewById(R.id.newCsvFileBtn);
        currentPathTxt = findViewById(R.id.currentPathTxt);
        fileListView = findViewById(R.id.fileListView);

        alertDialog = new AlertDialogManager(this);


        fileArrayList = new ArrayList<String>();

        currentPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + getString(R.string.app_name);
        File dir = new File(currentPath);
        if(!dir.exists()){
            dir.mkdir();
        }
        Log.d("alert", "현재경로" + currentPath);
        Log.d("alert", "접근권한" + Environment.getExternalStorageState());


        prevDirectoryBtn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                PrevDirectoryBtnClick(view);
            }
        });
        newCsvFileBtn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                NewCsvFileBtnClick(view);
            }
        });

        fileListAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,fileArrayList);
        fileListView.setAdapter(fileListAdapter);
        fileListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                FileListViewItemClick(adapterView, view, i, l);
            }
        });
        registerForContextMenu(fileListView);
        RefreshFiles();

        setResult(RESULT_OK,thisActivityIntent);
    }

    private void PrevDirectoryBtnClick(View view){
        if(currentPath.compareTo(Environment.getExternalStorageDirectory().getAbsolutePath()) != 0){//루트가 아니면
            int end = currentPath.lastIndexOf("/");///가 나오는 마지막 인덱스를 찾고
            String upPath = currentPath.substring(0, end);//그부분을 잘라버림 즉 위로가게됨
            currentPath = upPath;
            RefreshFiles();//리프레쉬
        }
    }

    private void NewCsvFileBtnClick(View view) {
        File file = null;
        do {
            String newFileName = getString(R.string.newCsvFileName) + Integer.toString(currentExistFileCount) + ".csv";
            System.out.println("생성될 파일 이름 : " + newFileName);
            file = new File(currentPath, newFileName);
            currentExistFileCount++;
        }while(file.exists());
        try {
            FileOutputStream fos = new FileOutputStream(file);
            fos.write("".getBytes());
            fos.close();
            RefreshFiles();

            System.out.println("파일생성 성공");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("파일생성 실패");
        }

    }

    private void FileListViewItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        String clickFileName = fileArrayList.get(position);//클릭된 위치의 값을 가져옴
        //디렉토리이면
        if (clickFileName.startsWith("[") && clickFileName.endsWith("]")) {
            clickFileName = clickFileName.substring(1, clickFileName.length() - 1);//[]부분을 제거해줌
        }
        //들어가기 위해 /와 터치한 파일 명을 붙여줌
        String clickResultFilePath = currentPath + "/" + clickFileName;
        File resultFile = new File(clickResultFilePath);//File 클래스 생성
        if (resultFile.isDirectory()) {//디렉토리면?
            currentPath = clickResultFilePath;//현재를 Path로 바꿔줌
            RefreshFiles();//리프레쉬
        } else {
            // 확장자 비교 작업을 하여 CSV파일만 읽어냄
            String fileExtension = ExtractExtension(clickFileName);
            if (fileExtension.equals(".csv")) {
                thisActivityIntent.putExtra("filePullPath", clickResultFilePath); // 파일 전체 경로를 전달
                thisActivityIntent.putExtra("fileName", clickFileName.replace(".csv","")); // 파일 이름을 전달
                setResult(RESULT_OK,thisActivityIntent); // 인텐트 적용
                finish(); // 창 종료

            } else {
                Toast.makeText(this, "선택할 수 없는 파일입니다.", Toast.LENGTH_SHORT).show();
            }
            //디렉토리가 아니면 토스트 메세지를 뿌림
            //Toast.makeText(FileExplorerActivity.this, fileArrayList.get(position), 0).show();
        }
    }

    void RefreshFiles(){
        currentPathTxt.setText(currentPath.replace(Environment.getExternalStorageDirectory().getAbsolutePath(),"Phone"));//현재 PATH를 가져옴
        fileArrayList.clear();//배열리스트를 지움
        File current = new File(currentPath);//현재 경로로 File클래스를 만듬

        String[] files = current.list();//현재 경로의 파일과 폴더 이름을 문자열 배열로 리턴
        //파일이 있다면?
        if(files != null){
            currentExistFileCount=files.length;
            //여기서 출력을 해줌
            for(int i = 0; i < files.length;i++){
                String Path = currentPath + "/" + files[i];
                String Name = "";
                File f = new File(Path);
                if(f.isDirectory()){
                    Name = "[" + files[i] + "]";//디렉토리면 []를 붙여주고
                }else{
                    Name = files[i];//파일이면 그냥 출력
                }
                fileArrayList.add(Name);//배열리스트에 추가해줌
            }
        } else {
            currentExistFileCount = 0;
        }
        //다끝나면 리스트뷰를 갱신시킴
        fileListAdapter.notifyDataSetChanged();
    }
    //출처: https://gakari.tistory.com/entry/안드로이드-파일-탐색기-만들기 [가카리의 공부방]


    //Context 메뉴로 등록한 View(여기서는 ListView)가 처음 클릭되어 만들어질 때 호출되는 메소드

    @Override

    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        // TODO Auto-generated method stub
        //res폴더의 menu플더안에 xml로 MenuItem추가하기.
        //mainmenu.xml 파일을 java 객체로 인플레이트(inflate)해서 menu객체에 추가
        getMenuInflater().inflate(R.menu.file_view_item_menu, menu);
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    //Context 메뉴로 등록한 View(여기서는 ListView)가 클릭되었을 때 자동으로 호출되는 메소드

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        //AdapterContextMenuInfo
        //AdapterView가 onCreateContextMenu할때의 추가적인 menu 정보를 관리하는 클래스
        //ContextMenu로 등록된 AdapterView(여기서는 Listview)의 선택된 항목에 대한 정보를 관리하는 클래스
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int index = info.position; //AdapterView안에서 ContextMenu를 보여즈는 항목의 위치
        //선택된 ContextMenu의  아이템아이디를 구별하여 원하는 작업 수행
        //예제에서는 선택된 ListView의 항목(String 문자열) data와 해당 메뉴이름을 출력함
        File selectedFile;
        switch (item.getItemId()) {
            case R.id.modify:
                selectedFile = new File(currentPath + "/" + fileArrayList.get(index));
                selectedTempFile = selectedFile;
                alertDialog.SetInputText(fileArrayList.get(index));
                alertDialog.SetOkBtnClickFunc(GetModifyOkClickListener());
                alertDialog.ShowAlertDialog();
                break;
            case R.id.delete:
                selectedFile = new File(currentPath + "/" + fileArrayList.get(index));
                selectedFile.delete();
                Toast.makeText(this, fileArrayList.get(index) + "가 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                RefreshFiles();
                break;
        }
        return true;
    }
    //출처: https://kitesoft.tistory.com/68 [안드로이드 어플 개발]


    private File selectedTempFile; // 파일 선택을 위한 임시변수

    private DialogInterface.OnClickListener GetModifyOkClickListener() {
        return new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                ModifyClickEvent();
            }
        };
    }


    private void ModifyClickEvent() {
        // 클릭 이벤트 시 작동함

        // 파일 확장자 먼저 뽑아냄
        String fileExtension = ExtractExtension(selectedTempFile.getName());
        String inputResult = ModifyFileNameCondition(alertDialog.GetInputTxtText(), fileExtension);
        if (inputResult.equals("")) {
            Toast.makeText(this, "이름이 수정에 실패했습니다.\n들어갈 수 없는 기호가 있거나 확장자가 바뀌었습니다.", Toast.LENGTH_SHORT).show();
            selectedTempFile = null;
            return;
        }
        File renameFile = new File(currentPath + "/" + inputResult);
        if (renameFile.exists()) {
            Toast.makeText(this, "이미 같은 이름의 파일이 존재합니다.", Toast.LENGTH_SHORT).show();
            selectedTempFile = null;
            return;
        }
        selectedTempFile.renameTo(renameFile);
        RefreshFiles();
        Toast.makeText(this, "이름이 수정되었습니다.", Toast.LENGTH_SHORT).show();
        selectedTempFile = null;
    }

    private String ModifyFileNameCondition(String input, String extension) {
        // 바꿀 수 있는 파일 이름에 대한 조건을 참조
        // ""로 리턴하면 실패했다는 의미
        if (input.length() <= 0) {
            // 파일 입력을 아무것도 입력 안했을 경우
            return "";
        } else{
            if (input.length() >= extension.length()){
              String subString = input.substring(input.length() - extension.length());
                if(!subString.equals(extension)) {
                    // 확장자가 다를 경우
                    return "";
                }
            }
            else{
                return "";
            }
        }
        return input;
    }

    private String ExtractExtension(String fileName){
        String result="";
        int findIndex=0;
        for(int charIndex = fileName.length()-1;charIndex >=0 ;charIndex--){
            if(fileName.charAt(charIndex)=='.'){
                findIndex=charIndex;
                break;
            }
        }
        if(findIndex>0){
            result=fileName.substring(findIndex);
        }
        return result;
    }

    public FileExplorerActivity() {
        System.out.println("액티비티 2 생성");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.out.println("액티비티 2 소멸");
    }
}