package com.whizbang.listster;

import com.google.firebase.database.IgnoreExtraProperties;


@IgnoreExtraProperties
public class UserList {

    private String mTitle;


    public UserList() {}


    public UserList(String title) {
        this.mTitle = title;
    }


    public String getTitle() {
        return mTitle;
    }
}
