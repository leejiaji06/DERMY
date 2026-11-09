package com.me.fyp;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static final int DURATION = 1000;
    private static final int DELAY_DURATION = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        View container = findViewById(R.id.container);
        View greenLogo = findViewById(R.id.big_logo);

        animate(container, greenLogo);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(MainActivity.this, LogOrSignActivity.class);
                startActivity(intent);
                finish();
            }
        }, DELAY_DURATION);
    }

    public static void animate(@NonNull View container, @NonNull View greenLogo) {
        AnimatorSet animatorSet = new AnimatorSet();

        // Scale and alpha animation for green logo
        ObjectAnimator greenScaleXAnimator = ObjectAnimator.ofFloat(greenLogo, View.SCALE_X, 0f, 1f);
        ObjectAnimator greenScaleYAnimator = ObjectAnimator.ofFloat(greenLogo, View.SCALE_Y, 0f, 1f);
        ObjectAnimator greenAlphaAnimator = ObjectAnimator.ofFloat(greenLogo, View.ALPHA, 0f, 1f);

        animatorSet.playTogether(
                greenScaleXAnimator, greenScaleYAnimator, greenAlphaAnimator
        );

        animatorSet.setDuration(DURATION);
        animatorSet.start();
    }

}
