package com.example.footup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class FriendActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    FirebaseUser user;
    ListView listview;
    ArrayAdapter<User> myAdapater;
    FirebaseDatabase db;
    DatabaseReference userRef;
    DatabaseReference emailRef;
    EditText friendEmail;
    ArrayList<String> friends;
    User friend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend);
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        db = FirebaseDatabase.getInstance();
        userRef = db.getReference().child("users");
        emailRef = db.getReference().child("emails");

        friend = null;
        friends = new ArrayList<>();
        friendEmail = (EditText) findViewById(R.id.friendEmail);
        listview = (ListView) findViewById(R.id.mylist);
        myAdapater = new ArrayAdapter<>(this, R.layout.line);
        listview.setAdapter(myAdapater);

        userRef.child(user.getUid()).child("friends").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                myAdapater.clear();
                friends.clear();
                if(snapshot.exists()){
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        String id = ds.getValue(String.class);
                        fetchFriend(id,1);
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
                String frnd = friends.get(i);
                Intent intent = new Intent(FriendActivity.this, FriendProfileActivity.class);
                intent.putExtra("id", frnd);
                startActivity(intent);
            }
        });

    }


    public void addFriend(View v) {
        String em = friendEmail.getText().toString().trim();
        if (badInput(em)) {
            return;
        }

        String e = em.substring(0, em.indexOf("."));
        emailRef.child(e).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.getResult().exists()) {
                    Toast.makeText(FriendActivity.this, "No account exists with that email", Toast.LENGTH_SHORT).show();
                }
                else {
                    Log.d("DEBUG", "email exists in server.");
                    String id = task.getResult().getValue().toString();
                    fetchFriend(id,2);
                }
            }
        });
        friendEmail.setText("");
    }

    public void fetchFriend(String id, int mode) {
        friend = null;
        userRef.child(id).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                friend = task.getResult().getValue(User.class);
                if (!friends.contains(id)) {
                    friends.add(id);
                    if (mode == 2) {
                        userRef.child(user.getUid()).child("friends").setValue(friends);
                        // update other guy's friends list
                        userRef.child(id).child("friends").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DataSnapshot> task) {
                                ArrayList<String> flist2 = new ArrayList<>();
                                for (DataSnapshot ds:task.getResult().getChildren()) {
                                    flist2.add(ds.getValue(String.class));
                                }
                                flist2.add(user.getUid());
                                userRef.child(id).child("friends").setValue(flist2);
                            }
                        });
                    }
                    myAdapater.add(friend);
                }
            }
        });
    }

    public boolean badInput(String e) {
        if (!(e.contains("@") && e.contains("."))) {
            Toast.makeText(FriendActivity.this, "Please input a valid email.", Toast.LENGTH_SHORT).show();
            return true;
        }
        if (e.equals(user.getEmail().toString())) {
            Toast.makeText(FriendActivity.this, "You can't add yourself.", Toast.LENGTH_SHORT).show();
            return true;
        }
        for (int i=0; i<myAdapater.getCount(); i++) {
            if(myAdapater.getItem(i).getEmail().equals(e)) {
                Toast.makeText(FriendActivity.this, "You already added this account.", Toast.LENGTH_SHORT).show();
                return true;
            }
        }
        return false;
    }

    private void alertView(final int position ) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(FriendActivity.this);
        dialog.setTitle("Unfriend user?")
                .setMessage("Remove " + myAdapater.getItem(position) + " from friends list?")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialoginterface, int i) {
                        dialoginterface.cancel();
                    }})
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialoginterface, int i) {
                        String id = friends.get(position);
                        friends.remove(position);
                        userRef.child(user.getUid()).child("friends").setValue(friends);
                        userRef.child(id).child("friends").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DataSnapshot> task) {
                                ArrayList<String> flist2 = new ArrayList<>();
                                for (DataSnapshot ds : task.getResult().getChildren()) {
                                    String value = ds.getValue(String.class);
                                    if (!value.equals(user.getUid())) flist2.add(value);
                                }
                                userRef.child(id).child("friends").setValue(flist2);
                            }
                        });
                    }
                }).show();
    }
}