package com.example.csvenglishworkbook;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.Locale;

public class TTSManager extends TextToSpeech{
    private Context context;
    private float soundVolume;
    public void SetSoundVolume(float value){
        soundVolume=value;
    }
    private float soundPlaySpeed;
    public void SetSoundPlaySpeed(float value){
        soundPlaySpeed=value;
    }

    public TTSManager(Context context) {
        super(context, GetTTSListener());
        this.context = context;
        soundVolume= (float) 0.1;
        soundPlaySpeed= (float) 1.0;

    }

    public static TextToSpeech.OnInitListener GetTTSListener(){
        OnInitListener listener = new TextToSpeech.OnInitListener(){
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
        this.setPitch(soundVolume);      // 음량
        this.setSpeechRate(soundPlaySpeed); // 재생속도
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
