package com.example.footup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class FriendProfileActivity extends AppCompatActivity {
    FirebaseDatabase db;
    DatabaseReference userRef;
    StorageReference ppicsRef;

    String uid;
    TextView prof;
    TextView email;
    TextView gPlayed;
    TextView numFriends;
    TextView numGroups;
    ImageView profimg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_profile);
        Intent intent = getIntent();
        uid = intent.getStringExtra("id");
        db = FirebaseDatabase.getInstance();
        userRef = db.getReference().child("users");
        ppicsRef = FirebaseStorage.getInstance().getReference().child("ppics");

        profimg = (ImageView) findViewById(R.id.profimg);
        prof = (TextView) findViewById(R.id.name);
        email = (TextView) findViewById(R.id.email);
        gPlayed = (TextView) findViewById(R.id.gplayed);
        numFriends = (TextView) findViewById(R.id.friendnum);
        numGroups = (TextView) findViewById(R.id.grpjoined);

        userRef.child(uid).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                User friend = task.getResult().getValue(User.class);
                prof.setText(friend.getName());
                email.setText(friend.getEmail());
                gPlayed.setText(Integer.toString(friend.getGamesPlayed()));
                numFriends.setText(Integer.toString(friend.numFriends()));
                numGroups.setText(Integer.toString(friend.numGroups()));
            }
        });
        String ufilename = uid + ".png";
        ppicsRef.child(ufilename).getBytes(1024*1024)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        profimg.setImageBitmap(bitmap);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                profimg.setImageResource(R.drawable.defaultpic);
            }
        });
    }

    public void goBack(View v){
        onBackPressed();
    }
}