package com.example.footup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    TextView email;
    TextView password;
    CheckBox remChck;
    Button logBtn;
    Button createBtn;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        email = (TextView) findViewById(R.id.email);
        password = (TextView) findViewById(R.id.pw);
        remChck = (CheckBox) findViewById(R.id.remChk);
        logBtn = (Button) findViewById(R.id.loginBtn);
        createBtn = (Button) findViewById(R.id.createAcctBtn);

    }

    @Override
    public void onResume(){
        super.onResume();
        email.setText("");
        password.setText("");
    }

    public void sign_in(View v) {
        logBtn.setClickable(false);
        createBtn.setClickable(false);
        String em = email.getText().toString();
        String pw = password.getText().toString();
        if (!checkInput(em, pw)) {
            Toast.makeText(MainActivity.this,
                    "Wrong Username or password", Toast.LENGTH_SHORT).show();
            logBtn.setClickable(true);
            createBtn.setClickable(true);
            return;
        }
        mAuth.signInWithEmailAndPassword(em, pw)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            Intent intent = new Intent(MainActivity.this, UserActivity.class);
                            startActivity(intent);

                        } else Toast.makeText(MainActivity.this,
                                "Wrong Username or password", Toast.LENGTH_SHORT).show();
                        logBtn.setClickable(true);
                        createBtn.setClickable(true);
                    }
                });
    }

    public boolean checkInput(String e, String pw){
        if (pw.length() < 8) return false;
        if (!(e.contains(".") && e.contains("@"))) return false;
        return true;
    }

    public void create_acct(View v){
        Intent intent = new Intent(this, CreateAccountActivity.class);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            email.setText(data.getStringExtra("em"));
            password.setText(data.getStringExtra("pw"));
            logBtn.performClick();
        }
    }
}