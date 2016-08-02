package com.whizbang.listster.listdetail;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.whizbang.listster.GoogleSignInActivity;
import com.whizbang.listster.R;
import com.whizbang.listster.databinding.ListBinding;

import java.util.HashMap;


public class ListDetailActivity extends AppCompatActivity {

    private static final String TAG = "Listster";

    private final static String EXTRA_LIST_KEY = "extra_list_key";
    private RecyclerView mRecyclerView;
    private ListDetailAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private String mListKey;
    private String mDisplayName;
    private String mListName;
    private DatabaseReference mDbRef;
    private ListBinding mBinding;
    private HashMap<String, String> mUserItemsRefs;
    private HashMap<String, ListDetailItem> mUserItems;


    public static Intent getStartIntent(Context context, String key) {
        Intent intent = new Intent(context, ListDetailActivity.class);
        intent.putExtra(EXTRA_LIST_KEY, key);
        return intent;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String key = getIntent().getStringExtra(EXTRA_LIST_KEY);
        if (key != null) {
            mListKey = key;
        }

        String token = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Instance token: " + token);

        mBinding = DataBindingUtil.setContentView(this, R.layout.list);
        final Toolbar toolbar = mBinding.toolbar;
        setTitle(mListKey);
        setSupportActionBar(toolbar);

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
                String author = mDisplayName;
                addItem(title, author, false);
                newTask.setText("");
            });
        }
    }


    private void addItem(String title, String author, boolean completed) {
//        String key = writeNewItem(title, author, completed);
        mAdapter.addItem(new ListDetailItem(null, title, author, completed));
    }


    @Override
    protected void onResume() {
        super.onResume();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            startActivity(new Intent(this, GoogleSignInActivity.class));
        } else {
            getUserData(user);

            FirebaseDatabase database = FirebaseDatabase.getInstance();
            mDbRef = database.getReference();

            DatabaseReference itemsRef = database.getReference("items");
            itemsRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    onItemsChange(dataSnapshot);
                }


                @Override
                public void onCancelled(DatabaseError error) {
                    ListDetailActivity.this.onCancelled(error);
                }
            });
        }
    }


    private void onItemsChange(DataSnapshot dataSnapshot) {
        GenericTypeIndicator<HashMap<String, ListDetailItem>> t = new
                GenericTypeIndicator<HashMap<String, ListDetailItem>>() {
        };
//        mUserItems = dataSnapshot.getValue(t);
//        if (mUserItemsRefs != null) {
//            updateUi();
//        }
    }


    private void onCancelled(DatabaseError error) {
        // Failed to read value
        Log.w(TAG, "Failed to read value.", error.toException());
    }

    private void updateUi() {
//        List<ListDetailItem> thisUsersLists = new ArrayList<>();
//        if (mUserItemsRefs != null) {
//            for (String listRef : mUserItemsRefs.values()) {
//                UserList userList = mUserItems.get(listRef);
//                userList.key = listRef;
//                Log.d(TAG, "Got list: " + userList);
//                thisUsersLists.add(userList);
//            }
//        }
//        Collections.sort(thisUsersLists, (lhs, rhs) -> {
//            // By last modified time, descending.
//            return Long.compare(rhs.lastModifedUtcMillis, lhs.lastModifedUtcMillis);
//        });
//
//
//        mAdapter.setItems(thisUsersLists);
    }

    private void getUserData(FirebaseUser user) {
        mDisplayName = user.getDisplayName();
        for (UserInfo profile : user.getProviderData()) {
            if (mDisplayName == null) {
                mDisplayName = profile.getDisplayName();
            }
        }
    }


    private String writeNewItem(String title, String author, boolean completed) {
        DatabaseReference newItem = mDbRef.child("items").push();
        String key = newItem.getKey();
        newItem.child("listKey").setValue(mListKey);
        newItem.child("title").setValue(title);
        newItem.child("user").setValue(author);
        newItem.child("status").setValue(completed);
        return key;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.activity_list_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_share:
                Log.d(TAG, "share");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
