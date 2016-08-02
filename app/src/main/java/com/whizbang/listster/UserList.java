package com.whizbang.listster;

import android.support.annotation.Keep;


public class UserList {

    public String title;


    @Keep
    public UserList() {}


    public UserList(String title) {
        this.title = title;
    }


    @Override
    public String toString() {
        return "UserList{" +
                "mTitle='" + title + '\'' +
                '}';
    }
}
