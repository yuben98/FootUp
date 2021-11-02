package com.example.footup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog.Builder;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.sql.Ref;
import java.util.concurrent.locks.Lock;

public class ProfileActivity extends AppCompatActivity {

    FirebaseUser user;
    FirebaseDatabase db;
    DatabaseReference userRef;
    User me;


    EditText name;
    EditText email;
    EditText password;
    EditText confpassword;
    EditText gamesPlayed;
    EditText numFriends;
    EditText numGroups;
    LinearLayout pwlay;
    Button edBtn;
    Button remBtn;
    Button svBtn;
    TextView profile;
    ImageView ppic;
    String ufilename;
    TextView tip;

    StorageReference ppicsRef;

    public static final int PICK_IMAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        user = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseDatabase.getInstance();
        String id = user.getUid();
        userRef = db.getReference().child("users");
        ppicsRef = FirebaseStorage.getInstance().getReference().child("ppics");

        profile = (TextView) findViewById(R.id.profile);
        ppic = (ImageView) findViewById(R.id.profimg);
        name = (EditText) findViewById(R.id.edit_name);
        email = (EditText) findViewById(R.id.edit_email);
        password = (EditText) findViewById(R.id.edit_pw);
        confpassword = (EditText) findViewById(R.id.conf_pw);
        pwlay = (LinearLayout) findViewById(R.id.confpw_layout);
        edBtn = (Button) findViewById(R.id.edBtn);
        svBtn = (Button) findViewById(R.id.svBtn);
        remBtn = (Button) findViewById(R.id.removeBtn);
        tip = (TextView) findViewById(R.id.tip);
        gamesPlayed = (EditText) findViewById(R.id.gamesPlayed);
        numFriends = (EditText) findViewById(R.id.numFriends);
        numGroups = (EditText) findViewById(R.id.numGroups);

        name.setEnabled(false);
        email.setText(user.getEmail());
        email.setEnabled(false);
        password.setEnabled(false);

        ufilename = id +".png";
        ppic.setClickable(false);

        ppicsRef.child(ufilename).getBytes(1024*1024)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        ppic.setImageBitmap(bitmap);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                ppic.setImageResource(R.drawable.defaultpic);
            }
        });

    }

    @Override
    public void onResume(){
        super.onResume();
        userRef.child(user.getUid()).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                me = task.getResult().getValue(User.class);
                String username = me.toString();
                name.setText(username);
                profile.setText(username.split(" ")[0] + "'s Profile");
                gamesPlayed.setText(Integer.toString(me.getGamesPlayed()));
                numFriends.setText(Integer.toString(me.numFriends()));
                numGroups.setText(Integer.toString(me.numGroups()));
            }
        });
    }

    public void edit(View v) {
        name.setEnabled(true);
        password.setEnabled(true);
        pwlay.setVisibility(View.VISIBLE);
        svBtn.setVisibility(View.VISIBLE);
        edBtn.setVisibility(View.GONE);
        remBtn.setVisibility(View.VISIBLE);
        confpassword.setText("");
        ppic.setClickable(true);
        tip.setVisibility(View.VISIBLE);
    }

    public void save_changes(View v) {
        if (nameCheck()) updateName();
        if (pwCheck()) updatePw();
        finish();
    }

    public void change_pic(View v){
        Intent gallery = new Intent();
        gallery.setType("image/*");
        gallery.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(gallery, "Select New Profile Picture"), PICK_IMAGE);
    }

    public void remove_pic(View v){
        ppicsRef.child(ufilename).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                remBtn.setVisibility(View.GONE);
                ppic.setImageResource(R.drawable.defaultpic);
                tip.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK) {
            Uri localuri = data.getData();
            Bitmap bitmap = null;
            try{
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), localuri);
                ppic.setImageBitmap(bitmap);
            } catch (Exception e){
                e.printStackTrace();
            }

            //upload img to storage
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG,100,stream );
            ppicsRef.child(ufilename).putBytes(stream.toByteArray());
        }
    }

    public boolean nameCheck(){
        if (name.getText().toString().trim().equals(me.toString())) return false;
        if (name.getText().toString().trim().equals("")) return false;
        return true;
    }


    public boolean pwCheck(){
        if (password.getText().toString().isEmpty()) return false;
        if (password.getText().toString().length() < 8) {
            Toast.makeText(ProfileActivity.this, "Password too short", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!password.getText().toString().equals(confpassword.getText().toString())){
            Toast.makeText(ProfileActivity.this, "Passwords don't match", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    public void updateName(){
        me.setName(name.getText().toString().trim());
        userRef.child(user.getUid()).child("name").setValue(me.toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(ProfileActivity.this, "Name updated.", Toast.LENGTH_SHORT).show();
                }
                else Toast.makeText(ProfileActivity.this, "Name not updated.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void updatePw(){
        user.updatePassword(password.getText().toString())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(ProfileActivity.this, "Password updated.", Toast.LENGTH_SHORT).show();
                        }
                        else Toast.makeText(ProfileActivity.this, "Password not updated. Please Sign in again", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}