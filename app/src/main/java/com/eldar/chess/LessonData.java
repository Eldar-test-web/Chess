package com.eldar.chess;

import java.util.ArrayList;
import java.util.List;

/** Playable "Learn To Play" lessons - one verified move each. */
public class LessonData {

    public static class Lesson {
        public String title, text, fen, from, to;

        Lesson(String title, String text, String fen, String from, String to) {
            this.title = title;
            this.text = text;
            this.fen = fen;
            this.from = from;
            this.to = to;
        }
    }

    public static List<Lesson> all() {
        List<Lesson> list = new ArrayList<>();
        list.add(new Lesson("Move the King",
                "The king is the most important piece. Move him one square in any direction - tap the king, then the glow square.",
                "8/8/8/3k4/8/8/4K3/8 w - - 0 1", "E2", "E3"));
        list.add(new Lesson("The Knight",
                "Knights jump in an L shape - two squares one way, one square the other. They jump over other pieces!",
                "8/8/8/8/8/8/8/K6N w - - 0 1", "H1", "G3"));
        list.add(new Lesson("The Rook",
                "Rooks slide in straight lines - along files and ranks, as far as the board allows.",
                "8/8/8/8/8/8/8/K6R w - - 0 1", "H1", "H8"));
        list.add(new Lesson("The Bishop",
                "Bishops move diagonally and never change colour - each bishop stays on squares of one colour.",
                "8/8/8/8/8/8/8/K6B w - - 0 1", "H1", "B7"));
        list.add(new Lesson("The Queen",
                "The queen combines rook and bishop moves - she is the most powerful piece on the board.",
                "8/8/8/8/8/8/8/K6Q w - - 0 1", "H1", "H7"));
        list.add(new Lesson("The Pawn",
                "Pawns move forward one square - or two squares on their very first move.",
                "8/8/8/8/8/8/4P3/4K3 w - - 0 1", "E2", "E4"));
        list.add(new Lesson("Capturing",
                "Pawns capture diagonally - move onto the enemy pawn to take it.",
                "8/8/8/8/4p3/3P4/8/4K3 w - - 0 1", "D3", "E4"));
        list.add(new Lesson("Check",
                "Attack the enemy king with your rook to give check. The king must deal with the threat.",
                "8/8/8/7k/8/8/8/4K2R w - - 0 1", "H1", "H4"));
        list.add(new Lesson("Checkmate",
                "Re8 is checkmate - the black king is trapped behind his own pawns and cannot escape!",
                "6k1/5ppp/8/8/8/8/8/4R1K1 w - - 0 1", "E1", "E8"));
        list.add(new Lesson("Castling",
                "Castle to tuck your king away and activate your rook - the king moves two squares toward the rook.",
                "r3k2r/8/8/8/8/8/8/R3K2R w KQkq - 0 1", "E1", "G1"));
        list.add(new Lesson("En Passant",
                "The sneakiest pawn capture in chess - take the enemy pawn that just dashed past you.",
                "8/8/8/3pP3/8/8/8/4K3 w - d6 0 1", "E5", "D6"));
        list.add(new Lesson("Promotion",
                "March your pawn to the last rank and promote it - to a brand new queen!",
                "8/4P3/8/8/8/8/8/4K3 w - - 0 1", "E7", "E8"));
        list.add(new Lesson("The Pin",
                "Pins freeze pieces in place. Slide your rook to the e-file to pin the knight to the black king.",
                "4k3/8/8/8/4n3/8/8/K5R1 w - - 0 1", "G1", "E1"));
        list.add(new Lesson("The Fork",
                "A fork attacks two pieces at once. Jump in with the knight and hit the king and the rook!",
                "5r2/8/1k6/4N3/8/8/8/4K3 w - - 0 1", "E5", "D7"));
        list.add(new Lesson("Back Rank Mate",
                "The black king is trapped behind his own pawns - deliver the back rank mate with your rook.",
                "6k1/5ppp/8/8/8/8/8/3R2K1 w - - 0 1", "D1", "D8"));
        list.add(new Lesson("Ladder Mate",
                "Two rooks work together like a ladder - one cuts off the 7th rank, the other delivers mate.",
                "7k/R7/8/8/8/8/8/1R5K w - - 0 1", "B1", "B8"));
        return list;
    }
}
