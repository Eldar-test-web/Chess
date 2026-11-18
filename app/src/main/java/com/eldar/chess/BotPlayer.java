package com.eldar.chess;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Piece;
import com.github.bhlangonijr.chesslib.Side;
import com.github.bhlangonijr.chesslib.Square;
import com.github.bhlangonijr.chesslib.move.Move;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Minimax bot built on top of chesslib move generation. */
public class BotPlayer {

    public interface Callback { void onMove(Move m); }

    private static final ExecutorService EXEC = Executors.newSingleThreadExecutor();
    private static final Random RND = new Random();

    public static void think(final GameSession session, final Callback cb) {
        final Board snapshot = new Board();
        snapshot.loadFromFen(session.board.getFen());
        final int depth = session.persona.depth;
        final int jitter = session.persona.jitter;
        EXEC.execute(new Runnable() {
            @Override public void run() {
                long t0 = System.currentTimeMillis();
                Move best = search(snapshot, depth, jitter);
                // keep it human: minimum "thinking" time
                long spent = System.currentTimeMillis() - t0;
                long min = 500 + RND.nextInt(1200);
                if (spent < min) {
                    try { Thread.sleep(min - spent); } catch (InterruptedException ignored) { }
                }
                final Move result = best;
                android.os.Handler h = new android.os.Handler(android.os.Looper.getMainLooper());
                h.post(new Runnable() {
                    @Override public void run() { cb.onMove(result); }
                });
            }
        });
    }

    private static Move search(Board root, int depth, int jitter) {
        List<Move> moves = root.legalMoves();
        if (moves.isEmpty()) return null;
        Side me = root.getSideToMove();
        List<Move> candidates = new ArrayList<>();
        int bestScore = Integer.MIN_VALUE;
        for (Move m : moves) {
            root.doMove(m);
            int score = -negamax(root, depth - 1, Integer.MIN_VALUE / 2, Integer.MAX_VALUE / 2,
                    -1, me);
            root.undoMove();
            if (score > bestScore) {
                bestScore = score;
                candidates.clear();
                candidates.add(m);
            } else if (score == bestScore) {
                candidates.add(m);
            }
        }
        // jitter: allow near-best moves for weaker personas
        if (jitter > 0 && candidates.size() == 1) {
            List<Move> near = new ArrayList<>();
            for (Move m : moves) {
                root.doMove(m);
                int score = -negamax(root, Math.max(0, depth - 1), Integer.MIN_VALUE / 2,
                        Integer.MAX_VALUE / 2, -1, me);
                root.undoMove();
                if (score >= bestScore - jitter) near.add(m);
            }
            if (!near.isEmpty()) candidates = near;
        }
        return candidates.get(RND.nextInt(candidates.size()));
    }

    /** negamax with alpha-beta; returns score from perspective of side to move on board. */
    private static int negamax(Board board, int depth, int alpha, int beta, int color, Side me) {
        if (board.isMated()) {
            // side to move is mated -> worst for them
            return -1000000 + (100 - depth);
        }
        if (board.isDraw() || board.isStaleMate() || board.isInsufficientMaterial()) return 0;
        if (depth <= 0) return eval(board, me);

        List<Move> moves = board.legalMoves();
        // order: captures first (cheap heuristic)
        List<Move> ordered = new ArrayList<>();
        for (Move m : moves) if (isCapture(board, m)) ordered.add(m);
        for (Move m : moves) if (!ordered.contains(m)) ordered.add(m);

        int best = Integer.MIN_VALUE / 2;
        for (Move m : ordered) {
            board.doMove(m);
            int score = -negamax(board, depth - 1, -beta, -alpha, -color, me);
            board.undoMove();
            if (score > best) best = score;
            if (best > alpha) alpha = best;
            if (alpha >= beta) break;
        }
        return best;
    }

    private static boolean isCapture(Board b, Move m) {
        return b.getPiece(m.getTo()) != Piece.NONE
                || (b.getPiece(m.getFrom()).getPieceType().name().equals("PAWN")
                && m.getFrom().getFile().ordinal() != m.getTo().getFile().ordinal());
    }

    /** Static evaluation from `me` perspective. */
    private static int eval(Board board, Side me) {
        int score = 0;
        for (Square s : Square.values()) {
            Piece p = board.getPiece(s);
            if (p == Piece.NONE || p.getPieceType().name().equals("NONE")) continue;
            boolean white = p.getPieceSide() == Side.WHITE;
            int v = pieceValue(p);
            v += positionalBonus(p, s);
            if (white == (me == Side.WHITE)) score += v; else score -= v;
        }
        return score;
    }

    private static int pieceValue(Piece p) {
        switch (p.getPieceType().name()) {
            case "PAWN": return 100;
            case "KNIGHT": return 320;
            case "BISHOP": return 330;
            case "ROOK": return 500;
            case "QUEEN": return 900;
            default: return 0;
        }
    }

    private static int positionalBonus(Piece p, Square s) {
        String t = p.getPieceType().name();
        int file = s.getFile().ordinal();
        int rank = s.getRank().ordinal();
        if (t.equals("KNIGHT") || t.equals("BISHOP")) {
            int dist = Math.max(Math.abs(file - 3), Math.abs(file - 4))
                    + Math.max(Math.abs(rank - 3), Math.abs(rank - 4));
            return (3 - dist) * 6;
        }
        if (t.equals("PAWN")) {
            int r = p.getPieceSide().name().equals("WHITE") ? rank : 7 - rank;
            return r * 4;
        }
        return 0;
    }
}
