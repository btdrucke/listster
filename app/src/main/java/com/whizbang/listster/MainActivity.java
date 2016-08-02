package com.whizbang.listster;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "Listster";

    private FirebaseUser mUser;
    private String mDisplayName;
    private Uri mPhotoUri;
    private String mUuid;
    private DatabaseReference mDbRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
        String value = dataSnapshot.getValue(String.class);
        Log.d(TAG, "Value is: " + value);
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
        newList.child("title").setValue(title);
        mDbRef.child("users").child("lists").push().setValue(key);
    }
}
