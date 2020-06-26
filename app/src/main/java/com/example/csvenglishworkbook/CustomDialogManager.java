package com.example.csvenglishworkbook;

import android.app.Dialog;
import android.content.Context;

public class CustomDialogManager {
    protected Dialog myDialog;
    private Context myContext;
    public CustomDialogManager(Context context, int layoutResID){
        myDialog = new Dialog(context);
        myDialog.setContentView(layoutResID);
        this.myContext=context;
    }

    public void Show(){
        myDialog.show();
    }
}
