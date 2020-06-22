package com.example.csvenglishworkbook;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import java.util.*;
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
        ArrayList<String> result = ConvertArrayToArrayList(split);
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
        ArrayList<ArrayList<String>> resultList = new ArrayList<ArrayList<String>>();
        String convertText = FileReadAndConvertToText(file);
        ArrayList<String> splitRowData = GetRowSplitTextList(convertText);
        for (String rowSplit:splitRowData) {
            ArrayList<String> splitColData = GetColumnSplitTextList(rowSplit);
            resultList.add(splitColData);
        }
        return resultList;
    }

    public static void SaveArrayListInDataBase(ArrayList<ArrayList<String>> inputArray, WorkbookSQLiteOpenHelper db){
        // db에 List를 저장해주는 함수
        // 넣을 때는 전부 string형태로 넣어줌. 내부에서 알아서 integer로 변환이 되어 DB에 저장됨
        for (ArrayList<String> outterList:inputArray) {
            System.out.println("집어 넣을 것들 : " + outterList.get(0) + ":" + outterList.get(1));
            if(outterList.get(2).length()==0){
                db.InsertData(outterList.get(0),outterList.get(1),"0");
            }
            else{
                db.InsertData(outterList.get(0),outterList.get(1),outterList.get(2));
            }
        }
    }

    public static void ShowDoubleArrayList(ArrayList<ArrayList<String>> inputArray){
        //디버깅전용 함수
        for (ArrayList<String> outterList:inputArray) {
            for (String innerStr:outterList) {
                System.out.print(innerStr + "|");
            }
            System.out.println("");
        }
    }
}
