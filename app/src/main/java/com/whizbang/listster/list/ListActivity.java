package com.whizbang.listster.list;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.EditText;
import android.widget.ImageView;

import com.whizbang.listster.R;


public class ListActivity extends AppCompatActivity {

    private final static String EXTRA_DISPLAY_NAME = "extra_display_name";
    private RecyclerView mRecyclerView;
    private ListAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private String mDisplayName;


    public static Intent getStartIntent(Context context, String displayName) {
        Intent intent = new Intent(context, ListActivity.class);
        intent.putExtra(EXTRA_DISPLAY_NAME, displayName);
        return intent;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list);

        String displayName = getIntent().getStringExtra(EXTRA_DISPLAY_NAME);
        if (displayName != null) {
            mDisplayName = displayName;
        }

        mRecyclerView = (RecyclerView) findViewById(R.id.list_items);
        final EditText newTask = (EditText) findViewById(R.id.new_item);
        ImageView addButton = (ImageView) findViewById(R.id.add_item_button);

        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new ListAdapter();
        mRecyclerView.setAdapter(mAdapter);

        if (addButton != null) {
            addButton.setOnClickListener(v -> {
                String title = newTask.getText().toString();
                String author = mDisplayName;
                addItem(new Item(title, author, false));
                newTask.setText("");
            });
        }
    }


    private void addItem(Item item) {
        mAdapter.addToDataSet(item);
    }
}
