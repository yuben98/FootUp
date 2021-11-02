package com.example.footup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
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

public class GroupMemberActivity extends AppCompatActivity {
    boolean isOwner;
    String curGrp;
    private FirebaseAuth mAuth;
    ListView listview;
    ArrayAdapter<User> myAdapater;
    FirebaseDatabase db;
    DatabaseReference userRef;
    DatabaseReference groupRef;
    ArrayList<String> members;
    User member;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_member);
        Intent intent = getIntent();
        isOwner = intent.getBooleanExtra("owner", false);
        curGrp = intent.getStringExtra("id");
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        userRef = db.getReference().child("users");
        groupRef = db.getReference().child("groups");

        member = null;
        members = new ArrayList<>();
        listview = (ListView) findViewById(R.id.mylist);
        myAdapater = new ArrayAdapter<>(this, R.layout.line);
        listview.setAdapter(myAdapater);

        groupRef.child(curGrp).child("members").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                myAdapater.clear();
                members.clear();
                if(snapshot.exists()){
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        String id = ds.getValue(String.class);
                        fetchMember(id);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        if (isOwner) {
            listview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                    if(!members.get(i).equals(mAuth.getCurrentUser().getUid())) alertView(i);
                    return true;
                }
            });
        }

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String mem = members.get(i);
                Intent intent = new Intent(GroupMemberActivity.this, FriendProfileActivity.class);
                intent.putExtra("id", mem);
                startActivity(intent);
            }
        });
    }

    public void fetchMember(String id) {
        member = null;
        userRef.child(id).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                member = task.getResult().getValue(User.class);
                if (!members.contains(id)) {
                    members.add(id);
                    myAdapater.add(member);
                }
            }
        });
    }

    private void alertView(final int position ) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(GroupMemberActivity.this);
        dialog.setTitle("Remove user")
                .setMessage("Remove " + myAdapater.getItem(position) + " from members list?")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialoginterface, int i) {
                        dialoginterface.cancel();
                    }})
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialoginterface, int i) {
                        String id = members.get(position);
                        members.remove(position);
                        groupRef.child(curGrp).child("members").setValue(members);
                        userRef.child(id).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DataSnapshot> task) {
                                ArrayList<String> grps = task.getResult().getValue(User.class).getGroups();
                                grps.remove(curGrp);
                                userRef.child(id).child("groups").setValue(grps);
                            }
                        });
                    }
                }).show();
    }
}