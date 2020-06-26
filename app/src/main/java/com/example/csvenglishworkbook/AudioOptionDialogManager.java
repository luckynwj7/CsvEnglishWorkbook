package com.example.csvenglishworkbook;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.TextView;

public class AudioOptionDialogManager extends CustomDialogManager {

    private TextView audioOptionSpeedStatusTxtView;

    private CheckBox audioOptionMuteFlagChkBox;

    private Button audioOptionExitBtn;
    private Button audioOptionBasicApplyBtn;
    private Button audioOptionApplyBtn;

    private SeekBar audioOptionSpeedSeekBar;

    private TTSManager baseTTSManager;


    public AudioOptionDialogManager(Context context, TTSManager tts){
        super(context, R.layout.audio_option_dialog);
        this.baseTTSManager = tts;


        audioOptionSpeedStatusTxtView = myDialog.findViewById(R.id.audioOptionSpeedStatusTxtView);

        audioOptionMuteFlagChkBox = myDialog.findViewById(R.id.audioOptionMuteFlagChkBox);

        audioOptionExitBtn = myDialog.findViewById(R.id.audioOptionExitBtn);
        audioOptionBasicApplyBtn = myDialog.findViewById(R.id.audioOptionBasicApplyBtn);
        audioOptionApplyBtn = myDialog.findViewById(R.id.audioOptionApplyBtn);

        audioOptionSpeedSeekBar = myDialog.findViewById(R.id.audioOptionSpeedSeekBar);

        audioOptionExitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myDialog.dismiss();
            }
        });

        audioOptionBasicApplyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 기본값으로 설정
                baseTTSManager.SetSoundMute(false);
                baseTTSManager.SetSoundPlaySpeed((float)1.0);
                SetSeekBarFromTTSManager();
                myDialog.dismiss();
            }
        });

        audioOptionApplyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                float speed = ConvertSpeedIntToFloat(audioOptionSpeedSeekBar.getProgress());
                baseTTSManager.SetSoundPlaySpeed(speed);

                boolean muteFlag = audioOptionMuteFlagChkBox.isChecked();
                baseTTSManager.SetSoundMute(muteFlag);
                SetSeekBarFromTTSManager();
                myDialog.dismiss();
            }
        });

        audioOptionSpeedSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                audioOptionSpeedStatusTxtView.setText(Integer.toString(audioOptionSpeedSeekBar.getProgress()));
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }


    @Override
    public void Show(){
        SetSeekBarFromTTSManager();
        super.Show();
    }

    private void SetSeekBarFromTTSManager(){
        int speed = ConvertSpeedFloatToInt(baseTTSManager.GetSoundPlaySpeed());
        audioOptionSpeedSeekBar.setProgress(speed,true);
        audioOptionSpeedStatusTxtView.setText(Integer.toString(speed));

        boolean muteFlag = baseTTSManager.GetSoundMute();
        audioOptionMuteFlagChkBox.setChecked(muteFlag);
    }

    // 알맞는 형태로 숫자 변환을 해주는 함수
    private int ConvertSpeedFloatToInt(float speed){
        int result = (int)(speed * (float)(100.0)); // 1.0을 기본이라 생각했을 때, 100%만 적용시킴(맥스가 200임)
        return result;
    }
    private float ConvertSpeedIntToFloat(int speed){
        float result = (float)speed / (float)(100.0);
        return result;
    }
}
