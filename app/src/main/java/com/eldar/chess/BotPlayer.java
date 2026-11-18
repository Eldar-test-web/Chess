package com.eldar.chess;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Piece;
import com.github.bhlangonijr.chesslib.Side;
import com.github.bhlangonijr.chesslib.Square;
import com.github.bhlangonijr.chesslib.move.Move;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Human-feeling opponent engine.
 *
 * Plays a believable game for its persona - natural openings, grabs hanging
 * material once in a while, defends when attacked - but a material governor
 * scripts the arc of the game: the opponent gradually sheds material toward a
 * planned balance, never delivers mate itself, and its will to defend a mating
 * attack fades out, so the player converts the win and it looks earned.
 */
public class BotPlayer {

    public interface Callback { void onMove(Move m); }

    private static final ExecutorService EXEC = Executors.newSingleThreadExecutor();
    private static final Random RND = new Random();

    /** per-game memory: how much will is left to defend a mating attack */
    private static class BotState { double defendChance; }
    private static final ConcurrentHashMap<GameSession, BotState> STATES = new ConcurrentHashMap<>();

    private static BotState state(GameSession s) {
        BotState st = STATES.get(s);
        if (st == null) {
            st = new BotState();
            st.defendChance = s.persona == null ? 0.5 : s.persona.defend;
            STATES.put(s, st);
        }
        return st;
    }

    public static void think(final GameSession session, final Callback cb) {
        final Board snapshot = new Board();
        snapshot.loadFromFen(session.board.getFen());
        EXEC.execute(new Runnable() {
            @Override public void run() {
                long t0 = System.currentTimeMillis();
                Move best = pick(session, snapshot);
                long spent = System.currentTimeMillis() - t0;
                // each character has its own "thinking" speed: nobody answers instantly
                long floor;
                GameSession.Persona p = session.persona;
                if (p != null && p.delayMaxMs > 0) {
                    floor = p.delayMinMs + (p.delayMaxMs > p.delayMinMs
                            ? RND.nextInt((int) (p.delayMaxMs - p.delayMinMs)) : 0);
                } else {
                    floor = 180 + RND.nextInt(220);
                }
                if (spent < floor) {
                    try { Thread.sleep(floor - spent); } catch (InterruptedException ignored) { }
                }
                final Move result = best;
                new android.os.Handler(android.os.Looper.getMainLooper()).post(new Runnable() {
                    @Override public void run() { cb.onMove(result); }
                });
            }
        });
    }

    // ------------------------------------------------------------------ pick

    private static Move pick(GameSession session, Board b) {
        List<Move> legal = b.legalMoves();
        if (legal.isEmpty()) return null;
        int plies = session.rawMoves.size();

        // 0) never end the game by our own hand: no delivering mate, no stalemating
        List<Move> safe = new ArrayList<>();
        for (Move m : legal) {
            b.doMove(m);
            boolean mate = b.isMated();
            boolean stale = b.isStaleMate();
            b.undoMove();
            if (!mate && !stale) safe.add(m);
        }
        if (safe.isEmpty()) {
            // forced: prefer a draw over a loss for the player
            for (Move m : legal) {
                b.doMove(m);
                boolean stale = b.isStaleMate();
                b.undoMove();
                if (stale) return m;
            }
            return legal.get(0);
        }

        int bal = balance(b, session);
        int target = targetBalance(plies);
        boolean collapse = plies >= 64
                || (plies >= 40 && bal <= -700)
                || (plies >= 46 && bal <= target);

        // 1) natural opening moves early on
        if (!collapse && plies < 6) {
            Move book = bookMove(session, b, safe);
            if (book != null) return book;
        }

        // 2) the player threatens mate in 1: defend only while there is will left,
        //    so the player can always convert in the end
        if (!collapse && plies >= 2 && userThreatensMateIn1(b)) {
            List<Move> defenses = new ArrayList<>();
            for (Move m : safe) {
                b.doMove(m);
                boolean still = userHasMateIn1(b);
                b.undoMove();
                if (!still) defenses.add(m);
            }
            if (defenses.isEmpty()) return safest(safe, b);       // nothing helps
            BotState st = state(session);
            if (RND.nextDouble() < st.defendChance) {
                st.defendChance = Math.max(0.05, st.defendChance * 0.45);
                return safest(defenses, b);
            }
            return safest(safe, b);   // "didn't see it" - player converts
        }

        // 3) material governor: shed pieces toward the scripted balance arc
        int need = bal - target;
        if (!collapse && need > 80) {
            Move blunder = pickBlunder(b, safe, need, plies);
            if (blunder != null) return blunder;
        }

        // 4) grab something once in a while - a real opponent takes free material
        if (!collapse && bal <= target + 60
                && session.persona != null && RND.nextDouble() < session.persona.aggro) {
            Move grab = bestGrab(b, safe);
            if (grab != null) return grab;
        }

        if (collapse) return collapseMove(b, safe);

        // 5) normal noisy play for the persona
        return naturalMove(b, safe, session);
    }

    /** where the opponent "wants" its material to be at this point of the game. */
    private static int targetBalance(int plies) {
        if (plies <= 10) return 0;
        return Math.max(-(plies - 10) * 33, -900);
    }

    private static int balance(Board b, GameSession s) {
        int bot = 0, user = 0;
        for (Square sq : Square.values()) {
            Piece p = b.getPiece(sq);
            if (p == Piece.NONE || p.getPieceSide() == null) continue;
            int v = pieceValue(p);
            if (p.getPieceSide() == s.botSide()) bot += v; else user += v;
        }
        return bot - user;
    }

    /**
     * Find a believable mistake: a quiet move that leaves the player a capture
     * worth about what the opponent is ready to give up at this stage.
     * Early game only pawns, then minors, then rooks - like a real slide.
     */
    private static Move pickBlunder(Board b, List<Move> safe, int need, int plies) {
        int tierCap = plies < 10 ? 130 : plies < 18 ? 360 : plies < 26 ? 550 : 1000;
        int budget = Math.min(need + 130, tierCap);
        Move best = null;
        int bestGap = Integer.MAX_VALUE;
        for (Move m : safe) {
            if (isCapture(b, m)) continue;             // mistakes are quiet moves
            b.doMove(m);
            int offer = biggestUserCapture(b);
            b.undoMove();
            if (offer < 60 || offer > budget) continue;
            int gap = Math.abs(offer - need);
            if (gap < bestGap) { bestGap = gap; best = m; }
        }
        return best;
    }

    private static Move bestGrab(Board b, List<Move> safe) {
        Move best = null;
        int bestScore = 40;   // must win at least a pawn's worth
        for (Move m : safe) {
            if (!isCapture(b, m)) continue;
            int gain = captureValue(b, m);
            b.doMove(m);
            int back = biggestUserCapture(b);
            b.undoMove();
            int score = gain - back;
            if (score > bestScore) { bestScore = score; best = m; }
        }
        return best;
    }

    /** aimless, beaten shuffle: quiet non-king moves while the player closes in. */
    private static Move collapseMove(Board b, List<Move> safe) {
        List<Move> quiet = new ArrayList<>();
        for (Move m : safe) {
            if (isCapture(b, m)) continue;
            Piece p = b.getPiece(m.getFrom());
            if (p.getPieceType().name().equals("KING")) continue;
            quiet.add(m);
        }
        List<Move> pool = quiet.isEmpty() ? safe : quiet;
        return pool.get(RND.nextInt(pool.size()));
    }

    // ------------------------------------------------------------------ helpers

    private static boolean userHasMateIn1(Board b) {
        for (Move m : b.legalMoves()) {
            b.doMove(m);
            boolean mate = b.isMated();
            b.undoMove();
            if (mate) return true;
        }
        return false;
    }

    private static boolean userThreatensMateIn1(Board b) {
        for (Move m : b.legalMoves()) {
            b.doMove(m);
            boolean t = userHasMateIn1(b);
            b.undoMove();
            if (t) return true;
        }
        return false;
    }

    /** the move a normal player would pick: the one that loses the least material. */
    private static Move safest(List<Move> moves, Board b) {
        List<Move> ties = new ArrayList<>();
        int bestOffer = Integer.MAX_VALUE;
        for (Move m : moves) {
            b.doMove(m);
            int offer = biggestUserCapture(b);
            b.undoMove();
            if (offer < bestOffer) { bestOffer = offer; ties.clear(); ties.add(m); }
            else if (offer == bestOffer) ties.add(m);
        }
        return ties.get(RND.nextInt(ties.size()));
    }

    private static int biggestUserCapture(Board b) {
        int best = 0;
        for (Move m : b.legalMoves()) {
            int v = captureValue(b, m);
            if (v > best) best = v;
        }
        return best;
    }

    private static int captureValue(Board b, Move m) {
        if (!isCapture(b, m)) return 0;
        Piece t = b.getPiece(m.getTo());
        if (t != Piece.NONE && !t.getPieceType().name().equals("NONE")) return pieceValue(t);
        return 100;   // en passant
    }

    private static boolean isCapture(Board b, Move m) {
        return b.getPiece(m.getTo()) != Piece.NONE
                || (b.getPiece(m.getFrom()).getPieceType().name().equals("PAWN")
                && m.getFrom().getFile().ordinal() != m.getTo().getFile().ordinal());
    }

    // ------------------------------------------------------------------ openings

    private static String uci(Move m) {
        return m.getFrom().toString().toLowerCase() + m.getTo().toString().toLowerCase();
    }

    /** small natural opening book so the first moves look like real online games. */
    private static Move bookMove(GameSession s, Board b, List<Move> safe) {
        String[][] book;
        if (b.getSideToMove() == Side.WHITE) {
            book = new String[][]{{"e2e4", "40"}, {"d2d4", "28"}, {"g1f3", "16"}, {"c2c4", "16"}};
        } else {
            String uf = s.rawMoves.isEmpty() ? ""
                    : s.rawMoves.get(s.rawMoves.size() - 1).getFrom().toString().toLowerCase();
            switch (uf) {
                case "e2": book = new String[][]{{"e7e5", "40"}, {"c7c5", "25"}, {"e7e6", "15"},
                        {"c7c6", "10"}, {"g8f6", "10"}}; break;
                case "d2": book = new String[][]{{"d7d5", "40"}, {"g8f6", "25"}, {"e7e6", "15"},
                        {"d7d6", "10"}, {"g7g6", "10"}}; break;
                case "c2": book = new String[][]{{"e7e5", "35"}, {"g8f6", "30"}, {"c7c5", "20"},
                        {"e7e6", "15"}}; break;
                case "g1": book = new String[][]{{"d7d5", "30"}, {"g8f6", "30"}, {"c7c5", "20"},
                        {"e7e6", "20"}}; break;
                default:   book = new String[][]{{"g8f6", "30"}, {"d7d5", "25"}, {"e7e5", "25"},
                        {"c7c5", "20"}}; break;
            }
        }
        int total = 0;
        List<Move> picks = new ArrayList<>();
        List<Integer> weights = new ArrayList<>();
        for (String[] e : book) {
            for (Move m : safe) {
                if (uci(m).equals(e[0])) {
                    int w = Integer.parseInt(e[1]);
                    picks.add(m);
                    weights.add(w);
                    total += w;
                    break;
                }
            }
        }
        if (picks.isEmpty()) return null;
        int r = RND.nextInt(total);
        for (int i = 0; i < picks.size(); i++) {
            r -= weights.get(i);
            if (r < 0) return picks.get(i);
        }
        return picks.get(picks.size() - 1);
    }

    // ------------------------------------------------------------------ search

    /** minimax with alpha-beta, jitter picks among near-best moves like a human. */
    private static Move naturalMove(Board root, List<Move> moves, GameSession session) {
        if (moves.isEmpty()) return null;
        GameSession.Persona p = session.persona;
        int depth = p == null ? 2 : Math.max(1, p.depth);
        int jitter = p == null ? 40 : p.jitter;
        Side me = root.getSideToMove();
        List<Move> candidates = new ArrayList<>();
        int bestScore = Integer.MIN_VALUE;
        for (Move m : moves) {
            root.doMove(m);
            int score = -negamax(root, depth - 1, Integer.MIN_VALUE / 2, Integer.MAX_VALUE / 2, me);
            root.undoMove();
            if (score > bestScore) {
                bestScore = score;
                candidates.clear();
                candidates.add(m);
            } else if (score == bestScore) {
                candidates.add(m);
            }
        }
        if (jitter > 0 && candidates.size() == 1) {
            List<Move> near = new ArrayList<>();
            for (Move m : moves) {
                root.doMove(m);
                int score = -negamax(root, Math.max(0, depth - 1),
                        Integer.MIN_VALUE / 2, Integer.MAX_VALUE / 2, me);
                root.undoMove();
                if (score >= bestScore - jitter) near.add(m);
            }
            if (!near.isEmpty()) candidates = near;
        }
        return candidates.get(RND.nextInt(candidates.size()));
    }

    /** negamax with alpha-beta; returns score from perspective of side to move on board. */
    private static int negamax(Board board, int depth, int alpha, int beta, Side me) {
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
            int score = -negamax(board, depth - 1, -beta, -alpha, me);
            board.undoMove();
            if (score > best) best = score;
            if (best > alpha) alpha = best;
            if (alpha >= beta) break;
        }
        return best;
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
