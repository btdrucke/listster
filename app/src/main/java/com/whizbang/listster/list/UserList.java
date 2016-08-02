package com.whizbang.listster.list;

import android.support.annotation.Keep;


public class UserList {

    public String title;
    public long lastModifedUtcMillis;
    public String key;


    @Keep
    public UserList() {
        this(null, null);
    }


    public UserList(String title, String key) {
        this.title = title;
        this.lastModifedUtcMillis = System.currentTimeMillis();
        this.key = key;
    }


    @Override
    public String toString() {
        return "UserList{" +
                "lastModifedUtcMillis=" + lastModifedUtcMillis +
                ", title='" + title + '\'' +
                '}';
    }
}
