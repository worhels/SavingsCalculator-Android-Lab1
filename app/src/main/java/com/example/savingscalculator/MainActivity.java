package com.example.savingscalculator;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.button_start).setOnClickListener(view ->
                startActivity(new Intent(this, IncomeActivity.class)));
        findViewById(R.id.button_exit).setOnClickListener(view -> finish());
    }
}
