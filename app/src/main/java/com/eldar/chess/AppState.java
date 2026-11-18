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
    public int bulletRating = 100;
    public int blitzRating = 100;
    public int rapidRating = 100;
    public int dailyRating = 100;
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
        bulletRating = p.getInt("rbullet", 100);
        blitzRating = p.getInt("rblitz", 100);
        rapidRating = p.getInt("rrapid", 100);
        dailyRating = p.getInt("rdaily", 100);
        lastOpponent = p.getString("lopp", "");
        lastResult = p.getString("lres", "");
        lastAccuracy = p.getString("lacc", "");
        // one-time seed of a strong club-player profile (not superhuman ratings)
        if (!p.getBoolean("seeded", false)) {
            gamesPlayed = Math.max(gamesPlayed, 1287);
            streak = Math.max(streak, 14);
            if (learnProgress < 8) learnProgress = 8;
            puzzleRating = Math.max(puzzleRating, 2415);
            puzzlesSolved = Math.max(puzzlesSolved, 1840);
            userRating = 1902;
            bulletRating = 1816;
            blitzRating = 1869;
            rapidRating = 1902;
            dailyRating = 1754;
            if (lastOpponent.isEmpty()) {
                lastOpponent = "codon201100";
                lastResult = "Win";
                lastAccuracy = "87.4";
            }
            p.edit().putBoolean("seeded", true).apply();
            save(c);
        }
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
                .putInt("rbullet", bulletRating)
                .putInt("rblitz", blitzRating)
                .putInt("rrapid", rapidRating)
                .putInt("rdaily", dailyRating)
                .putString("lopp", lastOpponent)
                .putString("lres", lastResult)
                .putString("lacc", lastAccuracy)
                .apply();
    }

    public void recordGame(String opponent, String result, String accuracy, int ratingDelta) {
        gamesPlayed++;
        streak = Math.max(1, streak);
        userRating = Math.max(100, userRating + ratingDelta);
        rapidRating = userRating;
        lastOpponent = opponent;
        lastResult = result;
        lastAccuracy = accuracy;
    }

    public boolean hasPlayed() { return gamesPlayed > 0; }
}
