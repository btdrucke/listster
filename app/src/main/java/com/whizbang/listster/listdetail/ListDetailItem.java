package com.whizbang.listster.listdetail;

import android.support.annotation.Keep;


public class ListDetailItem {

    public String listKey;
    public String title;
    public String lastModifiedUser;
    public long lastModifiedUtcMillis;
    public boolean completed;


    @Keep
    public ListDetailItem() {
        this(null, null, null, false);
    }


    public ListDetailItem(String listKey, String title, String lastModifiedUser,
            boolean completed) {
        this.listKey = listKey;
        this.title = title;
        this.lastModifiedUser = lastModifiedUser;
        this.lastModifiedUtcMillis = System.currentTimeMillis();
        this.completed = completed;
    }
}
