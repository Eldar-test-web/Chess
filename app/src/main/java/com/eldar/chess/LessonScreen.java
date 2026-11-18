package com.eldar.chess;

import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Piece;
import com.github.bhlangonijr.chesslib.Side;
import com.github.bhlangonijr.chesslib.Square;
import com.github.bhlangonijr.chesslib.move.Move;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Playable "Learn To Play" lesson: guided single-move task on a real board. */
public class LessonScreen {

    private static int index = 0;
    private static int[] sel = null;

    public static void setIndex(int i) { index = i; }

    public static View build(final MainActivity main) {
        final android.content.Context c = main;
        final List<LessonData.Lesson> lessons = LessonData.all();
        final int idx = Math.max(0, Math.min(lessons.size() - 1, index));
        final LessonData.Lesson lesson = lessons.get(idx);
        sel = null;

        LinearLayout root = Ui.linear(c, LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFF262421);
        LinearLayout content = Ui.linear(c, LinearLayout.VERTICAL);

        // ---- header ----
        LinearLayout top = Ui.linear(c, LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);
        top.setPadding(Ui.dp(c, 12), Ui.dp(c, 10), Ui.dp(c, 12), Ui.dp(c, 6));
        TextView back = Ui.glyph(c, "arrow_back", 22, Ui.WHITE);
        back.setGravity(Gravity.CENTER);
        back.setBackground(Ui.round(0xFF383A38, 9, c));
        back.setLayoutParams(new LinearLayout.LayoutParams(Ui.dp(c, 38), Ui.dp(c, 38)));
        back.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { main.navigate("learn"); }
        });
        top.addView(back);
        TextView title = Ui.text(c, lesson.title, 19, Ui.WHITE, true);
        LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        tp.setMargins(Ui.dp(c, 12), 0, 0, 0);
        title.setLayoutParams(tp);
        top.addView(title);
        top.addView(Ui.text(c, "Lesson " + (idx + 1) + "/" + lessons.size(), 13,
                0xFFA19D96, false));
        content.addView(top);

        // ---- coach bubble ----
        LinearLayout coachRow = Ui.linear(c, LinearLayout.HORIZONTAL);
        coachRow.setGravity(Gravity.CENTER_VERTICAL);
        coachRow.setPadding(Ui.dp(c, 12), Ui.dp(c, 4), Ui.dp(c, 12), Ui.dp(c, 8));
        ImageView coach = new ImageView(c);
        coach.setImageResource(R.drawable.avatar_coach_learn);
        coach.setScaleType(ImageView.ScaleType.FIT_XY);
        coachRow.addView(coach, new LinearLayout.LayoutParams(
                Ui.dp(c, 42), Ui.dp(c, 49)));
        TextView bubble = Ui.text(c, lesson.text, 12, 0xFF1F1F1F, false);
        bubble.setBackground(Ui.round(0xFFFFFFFF, 8, c));
        bubble.setPadding(Ui.dp(c, 12), Ui.dp(c, 8), Ui.dp(c, 12), Ui.dp(c, 8));
        bubble.setMaxLines(4);
        bubble.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        bp.setMargins(Ui.dp(c, 8), 0, 0, 0);
        coachRow.addView(bubble, bp);
        content.addView(coachRow);

        // ---- board ----
        final BoardView board = new BoardView(c);
        board.setState(lesson.fen, null);
        board.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        content.addView(board);

        // visual hint: dot the legal targets of the lesson piece
        hintTargets(board, lesson);

        // ---- bottom bar ----
        LinearLayout bottom = Ui.linear(c, LinearLayout.VERTICAL);
        bottom.setBackgroundColor(0xFF1C1B18);
        bottom.setPadding(Ui.dp(c, 12), Ui.dp(c, 10), Ui.dp(c, 12), Ui.dp(c, 12));

        final TextView status = Ui.text(c,
                "Tap your piece, then tap the square with the dot.", 13.5f, 0xFFA19D96, false);
        status.setGravity(Gravity.CENTER);
        bottom.addView(status);

        final TextView solution = Ui.text(c, "Show Solution", 13.5f, 0xFF8A8780, true);
        solution.setGravity(Gravity.CENTER);
        solution.setPadding(0, Ui.dp(c, 10), 0, Ui.dp(c, 6));
        solution.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                Toast.makeText(c, "Solution: " + lesson.from.toLowerCase() + " - "
                        + lesson.to.toLowerCase(), Toast.LENGTH_LONG).show();
            }
        });
        bottom.addView(solution);

        final boolean[] solved = {false};
        final TextView next = Ui.text(c, "Solve the move to continue", 16, 0xFF8A8780, true);
        next.setGravity(Gravity.CENTER);
        next.setBackground(Ui.round(0xFF33312E, 8, c));
        next.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(c, 52)));
        next.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (!solved[0]) {
                    Toast.makeText(c, "Solve the move first - tap Show Solution if stuck.",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                if (idx + 1 < lessons.size()) {
                    LessonScreen.index = idx + 1;
                    main.state.learnProgress = Math.max(main.state.learnProgress, idx + 1);
                    main.state.save(main);
                    main.navigate("lesson");
                } else {
                    main.state.learnProgress = lessons.size();
                    main.state.save(main);
                    Toast.makeText(c, "All 16 lessons complete!", Toast.LENGTH_LONG).show();
                    main.navigate("learn");
                }
            }
        });
        bottom.addView(next);
        ScrollView scroll = new ScrollView(c);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(0xFF262421);
        scroll.addView(content, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        root.addView(scroll, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));
        root.addView(bottom);

        board.setListener(new BoardView.Listener() {
            @Override public void onSquareTapped(int file, int rank) {
                if (solved[0]) return;
                String sq = String.valueOf((char) ('A' + file)) + (rank + 1);
                Board b = new Board();
                b.loadFromFen(lesson.fen);
                if (sel != null) {
                    String from = String.valueOf((char) ('A' + sel[0])) + (sel[1] + 1);
                    sel = null;
                    board.setSelection(null, null);
                    if (from.equals(lesson.from) && sq.equals(lesson.to)) {
                        Move m = findMove(b, from, sq);
                        if (m != null) {
                            b.doMove(m);
                            int ff = Square.valueOf(from).getFile().ordinal();
                            int fr = Square.valueOf(from).getRank().ordinal();
                            int tf = Square.valueOf(sq).getFile().ordinal();
                            int tr = Square.valueOf(sq).getRank().ordinal();
                            board.setState(b.getFen(), new int[]{ff, fr, tf, tr});
                            solved[0] = true;
                            main.state.learnProgress = Math.max(main.state.learnProgress,
                                    idx + 1);
                            main.state.save(main);
                            status.setText("Correct! " + lesson.title + " complete.");
                            status.setTextColor(0xFF81B64C);
                            next.setText("Next Lesson");
                            next.setBackground(Ui.greenFill(c, 8));
                            next.setTextColor(Ui.WHITE);
                            return;
                        }
                    }
                    if (!selectIfMovable(board, b, file, rank)) {
                        status.setText("Not quite - try " + lesson.from.toLowerCase()
                                + " " + lesson.to.toLowerCase() + ".");
                    }
                } else {
                    selectIfMovable(board, b, file, rank);
                }
            }
        });

        return root;
    }

    private static void hintTargets(BoardView board, LessonData.Lesson lesson) {
        try {
            Board b = new Board();
            b.loadFromFen(lesson.fen);
            Square f = Square.valueOf(lesson.from);
            int ff = f.getFile().ordinal(), fr = f.getRank().ordinal();
            Set<Integer> targets = new HashSet<>();
            for (Move m : b.legalMoves()) {
                if (m.getFrom() == f) {
                    targets.add(m.getTo().getFile().ordinal() * 8
                            + m.getTo().getRank().ordinal());
                }
            }
            board.setSelection(new int[]{ff, fr}, targets);
        } catch (Exception ignored) { }
    }

    private static boolean selectIfMovable(BoardView board, Board b, int file, int rank) {
        String sq = String.valueOf((char) ('A' + file)) + (rank + 1);
        try {
            Square f = Square.valueOf(sq);
            Piece p = b.getPiece(f);
            if (p != Piece.NONE && p.getPieceSide() == Side.WHITE) {
                Set<Integer> targets = new HashSet<>();
                for (Move m : b.legalMoves()) {
                    if (m.getFrom() == f) {
                        targets.add(m.getTo().getFile().ordinal() * 8
                                + m.getTo().getRank().ordinal());
                    }
                }
                sel = new int[]{file, rank};
                board.setSelection(sel, targets);
                return true;
            }
        } catch (Exception ignored) { }
        return false;
    }

    private static Move findMove(Board b, String from, String to) {
        try {
            Square f = Square.valueOf(from), t = Square.valueOf(to);
            for (Move m : b.legalMoves()) {
                if (m.getFrom() == f && m.getTo() == t
                        && (m.getPromotion() == null || m.getPromotion() == Piece.NONE
                        || m.getPromotion() == Piece.WHITE_QUEEN)) {
                    return m;
                }
            }
        } catch (Exception ignored) { }
        return null;
    }
}
