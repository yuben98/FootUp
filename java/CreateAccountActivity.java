package com.example.footup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.lang.reflect.Array;
import java.net.URL;
import java.util.ArrayList;

public class CreateAccountActivity extends AppCompatActivity {
    TextView email;
    TextView confemail;
    TextView password;
    TextView confpassword;
    private FirebaseAuth mAuth;
    FirebaseDatabase db;
    DatabaseReference userRef;
    DatabaseReference emailRef;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        userRef = db.getReference().child("users");
        emailRef = db.getReference().child("emails");
        email = (TextView) findViewById(R.id.email);
        confemail = (TextView) findViewById(R.id.confemail);
        password = (TextView) findViewById(R.id.pw);
        confpassword = (TextView) findViewById(R.id.confpw);
    }

    public void submit(View v) {
        String em = email.getText().toString().trim();
        String confem = confemail.getText().toString().trim();
        String pw =  password.getText().toString();
        String confpw = confpassword.getText().toString();

        if (!(em.contains("@") && em.contains(".")))
            Toast.makeText(this, "Please type in valid email.", Toast.LENGTH_SHORT).show();
        else if(!confpw.equals(pw))
            Toast.makeText(this, "Passwords don't match!", Toast.LENGTH_SHORT).show();
        else if(!confem.equals(em))
            Toast.makeText(this, "Emails don't match!", Toast.LENGTH_SHORT).show();
        else if(pw.length() < 8 )
            Toast.makeText(this, "Password too short", Toast.LENGTH_SHORT).show();
        else{
            mAuth.createUserWithEmailAndPassword(em, pw)
                    .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                        @Override
                        public void onSuccess(AuthResult authResult) {
                            Toast.makeText(CreateAccountActivity.this,
                                    "Account created successfully", Toast.LENGTH_SHORT).show();
                            User user = new User(em);
                            userRef.child(mAuth.getCurrentUser().getUid()).setValue(user);
                            String e = em.substring(0,em.indexOf("."));
                            emailRef.child(e).setValue(mAuth.getCurrentUser().getUid());
                            Intent resIntent = new Intent();
                            resIntent.putExtra("em", em);
                            resIntent.putExtra("pw", pw);
                            setResult(RESULT_OK, resIntent);
                            finish();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(CreateAccountActivity.this,
                            "Email already in use", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_CANCELED);
                }
            });
            }
        }
}