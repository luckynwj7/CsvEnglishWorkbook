package com.example.csvenglishworkbook;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;

import java.util.ArrayList;

public class CsvReader {

    private static String FileReadAndConvertToText(File file) {
        FileReader fr = null;
        BufferedReader bufrd = null;
        String result = "";
        char ch;
        try {
            // open file.
            fr = new FileReader(file);
            bufrd = new BufferedReader(fr);
            // read 1 char from file.
            ch = (char) bufrd.read();
            while (ch != -1) {
                result += ch;
                ch = (char) bufrd.read();
                if (ch == '\uFFFF') {
                    break;
                }
            }
            // close file.
            bufrd.close();
            fr.close();
            return result;
        } catch (Exception e) {
            return "";
        }
    }

    private static ArrayList<String> GetRowSplitTextList(String inputText) {
        String[] split = inputText.split("\n");
        ArrayList<String> result = ConvertArrayToArrayList(split);
        return result;
    }

    private static String[] GetColumnSplitTextListPrevProcess(String inputText){
        // 열로 자르는 함수. 3개의 컬럼이 있으므로 이 개수가 맞지 않을 경우 빈 칸으로 생성시킴
        // String[] 형태로 나오게 되므로 다른 함수를 거쳐서 List형태로 반환해야 함. 그래서 private로 제한함
        int colMax = 3;
        String[] split = inputText.split(",",colMax);
        if(split.length < colMax){
            for(int commaCount=0 ; commaCount<colMax - split.length; commaCount++){
                inputText+=",";
            }
            split = GetColumnSplitTextListPrevProcess(inputText);
        }
        return split;
    }

    private static ArrayList<String> GetColumnSplitTextList(String inputText){
        // 나눈 컬럼을 List로 만드는 함수
        String[] split = GetColumnSplitTextListPrevProcess(inputText);
        ArrayList<String> result = SplitTextHidingWordRevert(split); // 숨겨놓았던 문자(컴마나 따옴표)를 다시 삽입함
        //ArrayList<String> result = ConvertArrayToArrayList(split);
        return result;
    }


    private static ArrayList<String> ConvertArrayToArrayList(String[] inputStr){
        // 단순 배열을 arrayList형태로 만들어주는 함수. (String만 적용)
        ArrayList<String> resultList = new ArrayList<String>();
        for (String splitText:inputStr) {
            resultList.add(splitText);
        }
        return resultList;
    }

    public static ArrayList<ArrayList<String>> GetSplitDataArrayList(File file){
        // 최종적으로 arrayList를 받는 함수
        ArrayList<ArrayList<String>> resultList = new ArrayList<ArrayList<String>>();
        if(file.length()==0){
            // 빈 파일은 그대로 반환함
            return null;
        }
        String convertText = FileReadAndConvertToText(file);
        ArrayList<String> splitRowData = GetRowSplitTextList(convertText);
        for (String rowSplit:splitRowData) {
            if(rowSplit.equals(",,")){
                continue;
            }
            ArrayList<String> splitColData = GetColumnSplitTextList(SaveTextErrorPosition(rowSplit)); // 에러 포지션을 저장하고, 그것에 대한것을 나눔
            resultList.add(splitColData);
        }
        return resultList;
    }

    public static void SaveArrayListInDataBase(ArrayList<ArrayList<String>> inputArray, CustomSQLiteOpenHelper db){
        // db에 List를 저장해주는 함수
        // 넣을 때는 전부 string형태로 넣어줌. 내부에서 알아서 integer로 변환이 되어 DB에 저장됨
        for (ArrayList<String> outerList:inputArray) {
            System.out.println("집어 넣을 것들 : " + outerList.get(0) + ":" + outerList.get(1));
            if(outerList.get(0).length()==0 || outerList.get(1).length()==0){
                //집어 넣을 가치가 없는 것들은 제외시킴
                continue;
            }
            else if(outerList.get(2).length()==0){
                db.InsertData(outerList.get(0),outerList.get(1),"0");
            }
            else{
                db.InsertData(outerList.get(0),outerList.get(1),outerList.get(2));
            }
        }
    }




    /// 컴마와 쌍따옴표를 를 처리해주는 부분
    private static ArrayList<HashMap<Integer,String>> textErrorIndexList; // Split을 기준으로, 임시로 받아두는 리스트
    private static String SaveTextErrorPosition(String inputText) {

        int inputTextLength = inputText.length();
        boolean innerDoubleQuotesFlag = false; // 쌍따옴표 안에 들어왔다는 의미

        inputText = inputText + ",";

        textErrorIndexList = new ArrayList<>();
        int innerPosition = 0;//쌍따옴표가 열렸을 때의 내부 위치를 반환

        HashMap<Integer,String> errorPosition = new HashMap<Integer,String>();//에러 문자의 위치를 반환하는 리스트

        for(int charIndex=0;charIndex < inputTextLength; charIndex++) {
            if(innerDoubleQuotesFlag==false && inputText.charAt(charIndex)=='"') {
                // 들어왔음을 의미함
                innerDoubleQuotesFlag=true;


                inputText = inputText.substring(0,charIndex) + inputText.substring(charIndex+1); //현재 따옴표 삭제
                inputTextLength--; // 삭제를 했으니 길이도 줄임
                charIndex--; // 삭제했으니 반복수도 줄임

                continue;
            }
            else if (innerDoubleQuotesFlag==true && inputText.charAt(charIndex)=='"' && inputText.charAt(charIndex+1)==',') {
                // 나갔음을 의미함
                innerDoubleQuotesFlag=false;

                inputText = inputText.substring(0,charIndex) + inputText.substring(charIndex+1); //현재 따옴표 삭제
                inputTextLength--; // 삭제를 했으니 길이도 줄임
                charIndex--; // 삭제했으니 반복수도 줄임

                continue;
            }
            else if(innerDoubleQuotesFlag==false && inputText.charAt(charIndex)==',') {
                innerPosition = 0; // 문장위치 반환
                textErrorIndexList.add(errorPosition);
                errorPosition = new HashMap<Integer,String>();
            }



            // 들어왔을 때 동작하는 함수
            if(innerDoubleQuotesFlag) {
                if(inputText.charAt(charIndex)=='"' && inputText.charAt(charIndex+1)=='"') {
                    // 따옴표 두 개 뭉치의 위치를 저장 및 따옴표 하나 삭제
                    errorPosition.put(innerPosition, "\"");
                    inputText = inputText.substring(0,charIndex+0) + inputText.substring(charIndex+2); //현재 및 다음 따옴표 삭제
                    inputTextLength-=2; // 삭제를 했으니 길이도 줄임
                    charIndex--; // 삭제했으니 반복수도 줄임
                }
                else if(inputText.charAt(charIndex)==',') {
                    //컴마의 위치를 저장
                    errorPosition.put(innerPosition, ",");
                    inputText = inputText.substring(0,charIndex+0) + inputText.substring(charIndex+1); //현재 컴마 삭제
                    inputTextLength--; // 삭제를 했으니 길이도 줄임
                    charIndex--; // 삭제했으니 반복수도 줄임
                }
                innerPosition++;
            }

        }
        textErrorIndexList.add(errorPosition); //마지막 작업
        inputText = inputText.substring(0,inputTextLength); // 삭제할 거 전부 삭제하고 원래 배열로 돌아옴
        return inputText;
    }

    private static ArrayList<String> SplitTextHidingWordRevert(String[] splitArray) {
        if(textErrorIndexList==null) {
            System.out.println("사전에 정의된 배열이 없어 실행할 수 없음");
            return null;
        }
        int sentCount = 0;
        ArrayList<String> resultList = new ArrayList<String>();
        for (String text:splitArray) {
            for(int key:textErrorIndexList.get(sentCount).keySet()) {
                text = text.substring(0,key) + textErrorIndexList.get(sentCount).get(key) + text.substring(key);
            }
            sentCount++;
            resultList.add(text);
        }
        return resultList;
    }


    public static void ShowDoubleArrayList(ArrayList<ArrayList<String>> inputArray){
        //디버깅전용 함수
        for (ArrayList<String> outerList:inputArray) {
            for (String innerStr:outerList) {
                System.out.print(innerStr + "|");
            }
            System.out.println("");
        }
    }
}