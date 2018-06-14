package com.example.chandler.assassinsapp;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.appinvite.AppInvite;
import com.google.android.gms.appinvite.AppInviteInvitationResult;
import com.google.android.gms.appinvite.AppInviteReferral;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "MainActivity"; // for logging

    private GoogleApiClient mGoogleApiClient;
    private Game game = new Game();
    private Game.Player player = new Game.Player();
    private FirebaseAuth auth;
    private DatabaseReference mFirebaseDatabase;
    private FirebaseDatabase mFirebaseInstance;
    private boolean gameOwnerFound = false;
    private boolean gameHasStarted = false;
    private boolean playerAlreadyInGame = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();
        mFirebaseInstance = FirebaseDatabase.getInstance(); // access database
        mFirebaseDatabase = mFirebaseInstance.getReference("games"); // get reference to 'games' node

        // Build GoogleApiClient with AppInvite API for receiving deep links
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(AppInvite.API)
                .enableAutoManage(this, this)
                .build();

        // Check if this app was launched from a deep link. Setting autoLaunchDeepLink to true
        // would automatically launch the deep link if one is found.
        boolean autoLaunchDeepLink = true;
        AppInvite.AppInviteApi.getInvitation(mGoogleApiClient, this, autoLaunchDeepLink)
                .setResultCallback( new ResultCallback<AppInviteInvitationResult>() {
                    @Override
                    public void onResult(@NonNull AppInviteInvitationResult result) {

                        if (result.getStatus().isSuccess()) {
                            Intent intent = result.getInvitationIntent();
                            final String deepLink = AppInviteReferral.getDeepLink(intent); // Extract deep link from Intent

                            auth = FirebaseAuth.getInstance();

                            player.setGameId(deepLink);
                            player.setPlayerId(auth.getCurrentUser().getUid());
                            player.setPlayerName(auth.getCurrentUser().getEmail());

                            /* When player clicks on link, player is automatically added to the game */
                            mFirebaseDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                                int childCount = 0; // counts number of players currently in playerList
                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    playerAlreadyInGame = false; // initialize value
                                    for (DataSnapshot snap: dataSnapshot.child(deepLink).child("playerList").getChildren()) {
                                        if(Objects.equals(snap.child("playerId").getValue().toString(), auth.getCurrentUser().getUid())){
                                            playerAlreadyInGame = true; // checks to see if user is already in game
                                        }
                                        childCount++;
                                    }
                                    game.setPlayerCount(childCount); // updates playerCount

                                    int tempCounter = 0; // counter to run through playerList to find a unoccupied location
                                    if(!playerAlreadyInGame) {
                                        mFirebaseDatabase.child(deepLink).child("playerCount").setValue(game.getPlayerCount() + 1); // updates playerCount in database
                                        while(true) {
                                            if (dataSnapshot.child(deepLink).child("playerList").child(Integer.toString(tempCounter)).hasChildren()) {
                                                tempCounter++;
                                            }
                                            else{
                                                mFirebaseDatabase.child(deepLink).child("playerList").child(Integer.toString(tempCounter)).setValue(player); // adds player to playerList in database
                                                break;
                                            }
                                        }
                                    }
                                    else{
                                        Context context = getApplicationContext();
                                        CharSequence text = "You have already joined this game!";
                                        int duration = Toast.LENGTH_SHORT;

                                        Toast toast = Toast.makeText(context, text, duration);
                                        toast.setGravity(Gravity.CENTER, 0, 0);
                                        toast.show();
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {}
                            }); // end of listener

                        } else {
                            Log.d(TAG, "getInvitation: no deep link found.");
                        }
                    } // end of on result
                });
    } // end onCreate()

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
    }

    /** Called when user clicks the Create Game button **/
    public void createGame(final View view) {
        playerAlreadyInGame = false;
        mFirebaseDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT) // Object.equals() only works with KitKat (API 19) and higher
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snap: dataSnapshot.getChildren()) { // gets all games
                    for(DataSnapshot list: snap.child("playerList").getChildren()){ // gets list of players in each game
                        if(Objects.equals(list.child("playerId").getValue().toString(), auth.getCurrentUser().getUid())) {
                            playerAlreadyInGame = true;
                            break;
                        }
                    }
                }

                if (!playerAlreadyInGame){ // if user is not in a game, they can create a game as normal
                    Intent intent = new Intent(view.getContext(), CreateGame.class);
                    startActivity(intent);
                }
                else { // if user is in a game, they get a popup
                    Context context = getApplicationContext();
                    CharSequence text = "You are already in a game! You must leave your game before you can create another!";
                    int duration = Toast.LENGTH_SHORT;

                    Toast toast = Toast.makeText(context, text, duration);
                    toast.setGravity(Gravity.CENTER|Gravity.CENTER, 0, 0);
                    toast.show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    /** Called when user clicks the Game Management button **/
    public void manageGame(final View view) {
        gameOwnerFound = false;
        mFirebaseDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT) // Object.equals() only works with KitKat (API 19) and higher
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snap: dataSnapshot.getChildren()) {
                    if(Objects.equals(snap.child("gameOwner").getValue().toString(), auth.getCurrentUser().getUid())){
                        gameOwnerFound = true;
                        gameHasStarted = (boolean) snap.child("gameStarted").getValue();
                        break;
                    }
                }

                if (gameOwnerFound){ // if user owns a game, they can manage game as normal
                    if(gameHasStarted) { // if game has already started, user can no longer make changes
                        Context context = getApplicationContext();
                        CharSequence text = "You can no longer makes changes, game has started!";
                        int duration = Toast.LENGTH_SHORT;

                        Toast toast = Toast.makeText(context, text, duration);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();
                    }
                    else{
                        Intent intent = new Intent(view.getContext(), ManageGame.class);
                        startActivity(intent);
                    }
                }
                else { // if user does not own a game, they get a popup
                    Context context = getApplicationContext();
                    CharSequence text = "You must create a game before you can manage a game!";
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

    /** Called when user clicks the Current Game button **/
    public void currentGame(View view) {
        Intent intent = new Intent(this, CurrentGame.class);
        startActivity(intent);
    }

    /** Called when user clicks the Settings button **/
    public void settings(View view) {
        Intent intent = new Intent(this, Settings.class);
        startActivity(intent);
    }
}
