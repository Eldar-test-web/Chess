package com.eldar.chess;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.Handler;
import android.os.Looper;
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

/** Full-screen puzzle solver - the clean chess.com layout with coach bubble and controls. */
public class PuzzlesScreen {

    private static List<PuzzleData.Puzzle> queue = new ArrayList<>();
    private static int queueIndex = 0;
    static int sessionScore = 0;

    private static int[] sel = null;

    static void ensureQueue() {
        if (queue.isEmpty()) {
            queue.addAll(PuzzleData.all());
            Collections.shuffle(queue);
        }
    }

    static int progressIndex() {
        ensureQueue();
        return queueIndex % queue.size();
    }

    /** Current queue position clamped to the last puzzle (never wraps back to the start). */
    static int currentIndex() {
        ensureQueue();
        return Math.max(0, Math.min(queueIndex, queue.size() - 1));
    }

    /** True only once every puzzle in the queue has been solved. */
    static boolean allSolved() {
        ensureQueue();
        return queueIndex >= queue.size();
    }

    /** Move the queue to an already unlocked puzzle so a completed path tile can reopen it. */
    static void jumpTo(int index) {
        ensureQueue();
        if (queue.isEmpty()) return;
        queueIndex = Math.max(0, Math.min(queue.size() - 1, index));
    }

    static int totalPuzzles() {
        ensureQueue();
        return queue.size();
    }

    static void shuffleQueue() {
        ensureQueue();
        Collections.shuffle(queue);
        queueIndex = 0;
    }

    static void setTheme(String theme) {
        queue = new ArrayList<>();
        for (PuzzleData.Puzzle p : PuzzleData.all()) {
            if (theme == null || theme.equals(p.theme)) queue.add(p);
        }
        if (queue.isEmpty()) queue.addAll(PuzzleData.all());
        Collections.shuffle(queue);
        queueIndex = 0;
    }

    public static View build(final MainActivity main) {
        final Context c = main;
        sel = null;

        ensureQueue();
        final PuzzleData.Puzzle pz = queue.get(queueIndex % queue.size());
        final int number = queueIndex % queue.size();
        final int total = queue.size();

        LinearLayout root = Ui.linear(c, LinearLayout.VERTICAL);
        root.setBackgroundColor(Ui.BG);

        // ---- header: back, centered title, settings ----
        FrameLayout header = new FrameLayout(c);
        header.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(c, 50)));

        TextView back = Ui.glyph(c, "arrow_back", 24, Ui.WHITE);
        FrameLayout.LayoutParams blp = new FrameLayout.LayoutParams(
                Ui.dp(c, 46), Ui.dp(c, 46), Gravity.START | Gravity.CENTER_VERTICAL);
        blp.leftMargin = Ui.dp(c, 4);
        back.setLayoutParams(blp);
        back.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { main.navigate("puzzles"); }
        });
        header.addView(back);

        LinearLayout titleRow = Ui.linear(c, LinearLayout.HORIZONTAL);
        titleRow.setGravity(Gravity.CENTER_VERTICAL);
        TextView tIcon = Ui.glyph(c, "extension", 22, 0xFFC79A55);
        LinearLayout.LayoutParams ilp = new LinearLayout.LayoutParams(
                Ui.dp(c, 30), Ui.dp(c, 30));
        titleRow.addView(tIcon, ilp);
        TextView tTitle = Ui.text(c, "Puzzles", 18.5f, Ui.WHITE, true);
        tTitle.setIncludeFontPadding(false);
        LinearLayout.LayoutParams ttlp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ttlp.setMargins(Ui.dp(c, 4), 0, 0, 0);
        tTitle.setLayoutParams(ttlp);
        titleRow.addView(tTitle);
        FrameLayout.LayoutParams tlp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER);
        header.addView(titleRow, tlp);

        TextView gear = Ui.glyph(c, "settings", 22, 0xFFB9B4AC);
        FrameLayout.LayoutParams glp = new FrameLayout.LayoutParams(
                Ui.dp(c, 46), Ui.dp(c, 46), Gravity.END | Gravity.CENTER_VERTICAL);
        glp.rightMargin = Ui.dp(c, 4);
        gear.setLayoutParams(glp);
        gear.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { themePicker(main); }
        });
        header.addView(gear);
        root.addView(header);

        // ---- coach bubble ----
        LinearLayout coachRow = Ui.linear(c, LinearLayout.HORIZONTAL);
        coachRow.setGravity(Gravity.CENTER_VERTICAL);
        coachRow.setPadding(Ui.dp(c, 12), Ui.dp(c, 2), Ui.dp(c, 12), Ui.dp(c, 6));

        ImageView coach = new ImageView(c);
        coach.setImageDrawable(Ui.circleImage(c, R.drawable.avatar_coach_learn, 50));
        coachRow.addView(coach, new LinearLayout.LayoutParams(Ui.dp(c, 50), Ui.dp(c, 50)));

        LinearLayout bubble = Ui.linear(c, LinearLayout.VERTICAL);
        bubble.setBackground(Ui.round(0xFFFFFFFF, 10, c));
        bubble.setPadding(Ui.dp(c, 12), Ui.dp(c, 7), Ui.dp(c, 12), Ui.dp(c, 7));
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        bp.setMargins(Ui.dp(c, 8), 0, 0, 0);
        bubble.setLayoutParams(bp);

        LinearLayout moveRow = Ui.linear(c, LinearLayout.HORIZONTAL);
        moveRow.setGravity(Gravity.CENTER_VERTICAL);
        TextView box = Ui.glyph(c, "check_box_outline_blank", 15, 0xFF7FA650);
        moveRow.addView(box, new LinearLayout.LayoutParams(Ui.dp(c, 17), Ui.dp(c, 18)));
        moveRow.addView(Ui.text(c, "  White to Move", 14, 0xFF1E1D1B, true));
        bubble.addView(moveRow);
        LinearLayout.LayoutParams subLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        subLp.setMargins(0, Ui.dp(c, 2), 0, 0);
        TextView sub = Ui.text(c, "Find the best move for White. Theme: " + pz.theme + ".",
                11.5f, 0xFF5E5952, false);
        sub.setMaxLines(4);
        sub.setLineSpacing(Ui.dp(c, 0.5f), 1f);
        sub.setIncludeFontPadding(false);
        bubble.addView(sub, subLp);
        coachRow.addView(bubble);
        root.addView(coachRow);

        // ---- board, edge to edge like the original ----
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
                    if (correct && mated) {
                        main.state.puzzlesSolved++;
                        main.state.puzzleRating += 13;
                        main.state.save(main);
                        sessionScore += 13;
                        queueIndex++;
                        Toast.makeText(c, "Correct! +13", Toast.LENGTH_SHORT).show();
                        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                            @Override public void run() {
                                if ("puzzlesolve".equals(main.currentScreen())) {
                                    main.navigate("puzzlesolve");
                                }
                            }
                        }, 650);
                    } else if (m != null) {
                        Toast.makeText(c, "Keep looking - it's mate in one!",
                                Toast.LENGTH_SHORT).show();
                        main.state.puzzleRating = Math.max(100, main.state.puzzleRating - 8);
                        main.state.save(main);
                    }
                } else {
                    selectIfMovable(board, b, file, rank);
                }
            }
        });
        FrameLayout boardWrap = new FrameLayout(c);
        boardWrap.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));
        boardWrap.addView(board, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER));
        boardWrap.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override public void onLayoutChange(View v, int left, int top, int right, int bottom,
                                                 int oldLeft, int oldTop, int oldRight, int oldBottom) {
                int availW = right - left, availH = bottom - top;
                if (availW <= 0 || availH <= 0) return;
                int w = Math.min(availW, (int) ((availH - 1) / 1.0224f));
                FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) board.getLayoutParams();
                if (w > 0 && lp.width != w) {
                    lp.width = w;
                    lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                    board.setLayoutParams(lp);
                }
            }
        });
        root.addView(boardWrap);

        // ---- score strip ----
        LinearLayout strip = Ui.linear(c, LinearLayout.HORIZONTAL);
        strip.setGravity(Gravity.CENTER_VERTICAL);
        strip.setBackgroundColor(Ui.BG_DARK);
        strip.setPadding(Ui.dp(c, 14), Ui.dp(c, 7), Ui.dp(c, 14), Ui.dp(c, 6));

        LinearLayout scoreCol = Ui.linear(c, LinearLayout.VERTICAL);
        scoreCol.addView(Ui.text(c, "Score", 10, 0xFFA8A29A, true));
        scoreCol.addView(Ui.text(c, String.valueOf(sessionScore), 20, Ui.WHITE, true));
        strip.addView(scoreCol);
        strip.addView(Ui.spacerWeight(c, 1f));

        LinearLayout timeCol = Ui.linear(c, LinearLayout.HORIZONTAL);
        timeCol.setGravity(Gravity.CENTER_VERTICAL);
        timeCol.addView(Ui.glyph(c, "schedule", 16, 0xFFA8A29A));
        final TextView timer = Ui.text(c, "  0:00", 16, Ui.WHITE, true);
        timeCol.addView(timer);
        strip.addView(timeCol);
        strip.addView(Ui.spacerWeight(c, 1f));

        LinearLayout numberChip = Ui.linear(c, LinearLayout.HORIZONTAL);
        numberChip.setGravity(Gravity.CENTER_VERTICAL);
        numberChip.setBackground(Ui.round(0xFF6E4C29, 7, c));
        numberChip.setPadding(Ui.dp(c, 8), Ui.dp(c, 4), Ui.dp(c, 9), Ui.dp(c, 5));
        numberChip.addView(Ui.glyph(c, "extension", 13, 0xFFF0DCA9));
        numberChip.addView(Ui.text(c, "  " + (number + 1), 12.5f, Ui.WHITE, true));
        strip.addView(numberChip);
        root.addView(strip);

        root.addView(progressBar(c, (number + 1f) / total));

        // ---- controls: Hint / Back / Forward ----
        LinearLayout controls = Ui.linear(c, LinearLayout.HORIZONTAL);
        controls.setBackgroundColor(Ui.BG_DARK);
        controls.setPadding(Ui.dp(c, 10), Ui.dp(c, 7), Ui.dp(c, 10), Ui.dp(c, 8));
        LinearLayout.LayoutParams cLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(c, 52), 0f);
        controls.setLayoutParams(cLp);

        controls.addView(control(c, "lightbulb", "Hint", false, new View.OnClickListener() {
            @Override public void onClick(View v) {
                hint(c, board, pz);
            }
        }), controlLp(c, 1f, 0));
        controls.addView(control(c, "chevron_left", "Back", false, new View.OnClickListener() {
            @Override public void onClick(View v) { main.navigate("puzzles"); }
        }), controlLp(c, 1f, 5));
        controls.addView(control(c, "chevron_right", "Forward", true, new View.OnClickListener() {
            @Override public void onClick(View v) {
                queueIndex++;
                main.navigate("puzzlesolve");
            }
        }), controlLp(c, 1f, 5));
        root.addView(controls);

        // live timer
        final Handler handler = new Handler(Looper.getMainLooper());
        final int[] secs = {0};
        Runnable tick = new Runnable() {
            @Override public void run() {
                if (!timer.isAttachedToWindow()) return;
                secs[0]++;
                timer.setText("  " + (secs[0] / 60) + ":" + String.format("%02d", secs[0] % 60));
                handler.postDelayed(this, 1000);
            }
        };
        handler.postDelayed(tick, 1000);

        return root;
    }

    private static LinearLayout.LayoutParams controlLp(Context c, float weight, int leftMargin) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.MATCH_PARENT, weight);
        lp.setMargins(Ui.dp(c, leftMargin), 0, 0, 0);
        return lp;
    }

    private static View control(Context c, String glyph, String label, boolean primary,
                                View.OnClickListener click) {
        LinearLayout b = Ui.linear(c, LinearLayout.HORIZONTAL);
        b.setGravity(Gravity.CENTER);
        b.setBackground(primary ? Ui.greenFill(c, 10) : Ui.round(0xFF3B3935, 10, c));
        TextView g = Ui.glyph(c, glyph, 18, primary ? Ui.WHITE : 0xFFD7D2CA);
        b.addView(g);
        TextView t = Ui.text(c, label, 12, primary ? Ui.WHITE : 0xFFD7D2CA, true);
        t.setSingleLine(true);
        t.setMaxLines(1);
        LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tp.setMargins(Ui.dp(c, 4), 0, 0, 0);
        t.setLayoutParams(tp);
        b.addView(t);
        b.setOnClickListener(click);
        return b;
    }

    private static View progressBar(Context c, float fraction) {
        final float f = Math.max(0f, Math.min(1f, fraction));
        View v = new View(c) {
            @Override protected void onDraw(Canvas canvas) {
                Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
                p.setColor(0xFF3B3935);
                canvas.drawRect(0, 0, getWidth(), getHeight(), p);
                if (f > 0) {
                    p.setShader(new LinearGradient(0, 0, getWidth() * f, 0,
                            0xFFBFE27A, 0xFF86B24E, Shader.TileMode.CLAMP));
                    canvas.drawRect(0, 0, getWidth() * f, getHeight(), p);
                }
            }
        };
        v.setWillNotDraw(false);
        v.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(c, 5)));
        return v;
    }

    private static void themePicker(final MainActivity main) {
        final String[] names = {"Green", "Wood", "Blue", "Grey"};
        new AlertDialog.Builder(main)
                .setTitle("Board Theme")
                .setItems(names, new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface d, int which) {
                        AppState.theme = which;
                        main.state.save(main);
                        main.navigate("puzzlesolve");
                    }
                })
                .setNegativeButton("Close", null)
                .show();
    }

    private static void hint(Context c, BoardView board, PuzzleData.Puzzle pz) {
        try {
            Board b = new Board();
            b.loadFromFen(pz.fen);
            Square f = Square.valueOf(pz.from);
            com.github.bhlangonijr.chesslib.Piece p = b.getPiece(f);
            if (p == com.github.bhlangonijr.chesslib.Piece.NONE) return;
            Set<Integer> targets = new HashSet<>();
            for (Move m : b.legalMoves()) {
                if (m.getFrom() == f) {
                    targets.add(m.getTo().getFile().ordinal() * 8
                            + m.getTo().getRank().ordinal());
                }
            }
            sel = new int[]{f.getFile().ordinal(), f.getRank().ordinal()};
            board.setSelection(sel, targets);
            String type = p.getPieceType().name().toLowerCase()
                    .replace("white_", "").replace("black_", "");
            Toast.makeText(c, "Hint: your " + type + " can finish this.",
                    Toast.LENGTH_SHORT).show();
        } catch (Exception ignored) { }
    }

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
}
