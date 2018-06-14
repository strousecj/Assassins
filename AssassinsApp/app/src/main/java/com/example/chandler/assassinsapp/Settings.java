package com.example.chandler.assassinsapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class Settings extends AppCompatActivity {

    private FirebaseAuth auth;
    private Button logoutButton, notificationsButton, leaveGameButton;

    private DatabaseReference mFirebaseDatabase;
    private FirebaseDatabase mFirebaseInstance;
    private boolean playerAlreadyInGame = false;
    private boolean gameOwnerFound = false;
    private String gameIdUserBelongsTo;
    private String playerNumberInPlayerList;
    private int numPlayersInGame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Intent intent = getIntent();
        ViewGroup layout = (ViewGroup) findViewById(R.id.activity_settings);

        //get Firebase auth instance
        auth = FirebaseAuth.getInstance();
        auth = FirebaseAuth.getInstance();
        mFirebaseInstance = FirebaseDatabase.getInstance(); // access database
        mFirebaseDatabase = mFirebaseInstance.getReference("games"); // get reference to 'games' node

        logoutButton = (Button) findViewById(R.id.logoutButton);
        leaveGameButton = (Button) findViewById(R.id.leaveGameButton);

        // add functionality later, notifications not working yet
        ToggleButton toggle = (ToggleButton) findViewById(R.id.notificationsButton);
        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked){
                if(isChecked){
                    // toggle is enabled
                }
                else{
                    // toggle is disabled
                }
            }
        });

        // this listener will be called when there is change in firebase user session
        FirebaseAuth.AuthStateListener authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    // user auth state is changed - user is null
                    // launch login activity
                    startActivity(new Intent(Settings.this, LoginActivity.class));
                    finish();
                }
            }
        };

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
                Intent intent = new Intent(Settings.this, LoginActivity.class);
                startActivity(intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            }
        });

        leaveGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                leaveGame(v);
            }
        });
    }

    private void createAndShowAlertDialog(boolean gameOwner, final String gameId, final String playerNum, final int playersInGame) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if(gameOwner) {
            builder.setTitle("Are you sure you want to leave?\nGame will be deleted if owner leaves.");
            builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    mFirebaseDatabase.child(gameId).removeValue();
                    dialog.dismiss();
                }
            });
            builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                }
            });
        }
        else{
            builder.setTitle("Are you sure you want to leave the game?");
            builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    mFirebaseDatabase.child(gameId).child("playerList").child(playerNum).removeValue();
                    mFirebaseDatabase.child(gameId).child("playerCount").setValue(playersInGame - 1);
                    dialog.dismiss();
                }
            });
            builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                }
            });
        }

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    //log out method
    public void signOut() {
        auth.signOut();
    }

    public void leaveGame(final View view) {
        playerAlreadyInGame = false;
        mFirebaseDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT) // Object.equals() only works with KitKat (API 19) and higher
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snap: dataSnapshot.getChildren()) { // gets all games
                    for(DataSnapshot list: snap.child("playerList").getChildren()){ // gets list of players in each game
                        if(Objects.equals(list.child("playerId").getValue().toString(), auth.getCurrentUser().getUid())) {
                            playerAlreadyInGame = true;
                            gameIdUserBelongsTo = list.child("gameId").getValue().toString();
                            playerNumberInPlayerList = list.getKey();
                            numPlayersInGame = (int) snap.child("playerList").getChildrenCount();
                            break;
                        }
                    }
                }

                if (playerAlreadyInGame){ // if user is in a game, next will check if user owns a game
                    gameOwnerFound = false;
                    for (DataSnapshot snap: dataSnapshot.getChildren()) {
                        if(Objects.equals(snap.child("gameOwner").getValue().toString(), auth.getCurrentUser().getUid())){
                            gameOwnerFound = true;
                            break;
                        }
                    }
                    createAndShowAlertDialog(gameOwnerFound, gameIdUserBelongsTo, playerNumberInPlayerList, numPlayersInGame);
                }
                else { // if user is not in a game, they get a popup
                    Context context = getApplicationContext();
                    CharSequence text = "You are not in a game yet!";
                    int duration = Toast.LENGTH_SHORT;

                    Toast toast = Toast.makeText(context, text, duration);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }
}
