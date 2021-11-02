package com.example.footup;

import java.util.ArrayList;

public class Group {
    private String name;
    private String owner;
    private ArrayList<String> members;
    private String plannedGame;
    private int gamesPlayed;


    public Group() {
        name = "";
        owner = "";
        members = new ArrayList<>();
        plannedGame = "";
        gamesPlayed = 0;
    }

    public Group(String n) {
        this();
        name = n;
    }

    public String toString() {
        return name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public ArrayList<String> getMembers() {
        return members;
    }

    public void setMembers(ArrayList<String> members) {
        this.members = members;
    }

    public String getPlannedGame() {
        return plannedGame;
    }

    public void setPlannedGame(String plannedGame) {
        this.plannedGame = plannedGame;
    }

    public int getGamesPlayed() {
        return gamesPlayed;
    }

    public void setGamesPlayed(int gamesPlayed) {
        this.gamesPlayed = gamesPlayed;
    }

    public void addMember(String id) {
        members.add(id);
    }

    public int numMembers() {
        return members.size();
    }

    public void gamePlayed(Boolean p) {
        if (p) gamesPlayed++;
        plannedGame = "";
    }
}
