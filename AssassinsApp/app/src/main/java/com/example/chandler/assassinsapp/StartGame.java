package com.example.chandler.assassinsapp;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class StartGame extends AppCompatActivity {

    private FirebaseAuth auth;
    private DatabaseReference mFirebaseDatabase;
    private FirebaseDatabase mFirebaseInstance;
    private String gameToStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_game);

        Intent intent = getIntent();
        ViewGroup layout = (ViewGroup) findViewById(R.id.activity_start_game);
        Button startGameButton = (Button) findViewById(R.id.start_game_confirmation_button);
        Button cancelStartGameButton = (Button) findViewById(R.id.cancel_start_game_button);

        auth = FirebaseAuth.getInstance();
        mFirebaseInstance = FirebaseDatabase.getInstance(); // access database
        mFirebaseDatabase = mFirebaseInstance.getReference("games"); // get reference to 'games' node
        mFirebaseDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snap : dataSnapshot.getChildren()) {
                    if (Objects.equals(snap.child("gameOwner").getValue().toString(), auth.getCurrentUser().getUid())) {
                        gameToStart = snap.child("gameId").getValue().toString();
                        break;
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });

        startGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startGameConfirmed(gameToStart);
            }
        });

        cancelStartGameButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                cancelStartGame();
            }
        });
    }

    public void startGameConfirmed(String gameId) {
        mFirebaseDatabase.child(gameId).child("gameStarted").setValue(true);

        Context context = getApplicationContext();
        CharSequence text = "Game has been started!";
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, text, duration);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void cancelStartGame(){
        Intent intent = new Intent(this, ManageGame.class);
        startActivity(intent);
    }
}