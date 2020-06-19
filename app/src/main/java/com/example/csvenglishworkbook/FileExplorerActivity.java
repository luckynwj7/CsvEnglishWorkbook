package com.example.csvenglishworkbook;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_explorer);
        thisActivityIntent = new Intent();

        prevDirectoryBtn = findViewById(R.id.prevDirectoryBtn);
        newCsvFileBtn = findViewById(R.id.newCsvFileBtn);
        currentPathTxt = findViewById(R.id.currentPathTxt);
        fileListView = findViewById(R.id.fileListView);

        fileArrayList = new ArrayList<String>();

        currentPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Test";
        //currentPath = getExternalCacheDir().getAbsolutePath();
        Log.d("alert","현재경로" + currentPath);
        Log.d("alert","접근권한" + Environment.getExternalStorageState());


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
                FileListViewItemClick(adapterView, view, i , l);
            }
        });
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
        String newFileName = getString(R.string.newCsvFileName) + " " + Integer.toString(currentExistFileCount) + ".csv";
        System.out.println("생성될 파일 이름 : " + newFileName);
        File file = new File(currentPath, newFileName);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            fos.write("testStr".getBytes());
            fos.close();
            RefreshFiles();

            System.out.println("파일생성 성공");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("파일생성 실패");
        }

    }

    private void FileListViewItemClick(AdapterView<?> adapterView, View view, int position, long id){
        String Name = fileArrayList.get(position);//클릭된 위치의 값을 가져옴
        //디렉토리이면
        if(Name.startsWith("[") && Name.endsWith("]")){
            Name = Name.substring(1, Name.length() - 1);//[]부분을 제거해줌
        }
        //들어가기 위해 /와 터치한 파일 명을 붙여줌
        String Path = currentPath + "/" + Name;
        File f = new File(Path);//File 클래스 생성
        if(f.isDirectory()){//디렉토리면?
            currentPath = Path;//현재를 Path로 바꿔줌
            RefreshFiles();//리프레쉬
        }else {//디렉토리가 아니면 토스트 메세지를 뿌림
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
        }
        else{
            currentExistFileCount = 0;
        }
        //다끝나면 리스트뷰를 갱신시킴
        fileListAdapter.notifyDataSetChanged();
    }
    //출처: https://gakari.tistory.com/entry/안드로이드-파일-탐색기-만들기 [가카리의 공부방]



    private void PutResultIntoIntent(String value){
        thisActivityIntent.putExtra("pathResult",value);
    }

    public FileExplorerActivity(){
        System.out.println("액티비티 2 생성");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.out.println("액티비티 2 소멸");
    }
}