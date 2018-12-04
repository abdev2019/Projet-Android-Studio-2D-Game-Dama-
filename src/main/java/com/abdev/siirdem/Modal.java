package com.abdev.siirdem;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class Modal extends Dialog implements View.OnClickListener
{
    public Activity c;
    public Button yes, no, bloquer;
    TextView title, message;
    public EditText editText;

    public Modal(Activity a) {
        super(a);
        this.c = a;
        setCancelable(false);
        setCanceledOnTouchOutside(false);
        editText = new EditText( a );
    }


    public void setTitle(String title){
        if(this.title!=null)
            this.title.setText(title+"");
    }

    public void setMessage(String message){
        if(this.message!=null)
            this.message.setText(message+"");
    }

    public void clear(){
        this.getWindow().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#e9f3fa")));
    }

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.modal);
        yes = (Button) findViewById(R.id.btn_yes);
        no = (Button) findViewById(R.id.btn_no);
        bloquer = (Button) findViewById(R.id.id_btnBloquer);

        title = (TextView)findViewById(R.id.title_diag);
        message = (TextView)findViewById(R.id.message_diag);

        yes.setOnClickListener(this);
        no.setOnClickListener(this);
        bloquer.setOnClickListener(this);

        View.OnTouchListener onTouchListener = new View.OnTouchListener() {
            @Override public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction()==MotionEvent.ACTION_DOWN) v.setBackgroundColor(Color.rgb(100,100,255));
                else if(event.getAction()==MotionEvent.ACTION_UP) v.setBackgroundColor(Color.TRANSPARENT);
                return false;
            }
        };
        yes.setOnTouchListener(onTouchListener);
        no.setOnTouchListener(onTouchListener);
    }

    @Override public void onClick(View v) {

    }

}