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

public class CategoryActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);
        //actionBar hide
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

    }

    public void onClick(View v){
        switch (v.getId()){
            case R.id.see:
                Intent intent = new Intent(getApplication(),MapActivity.class);
                startActivity(intent);
                finish();
                break;

            case R.id.manage:
                Intent intent2 = new Intent(getApplication(),ManageActivity.class);
                startActivity(intent2);
                finish();
                break;
        }

    }
}