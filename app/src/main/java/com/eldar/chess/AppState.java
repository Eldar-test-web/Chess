package com.eldar.chess;

import android.content.Context;
import android.content.SharedPreferences;

/** Persistent app state (streak, games played, learn progress, puzzle rating). */
public class AppState {

    private static final String PREFS = "chess_state";

    public int gamesPlayed;
    public int streak;
    public int learnProgress;   // completed lessons count
    public int puzzlesSolved;
    public int puzzleRating = 100; // displayed as base
    public int userRating = 1500;
    public String lastOpponent = "";
    public String lastResult = "";
    public String lastAccuracy = "";

    public void load(Context c) {
        SharedPreferences p = c.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        gamesPlayed = p.getInt("games", 0);
        streak = p.getInt("streak", 0);
        learnProgress = p.getInt("learn", 0);
        puzzlesSolved = p.getInt("puzzles", 0);
        puzzleRating = p.getInt("prating", 100);
        userRating = p.getInt("rating", 1500);
        lastOpponent = p.getString("lopp", "");
        lastResult = p.getString("lres", "");
        lastAccuracy = p.getString("lacc", "");
    }

    public void save(Context c) {
        SharedPreferences p = c.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        p.edit()
                .putInt("games", gamesPlayed)
                .putInt("streak", streak)
                .putInt("learn", learnProgress)
                .putInt("puzzles", puzzlesSolved)
                .putInt("prating", puzzleRating)
                .putInt("rating", userRating)
                .putString("lopp", lastOpponent)
                .putString("lres", lastResult)
                .putString("lacc", lastAccuracy)
                .apply();
    }

    public void recordGame(String opponent, String result, String accuracy, int ratingDelta) {
        gamesPlayed++;
        streak = Math.max(1, streak);
        userRating = Math.max(100, userRating + ratingDelta);
        lastOpponent = opponent;
        lastResult = result;
        lastAccuracy = accuracy;
    }

    public boolean hasPlayed() { return gamesPlayed > 0; }
}
