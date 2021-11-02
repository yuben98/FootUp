package com.example.footup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;

public class GroupPageActivity extends AppCompatActivity {
    FirebaseDatabase db;
    DatabaseReference userRef;
    DatabaseReference groupRef;
    DatabaseReference gameRef;
    StorageReference gpicsRef;

    TextView grpName;
    TextView owner;
    TextView gPlayed;
    TextView numMembers;
    TextView nextGame;
    TextView invCode;
    ImageView grpimg;
    Button members;
    Button addGameBtn;
    Button registerBtn;
    Game game;
    Group group;

    Boolean isOwner;
    String curGrp;
    String gfilename;
    String uid;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_page);
        Intent intent = getIntent();
        curGrp = intent.getStringExtra("id");
        isOwner = intent.getBooleanExtra("owner", false);

        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db = FirebaseDatabase.getInstance();
        userRef = db.getReference().child("users");
        groupRef = db.getReference().child("groups");
        gameRef = db.getReference().child("games");
        gpicsRef = FirebaseStorage.getInstance().getReference().child("gpics");

        grpName = (TextView) findViewById(R.id.name);
        owner = (TextView) findViewById(R.id.owner);
        gPlayed = (TextView) findViewById(R.id.gamesPlayed);
        numMembers =(TextView) findViewById(R.id.numMembers);
        nextGame = (TextView) findViewById(R.id.gameInfo);
        invCode = (TextView) findViewById(R.id.invcode);
        grpimg = (ImageView) findViewById(R.id.grpimg);
        members = (Button) findViewById(R.id.memButton);
        addGameBtn = (Button) findViewById(R.id.addGame);
        registerBtn = (Button) findViewById(R.id.regBtn);

        invCode.setText("invitation code: " + curGrp);
        if (isOwner) ownerMode();
        else grpimg.setClickable(false);

        groupRef.child(curGrp).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                group = snapshot.getValue(Group.class);
                grpName.setText(group.getName());
                gPlayed.setText(Integer.toString(group.getGamesPlayed()));
                numMembers.setText(Integer.toString(group.numMembers()));
                userRef.child(group.getOwner()).child("name").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        owner.setText(task.getResult().getValue().toString());
                    }
                });

                if(group.getPlannedGame().equals("")) {
                    registerBtn.setVisibility(View.GONE);
                    nextGame.setText("No Planned Game");
                    addGameBtn.setText("Add a Game");
                }
                else {
                    registerBtn.setVisibility(View.VISIBLE);
                    addGameBtn.setText("Modify Game");
                    gameRef.child(group.getPlannedGame()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                    game = snapshot.getValue(Game.class);
                                nextGame.setText(game.toString());
                                if (game.getPlayers().contains(uid)) {
                                    registerBtn.setText("Unregister for Game");
                                } else registerBtn.setText("Register for Game");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        gfilename = curGrp + ".png";
        gpicsRef.child(gfilename).getBytes(1024*1024)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        grpimg.setImageBitmap(bitmap);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                grpimg.setImageResource(R.drawable.defaultgrp);
            }
        });
    }

    public void ownerMode() {
        addGameBtn.setVisibility(View.VISIBLE);
        grpimg.setClickable(true);
    }

    public void changePic(View v) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(GroupPageActivity.this);
        dialog.setTitle("Change Group Image?")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialoginterface, int i) {
                        dialoginterface.cancel();
                    }})
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialoginterface, int i) {
                        changeOptions();
                    }
                }).show();
    }

    public void changeOptions() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(GroupPageActivity.this);
        dialog.setTitle("Upload/Remove image")
                .setNegativeButton("Remove Group Image", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialoginterface, int i) {
                        gpicsRef.child(gfilename).delete();
                    }})
                .setPositiveButton("Upload New Image", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialoginterface, int i) {
                        change_pic();
                    }
                }).show();
    }

    public void change_pic(){
        Intent gallery = new Intent();
        gallery.setType("image/*");
        gallery.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(gallery, "Select New Group Picture"), 1);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            Uri localuri = data.getData();
            Bitmap bitmap = null;
            try{
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), localuri);
                grpimg.setImageBitmap(bitmap);
            } catch (Exception e){
                e.printStackTrace();
            }

            //upload img to storage
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG,100,stream );
            gpicsRef.child(gfilename).putBytes(stream.toByteArray());
        }
    }

    public void listMembers(View v) {
        Intent intent = new Intent(this, GroupMemberActivity.class);
        intent.putExtra("owner", isOwner);
        intent.putExtra("id", curGrp);
        startActivity(intent);
    }

    public void cpy(View v) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("inv", curGrp);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, "Code Copied to Clipboard", Toast.LENGTH_SHORT).show();
    }

    public void register(View v) {
        if (game == null) return;
        ArrayList<String> roster = game.getPlayers();
        if (roster.contains(uid)) {
            roster.remove(uid);
            game.setPlayers(roster);
            gameRef.child(group.getPlannedGame()).setValue(game);
            userRef.child(uid).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    User me = task.getResult().getValue(User.class);
                    me.removeGame(group.getPlannedGame(), false);
                    userRef.child(uid).setValue(me);
                }
            });
        }
        else {
            roster.add(uid);
            game.setPlayers(roster);
            gameRef.child(group.getPlannedGame()).setValue(game);
            userRef.child(uid).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    User me = task.getResult().getValue(User.class);
                    me.addGame(group.getPlannedGame());
                    userRef.child(uid).setValue(me);
                }
            });
        }
    }

    public void addGame(View v) {
        String[] choices = {"Cancel", "Change Location", "Change Date/Time", "Game has been played", "Cancel Game"};
        String key = group.getPlannedGame();
        if (key.equals("")) {
            key = gameRef.push().getKey();
            group.setPlannedGame(key);
            groupRef.child(curGrp).setValue(group);
            gameRef.child(key).setValue(new Game());
        }
        else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Modify Game");
            builder.setItems(choices, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int i) {
                    // the user clicked on colors[which]
                    if (i==0) dialog.cancel();
                    else if (i==1) {
                        changeWhere();
                    }
                    else if (i==2){
                        changeWhen();
                    }
                    else if (i==3){
                        gameDone(true);
                    }
                    else {
                        gameCancelled();
                    }
                }
            }).show();
        }
    }

    public void changeWhere() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Change Location");
        EditText loc = new EditText(this);
        loc.setHint("Where?");
        dialog.setView(loc);
        dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialoginterface, int i) {
                dialoginterface.cancel();
            }})
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialoginterface, int i) {
                        String input = loc.getText().toString().trim();
                        if (!badInput(input)) {
                            game.setWhere(input);
                            gameRef.child(group.getPlannedGame()).setValue(game);
                        }
                    }
                }).show();
    }
    public void changeWhen() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Change Date/Time");
        EditText t = new EditText(this);
        t.setHint("When?");
        dialog.setView(t);
        dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialoginterface, int i) {
                dialoginterface.cancel();
            }})
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialoginterface, int i) {
                        String input = t.getText().toString().trim();
                        if (!badInput(input)) {
                            game.setWhen(input);
                            gameRef.child(group.getPlannedGame()).setValue(game);
                        }
                    }
                }).show();
    }

    public void gameDone(Boolean played) {
        groupRef.child(curGrp).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                Group grp = task.getResult().getValue(Group.class);
                gameRef.child(grp.getPlannedGame()).removeValue();
                for (String userid : grp.getMembers()) {
                    userRef.child(userid).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DataSnapshot> task) {
                            User cur = task.getResult().getValue(User.class);
                            cur.removeGame(grp.getPlannedGame(), played);
                            userRef.child(userid).setValue(cur);
                        }
                    });
                }
            }
        });
        group.gamePlayed(played);
        groupRef.child(curGrp).setValue(group);
    }

    public void gameCancelled() {
        gameDone(false);
    }

    public boolean badInput(String code) {
        if (code.contains(".") || code.contains("$") || code.contains("#") || code.contains("[") || code.contains("]") || code.contains("\\")) {
            Toast.makeText(GroupPageActivity.this, "Name contains invalid character", Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }

    public void inviteFriend(View v) {
        Intent intent = new Intent(this, InviteFriendActivity.class);
        intent.putExtra("group", curGrp);
        startActivity(intent);
    }
}