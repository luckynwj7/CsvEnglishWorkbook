package com.example.csvenglishworkbook;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class CsvReader {

    public static String FileReadAndConvertToText(File file) {
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

    public static void GetRowSplitTextList(String inputText) {
        String[] split = inputText.split("\n");
    }
}
