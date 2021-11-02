package com.example.footup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class InviteFriendActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    FirebaseUser user;
    ListView listview;
    ArrayAdapter<User> myAdapater;
    FirebaseDatabase db;
    DatabaseReference userRef;
    DatabaseReference groupRef;
    ArrayList<String> friends;
    User friend;
    String curGrp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite_friend);
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        db = FirebaseDatabase.getInstance();
        userRef = db.getReference().child("users");
        groupRef = db.getReference().child("groups");
        curGrp = getIntent().getStringExtra("group");

        friend = null;
        friends = new ArrayList<>();
        listview = (ListView) findViewById(R.id.mylist);
        myAdapater = new ArrayAdapter<>(this, R.layout.line);
        listview.setAdapter(myAdapater);

        userRef.child(user.getUid()).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                User me = task.getResult().getValue(User.class);
                for (String frId : me.getFriends()) fetchFriend(frId);
            }
        });

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String frId = friends.get(i);
                friends.remove(i);
                myAdapater.remove(myAdapater.getItem(i));
                //add group to friends groups list
                userRef.child(frId).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        User frd = task.getResult().getValue(User.class);
                        frd.addGroup(curGrp);
                        userRef.child(frId).setValue(frd);
                    }
                });
                //add friend to group members list
                groupRef.child(curGrp).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        Group grp = task.getResult().getValue(Group.class);
                        grp.addMember(frId);
                        groupRef.child(curGrp).setValue(grp);
                    }
                });
            }
        });
    }

    public void fetchFriend(String id) {
        friend = null;
        userRef.child(id).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                friend = task.getResult().getValue(User.class);
                if (!friend.getGroups().contains(curGrp)) {
                    friends.add(id);
                    myAdapater.add(friend);
                }
            }
        });
    }
}