package com.example.footup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

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

public class GroupActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    FirebaseUser user;
    ListView listview;
    ArrayAdapter<Group> myAdapater;
    FirebaseDatabase db;
    DatabaseReference userRef;
    DatabaseReference groupRef;
    EditText groupCode;
    ArrayList<String> groups;
    Group group;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        db = FirebaseDatabase.getInstance();
        userRef = db.getReference().child("users");
        groupRef = db.getReference().child("groups");

        group = null;
        groups = new ArrayList<>();
        groupCode = (EditText) findViewById(R.id.grpCode);
        listview = (ListView) findViewById(R.id.mylist);
        myAdapater = new ArrayAdapter<>(this, R.layout.line);
        listview.setAdapter(myAdapater);

        userRef.child(user.getUid()).child("groups").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                myAdapater.clear();
                groups.clear();
                if(snapshot.exists()){
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        String id = ds.getValue(String.class);
                        getGroup(id,1);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        listview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                alertView(i);
                return true;
            }
        });

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String grp = groups.get(i);
                Intent intent = new Intent(GroupActivity.this, GroupPageActivity.class);
                //if user is owner
                boolean owner = isOwner(myAdapater.getItem(i));
                intent.putExtra("id", grp);
                intent.putExtra("owner", owner);
                startActivity(intent);
            }
        });
    }

    public void joinGroup(View v) {
        String code = groupCode.getText().toString().trim();
        if (badInput(code)) {
            return;
        }

        groupRef.child(code).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.getResult().exists()) {
                    Toast.makeText(GroupActivity.this, "invalid code", Toast.LENGTH_SHORT).show();
                }
                else {
                    Log.d("DEBUG", "group exists in server.");
                    getGroup(code,2);
                }
            }
        });
        groupCode.setText("");
    }

    public void getGroup(String id, int mode) {
        group = null;
        groupRef.child(id).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                group = task.getResult().getValue(Group.class);
                if (!groups.contains(id)) {
                    groups.add(id);
                    if (mode == 2) {
                        userRef.child(user.getUid()).child("groups").setValue(groups);
                        // update group's member list
                        groupRef.child(id).child("members").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DataSnapshot> task) {
                                ArrayList<String> glist2 = new ArrayList<>();
                                for (DataSnapshot ds:task.getResult().getChildren()) {
                                    glist2.add(ds.getValue(String.class));
                                }
                                glist2.add(user.getUid());
                                groupRef.child(id).child("members").setValue(glist2);
                            }
                        });
                    }
                    myAdapater.add(group);
                }
            }
        });
    }

    public boolean badInput(String code) {
        if (code.contains(".") || code.contains("$") || code.contains("#") || code.contains("[") || code.contains("]") || code.contains("\\")) {
            Toast.makeText(GroupActivity.this, "Name contains invalid character", Toast.LENGTH_SHORT).show();
            return true;
        }

        if (code.equals("")) {
            Toast.makeText(GroupActivity.this, "Type/paste an invitation code", Toast.LENGTH_SHORT).show();
            return true;
        }

        for (int i=0; i<myAdapater.getCount(); i++) {
            if(groups.get(i).equals(code)) {
                Toast.makeText(GroupActivity.this, "You're already a member of this group.", Toast.LENGTH_SHORT).show();
                return true;
            }
        }
        return false;
    }

    private void alertView(final int position ) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(GroupActivity.this);
        dialog.setTitle("Leave Group?")
                .setMessage("Leave " + myAdapater.getItem(position) + " ?")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialoginterface, int i) {
                        dialoginterface.cancel();
                    }})
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialoginterface, int i) {
                        String id = groups.get(position);
                        groups.remove(position);
                        userRef.child(user.getUid()).child("groups").setValue(groups);
                        userRef.child(user.getUid()).child("numGroups").setValue(groups.size());
                        groupRef.child(id).child("members").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DataSnapshot> task) {
                                ArrayList<String> glist2 = new ArrayList<>();
                                for (DataSnapshot ds : task.getResult().getChildren()) {
                                    String value = ds.getValue(String.class);
                                    if (!value.equals(user.getUid())) glist2.add(value);
                                }
                                if (!glist2.isEmpty()) groupRef.child(id).child("members").setValue(glist2);
                                else groupRef.child(id).removeValue();
                            }
                        });
                    }
                }).show();
    }

    public boolean isOwner(Group grp){
        String id = user.getUid();
        if (grp.getOwner().equals(id)) return true;
        return false;
    }

    public void createGroup(View v) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Create a New Group");
        EditText grpName = new EditText(this);
        grpName.setHint("Group Name");
        dialog.setView(grpName);
        dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialoginterface, int i) {
                dialoginterface.cancel();
            }})
                .setPositiveButton("Create", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialoginterface, int i) {
                        String n = grpName.getText().toString().trim();
                        if (!badInput(n)) {
                            Group grp = new Group(n);
                            grp.setOwner(user.getUid());
                            grp.addMember(user.getUid());
                            String key = groupRef.push().getKey();
                            groupRef.child(key).setValue(grp);
                            userRef.child(user.getUid()).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DataSnapshot> task) {
                                    User me = task.getResult().getValue(User.class);
                                    me.addGroup(key);
                                    userRef.child(user.getUid()).setValue(me);
                                }
                            });
                        }
                    }
                }).show();
    }
}