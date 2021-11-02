package com.example.footup;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.w3c.dom.Text;

public class UserActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    FirebaseUser user;
    FirebaseDatabase db;
    DatabaseReference userRef;
    User me;
    TextView greet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        greet = (TextView) findViewById(R.id.greet);
        db = FirebaseDatabase.getInstance();
        String id = user.getUid();
        userRef = db.getReference().child("users");
    }

    @Override
    public void onResume() {
        super.onResume();
        userRef.child(user.getUid()).child("name").get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                String n = dataSnapshot.getValue(String.class);
                greet.setText("Hello, "+ n);
            }
        });
    }

    public void profClick(View v) {
        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);
    }

    public void sign_out(View v) {
        FirebaseAuth.getInstance().signOut();
        finish();
    }

    public void frdClick(View v) {
        Intent intent = new Intent(this, FriendActivity.class);
        startActivity(intent);
    }

    public void grpClick(View v) {
        Intent intent = new Intent(this, GroupActivity.class);
        startActivity(intent);
    }

    public void gameClick(View v) {
        Intent intent = new Intent(this, GameActivity.class);
        startActivity(intent);
    }
}