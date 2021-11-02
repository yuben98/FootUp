package com.example.footup;

import java.util.ArrayList;

public class Game {
    private ArrayList<String> players;
    private boolean openToPublic;
    String where;
    String when;


    public Game() {
        players = new ArrayList<>();
        openToPublic = false;
        where = "";
        when = "";
    }

    public String toString(){
        int size = players.size();
        String res = Integer.toString(size) + " registered player";
        if (size > 1) res+= "s";
        res+= "\nPublic: ";
        if (openToPublic) res += "yes";
        else res+= "no";
        res += "\nWhere: " + where;
        res += "\nWhen: " + when;
        return res;
    }

    public boolean getOpenToPublic() { return openToPublic; }
    public void setOpenToPublic(boolean openToPublic) { this.openToPublic = openToPublic; }

    public void setPlayers(ArrayList<String> players) { this.players = players; }
    public ArrayList<String> getPlayers() { return players; }

    public String getWhere() {return where;}
    public void setWhere(String w) {where = w;}

    public String getWhen() {return when; }
    public void setWhen(String w) {when = w; }
}
