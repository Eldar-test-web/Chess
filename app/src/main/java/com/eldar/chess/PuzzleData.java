package com.eldar.chess;

import java.util.ArrayList;
import java.util.List;

/** Mate-in-1 puzzle set (all solutions verified by hand). */
public class PuzzleData {

    public static class Puzzle {
        public String fen;
        public String from, to;
        public int rating;

        Puzzle(String fen, String from, String to, int rating) {
            this.fen = fen; this.from = from; this.to = to; this.rating = rating;
        }
    }

    public static List<Puzzle> all() {
        List<Puzzle> list = new ArrayList<>();
        // Re8# - back rank mate
        list.add(new Puzzle("6k1/5ppp/8/8/8/8/8/4R1K1 w - - 0 1", "E1", "E8", 120));
        // Rd8# - back rank mate
        list.add(new Puzzle("6k1/5ppp/8/8/8/8/8/3R2K1 w - - 0 1", "D1", "D8", 120));
        // Qg8# with Kb6 cover
        list.add(new Puzzle("k7/8/1K6/8/8/8/6Q1/8 w - - 0 1", "G2", "G8", 140));
        // Rb8# ladder with Ra7 on 7th
        list.add(new Puzzle("7k/R7/8/8/8/8/8/1R5K w - - 0 1", "B1", "B8", 160));
        // Nf7# smothered (Kh8, Rg8, pawns g7 h7)
        list.add(new Puzzle("6rk/6pp/3N4/8/8/8/8/K7 w - - 0 1", "D6", "F7", 250));
        // Qg7# protected by Kf6
        list.add(new Puzzle("6k1/8/5K2/8/8/8/8/6Q1 w - - 0 1", "G1", "G7", 180));
        // Qb7# protected by Ka6
        list.add(new Puzzle("k7/8/KQ6/8/8/8/8/8 w - - 0 1", "B6", "B7", 100));
        return list;
    }
}
