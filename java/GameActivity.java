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

public class GameActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    FirebaseUser user;
    ListView listview;
    ArrayAdapter<Game> myAdapater;
    FirebaseDatabase db;
    DatabaseReference userRef;
    DatabaseReference gameRef;
    ArrayList<String> games;
    Game game;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        db = FirebaseDatabase.getInstance();
        userRef = db.getReference().child("users");
        gameRef = db.getReference().child("games");

        game = null;
        games = new ArrayList<>();
        listview = (ListView) findViewById(R.id.mylist);
        myAdapater = new ArrayAdapter<>(this, R.layout.line);
        listview.setAdapter(myAdapater);

        userRef.child(user.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                myAdapater.clear();
                games.clear();
                User me = snapshot.getValue(User.class);
                for (String game : me.getUpcomingGames()) {
                    fetchGame(game);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void fetchGame(String id) {
        game = null;
        gameRef.child(id).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                game = task.getResult().getValue(Game.class);
                if (!games.contains(id)) {
                    games.add(id);
                    myAdapater.add(game);
                }
            }
        });
    }
}