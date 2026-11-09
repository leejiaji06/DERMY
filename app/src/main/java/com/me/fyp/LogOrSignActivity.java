package com.me.fyp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class LogOrSignActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_or_sign);
    }

    public void goLogIn(View view)
    {
        Intent goLogIn = new Intent(this, LogInActivity.class);
        startActivity(goLogIn);
    }

    public void goSignUp(View view)
    {
        Intent goSignUp = new Intent(this, SignUpActivity.class);
        startActivity(goSignUp);
    }
}