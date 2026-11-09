package com.me.fyp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.me.fyp.databinding.ActivityHistoryBinding;

public class HistoryActivity extends BaseActivity {
    ActivityHistoryBinding activityHistoryBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityHistoryBinding = ActivityHistoryBinding.inflate(getLayoutInflater());
        setContentView(activityHistoryBinding.getRoot());
        allocatedActivityTitle("History");
    }

    public void goDetect(View view)
    {
        Intent goDetect = new Intent(this, DetectionActivityHistory.class);
        startActivity(goDetect);
    }

    public void goClassify(View view)
    {
        Intent goClassify = new Intent(this, ClassificationActivityHistory.class);
        startActivity(goClassify);
    }
}