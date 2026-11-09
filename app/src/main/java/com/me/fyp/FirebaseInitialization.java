package com.me.fyp;

import android.app.Application;

import com.google.firebase.FirebaseApp;

public class FirebaseInitialization extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);
    }
}
