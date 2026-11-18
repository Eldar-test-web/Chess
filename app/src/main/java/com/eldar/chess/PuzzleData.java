package com.eldar.chess;

import java.util.ArrayList;
import java.util.List;

/** Mate-in-1 puzzle set (every solution verified with chesslib). */
public class PuzzleData {

    public static class Puzzle {
        public String fen;
        public String from, to;
        public String theme;
        public int rating;

        Puzzle(String fen, String from, String to, String theme, int rating) {
            this.fen = fen; this.from = from; this.to = to;
            this.theme = theme; this.rating = rating;
        }
    }

    public static List<Puzzle> all() {
        List<Puzzle> list = new ArrayList<>();
        list.add(new Puzzle("6k1/5ppp/8/8/8/8/8/4R1K1 w - - 0 1",
                "E1", "E8", "Back Rank Mate", 120));
        list.add(new Puzzle("6k1/5ppp/8/8/8/8/8/3R2K1 w - - 0 1",
                "D1", "D8", "Back Rank Mate", 120));
        list.add(new Puzzle("6k1/5ppp/8/8/8/8/1Q6/6K1 w - - 0 1",
                "B2", "B8", "Queen Mate", 150));
        list.add(new Puzzle("6k1/5ppp/8/8/8/8/8/2Q3K1 w - - 0 1",
                "C1", "C8", "Queen Mate", 150));
        list.add(new Puzzle("3r2k1/5ppp/8/8/8/8/8/3Q2K1 w - - 0 1",
                "D1", "D8", "Capture the Defender", 200));
        list.add(new Puzzle("6k1/5ppp/8/8/8/8/8/1R4K1 w - - 0 1",
                "B1", "B8", "Rook Mate", 140));
        list.add(new Puzzle("6k1/5ppp/8/8/8/8/6P1/1R4K1 w - - 0 1",
                "B1", "B8", "Rook Mate", 150));
        list.add(new Puzzle("2k5/8/2K5/8/8/8/8/7R w - - 0 1",
                "H1", "H8", "King & Rook", 110));
        list.add(new Puzzle("7k/8/6K1/8/8/8/8/5R2 w - - 0 1",
                "F1", "F8", "King & Rook", 120));
        list.add(new Puzzle("6k1/5ppp/8/8/8/8/8/Q4BK1 w - - 0 1",
                "A1", "A8", "Queen Mate", 170));
        list.add(new Puzzle("6k1/5ppp/8/8/8/8/8/3R1RK1 w - - 0 1",
                "D1", "D8", "Double Rooks", 160));
        list.add(new Puzzle("6k1/5ppp/8/8/8/8/8/3Q2K1 w - - 0 1",
                "D1", "D8", "Queen Mate", 140));
        list.add(new Puzzle("6k1/5ppp/8/8/8/8/8/R5K1 w - - 0 1",
                "A1", "A8", "Rook Mate", 130));
        list.add(new Puzzle("7k/R7/8/8/8/8/8/1R5K w - - 0 1",
                "B1", "B8", "Ladder Mate", 160));
        list.add(new Puzzle("6rk/6pp/3N4/8/8/8/8/K7 w - - 0 1",
                "D6", "F7", "Smothered Mate", 250));
        list.add(new Puzzle("6k1/8/5K2/8/8/8/8/6Q1 w - - 0 1",
                "G1", "G7", "Queen & King", 180));
        return list;
    }
}
