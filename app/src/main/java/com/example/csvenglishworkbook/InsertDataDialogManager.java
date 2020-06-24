package com.example.csvenglishworkbook;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class InsertDataDialogManager{

    private Dialog myDialog;
    private EditText insertDataDialogViewingWordEditTxt;
    public String GetViewingWordTxt(){
        return insertDataDialogViewingWordEditTxt.getText().toString();
    }
    private EditText insertDataDialogHidingWordEditTxt;
    public String GetHidingWordTxt(){
        return insertDataDialogHidingWordEditTxt.getText().toString();
    }

    private Button insertDataDialogExitBtn;
    private Button insertDataDialogOKBtn;

    public InsertDataDialogManager(Context context){
        myDialog = new Dialog(context);
        myDialog.setContentView(R.layout.insert_data_dialog);

        insertDataDialogViewingWordEditTxt = myDialog.findViewById(R.id.insertDataDialogViewingWordEditTxt);
        insertDataDialogHidingWordEditTxt = myDialog.findViewById(R.id.insertDataDialogHidingWordEditTxt);
        insertDataDialogExitBtn = myDialog.findViewById(R.id.insertDataDialogExitBtn);
        insertDataDialogOKBtn = myDialog.findViewById(R.id.insertDataDialogOKBtn);


        insertDataDialogExitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myDialog.dismiss();
            }
        });

        insertDataDialogOKBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                WorkbookActivity.NewDataInsert(insertDataDialogViewingWordEditTxt.getText().toString(), insertDataDialogHidingWordEditTxt.getText().toString());
                insertDataDialogViewingWordEditTxt.setText("");
                insertDataDialogHidingWordEditTxt.setText("");
                myDialog.dismiss();
            }
        });

    }

    public void Show(){
        myDialog.show();
    }

}
