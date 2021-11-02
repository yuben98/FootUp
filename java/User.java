package com.example.footup;

import java.util.ArrayList;

public class User{
    private String name;
    private String email;
    private int gamesPlayed;
    private ArrayList<String> friends;
    private ArrayList<String> groups;
    private ArrayList<String> upcomingGames;


    public User () {
        name = "";
        email = "";
        gamesPlayed = 0;
        friends = new ArrayList<>();
        groups = new ArrayList<>();
        upcomingGames = new ArrayList<>();
    }

    public User(String e){
        this();
        email = e;
        name = e.split("@")[0];
    }

    public String toString() {
        if (!name.isEmpty()) return name;
        return email.split("@")[0];
    }

    public void setName(String name) {
        this.name = name;
    }
    public String getName() { return name; }

    public String getEmail() {
        return email;
    }
    public int getGamesPlayed() { return gamesPlayed; }

    public void setFriends(ArrayList<String> friends) { this.friends = friends; }
    public ArrayList<String> getFriends() { return friends; }

    public void setGroups(ArrayList<String> groups) { this.groups = groups; }
    public ArrayList<String> getGroups() { return groups; }

    public void setUpcomingGames(ArrayList<String> upcomingGames) {this.upcomingGames = upcomingGames; }
    public ArrayList<String > getUpcomingGames() { return upcomingGames; }


    public int numFriends() {
        if(friends == null) return 0;
        return friends.size(); }

    public int numGroups() {
        if(groups == null) return 0;
        return groups.size();
    }

    public void addGroup(String g) {
        if (!groups.contains(g)) groups.add(g);
    }

    public void removeGame(String game, boolean played) {
        if (upcomingGames.contains(game)) {
            upcomingGames.remove(game);
            if (!played) gamesPlayed--;
        }
    }

    public void addGame(String game) {
        upcomingGames.add(game);
        gamesPlayed++;
    }
}
