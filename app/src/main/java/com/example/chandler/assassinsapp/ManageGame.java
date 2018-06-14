package com.example.chandler.assassinsapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.gms.appinvite.AppInviteInvitation;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class ManageGame extends AppCompatActivity {

    private static final String TAG = "ManageGameActivity";
    private static final int REQUEST_INVITE = 0; // Not really sure what this value does...

    private FirebaseAuth auth;
    private DatabaseReference mFirebaseDatabase;
    private FirebaseDatabase mFirebaseInstance;

    private Game game = new Game();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_game);

        auth = FirebaseAuth.getInstance();
        mFirebaseInstance = FirebaseDatabase.getInstance(); // access database
        mFirebaseDatabase = mFirebaseInstance.getReference("games"); // get reference to 'games' node

        Intent intent = getIntent();
        ViewGroup layout = (ViewGroup) findViewById(R.id.activity_manage_game);
        Button inviteButton = (Button) findViewById(R.id.send_invite_button);

        inviteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onInviteClicked();
            }
        });
    } // end onCreate()

    /** User Clicks on 'Invite' button **/
    public void onInviteClicked() {
        auth = FirebaseAuth.getInstance();

        mFirebaseDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT) // Object.equals() only works with KitKat (API 19) and higher
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snap: dataSnapshot.getChildren()) { // checks each game in database
                    if(Objects.equals(snap.child("gameOwner").getValue().toString(), auth.getCurrentUser().getUid())){
                        game.setGameId(snap.child("gameId").getValue().toString());
                        Intent intent = new AppInviteInvitation.IntentBuilder(getString(R.string.invitation_title))
                                .setMessage(getString(R.string.invitation_message))
                                .setDeepLink(Uri.parse(game.getGameId())) // deepLink will replace Placeholder below
                                .setEmailSubject("Assassins Invitation")
                                .setEmailHtmlContent("<html> <body>" +
                                        "<a href=\"%%APPINVITE_LINK_PLACEHOLDER%%\">Invite Link</a>" + //
                                        "</body> </html>")
                                .build();
                        startActivityForResult(intent, REQUEST_INVITE);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    } // end onInviteClicked

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: requestCode=" + requestCode + ", resultCode=" + resultCode);

        if (requestCode == REQUEST_INVITE) {
            if (resultCode == RESULT_OK) {
                // Get the invitation IDs of all sent messages
                String[] ids = AppInviteInvitation.getInvitationIds(resultCode, data);
                for (String id : ids) {
                    Log.d(TAG, "onActivityResult: sent invitation " + id);
                }
            } else {
                // Sending failed or it was canceled, show failure message to the user
                Log.d(TAG, (getString(R.string.send_failed))); // add fail message later
            }
        }
    }

    /** Called when user clicks the Start Game button **/
    public void startGame(View view) {
        Intent intent = new Intent(this, StartGame.class);
        startActivity(intent);
    }
}
