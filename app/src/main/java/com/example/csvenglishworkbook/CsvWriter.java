package com.example.csvenglishworkbook;

import java.io.File;
import java.io.FileWriter;

import java.util.ArrayList;

public class CsvWriter
{
    public static void WriteScvFile(String filePath, CustomSQLiteOpenHelper db) {
        File writeFile = new File(filePath);
        FileWriter fw = null;

        String inputText = GetStringContentsConvertFromDB(db);
        try {
            // open file.
            fw = new FileWriter(writeFile);

            // write file.
            fw.write(inputText);

        } catch (Exception e) {
            e.printStackTrace();
        }

        // close file.
        if (fw != null) {
            // catch Exception here or throw.
            try {
                fw.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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
        result = result.substring(0,result.length()-1); // 마지막 공백문자를 제거
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
