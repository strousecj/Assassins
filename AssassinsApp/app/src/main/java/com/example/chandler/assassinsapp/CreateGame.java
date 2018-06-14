package com.example.chandler.assassinsapp;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;

public class CreateGame extends AppCompatActivity implements View.OnClickListener{

    Button startDatePickerButton, startTimePickerButton, endDatePickerButton, endTimePickerButton, createGameButton;
    EditText locationText, weaponText, weaponRuleText, safePlaceText, startDateText, startTimeText, endDateText, endTimeText;
    private int mYear, mMonth, mDay, mHour, mMinute;

    private FirebaseAuth auth;
    private DatabaseReference mFirebaseDatabase;
    private FirebaseDatabase mFirebaseInstance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_game);

        Intent intent = getIntent();
        ViewGroup layout = (ViewGroup) findViewById(R.id.activity_create_game);

        auth = FirebaseAuth.getInstance();

        locationText = (EditText) findViewById(R.id.location_text);
        weaponText = (EditText) findViewById(R.id.weapon_text);
        weaponRuleText = (EditText) findViewById(R.id.weapon_rule_text);
        safePlaceText = (EditText) findViewById(R.id.safe_place_text);

        startDatePickerButton = (Button) findViewById(R.id.start_date_button);
        startDateText = (EditText) findViewById(R.id.start_date_text);
        startTimePickerButton = (Button) findViewById(R.id.start_time_button);
        startTimeText = (EditText) findViewById(R.id.start_time_text);

        endDatePickerButton = (Button) findViewById(R.id.end_date_button);
        endDateText = (EditText) findViewById(R.id.end_date_text);
        endTimePickerButton = (Button) findViewById(R.id.end_time_button);
        endTimeText = (EditText) findViewById(R.id.end_time_text);

        createGameButton = (Button) findViewById(R.id.create_game_button);

        startDatePickerButton.setOnClickListener(this);
        startTimePickerButton.setOnClickListener(this);
        endDatePickerButton.setOnClickListener(this);
        endTimePickerButton.setOnClickListener(this);

        createGameButton.setOnClickListener(this);

        mFirebaseInstance = FirebaseDatabase.getInstance(); // access database
        mFirebaseDatabase = mFirebaseInstance.getReference("games"); // get reference to 'games' node
    } // end of onCreate()

    @Override
    public void onClick(View v) {
        if(v == startDatePickerButton){
            // Get Current Date
            final Calendar c = Calendar.getInstance();
            mYear = c.get(Calendar.YEAR);
            mMonth = c.get(Calendar.MONTH);
            mDay = c.get(Calendar.DAY_OF_MONTH);

            // Launch Date Picker
            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year,
                                              int monthOfYear, int dayOfMonth){
                            startDateText.setText((monthOfYear + 1) + "/" + dayOfMonth + "/" + year);
                        }
                    }, mYear, mMonth, mDay);
                datePickerDialog.show();
        }

        if(v == startTimePickerButton){
            // Get Current Time
            final Calendar c = Calendar.getInstance();
            mHour = c.get(Calendar.HOUR);
            mMinute = c.get(Calendar.MINUTE);

            // Launch Time Picker Dialog
            TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                    new TimePickerDialog.OnTimeSetListener() {

                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                            String timeOutput = String.format("%02d:%02d", hourOfDay, minute);
                            startTimeText.setText(timeOutput);
                        }
                    }, mHour, mMinute, false);
            timePickerDialog.show();
        }

        if(v == endDatePickerButton){
            // Get Current Date
            final Calendar c = Calendar.getInstance();
            mYear = c.get(Calendar.YEAR);
            mMonth = c.get(Calendar.MONTH);
            mDay = c.get(Calendar.DAY_OF_MONTH);

            // Launch Date Picker
            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year,
                                              int monthOfYear, int dayOfMonth){
                            endDateText.setText((monthOfYear + 1) + "/" + dayOfMonth + "/" + year);
                        }
                    }, mYear, mMonth, mDay);
            datePickerDialog.show();
        }

        if(v == endTimePickerButton){
            // Get Current Time
            final Calendar c = Calendar.getInstance();
            mHour = c.get(Calendar.HOUR);
            mMinute = c.get(Calendar.MINUTE);

            // Launch Time Picker Dialog
            TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                    new TimePickerDialog.OnTimeSetListener() {

                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                            String timeOutput = String.format("%02d:%02d", hourOfDay, minute);
                            endTimeText.setText(timeOutput);
                        }
                    }, mHour, mMinute, false);
            timePickerDialog.show();
        }

        if(v == createGameButton){
            Game game = new Game();

            game.setGameId(mFirebaseDatabase.push().getKey());
            game.setGameOwner(auth.getCurrentUser().getUid());
            game.setLocation(locationText.getText().toString());
            game.setWeapon(weaponText.getText().toString());
            game.setWeaponRule(weaponRuleText.getText().toString());
            game.setSafePlace(safePlaceText.getText().toString());
            game.setStartDate(startDateText.getText().toString());
            game.setStartTime(startTimeText.getText().toString());
            game.setEndDate(endDateText.getText().toString());
            game.setEndTime(endTimeText.getText().toString());
            game.addPlayer(auth.getCurrentUser().getUid(), auth.getCurrentUser().getEmail());
            game.setGameStarted(false);
            mFirebaseDatabase.child(game.getGameId()).setValue(game);

            finish();
        }
    } // end of onClick()
}
