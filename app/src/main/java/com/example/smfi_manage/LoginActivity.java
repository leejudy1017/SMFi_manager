package com.example.smfi_manage;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LoginActivity extends AppCompatActivity {

    /*
    //retrofit
    public static final Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(RetrofitAPI.API_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build();
    public static final RetrofitAPI apiService = retrofit.create(RetrofitAPI.class);
    */

    ProgressDialog customProgressDialog;
    EditText email;
    EditText pwd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //actionBar hide
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        customProgressDialog = new ProgressDialog(this);
        customProgressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        //로딩창 주변이 클릭되도 종료되지 않음
        //customProgressDialog.setCancelable(false);

        email = findViewById(R.id.email);
        pwd = findViewById(R.id.pwd);
    }

    public void onClick(View v){

        switch (v.getId()){
            case R.id.loginBtn:
                Log.i("email",email.getText().toString());
                Log.i("pwd",pwd.getText().toString());

                Intent intent = new Intent(getApplication(),MapActivity.class);
                startActivity(intent);
                finish();
                //customProgressDialog.show();
                //customProgressDialog.dismiss();
                break;

            case R.id.joinBtn:
                Intent intent2 = new Intent(getApplication(),JoinActivity.class);
                startActivity(intent2);
                finish();
                break;
        }
    }
}