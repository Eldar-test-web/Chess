package com.eldar.chess;

import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

/** Learn screen - lesson path, 1:1 with screenshot 072013. */
public class LearnScreen {

    static class Lesson {
        String title; String glyph; int pieceRes;
        Lesson(String title, String glyph, int pieceRes) {
            this.title = title; this.glyph = glyph; this.pieceRes = pieceRes;
        }
    }

    private static final Lesson[] LESSONS = {
            new Lesson("How the King moves", "♔", R.drawable.piece3d_knight),
            new Lesson("How the Knight moves", "♞", R.drawable.piece3d_knight),
            new Lesson("How the Rook moves", "♜", R.drawable.piece3d_rook),
            new Lesson("How the Bishop moves", "♝", R.drawable.piece3d_bishop),
            new Lesson("How the Queen moves", "♛", R.drawable.piece3d_queen),
            new Lesson("How the Pawn moves", "♟", R.drawable.piece3d_pawn),
            new Lesson("Capturing pieces", "✕", 0),
            new Lesson("Check and Checkmate", "✕", 0),
    };

    private static final int LOCKED = 0xFF1E4A50;
    private static final int CURRENT = 0xFF3BA99C;
    private static final int DONE = 0xFF2A7A86;

    public static View build(final MainActivity main) {
        final android.content.Context c = main;

        LinearLayout root = Ui.linear(c, LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFF211F1C);

        // ---- title ----
        LinearLayout top = Ui.linear(c, LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER);
        top.setPadding(0, Ui.dp(c, 12), 0, Ui.dp(c, 8));
        ImageView cap = new ImageView(c);
        cap.setImageResource(R.drawable.icon_learn_title);
        cap.setLayoutParams(new LinearLayout.LayoutParams(Ui.dp(c, 26), Ui.dp(c, 22)));
        top.addView(cap);
        top.addView(Ui.text(c, "  Learn", 17, Ui.WHITE, true));
        root.addView(top);

        // ---- speech bubble ----
        LinearLayout speech = Ui.linear(c, LinearLayout.HORIZONTAL);
        speech.setPadding(Ui.dp(c, 8), Ui.dp(c, 4), Ui.dp(c, 8), Ui.dp(c, 6));
        ImageView av = new ImageView(c);
        av.setImageResource(R.drawable.avatar_coach_bubble);
        av.setScaleType(ImageView.ScaleType.FIT_CENTER);
        speech.addView(av, new LinearLayout.LayoutParams(Ui.dp(c, 44), Ui.dp(c, 48)));
        final TextView bubble = Ui.text(c, bubbleText(main.state.learnProgress), 13,
                0xFF3B3935, false);
        bubble.setBackground(Ui.round(0xFFFFFFFF, 10, c));
        bubble.setPadding(Ui.dp(c, 10), Ui.dp(c, 8), Ui.dp(c, 10), Ui.dp(c, 8));
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        bp.setMargins(Ui.dp(c, 6), 0, 0, 0);
        bubble.setLayoutParams(bp);
        speech.addView(bubble);
        root.addView(speech);

        // ---- path ----
        ScrollView scroll = new ScrollView(c);
        scroll.setBackgroundColor(0xFF262421);
        scroll.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        LinearLayout path = Ui.linear(c, LinearLayout.VERTICAL);
        path.setPadding(Ui.dp(c, 16), Ui.dp(c, 16), Ui.dp(c, 16), Ui.dp(c, 24));
        path.setGravity(Gravity.CENTER_HORIZONTAL);
        scroll.addView(path, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        int progress = main.state.learnProgress;
        // build bottom-to-top: current lesson at bottom, later lessons above
        for (int i = LESSONS.length - 1; i >= 0; i--) {
            final int idx = i;
            boolean done = idx < progress;
            boolean current = idx == progress;
            Lesson l = LESSONS[idx];

            LinearLayout row = Ui.linear(c, LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER);
            row.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(c, 96)));

            boolean left = (LESSONS.length - 1 - i) % 2 == 0; // zigzag
            Ui.spacerWeight(c, 1f);
            LinearLayout stack = Ui.linear(c, LinearLayout.VERTICAL);
            stack.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(
                    0, Ui.dp(c, 96), 2f);
            stack.setLayoutParams(sp);

            FrameLayout diamondWrap = new FrameLayout(c);
            View diamond = new View(c);
            diamond.setBackground(Ui.round(current ? CURRENT : done ? DONE : LOCKED, 8, c));
            diamond.setRotation(45f);
            diamondWrap.addView(diamond, new FrameLayout.LayoutParams(
                    Ui.dp(c, 52), Ui.dp(c, 52)));

            TextView glyph = Ui.text(c, l.glyph, 22, current ? Ui.WHITE : 0xFF8FB7BC, true);
            glyph.setGravity(Gravity.CENTER);
            diamondWrap.addView(glyph, new FrameLayout.LayoutParams(
                    Ui.dp(c, 52), Ui.dp(c, 52), Gravity.CENTER));

            if (current) {
                TextView next = Ui.text(c, "Next", 12, Ui.WHITE, true);
                next.setGravity(Gravity.CENTER);
                next.setBackground(Ui.round(0xFF1C1B18, 6, c));
                next.setPadding(Ui.dp(c, 10), Ui.dp(c, 4), Ui.dp(c, 10), Ui.dp(c, 4));
                FrameLayout.LayoutParams np = new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                        Gravity.CENTER_HORIZONTAL | Gravity.TOP);
                np.setMargins(0, Ui.dp(c, 0), 0, 0);
                next.setLayoutParams(np);
                diamondWrap.addView(next);
                next.setTranslationY(-Ui.dp(c, 14));
            }
            stack.addView(diamondWrap, new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            row.addView(stack);

            if (l.pieceRes != 0) {
                ImageView piece = new ImageView(c);
                piece.setImageResource(l.pieceRes);
                piece.setLayoutParams(new LinearLayout.LayoutParams(
                        Ui.dp(c, 60), Ui.dp(c, 84)));
                row.addView(piece);
                Ui.spacerWeight(c, 1f);
            } else {
                Ui.spacerWeight(c, 2f);
            }
            path.addView(row);
        }

        root.addView(scroll);

        // ---- bottom fixed section ----
        LinearLayout bottom = Ui.linear(c, LinearLayout.VERTICAL);
        bottom.setBackgroundColor(0xFF1C1B18);
        bottom.setPadding(Ui.dp(c, 12), Ui.dp(c, 8), Ui.dp(c, 12), Ui.dp(c, 10));
        bottom.addView(Ui.text(c, "Learn To Play", 16, Ui.WHITE, true));

        LinearLayout actions = Ui.linear(c, LinearLayout.HORIZONTAL);
        actions.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams ap = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ap.setMargins(0, Ui.dp(c, 8), 0, 0);
        actions.setLayoutParams(ap);

        ImageView listBtn = Ui.img(c, R.drawable.ic_list_options, 20);
        listBtn.setColorFilter(0xFFB5B0A8);
        listBtn.setBackground(Ui.round(0xFF3B3935, 8, c));
        listBtn.setPadding(Ui.dp(c, 12), Ui.dp(c, 12), Ui.dp(c, 12), Ui.dp(c, 12));
        listBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                Toast.makeText(c, "Lessons: King, Knight, Rook, Bishop, Queen, Pawn",
                        Toast.LENGTH_SHORT).show();
            }
        });
        actions.addView(listBtn);

        TextView nextLesson = Ui.text(c, "Next Lesson", 16, Ui.WHITE, true);
        nextLesson.setGravity(Gravity.CENTER);
        nextLesson.setBackground(Ui.round(Ui.GREEN, 10, c));
        LinearLayout.LayoutParams nlp = new LinearLayout.LayoutParams(
                0, Ui.dp(c, 44), 1f);
        nlp.setMargins(Ui.dp(c, 10), 0, 0, 0);
        nextLesson.setLayoutParams(nlp);
        nextLesson.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (main.state.learnProgress < LESSONS.length) {
                    main.state.learnProgress++;
                } else {
                    main.state.learnProgress = 0;
                    Toast.makeText(c, "All lessons complete! Starting again.", Toast.LENGTH_SHORT).show();
                }
                main.state.save(main);
                main.navigate("learn");
            }
        });
        actions.addView(nextLesson);
        bottom.addView(actions);
        root.addView(bottom);

        return root;
    }

    private static String bubbleText(int progress) {
        switch (progress) {
            case 0: return "Learn how to move the king, the most important piece in chess.";
            case 1: return "Knights move in an L shape - over other pieces too!";
            case 2: return "Rooks move in straight lines - rows and columns.";
            case 3: return "Bishops move diagonally - each stays on its own color.";
            case 4: return "The queen is the most powerful piece - she does it all!";
            case 5: return "Pawns move forward one, but capture diagonally.";
            case 6: return "Capture an enemy piece by moving onto its square.";
            default: return "Check! Attack the king so he cannot escape.";
        }
    }
}
