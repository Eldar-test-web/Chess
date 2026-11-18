package com.eldar.chess;

import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
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

/** Puzzles screen: 16 verified mate-in-1 puzzles with coach hints. */
public class PuzzlesScreen {

    private static List<PuzzleData.Puzzle> queue = null;
    private static int queueIndex = 0;

    public static View build(final MainActivity main) {
        final android.content.Context c = main;
        sel = null;

        LinearLayout root = Ui.linear(c, LinearLayout.VERTICAL);
        root.setBackgroundColor(Ui.BG);

        LinearLayout top = Ui.linear(c, LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER);
        top.setPadding(0, Ui.dp(c, 8), 0, Ui.dp(c, 4));
        TextView icon = Ui.glyph(c, "extension", 30, Ui.GREEN);
        icon.setLayoutParams(new LinearLayout.LayoutParams(Ui.dp(c, 30), Ui.dp(c, 34)));
        top.addView(icon);
        top.addView(Ui.text(c, "  Puzzles", 21, Ui.WHITE, true));
        root.addView(top);

        LinearLayout info = Ui.linear(c, LinearLayout.HORIZONTAL);
        info.setGravity(Gravity.CENTER_VERTICAL);
        info.setPadding(Ui.dp(c, 14), 0, Ui.dp(c, 14), Ui.dp(c, 2));
        final TextView rating = Ui.text(c, "Puzzle rating: " + main.state.puzzleRating, 15,
                Ui.GREY, true);
        info.addView(rating);
        info.addView(Ui.spacerWeight(c, 1f));
        TextView solved = Ui.text(c, "Solved: " + main.state.puzzlesSolved, 15, Ui.GREY, false);
        info.addView(solved);
        root.addView(info, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        if (queue == null || queue.isEmpty()) {
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
                    boolean mated = false;
                    if (correct && m != null) {
                        try { mated = b.isMated(); } catch (Exception ignored) { }
                    }
                    if (mated) {
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
                    selectIfMovable(board, b, file, rank);
                }
            }
        });

        return puzzleLayout(main, board, pz, queueIndex % queue.size(), queue.size());
    }

    private static int[] sel = null;

    private static void selectIfMovable(BoardView board, Board b, int file, int rank) {
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
                                     final PuzzleData.Puzzle pz, final int number,
                                     final int total) {
        final android.content.Context c = main;
        LinearLayout root = Ui.linear(c, LinearLayout.VERTICAL);
        root.setBackgroundColor(Ui.BG);

        LinearLayout top = Ui.linear(c, LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER);
        top.setPadding(0, Ui.dp(c, 8), 0, Ui.dp(c, 4));
        TextView icon = Ui.glyph(c, "extension", 30, Ui.GREEN);
        icon.setLayoutParams(new LinearLayout.LayoutParams(Ui.dp(c, 30), Ui.dp(c, 34)));
        top.addView(icon);
        top.addView(Ui.text(c, "  Puzzles", 21, Ui.WHITE, true));
        root.addView(top);

        LinearLayout info = Ui.linear(c, LinearLayout.HORIZONTAL);
        info.setGravity(Gravity.CENTER_VERTICAL);
        info.setPadding(Ui.dp(c, 14), 0, Ui.dp(c, 14), Ui.dp(c, 2));
        final TextView rating = Ui.text(c, "Puzzle rating: " + main.state.puzzleRating, 15,
                Ui.GREY, true);
        info.addView(rating);
        info.addView(Ui.spacerWeight(c, 1f));
        info.addView(Ui.text(c, "Solved: " + main.state.puzzlesSolved, 15, Ui.GREY, false));
        root.addView(info);

        // coach bubble
        LinearLayout coachRow = Ui.linear(c, LinearLayout.HORIZONTAL);
        coachRow.setGravity(Gravity.CENTER_VERTICAL);
        coachRow.setPadding(Ui.dp(c, 12), 0, Ui.dp(c, 12), Ui.dp(c, 4));
        ImageView coach = new ImageView(c);
        coach.setImageResource(R.drawable.avatar_coach_learn);
        coach.setScaleType(ImageView.ScaleType.FIT_XY);
        coachRow.addView(coach, new LinearLayout.LayoutParams(
                Ui.dp(c, 34), Ui.dp(c, 40)));
        TextView bubble = Ui.text(c, "This one is mate in 1 - find the best move!",
                12, 0xFF1F1F1F, false);
        bubble.setBackground(Ui.round(0xFFFFFFFF, 8, c));
        bubble.setPadding(Ui.dp(c, 12), Ui.dp(c, 8), Ui.dp(c, 12), Ui.dp(c, 8));
        bubble.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        bp.setMargins(Ui.dp(c, 8), 0, 0, 0);
        coachRow.addView(bubble, bp);
        root.addView(coachRow);

        FrameLayout boardWrap = new FrameLayout(c);
        boardWrap.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));
        boardWrap.addView(board, new FrameLayout.LayoutParams(
                Ui.dp(c, 336), ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER));
        root.addView(boardWrap);

        LinearLayout bottom = Ui.linear(c, LinearLayout.VERTICAL);
        bottom.setBackgroundColor(0xFF1C1B18);
        bottom.setPadding(Ui.dp(c, 12), Ui.dp(c, 8), Ui.dp(c, 12), Ui.dp(c, 8));

        bottom.addView(Ui.text(c, "Puzzle " + (number + 1) + "/" + total + " - "
                + pz.theme, 15.5f, Ui.WHITE, true));
        LinearLayout meta = Ui.linear(c, LinearLayout.HORIZONTAL);
        meta.setGravity(Gravity.CENTER_VERTICAL);
        meta.addView(Ui.text(c, "Mate in 1  •  Rating " + pz.rating, 11.5f,
                0xFFA19D96, false));
        meta.addView(Ui.spacerWeight(c, 1f));
        TextView skip = Ui.text(c, "Next Puzzle  ▸", 13, 0xFFA19D96, true);
        skip.setPadding(Ui.dp(c, 10), Ui.dp(c, 6), 0, Ui.dp(c, 4));
        skip.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                queueIndex++;
                main.navigate("puzzles");
            }
        });
        meta.addView(skip);
        bottom.addView(meta);

        TextView solve = Ui.text(c, "Show Solution", 17, Ui.WHITE, true);
        solve.setGravity(Gravity.CENTER);
        solve.setBackground(Ui.greenFill(c, 8));
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(c, 44));
        sp.setMargins(0, Ui.dp(c, 8), 0, 0);
        solve.setLayoutParams(sp);
        solve.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                Toast.makeText(c, "Solution: " + pz.from.toLowerCase()
                        + pz.to.toLowerCase() + " (mate)", Toast.LENGTH_LONG).show();
            }
        });
        bottom.addView(solve);

        root.addView(bottom);
        return root;
    }
}
