package com.eldar.chess;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Piece;
import com.github.bhlangonijr.chesslib.Side;
import com.github.bhlangonijr.chesslib.Square;
import com.github.bhlangonijr.chesslib.move.Move;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Wraps the chesslib Board (github.com/bhlangonijr/chesslib) with game flow state. */
public class GameSession {

    public enum Status { SEARCHING, ACTIVE, ENDED }
    public enum Result { WIN, LOSS, DRAW }

    public interface Listener {
        void onPositionChanged();
        void onGameEnded(Result result, String reason);
    }

    public static class Persona {
        public String name; public int rating; public int depth; public int jitter;
        public boolean coach;
        public Persona(String name, int rating, int depth, int jitter, boolean coach) {
            this.name = name; this.rating = rating; this.depth = depth;
            this.jitter = jitter; this.coach = coach;
        }
    }

    public Board board = new Board();
    public Status status = Status.SEARCHING;
    public Result result = null;
    public String endReason = "";

    /** pass & play: both sides are local humans */
    public boolean passAndPlay = false;

    public long clockWhiteMs = 600000;
    public long clockBlackMs = 600000;

    public Persona persona;
    public String userName = "AttachableOil9";
    public int userRating = 1500;

    public final List<String> sanHistory = new ArrayList<>();
    public final List<Move> rawMoves = new ArrayList<>();

    public Listener listener;

    /** index into history currently displayed; -1 = live position */
    public int viewIndex = -1;

    public GameSession(Persona persona, long timeMs) {
        this.persona = persona;
        this.clockWhiteMs = timeMs;
        this.clockBlackMs = timeMs;
    }

    public boolean humanTurn() {
        if (status != Status.ACTIVE) return false;
        if (viewIndex != -1) return false;
        if (passAndPlay) return true;
        return board.getSideToMove() == Side.WHITE;
    }

    public boolean inViewPast() { return viewIndex != -1; }

    /** Try to play a move for the local user. Returns SAN or null if illegal. */
    public String humanMove(String from, String to) {
        if (!humanTurn()) return null;
        Move m = findMove(from, to, null);
        if (m == null) return null;
        return playMove(m);
    }

    public Move findMove(String from, String to, String promotion) {
        Square f = Square.valueOf(from);
        Square t = Square.valueOf(to);
        Piece promo = null;
        if (promotion != null) {
            String p = promotion.toLowerCase();
            Side s = board.getSideToMove();
            if (p.startsWith("q")) promo = s == Side.WHITE ? Piece.WHITE_QUEEN : Piece.BLACK_QUEEN;
            else if (p.startsWith("r")) promo = s == Side.WHITE ? Piece.WHITE_ROOK : Piece.BLACK_ROOK;
            else if (p.startsWith("b")) promo = s == Side.WHITE ? Piece.WHITE_BISHOP : Piece.BLACK_BISHOP;
            else promo = s == Side.WHITE ? Piece.WHITE_KNIGHT : Piece.BLACK_KNIGHT;
        }
        for (Move m : board.legalMoves()) {
            if (m.getFrom() == f && m.getTo() == t) {
                if (m.getPromotion() != null && m.getPromotion() != Piece.NONE) {
                    if (promo == null || !m.getPromotion().equals(promo)) continue;
                }
                return m;
            }
        }
        return null;
    }

    /** Play a move for the currently moving side. Returns SAN. */
    public String playMove(Move m) {
        String san = sanFor(m);
        board.doMove(m);
        rawMoves.add(m);
        sanHistory.add(san);
        viewIndex = -1;
        checkGameEnd();
        if (listener != null) listener.onPositionChanged();
        return san;
    }

    /** Build SAN BEFORE doing the move. */
    private String sanFor(Move m) {
        Piece moved = board.getPiece(m.getFrom());
        String type = moved.getPieceType().name();
        // castling
        if (type.equals("KING")
                && Math.abs(m.getFrom().getFile().ordinal() - m.getTo().getFile().ordinal()) == 2) {
            return m.getTo().getFile().ordinal() >= 6 ? "O-O" : "O-O-O";
        }
        boolean capture;
        Piece target = board.getPiece(m.getTo());
        if (target != Piece.NONE) capture = true;
        else capture = type.equals("PAWN")
                && m.getFrom().getFile().ordinal() != m.getTo().getFile().ordinal(); // en passant

        String letter;
        switch (type) {
            case "KNIGHT": letter = "N"; break;
            case "BISHOP": letter = "B"; break;
            case "ROOK": letter = "R"; break;
            case "QUEEN": letter = "Q"; break;
            case "KING": letter = "K"; break;
            default: letter = ""; break;
        }

        String disamb = "";
        if (!letter.isEmpty()) {
            boolean ambFile = false, ambRank = false, amb = false;
            for (Move o : board.legalMoves()) {
                if (o.getFrom() == m.getFrom() && o.getTo() == m.getTo()) continue;
                if (board.getPiece(o.getFrom()) == moved && o.getTo() == m.getTo()) {
                    amb = true;
                    if (o.getFrom().getFile() == m.getFrom().getFile()) ambRank = true;
                    if (o.getFrom().getRank() == m.getFrom().getRank()) ambFile = true;
                }
            }
            if (amb) {
                if (ambFile && !ambRank) disamb = fileChar(m.getFrom());
                else if (ambRank && !ambFile) disamb = rankChar(m.getFrom());
                else disamb = sqNotation(m.getFrom());
            }
        }

        String dest = sqNotation(m.getTo());
        String san;
        if (letter.isEmpty()) {
            san = capture ? fileChar(m.getFrom()) + "x" + dest : dest;
        } else {
            san = letter + disamb + (capture ? "x" : "") + dest;
        }
        if (m.getPromotion() != null && m.getPromotion() != Piece.NONE) {
            String pr = m.getPromotion().getPieceType().name();
            san += "=" + pr.substring(0, 1);
        }
        return san;
    }

    private static String fileChar(Square s) {
        return String.valueOf((char) ('a' + s.getFile().ordinal()));
    }

    private static String rankChar(Square s) {
        return String.valueOf((char) ('1' + s.getRank().ordinal()));
    }

    private static String sqNotation(Square s) {
        return fileChar(s) + rankChar(s);
    }

    public void checkGameEnd() {
        if (board.isMated()) {
            boolean humanMated = board.getSideToMove() == Side.WHITE; // human is white
            finish(humanMated ? Result.LOSS : Result.WIN, "Checkmate");
        } else if (board.isDraw() || board.isStaleMate() || board.isInsufficientMaterial()) {
            finish(Result.DRAW, board.isStaleMate() ? "Stalemate" : "Draw");
        }
    }

    public void flagFall(boolean whiteRanOut) {
        if (whiteRanOut) finish(Result.LOSS, "You ran out of time");
        else finish(Result.WIN, "Opponent ran out of time");
    }

    public void resign() { finish(Result.LOSS, "You resigned"); }

    private void finish(Result r, String reason) {
        if (status == Status.ENDED) return;
        status = Status.ENDED;
        result = r;
        endReason = reason;
        if (listener != null) listener.onGameEnded(r, reason);
    }

    public void tickClock(long deltaMs) {
        if (status != Status.ACTIVE) return;
        if (board.getSideToMove() == Side.WHITE) clockWhiteMs -= deltaMs;
        else clockBlackMs -= deltaMs;
        if (clockWhiteMs <= 0) { clockWhiteMs = 0; flagFall(true); }
        else if (clockBlackMs <= 0) { clockBlackMs = 0; flagFall(false); }
    }

    public String fmtClock(long ms) {
        long total = Math.max(0, ms) / 1000;
        return (total / 60) + ":" + String.format("%02d", (total % 60));
    }

    // ---------- move history viewing ----------
    public String fenAtView() {
        if (viewIndex == -1 || rawMoves.isEmpty()) return board.getFen();
        Board b = new Board();
        for (int i = 0; i <= viewIndex && i < rawMoves.size(); i++) b.doMove(rawMoves.get(i));
        return b.getFen();
    }

    public Move lastRawMove() {
        int idx = viewIndex == -1 ? rawMoves.size() - 1 : viewIndex;
        if (idx < 0 || idx >= rawMoves.size()) return null;
        return rawMoves.get(idx);
    }

    public void stepBack() {
        if (rawMoves.isEmpty()) return;
        viewIndex = viewIndex == -1 ? rawMoves.size() - 1 : Math.max(0, viewIndex - 1);
        if (listener != null) listener.onPositionChanged();
    }

    public void stepForward() {
        if (viewIndex == -1) return;
        viewIndex++;
        if (viewIndex >= rawMoves.size() - 1) viewIndex = -1;
        if (listener != null) listener.onPositionChanged();
    }

    public void clearView() {
        if (viewIndex != -1) { viewIndex = -1; if (listener != null) listener.onPositionChanged(); }
    }
}
