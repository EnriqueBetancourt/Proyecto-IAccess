package com.example.iacccess;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class PantallaSplash extends AppCompatActivity {

    public static int tiempoCarga = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pantalla_splash);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(PantallaSplash.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        }, tiempoCarga);
    }
}