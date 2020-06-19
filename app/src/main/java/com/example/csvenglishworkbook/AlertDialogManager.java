package com.example.csvenglishworkbook;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.EditText;

public class AlertDialogManager {

    // OK와 Cancel만 있습니다.

    private AlertDialog.Builder builder;
    private AlertDialog alertDialog;
    private EditText inputEditTxt;


    public AlertDialogManager(Context context) {
        builder = new AlertDialog.Builder(context);

        inputEditTxt = new EditText(context);

        if (inputEditTxt.getParent() != null)
            ((ViewGroup) inputEditTxt.getParent()).removeView(inputEditTxt);
        builder.setView(inputEditTxt);


        builder.setPositiveButton(R.string.alertDialogOK,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        System.out.println("다이얼로그 기본 OK");
                    }
                });

        builder.setNegativeButton(R.string.alertDialogCancel,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        System.out.println("다이얼로그 기본 캔슬");
                    }
                });

        alertDialog = builder.create();
    }

    public void SetInputText(String input) {
        if (inputEditTxt.getParent() != null)
            ((ViewGroup) inputEditTxt.getParent()).removeView(inputEditTxt);
        inputEditTxt.setText(input);
        builder.setView(inputEditTxt);
    }

    public String GetInputTxtText() {
        return inputEditTxt.getText().toString();
    }

    public void SetOkBtnClickFunc(DialogInterface.OnClickListener dialogListener) {
        builder.setPositiveButton(R.string.alertDialogOK, dialogListener);
        alertDialog = builder.create();
    }

    public void SetCancelBtnClickFunc(DialogInterface.OnClickListener dialogListener) {
        builder.setNegativeButton(R.string.alertDialogCancel, dialogListener);
        alertDialog = builder.create();
    }


    public void ShowAlertDialog() {
        if (alertDialog != null) {
            alertDialog.show();
        } else {
            Log.d("alert", "다이얼로그 객체가 없습니다");
        }
    }

}
