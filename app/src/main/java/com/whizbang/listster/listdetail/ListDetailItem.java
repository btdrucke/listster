package com.whizbang.listster.listdetail;

public class ListDetailItem {

    public String listKey;
    public String title;
    public String author;
    public boolean completed;


    public ListDetailItem(String listKey, String title, String author, boolean completed) {
        this.listKey = listKey;
        this.title = title;
        this.author = author;
        this.completed = completed;
    }
}
