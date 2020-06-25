package com.example.csvenglishworkbook;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;

import java.io.OutputStreamWriter;
import java.util.ArrayList;

public class CsvWriter
{
    public static void WriteScvFile(String filePath, CustomSQLiteOpenHelper db) {
        File writeFile = new File(filePath);
        FileWriter fw = null;

        String inputText = GetStringContentsConvertFromDB(db);
        try {
            FileOutputStream output=new FileOutputStream(filePath,false);
            //true로 두면 이어서 쓰고 , false로 쓰면 새로 씀
            OutputStreamWriter writer=new OutputStreamWriter(output,"euc-kr");
            BufferedWriter out=new BufferedWriter(writer);
            out.write(inputText);
            //out.append("");
            out.close();
        }
        catch(Exception e) {
            System.out.println("실패");
        }
        // 출처 https://m.blog.naver.com/PostView.nhn?blogId=software705&logNo=220587262406&proxyReferer=https:%2F%2Fwww.google.com%2F
    }

    private static String GetStringContentsConvertFromDB(CustomSQLiteOpenHelper db){
        String result = "";
        int maxRowCount = db.DataTableRowCount();

        for(int rowCount = 1; rowCount<=maxRowCount;rowCount++){
            ArrayList<Object> tempArray = db.SelectRowAllData(rowCount);

            String inputText = (String)tempArray.get(1);
            inputText = RevertOriginalCsvType(inputText);
            result += inputText;
            result += ",";

            inputText = (String)tempArray.get(2);
            inputText = RevertOriginalCsvType(inputText);
            result += inputText;
            result += ",";

            result += Integer.toString((Integer)tempArray.get(3));
            result += "\n";
        }
        if(result.length()>1){
            result = result.substring(0,result.length()-1); // 마지막 공백문자를 제거
        }
        return result;
    }

    private static String RevertOriginalCsvType(String inputText){
        String result = inputText;
        if(inputText.contains(",") || inputText.contains(",")){
            inputText = inputText.replaceAll("\"","\"\"");
            result = "\"" + inputText + "\"";

        }
        return result;
    }

}
