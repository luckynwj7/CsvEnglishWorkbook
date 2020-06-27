package com.example.csvenglishworkbook;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.widget.Toast;
import java.util.ArrayList;

import org.w3c.dom.Text;

import java.util.Locale;

public class TTSManager extends TextToSpeech{
    private Context context;

    private float soundPitch;

    private boolean soundMuteFlag;
    public boolean GetSoundMute(){
        return soundMuteFlag;
    }
    public void SetSoundMute(boolean value){
        String inputValue = null;
        if(value == true){
            inputValue="1";
        }
        else{
            inputValue="0";
        }
        FileSelectActivity.GetAudioOptionsSQLiteOpenHelper().UpdateData(1,inputValue, null);
        soundMuteFlag = value;
    }

    private float soundPlaySpeed;
    public float GetSoundPlaySpeed(){
        return soundPlaySpeed;
    }
    public void SetSoundPlaySpeed(float value){
        String inputValue = Float.toString(value);
        FileSelectActivity.GetAudioOptionsSQLiteOpenHelper().UpdateData(1,null, inputValue);
        soundPlaySpeed=value;
    }

    public TTSManager(Context context) {
        super(context, GetTTSListener());
        this.context = context;
        soundPitch = (float) 0; // 기본값

        try{
            // DB에서 값을 가져옴
            ArrayList<Object> databaseValue = FileSelectActivity.GetAudioOptionsSQLiteOpenHelper().SelectRowAllData(1);
            soundPlaySpeed= (float) databaseValue.get(2);

            int temp = (int) databaseValue.get(1);
            if(temp==0){
                soundMuteFlag = false;
            }
            else{
                soundMuteFlag = true;
            }
        }
        catch(Exception e){
            System.out.println("DB에서 오디오 옵션값을 가져오는 과정에서 오류가 발생함");
            soundPlaySpeed=(float)1.0;
            soundMuteFlag=false;
        }

    }

    private static TextToSpeech.OnInitListener GetTTSListener(){
        final OnInitListener listener = new TextToSpeech.OnInitListener(){
            @Override
            public void onInit(int i) {
            }
        };
        return listener;
    }


    @Override
    protected void finalize() throws Throwable {
        System.out.println("종료됨");
        super.finalize();
    }

    public void Speech(String inputText) {
        SetSpeechLanguage(inputText);
        this.setPitch(soundPitch);
        this.setSpeechRate(soundPlaySpeed); // 재생속도

        if(soundMuteFlag){
            System.out.println("음소거");
            // 음소거 모드
            return;
        }
        this.speak(inputText, TextToSpeech.QUEUE_FLUSH, null);
    }
    private void SetSpeechLanguage(String language){
        String testText = DiscriminateTextLanguage(language);
        if(testText=="korean"){
            System.out.println("한국어 적용");
            this.setLanguage(Locale.KOREAN);
        }
        else if(testText=="english"){
            System.out.println("영어 적용");
            this.setLanguage(Locale.ENGLISH);
        }

    }
    public static String DiscriminateTextLanguage(String text){
        // 문장이 아스키코드면 영어로 인식, 그 외에는 모두 한국어로 인식함
        String result = "korean";
        int charIndex;
        for(charIndex=0;charIndex<text.length();charIndex++){
            char finalChar = text.charAt(charIndex);
            if(finalChar<=122){
                result = "english";
            }
            else if(finalChar>122){
                result = "korean";
                break;
            }
        }
        return result;
    }

    public void SetTTSLanguage(String inputText) {
        Integer language = null;
        if (inputText != "") {
            language = this.setLanguage(Locale.KOREAN);
        } else if (inputText == "") {
            language = this.setLanguage(Locale.ENGLISH);
        }
        if (language==null || language == TextToSpeech.LANG_MISSING_DATA || language == TextToSpeech.LANG_NOT_SUPPORTED) {
            Toast.makeText(context, "지원하지 않는 언어입니다.", Toast.LENGTH_SHORT).show();
        }
    }

}
