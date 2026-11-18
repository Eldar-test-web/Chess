package com.eldar.chess;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/** Learn screen - scrollable lesson path with large touch targets. */
public class LearnScreen {

    private static final int LESSON_COUNT = 12;

    public static View build(final MainActivity main) {
        final Context c = main;

        LinearLayout root = Ui.linear(c, LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFF262421);

        // ---- header ----
        LinearLayout top = Ui.linear(c, LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER);
        top.setBackgroundColor(0xFF262421);
        TextView cap = Ui.glyph(c, "school", 20, 0xFF7CC4F5);
        top.addView(cap, new LinearLayout.LayoutParams(Ui.dp(c, 24), Ui.dp(c, 24)));
        TextView title = Ui.text(c, "Learn", 17, Ui.WHITE, true);
        LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tp.setMargins(Ui.dp(c, 5), 0, 0, 0);
        title.setLayoutParams(tp);
        top.addView(title);
        root.addView(top, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(c, 34)));

        // ---- path area (internally scrollable lattice + pinned coach overlay) ----
        FrameLayout area = new FrameLayout(c);
        area.setClipChildren(true);
        area.addView(new LearnPathView(c), new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        ImageView coach = new ImageView(c);
        coach.setImageResource(R.drawable.avatar_coach_learn);
        coach.setScaleType(ImageView.ScaleType.FIT_XY);
        FrameLayout.LayoutParams cp = new FrameLayout.LayoutParams(
                Ui.dp(c, 46), Ui.dp(c, 54), Gravity.TOP | Gravity.START);
        cp.leftMargin = Ui.dp(c, 22);
        cp.topMargin = Ui.dp(c, 8);
        area.addView(coach, cp);

        TextView bubble = Ui.text(c, bubbleText(main.state.learnProgress), 10.5f,
                0xFF1F1F1F, false);
        bubble.setBackground(Ui.round(0xFFFFFFFF, 7, c));
        bubble.setPadding(Ui.dp(c, 14), Ui.dp(c, 6), Ui.dp(c, 12), Ui.dp(c, 6));
        bubble.setMaxLines(2);
        bubble.setGravity(Gravity.CENTER_VERTICAL);
        FrameLayout.LayoutParams bp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.TOP);
        bp.leftMargin = Ui.dp(c, 71);
        bp.rightMargin = Ui.dp(c, 26);
        bp.topMargin = Ui.dp(c, 19);
        area.addView(bubble, bp);
        root.addView(area, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        // ---- "Learn To Play" band ----
        FrameLayout band = new FrameLayout(c);
        GradientDrawable bandBg = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{0xFF1E3133, 0xFF16454F, 0xFF0A4F5D});
        band.setBackground(bandBg);
        TextView bandText = Ui.text(c, "Learn To Play", 16.5f, Ui.WHITE, true);
        FrameLayout.LayoutParams btp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.START | Gravity.CENTER_VERTICAL);
        btp.leftMargin = Ui.dp(c, 55);
        band.addView(bandText, btp);
        root.addView(band, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(c, 54)));

        // ---- bottom action bar ----
        LinearLayout actions = Ui.linear(c, LinearLayout.HORIZONTAL);
        actions.setGravity(Gravity.CENTER_VERTICAL);
        actions.setBackgroundColor(0xFF242624);
        actions.setPadding(Ui.dp(c, 22), 0, Ui.dp(c, 22), 0);

        TextView listGlyph = Ui.glyph(c, "menu", 22, 0xFFC9C7C3);
        listGlyph.setGravity(Gravity.CENTER);
        listGlyph.setBackground(Ui.round(0xFF383A38, 8, c));
        listGlyph.setLayoutParams(new LinearLayout.LayoutParams(
                Ui.dp(c, 46), Ui.dp(c, 46)));
        listGlyph.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                Toast.makeText(c, "Lessons: King, Knight, Rook, Bishop, Queen, Pawn, Capturing",
                        Toast.LENGTH_SHORT).show();
            }
        });
        actions.addView(listGlyph);

        TextView nextLesson = Ui.text(c, "Next Lesson", 15.5f, Ui.WHITE, true);
        nextLesson.setGravity(Gravity.CENTER);
        nextLesson.setBackground(Ui.greenFill(c, 7));
        LinearLayout.LayoutParams nlp = new LinearLayout.LayoutParams(0, Ui.dp(c, 48), 1f);
        nlp.setMargins(Ui.dp(c, 10), 0, 0, 0);
        nextLesson.setLayoutParams(nlp);
        nextLesson.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (main.state.learnProgress < LESSON_COUNT) {
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
        root.addView(actions, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(c, 60)));

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
            case 7: return "Check! Attack the king so he cannot escape.";
            case 8: return "Checkmate - the king is trapped and the game is won!";
            case 9: return "Castle to tuck your king away and activate a rook.";
            case 10: return "En passant - the sneakiest pawn capture in chess!";
            case 11: return "Promote your pawn to a queen on the last rank!";
            default: return "You've mastered the basics - keep practicing!";
        }
    }
}
