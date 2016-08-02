package com.whizbang.listster;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView.LayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;

import com.google.android.gms.appinvite.AppInvite;
import com.google.android.gms.appinvite.AppInviteReferral;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
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
import com.whizbang.listster.databinding.ActivityMainBinding;
import com.whizbang.listster.list.UserList;
import com.whizbang.listster.list.UserListItemAdapter;
import com.whizbang.listster.listdetail.ListDetailActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;


public class MainActivity extends AppCompatActivity
        implements OnClickListener, OnConnectionFailedListener {

    private static final String TAG = "Listster";

    private String mDisplayName;
    private Uri mPhotoUri;
    private String mUuid;
    private DatabaseReference mDbRef;
    private boolean mToolbarCollapsed = true;
    private ActivityMainBinding mBinding;
    private UserListItemAdapter mAdapter;
    private HashMap<String, String> mUserListRefs;
    private HashMap<String, UserList> mUserLists;
    private InputMethodManager mInputMethodManager;
    private GoogleApiClient mGoogleApiClient;
    private String mRequestedListRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String token = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Instance token: " + token);

        processIntent(getIntent());

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        final Toolbar toolbar = mBinding.toolbar;
        mBinding.appbar.setExpanded(false);
        setSupportActionBar(toolbar);
        mInputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

        mBinding.fab.setOnClickListener(v -> {
            mBinding.titleTextInputLayout.setVisibility(View.VISIBLE);
            mBinding.titleEditText.requestFocus();
            mToolbarCollapsed = false;
            showKeyboard();
        });

        mBinding.titleEditText.setOnEditorActionListener((v, actionId, event) -> {
            boolean handled = false;
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                addList(v.getText().toString());
                handled = true;
                mBinding.titleEditText.setText("");
                hideKeyboard(v);
                mBinding.getRoot().requestFocus();
                mBinding.titleTextInputLayout.setVisibility(View.GONE);
                mToolbarCollapsed = true;
            }
            return handled;
        });
        mBinding.recycler.setHasFixedSize(true);
        LayoutManager layoutManager = new LinearLayoutManager(this);
        mBinding.recycler.setLayoutManager(layoutManager);

        mAdapter = new UserListItemAdapter(new ArrayList<>(), this);
        mBinding.recycler.setAdapter(mAdapter);

        mGoogleApiClient = new GoogleApiClient.Builder(this).enableAutoManage(this, this)
                .addApi(AppInvite.API)
                .build();

        boolean autoLaunchDeepLink = false;
        AppInvite.AppInviteApi.getInvitation(mGoogleApiClient, this, autoLaunchDeepLink)
                .setResultCallback(result -> {
                    if (result.getStatus().isSuccess()) {
                        // Extract deep link from Intent
                        Intent intent = result.getInvitationIntent();
                        String deepLink = AppInviteReferral.getDeepLink(intent);

                        Log.d(TAG, deepLink);
                    } else {
                        Log.d(TAG, "getInvitation: no deep link found.");
                    }
                });
    }


    private void showKeyboard() {
        mInputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }


    private void hideKeyboard(View v) {
        mInputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }


    private void addList(String listTitle) {
        String key = writeNewList(listTitle);
        mAdapter.addItem(new UserList(listTitle, key));
    }


    @Override
    public void onBackPressed() {
        if (!mToolbarCollapsed) {
            mBinding.titleTextInputLayout.setVisibility(View.GONE);
        } else {
            super.onBackPressed();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            startActivity(new Intent(this, GoogleSignInActivity.class));
        } else {
            getUserData(user);
            mBinding.toolbarTitle.setText(getString(R.string.users_lists, mDisplayName));

            FirebaseDatabase database = FirebaseDatabase.getInstance();
            mDbRef = database.getReference();

            DatabaseReference usersListsRef = database.getReference("users/" + mUuid + "/lists");
            usersListsRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    onUsersChange(dataSnapshot);
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


    private void onUsersChange(DataSnapshot dataSnapshot) {
        GenericTypeIndicator<HashMap<String, String>> t = new GenericTypeIndicator<HashMap<String, String>>() {
        };
        mUserListRefs = dataSnapshot.getValue(t);
        if (mUserLists != null) {
            updateUi();
        }
    }


    private void onListsChange(DataSnapshot dataSnapshot) {
        GenericTypeIndicator<HashMap<String, UserList>> t = new GenericTypeIndicator<HashMap<String, UserList>>() {
        };
        mUserLists = dataSnapshot.getValue(t);
        if (mUserListRefs != null) {
            updateUi();
        }
    }


    private void onCancelled(DatabaseError error) {
        // Failed to read value
        Log.w(TAG, "Failed to read value.", error.toException());
    }


    private void updateUi() {
        List<UserList> thisUsersLists = new ArrayList<>();
        if (mUserListRefs != null) {
            for (String listRef : mUserListRefs.values()) {
                UserList userList = mUserLists.get(listRef);
                userList.key = listRef;
                Log.d(TAG, "Got list: " + userList);
                thisUsersLists.add(userList);
                if (listRef.equals(mRequestedListRef)) {
                    Log.d(TAG, "Going to list detail");
                    startActivity(ListDetailActivity.getStartIntent(this, listRef));
                }
            }
        }
        Collections.sort(thisUsersLists, (lhs, rhs) -> {
            // By last modified time, descending.
            return Long.compare(rhs.lastModifedUtcMillis, lhs.lastModifedUtcMillis);
        });


        mAdapter.setItems(thisUsersLists);
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


    private String writeNewList(String title) {
        DatabaseReference newList = mDbRef.child("lists").push();
        String key = newList.getKey();
        newList.setValue(new UserList(title, key));
        mDbRef.child("users").child(mUuid).child("lists").push().setValue(key);
        return key;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.activity_menu, menu);
        return true;
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        processIntent(intent);
    }


    private void processIntent(Intent intent) {
        Uri data = intent.getData();
        if (data != null) {
            mRequestedListRef = data.getQueryParameter("list");
            Log.d(TAG, "Requested list ref: " + mRequestedListRef);
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_logout:
                GoogleSignInActivity.signOutFromApp(this);
                return true;
            case R.id.menu_item_link_test:
                Uri link = Uri.parse("listster://link/?list=-KOAgLJMsHeGxLJ-JgF4");
                Intent intent = new Intent(Intent.ACTION_VIEW, link);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public void onClick(View v) {
        int position = mBinding.recycler.getChildAdapterPosition(v);
        String key = mAdapter.dataSet.get(position).key;
        startActivity(ListDetailActivity.getStartIntent(this, key));
    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.w(TAG, "Connection error: " + connectionResult.getErrorMessage());
    }
}
