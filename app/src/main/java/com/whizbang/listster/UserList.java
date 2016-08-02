package com.whizbang.listster;

import android.support.annotation.Keep;


public class UserList {

    public String title;
    public long lastModifedUtcMillis;


    @Keep
    public UserList() {
        this(null);
    }


    public UserList(String title) {
        this.title = title;
        this.lastModifedUtcMillis = System.currentTimeMillis();
    }


    @Override
    public String toString() {
        return "UserList{" +
                "lastModifedUtcMillis=" + lastModifedUtcMillis +
                ", title='" + title + '\'' +
                '}';
    }
}
