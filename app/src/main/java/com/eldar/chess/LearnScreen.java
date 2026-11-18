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
        String title; String glyph; boolean hasPiece;
        Lesson(String title, String glyph, boolean hasPiece) {
            this.title = title; this.glyph = glyph; this.hasPiece = hasPiece;
        }
    }

    private static final Lesson[] LESSONS = {
            new Lesson("How the King moves", "♔", true),
            new Lesson("How the Knight moves", "♞", true),
            new Lesson("How the Rook moves", "♜", true),
            new Lesson("How the Bishop moves", "♝", true),
            new Lesson("How the Queen moves", "♛", true),
            new Lesson("How the Pawn moves", "♟", true),
            new Lesson("Capturing pieces", "✕", false),
            new Lesson("Check and Checkmate", "✕", false),
    };

    private static final int LOCKED = 0xFF1E4A50;
    private static final int CURRENT = 0xFF62F6CA;
    private static final int DONE = 0xFF2A7A86;

    public static View build(final MainActivity main) {
        final android.content.Context c = main;

        LinearLayout root = Ui.linear(c, LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFF211F1C);

        // ---- title ----
        LinearLayout top = Ui.linear(c, LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER);
        top.setPadding(0, Ui.dp(c, 12), 0, Ui.dp(c, 8));
        TextView cap = Ui.glyph(c, "school", 22, 0xFF7CC4F5);
        cap.setLayoutParams(new LinearLayout.LayoutParams(Ui.dp(c, 26), Ui.dp(c, 26)));
        top.addView(cap);
        top.addView(Ui.text(c, "  Learn", 17, Ui.WHITE, true));
        root.addView(top);

        // ---- speech bubble ----
        LinearLayout speech = Ui.linear(c, LinearLayout.HORIZONTAL);
        speech.setPadding(Ui.dp(c, 8), Ui.dp(c, 4), Ui.dp(c, 8), Ui.dp(c, 6));
        FrameLayout av = new FrameLayout(c);
        View avBg = new View(c);
        avBg.setBackground(Ui.round(0xFFD9D5CC, 21, c));
        av.addView(avBg, new FrameLayout.LayoutParams(
                Ui.dp(c, 42), Ui.dp(c, 42), Gravity.CENTER));
        av.addView(Ui.glyph(c, "person", 26, 0xFF3B3935),
                new FrameLayout.LayoutParams(
                        Ui.dp(c, 42), Ui.dp(c, 42), Gravity.CENTER));
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
        // the glow around the current lesson spills outside its row
        scroll.setClipChildren(false);
        scroll.setClipToPadding(false);
        path.setClipChildren(false);
        path.setClipToPadding(false);
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
                    ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(c, 68)));
            row.setClipChildren(false);

            boolean left = (LESSONS.length - 1 - i) % 2 == 0; // zigzag
            TextView piece = null;
            if (l.hasPiece) {
                piece = Ui.text(c, l.glyph, 32, 0xFFEDEBE6, false);
                piece.setGravity(Gravity.CENTER);
                piece.setLayoutParams(new LinearLayout.LayoutParams(
                        Ui.dp(c, 46), Ui.dp(c, 58)));
            }
            if (left) row.addView(Ui.spacerWeight(c, 0.7f));
            else {
                if (piece != null) row.addView(piece);
                row.addView(Ui.spacerWeight(c, 0.7f));
            }
            LinearLayout stack = Ui.linear(c, LinearLayout.VERTICAL);
            stack.setGravity(Gravity.CENTER);
            stack.setClipChildren(false);
            LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, Ui.dp(c, 68));
            stack.setLayoutParams(sp);

            FrameLayout diamondWrap = new FrameLayout(c);
            diamondWrap.setClipChildren(false);
            if (current) {                // mint halo bleeding out around the current lesson (screenshot 072013)
                View glow = new View(c);
                android.graphics.drawable.GradientDrawable g =
                        new android.graphics.drawable.GradientDrawable(
                                android.graphics.drawable.GradientDrawable.Orientation.TL_BR,
                                new int[]{0x8C62F6CA, 0x2E62F6CA, 0x0062F6CA});
                g.setGradientType(android.graphics.drawable.GradientDrawable.RADIAL_GRADIENT);
                g.setGradientRadius(Ui.dp(c, 62));
                g.setShape(android.graphics.drawable.GradientDrawable.OVAL);
                glow.setBackground(g);
                FrameLayout.LayoutParams glp = new FrameLayout.LayoutParams(
                        Ui.dp(c, 132), Ui.dp(c, 132), Gravity.CENTER);
                glp.setMargins(-Ui.dp(c, 18), -Ui.dp(c, 18), -Ui.dp(c, 18), -Ui.dp(c, 18));
                diamondWrap.addView(glow, glp);
            }
            View diamond = new View(c);
            diamond.setBackground(Ui.round(current ? CURRENT : done ? DONE : LOCKED, 8, c));
            diamond.setRotation(45f);
            diamondWrap.addView(diamond, new FrameLayout.LayoutParams(
                    Ui.dp(c, 40), Ui.dp(c, 40), Gravity.CENTER));

            TextView glyph = Ui.text(c, l.glyph, 18, current ? Ui.WHITE : 0xFF8FB7BC, true);
            glyph.setGravity(Gravity.CENTER);
            diamondWrap.addView(glyph, new FrameLayout.LayoutParams(
                    Ui.dp(c, 40), Ui.dp(c, 40), Gravity.CENTER));

            if (current) {
                // "Next" tooltip: white bubble, dark text, warm halo, small tail
                FrameLayout bubbleWrap = new FrameLayout(c);
                bubbleWrap.setClipChildren(false);
                View halo = new View(c);
                android.graphics.drawable.GradientDrawable hg =
                        new android.graphics.drawable.GradientDrawable(
                                android.graphics.drawable.GradientDrawable.Orientation.TL_BR,
                                new int[]{0x55E8E27A, 0x00E8E27A});
                hg.setGradientType(android.graphics.drawable.GradientDrawable.RADIAL_GRADIENT);
                hg.setGradientRadius(Ui.dp(c, 46));
                hg.setShape(android.graphics.drawable.GradientDrawable.OVAL);
                halo.setBackground(hg);
                FrameLayout.LayoutParams hlp = new FrameLayout.LayoutParams(
                        Ui.dp(c, 130), Ui.dp(c, 84), Gravity.CENTER_HORIZONTAL | Gravity.TOP);
                hlp.setMargins(0, -Ui.dp(c, 16), 0, 0);
                bubbleWrap.addView(halo, hlp);

                TextView next = Ui.text(c, "Next", 12, 0xFF312E2B, true);
                next.setGravity(Gravity.CENTER);
                next.setBackground(Ui.round(0xFFF8F8F8, 7, c));
                next.setPadding(Ui.dp(c, 12), Ui.dp(c, 3), Ui.dp(c, 12), Ui.dp(c, 3));
                FrameLayout.LayoutParams np = new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                        Gravity.CENTER_HORIZONTAL | Gravity.TOP);
                bubbleWrap.addView(next, np);

                View nextTail = new View(c);
                nextTail.setBackground(Ui.round(0xFFF8F8F8, 2, c));
                nextTail.setRotation(45f);
                FrameLayout.LayoutParams tpp = new FrameLayout.LayoutParams(
                        Ui.dp(c, 10), Ui.dp(c, 10),
                        Gravity.CENTER_HORIZONTAL | Gravity.TOP);
                tpp.setMargins(0, Ui.dp(c, 22), 0, 0);
                bubbleWrap.addView(nextTail, tpp);

                FrameLayout.LayoutParams bwp = new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                        Gravity.CENTER_HORIZONTAL | Gravity.TOP);
                bwp.setMargins(0, -Ui.dp(c, 12), 0, 0);
                diamondWrap.addView(bubbleWrap, bwp);
            }
            stack.addView(diamondWrap, new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            row.addView(stack);

            if (left) {
                if (piece != null) row.addView(piece);
                row.addView(Ui.spacerWeight(c, 1.3f));
            } else {
                row.addView(Ui.spacerWeight(c, 1.3f));
            }
            path.addView(row);
        }

        root.addView(scroll);
        scroll.post(new Runnable() {
            @Override public void run() { scroll.fullScroll(View.FOCUS_DOWN); }
        });

        // ---- bottom fixed section ----
        LinearLayout bottom = Ui.linear(c, LinearLayout.VERTICAL);
        bottom.setBackgroundColor(0xFF242624);

        // teal gradient title band (screenshot 072013)
        LinearLayout band = Ui.linear(c, LinearLayout.VERTICAL);
        android.graphics.drawable.GradientDrawable bandBg =
                new android.graphics.drawable.GradientDrawable(
                        android.graphics.drawable.GradientDrawable.Orientation.TOP_BOTTOM,
                        new int[]{0xFF262421, 0xFF17393F, 0xFF0A4F5D});
        band.setBackground(bandBg);
        band.setPadding(Ui.dp(c, 58), Ui.dp(c, 30), Ui.dp(c, 12), Ui.dp(c, 8));
        band.addView(Ui.text(c, "Learn To Play", 13, Ui.WHITE, true));
        bottom.addView(band);

        LinearLayout actions = Ui.linear(c, LinearLayout.HORIZONTAL);
        actions.setGravity(Gravity.CENTER_VERTICAL);
        actions.setPadding(Ui.dp(c, 12), Ui.dp(c, 8), Ui.dp(c, 12), Ui.dp(c, 10));

        TextView listGlyph = Ui.glyph(c, "menu", 18, 0xFFB5B0A8);
        listGlyph.setGravity(Gravity.CENTER);
        listGlyph.setBackground(Ui.round(0xFF3B3935, 6, c));
        listGlyph.setLayoutParams(new LinearLayout.LayoutParams(
                Ui.dp(c, 34), Ui.dp(c, 34)));
        listGlyph.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                Toast.makeText(c, "Lessons: King, Knight, Rook, Bishop, Queen, Pawn",
                        Toast.LENGTH_SHORT).show();
            }
        });
        actions.addView(listGlyph);

        TextView nextLesson = Ui.text(c, "Next Lesson", 14, Ui.WHITE, true);
        nextLesson.setGravity(Gravity.CENTER);
        nextLesson.setBackground(Ui.round(Ui.GREEN, 8, c));
        LinearLayout.LayoutParams nlp = new LinearLayout.LayoutParams(
                0, Ui.dp(c, 34), 1f);
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
