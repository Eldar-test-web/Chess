package com.eldar.chess;

import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Side;
import com.github.bhlangonijr.chesslib.Square;
import com.github.bhlangonijr.chesslib.move.Move;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Puzzles screen: mate-in-1 solver (consistent dark style). */
public class PuzzlesScreen {

    private static List<PuzzleData.Puzzle> queue = null;
    private static int queueIndex = 0;

    public static View build(final MainActivity main) {
        final android.content.Context c = main;

        LinearLayout root = Ui.linear(c, LinearLayout.VERTICAL);
        root.setBackgroundColor(Ui.BG);

        LinearLayout top = Ui.linear(c, LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER);
        top.setPadding(0, Ui.dp(c, 12), 0, Ui.dp(c, 8));
        ImageView icon = new ImageView(c);
        icon.setImageResource(R.drawable.icon_puzzle_green2);
        icon.setLayoutParams(new LinearLayout.LayoutParams(Ui.dp(c, 26), Ui.dp(c, 30)));
        top.addView(icon);
        top.addView(Ui.text(c, "  Puzzles", 17, Ui.WHITE, true));
        root.addView(top);

        LinearLayout info = Ui.linear(c, LinearLayout.HORIZONTAL);
        info.setPadding(Ui.dp(c, 12), Ui.dp(c, 2), Ui.dp(c, 12), Ui.dp(c, 6));
        final TextView rating = Ui.text(c, "Puzzle rating: " + main.state.puzzleRating, 13,
                Ui.GREY, true);
        info.addView(rating);
        Ui.spacerWeight(c, 1f);
        TextView solved = Ui.text(c, "Solved: " + main.state.puzzlesSolved, 13, Ui.GREY, false);
        info.addView(solved);
        root.addView(info);

        if (queue == null) {
            queue = new ArrayList<>(PuzzleData.all());
            Collections.shuffle(queue);
            queueIndex = 0;
        }
        if (queue.isEmpty()) {
            queue = new ArrayList<>(PuzzleData.all());
            Collections.shuffle(queue);
            queueIndex = 0;
        }
        final PuzzleData.Puzzle pz = queue.get(queueIndex % queue.size());

        final BoardView board = new BoardView(c);
        board.setState(pz.fen, null);
        board.setListener(new BoardView.Listener() {
            @Override public void onSquareTapped(int file, int rank) {
                Board b = new Board();
                b.loadFromFen(pz.fen);
                String sq = String.valueOf((char) ('A' + file)) + (rank + 1);
                if (sel != null) {
                    String from = String.valueOf((char) ('A' + sel[0])) + (sel[1] + 1);
                    boolean correct = sq.equals(pz.to) && from.equals(pz.from);
                    Move m = findMove(b, from, sq);
                    sel = null;
                    board.setSelection(null, null);
                    if (m != null) b.doMove(m);
                    if (correct && b.isMated()) {
                        main.state.puzzlesSolved++;
                        main.state.puzzleRating += 13;
                        main.state.save(main);
                        queueIndex++;
                        Toast.makeText(c, "Correct! +13", Toast.LENGTH_SHORT).show();
                        main.navigate("puzzles");
                    } else if (m != null) {
                        Toast.makeText(c, "Keep looking - it's mate in one!",
                                Toast.LENGTH_SHORT).show();
                        main.state.puzzleRating = Math.max(100, main.state.puzzleRating - 8);
                        main.state.save(main);
                        rating.setText("Puzzle rating: " + main.state.puzzleRating);
                    }
                } else {
                    selectIfMovable(board, b, file, rank, pz);
                }
            }
        });

        final Board[] stateRef = new Board[]{new Board()};
        stateRef[0].loadFromFen(pz.fen);

        return puzzleLayout(main, board, pz, rating);
    }

    private static int[] sel = null;

    private static void selectIfMovable(BoardView board, Board b, int file, int rank,
                                        PuzzleData.Puzzle pz) {
        String sq = String.valueOf((char) ('A' + file)) + (rank + 1);
        try {
            Square f = Square.valueOf(sq);
            com.github.bhlangonijr.chesslib.Piece p = b.getPiece(f);
            if (p != com.github.bhlangonijr.chesslib.Piece.NONE
                    && p.getPieceSide() == Side.WHITE) {
                Set<Integer> targets = new HashSet<>();
                for (Move m : b.legalMoves()) {
                    if (m.getFrom() == f) {
                        targets.add(m.getTo().getFile().ordinal() * 8
                                + m.getTo().getRank().ordinal());
                    }
                }
                sel = new int[]{file, rank};
                board.setSelection(sel, targets);
            }
        } catch (Exception ignored) { }
    }

    private static void deselect() {
        sel = null;
    }

    private static Move findMove(Board b, String from, String to) {
        try {
            Square f = Square.valueOf(from), t = Square.valueOf(to);
            for (Move m : b.legalMoves()) {
                if (m.getFrom() == f && m.getTo() == t
                        && (m.getPromotion() == null || m.getPromotion()
                        == com.github.bhlangonijr.chesslib.Piece.NONE
                        || m.getPromotion() == com.github.bhlangonijr.chesslib.Piece.WHITE_QUEEN)) {
                    return m;
                }
            }
        } catch (Exception ignored) { }
        return null;
    }

    private static View puzzleLayout(final MainActivity main, final BoardView board,
                                     final PuzzleData.Puzzle pz, TextView rating) {
        final android.content.Context c = main;
        LinearLayout root = (LinearLayout) ((ViewGroup) rating.getParent()).getParent();

        board.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));
        root.addView(board, 2);

        LinearLayout bottom = Ui.linear(c, LinearLayout.VERTICAL);
        bottom.setBackgroundColor(0xFF1C1B18);
        bottom.setPadding(Ui.dp(c, 12), Ui.dp(c, 8), Ui.dp(c, 12), Ui.dp(c, 10));

        TextView title = Ui.text(c, "Mate in 1 - find the best move!", 14, Ui.WHITE, true);
        bottom.addView(title);

        TextView solve = Ui.text(c, "Show Solution", 15, Ui.WHITE, true);
        solve.setGravity(Gravity.CENTER);
        solve.setBackground(Ui.round(Ui.GREEN, 10, c));
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(c, 42));
        sp.setMargins(0, Ui.dp(c, 8), 0, 0);
        solve.setLayoutParams(sp);
        solve.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                Toast.makeText(c, "Solution: " + pz.from.toLowerCase() + pz.to.toLowerCase()
                        + " (mate)", Toast.LENGTH_LONG).show();
            }
        });
        bottom.addView(solve);
        root.addView(bottom);
        return root;
    }
}
