package com.whizbang.listster.listdetail;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.EditText;
import android.widget.ImageView;

import com.whizbang.listster.R;


public class ListDetailActivity extends AppCompatActivity {

    private final static String EXTRA_LIST_KEY = "extra_list_key";
    private RecyclerView mRecyclerView;
    private ListDetailAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private String mListKey;


    public static Intent getStartIntent(Context context, String key) {
        Intent intent = new Intent(context, ListDetailActivity.class);
        intent.putExtra(EXTRA_LIST_KEY, key);
        return intent;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list);

        String key = getIntent().getStringExtra(EXTRA_LIST_KEY);
        if (key != null) {
            mListKey = key;
        }

        mRecyclerView = (RecyclerView) findViewById(R.id.list_items);
        final EditText newTask = (EditText) findViewById(R.id.new_item);
        ImageView addButton = (ImageView) findViewById(R.id.add_item_button);

        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new ListDetailAdapter();
        mRecyclerView.setAdapter(mAdapter);

        if (addButton != null) {
            addButton.setOnClickListener(v -> {
                String title = newTask.getText().toString();
                String author = mListKey;
                addItem(new ListDetailItem(title, author, false));
                newTask.setText("");
            });
        }
    }


    private void addItem(ListDetailItem item) {
        mAdapter.addToDataSet(item);
    }
}
