package com.whizbang.listster;

import android.app.Application;

import com.google.firebase.messaging.FirebaseMessaging;


public class ListsterApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseMessaging.getInstance().subscribeToTopic("new-lists");
    }
}
