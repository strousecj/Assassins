package com.example.chandler.assassinsapp;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class Game {
    private String gameId;
    private String gameOwner;
    private String location;
    private String weapon;
    private String weaponRule;
    private String safePlace;
    private String startDate;
    private String startTime;
    private String endDate;
    private String endTime;
    private int playerCount;
    private boolean gameStarted;
    private static ArrayList<Player> playerList = new ArrayList<>();

    private DatabaseReference mDatabase;

    public static class Player {
        private String playerId;
        private String playerName;
        private String killerName;
        private String dateOfDeath;
        private String timeOfDeath;
        private String gameId;

        public Player(){
        }

//        public Player(String playerId, String playerName, String killerName, String dateOfDeath, String timeOfDeath, String gameId){
//            this.playerId = playerId;
//            this.playerName = playerName;
//            this.killerName = killerName;
//            this.dateOfDeath = dateOfDeath;
//            this.timeOfDeath = timeOfDeath;
//            this.gameId = gameId;
//        }

        public String getPlayerId(){ return playerId; }
        public void setPlayerId(String id){ this.playerId = id; }

        public String getPlayerName(){ return playerName; }
        public void setPlayerName(String name){ this.playerName = name; }

        public String getKillerName() { return killerName; }
        public void setKillerName(String killerName) { this.killerName = killerName; }

        public String getDateOfDeath() { return this.dateOfDeath; }
        public void setDateOfDeath(String dateOfDeath) { this.dateOfDeath = dateOfDeath; }

        public String getTimeOfDeath() { return this.timeOfDeath; }
        public void setTimeOfDeath(String timeOfDeath) { this.timeOfDeath = timeOfDeath; }

        public String getGameId() { return this.gameId; }
        public void setGameId(String gameId) { this.gameId = gameId; }
    } // end Player class

    public Game(){
    }

//    public Game(String gameId, String location, String weapon, String weaponRule, String safePlace,
//                    String startDate, String startTime, String endDate, String endTime, Player player) {
//        this.gameId = gameId;
//        this.location = location;
//        this.weapon = weapon;
//        this.weaponRule = weaponRule;
//        this.safePlace = safePlace;
//        this.startDate = startDate;
//        this.startTime = startTime;
//        this.endDate = endDate;
//        this.endTime = endTime;
//        playerList.add(player);
//    }

    public String getGameId() { return gameId; }
    public void setGameId(String gameId) { this.gameId = gameId; }

    public String getGameOwner() { return gameOwner; }
    public void setGameOwner(String gameOwner) { this.gameOwner = gameOwner; }

    public String getLocation() {
        return location;
    }
    public void setLocation(String location) {
        this.location = location;
    }

    public String getWeapon() { return weapon; }
    public void setWeapon(String weapon) { this.weapon = weapon; }

    public String getWeaponRule() { return weaponRule; }
    public void setWeaponRule(String weaponRule) { this.weaponRule = weaponRule; }

    public String getSafePlace() { return safePlace; }
    public void setSafePlace(String safePlace) { this.safePlace = safePlace; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public ArrayList getPlayerList() { return playerList; }

    // adds player to every game player creates... which is bad
    public void addPlayer(String id, String name) {
        mDatabase = FirebaseDatabase.getInstance().getReference();

        Player player = new Player();
        player.setPlayerId(id);
        player.setPlayerName(name);
        player.setGameId(getGameId());
        playerList.add(player);
        playerCount++;
    }

    public int getPlayerCount(){
        return playerCount;
    }

    public void setPlayerCount(int count){
        this.playerCount = count;
    }

    public String getGameIdFromPlayerId(String playerId){
        for(Player player : playerList) { // for each player in the playerList
            if (player.getPlayerId() == playerId){
                return player.gameId;
            }
        }
        return null;
    }
    public boolean getGameStarted() { return gameStarted; }
    public void setGameStarted(boolean started) { this.gameStarted = started; }
} // end Game class
