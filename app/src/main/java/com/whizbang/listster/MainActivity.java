package com.whizbang.listster;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView.LayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.whizbang.listster.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "Listster";

    private FirebaseUser mUser;
    private String mDisplayName;
    private Uri mPhotoUri;
    private String mUuid;
    private DatabaseReference mDbRef;
    private boolean mToolbarCollapsed = true;
    private ActivityMainBinding mBinding;
    private ListItemAdapter mAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        final Toolbar toolbar = mBinding.toolbar;
        mBinding.appbar.setExpanded(false);
        setSupportActionBar(toolbar);
        mBinding.fab.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mBinding.appbar.setExpanded(true);
                mBinding.titleEditText.setFocusable(true);
                mBinding.titleEditText.requestFocus();
                mToolbarCollapsed = false;
            }
        });

        mBinding.titleEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    getWindow().setSoftInputMode(
                            WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                }
            }
        });
        mBinding.titleEditText.setOnEditorActionListener(new OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    addList(v.getText().toString());
                    handled = true;
                }
                return handled;
            }
        });
        mBinding.recycler.setHasFixedSize(true);
        LayoutManager layoutManager = new LinearLayoutManager(this);
        mBinding.recycler.setLayoutManager(layoutManager);

        mAdapter = new ListItemAdapter(new ArrayList<ListItemRowModel>());
        mBinding.recycler.setAdapter(mAdapter);
    }


    private void addList(String listTitle) {
        mAdapter.addItem(new ListItemRowModel(listTitle));
        writeNewList(listTitle);
    }


    @Override
    public void onBackPressed() {
        if (!mToolbarCollapsed) {
            mBinding.appbar.setExpanded(false);
        } else {
            super.onBackPressed();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            startActivity(new Intent(this, GoogleSignInActivity.class));
        } else {
            getUserData(user);
            setTitle(mDisplayName);

            FirebaseDatabase database = FirebaseDatabase.getInstance();
            mDbRef = database.getReference();

            DatabaseReference usersListsRef = database.getReference("users/" + mUuid);
            usersListsRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    onListsChange(dataSnapshot);
                }


                @Override
                public void onCancelled(DatabaseError error) {
                    MainActivity.this.onCancelled(error);
                }
            });

            DatabaseReference listsRef = database.getReference("lists");
            listsRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    onListsChange(dataSnapshot);
                }


                @Override
                public void onCancelled(DatabaseError error) {
                    MainActivity.this.onCancelled(error);
                }
            });

        }
    }


    private void onListsChange(DataSnapshot dataSnapshot) {
        GenericTypeIndicator<List<UserList>> t = new GenericTypeIndicator<List<UserList>>() {
        };
        //        List<UserList> messages = dataSnapshot.getValue(t);
    }


    private void onCancelled(DatabaseError error) {
        // Failed to read value
        Log.w(TAG, "Failed to read value.", error.toException());
    }


    private void getUserData(FirebaseUser user) {
        mDisplayName = user.getDisplayName();
        mPhotoUri = user.getPhotoUrl();
        mUuid = user.getUid();
        for (UserInfo profile : user.getProviderData()) {
            if (mDisplayName == null) {
                mDisplayName = profile.getDisplayName();
            }
            if (mPhotoUri == null) {
                mPhotoUri = profile.getPhotoUrl();
            }
        }
    }


    private void writeNewList(String title) {
        DatabaseReference newList = mDbRef.child("lists").push();
        String key = newList.getKey();
        newList.setValue(new UserList(title));
        mDbRef.child("users").child(mUuid).child("lists").push().setValue(key);
    }
}
