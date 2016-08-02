package com.whizbang.listster;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView.LayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
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
        implements OnClickListener, OnConnectionFailedListener, OnLongClickListener {

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
    private String mListRefToShow;
    private int mSelectedColor;
    private int mUnselectedColor;
    private int mSelectedCount;
    private MenuItem mDeleteItem;
    private String mInviteListRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String token = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Instance token: " + token);

        if (savedInstanceState == null) {
            processIntent(getIntent());
        }

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

        mAdapter = new UserListItemAdapter(new ArrayList<>(), this, this);
        mBinding.recycler.setAdapter(mAdapter);

        mGoogleApiClient = new GoogleApiClient.Builder(this).enableAutoManage(this, this)
                .addApi(AppInvite.API)
                .build();

        AppInvite.AppInviteApi.getInvitation(mGoogleApiClient, this, false)
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
        mSelectedColor = ContextCompat.getColor(this, R.color.colorAccent);
        mUnselectedColor = ContextCompat.getColor(this, R.color.cardview_light_background);
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
        if (mUserListRefs == null) {
            mUserListRefs = new HashMap<>();
        }
        if (mUserLists != null) {
            updateUi();
        }
    }


    private void onListsChange(DataSnapshot dataSnapshot) {
        GenericTypeIndicator<HashMap<String, UserList>> t = new GenericTypeIndicator<HashMap<String, UserList>>() {
        };
        mUserLists = dataSnapshot.getValue(t);
        if (mUserLists == null) {
            mUserLists = new HashMap<>();
        }
        if (mUserListRefs != null) {
            updateUi();
        }
    }


    private void onCancelled(DatabaseError error) {
        // Failed to read value
        Log.w(TAG, "Failed to read value.", error.toException());
    }


    private void updateUi() {
        if (mInviteListRef != null) {
            boolean didAdd = addListToUser(mInviteListRef);
            mListRefToShow = mInviteListRef;
            mInviteListRef = null;
            if (didAdd) {
                return;
            }
        }

        List<UserList> thisUsersLists = new ArrayList<>();
        if (mUserListRefs != null) {
            for (String listRef : mUserListRefs.values()) {
                UserList userList = mUserLists.get(listRef);
                if (userList != null) {
                    userList.key = listRef;
                    Log.d(TAG, "Got list: " + userList);
                    thisUsersLists.add(userList);

                    if (listRef.equals(mListRefToShow)) {
                        Log.d(TAG, "Going to list detail");
                        mListRefToShow = null;
                        startActivity(
                                ListDetailActivity.getStartIntent(this, mDisplayName, mPhotoUri,
                                        userList.title, listRef));
                    }
                }
            }
        }
        Collections.sort(thisUsersLists, (lhs, rhs) -> {
            // By last modified time, descending.
            return Long.compare(rhs.lastModifedUtcMillis, lhs.lastModifedUtcMillis);
        });


        mAdapter.setItems(thisUsersLists);
    }


    private boolean addListToUser(String listRef) {
        if (mUserListRefs.containsValue(listRef)) {
            // Already got it.
            return false;
        }
        mDbRef.child("users").child(mUuid).child("lists").push().setValue(listRef);
        return true;
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
        mDeleteItem = menu.findItem(R.id.menu_item_delete);
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
            String link = data.getQueryParameter("link");
            if (link != null) {
                data = Uri.parse(link);
            }
            mInviteListRef = data.getQueryParameter("invite");
            mListRefToShow = data.getQueryParameter("list");
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_logout:
                GoogleSignInActivity.signOutFromApp(this);
                return true;
            case R.id.menu_item_link_test:
                Uri link = Uri.parse("https://g5xnr.app.goo.gl/?apn=com.whizbang.listster")
                        .buildUpon()
                        .appendQueryParameter("link",
                                "https://listster?invite=-KOBBCp0W4tQXbav6Eq4")
                        .build();
                Intent intent = new Intent(Intent.ACTION_VIEW, link);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;
            case R.id.menu_item_delete:
                for (int i = 0; i < mBinding.recycler.getChildCount(); i++) {
                    View child = mBinding.recycler.getChildAt(i);
                    if (child.isSelected()) {
                        int position = mBinding.recycler.getChildAdapterPosition(child);
                        UserList list = mAdapter.dataSet.get(position);
                        mDbRef.child("lists").child(list.key).removeValue();
                    }
                }
                unselectAll();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private void unselectAll() {
        for (int i = 0; i < mBinding.recycler.getChildCount(); i++) {
            unselectView(mBinding.recycler.getChildAt(i));
        }
    }


    @Override
    public void onClick(View v) {
        if (mSelectedCount > 0 && !v.isSelected()) {
            selectView(v);
        } else if (v.isSelected()) {
            unselectView(v);
        } else {
            int position = mBinding.recycler.getChildAdapterPosition(v);
            UserList userList = mAdapter.dataSet.get(position);
            String key = userList.key;
            startActivity(
                    ListDetailActivity.getStartIntent(this, mDisplayName, mPhotoUri, userList.title,
                            key));
        }
    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.w(TAG, "Connection error: " + connectionResult.getErrorMessage());
    }


    @Override
    public boolean onLongClick(View v) {
        if (v.isSelected()) {
            unselectView(v);
        } else {
            selectView(v);
        }
        return true;
    }


    private void selectView(View v) {
        mSelectedCount++;
        showTrashCan();
        v.setSelected(true);
        ((CardView) v).setCardBackgroundColor(mSelectedColor);
    }


    private void showTrashCan() {
        mDeleteItem.setVisible(true);
    }


    private void unselectView(View v) {
        mSelectedCount--;
        if (mSelectedCount <= 0) {
            hideTrashCan();
        }
        v.setSelected(false);
        ((CardView) v).setCardBackgroundColor(mUnselectedColor);
    }


    private void hideTrashCan() {
        mDeleteItem.setVisible(false);
    }
}
