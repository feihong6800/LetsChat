package com.example.letschat;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.LayoutInflater;

public class Reset {

    private Activity activity;
    private AlertDialog alertDialog;

    Reset(Activity myActivity){
        activity = myActivity;
    }

    void startLodingDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        LayoutInflater inflater=activity.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.email_sent,null));
        builder.setCancelable(true);

        alertDialog=builder.create();
        alertDialog.show();
    }
    void dissmissDialog(){
        alertDialog.dismiss();
    }
}
