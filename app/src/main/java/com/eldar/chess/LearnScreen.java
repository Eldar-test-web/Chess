package com.eldar.chess;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/** Learn screen - long 3-section lesson journey with pinned coach and caption band. */
public class LearnScreen {

    private static final int LESSON_COUNT = 16;

    public static View build(final MainActivity main) {
        final Context c = main;

        LinearLayout root = Ui.linear(c, LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFF262421);

        // ---- header ----
        LinearLayout top = Ui.linear(c, LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER);
        top.setBackgroundColor(0xFF262421);
        TextView cap = Ui.glyph(c, "school", 24, 0xFF2F86C8);
        top.addView(cap, new LinearLayout.LayoutParams(Ui.dp(c, 30), Ui.dp(c, 30)));
        TextView title = Ui.text(c, "Learn", 20, Ui.WHITE, true);
        LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tp.setMargins(Ui.dp(c, 3), 0, 0, 0);
        title.setLayoutParams(tp);
        top.addView(title);
        root.addView(top, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(c, 46)));

        // ---- path area ----
        FrameLayout area = new FrameLayout(c);
        area.setClipChildren(true);
        final LearnPathView path = new LearnPathView(c);
        path.setProgress(main.state.learnProgress);
        area.addView(path, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        // pinned coach + speech bubble
        ImageView coach = new ImageView(c);
        coach.setImageResource(R.drawable.avatar_coach_learn);
        coach.setScaleType(ImageView.ScaleType.FIT_CENTER);
        FrameLayout.LayoutParams cp = new FrameLayout.LayoutParams(
                Ui.dp(c, 58), Ui.dp(c, 68), Gravity.TOP | Gravity.START);
        cp.leftMargin = Ui.dp(c, 10);
        cp.topMargin = 0;
        area.addView(coach, cp);

        TextView bubble = Ui.text(c, bubbleText(main.state.learnProgress), 8.5f,
                0xFF1F1F1F, false);
        bubble.setBackground(bubbleBg(c));
        bubble.setPadding(Ui.dp(c, 12), Ui.dp(c, 6), Ui.dp(c, 12), Ui.dp(c, 6));
        bubble.setMaxLines(3);
        bubble.setIncludeFontPadding(false);
        bubble.setGravity(Gravity.CENTER_VERTICAL);
        FrameLayout.LayoutParams bp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.TOP);
        bp.leftMargin = Ui.dp(c, 80);
        bp.rightMargin = Ui.dp(c, 16);
        bp.topMargin = Ui.dp(c, 12);
        area.addView(bubble, bp);

        // caption band sits on the section color, like the original course
        FrameLayout band = new FrameLayout(c);
        final int[] bandColor = {LearnPathView.BAND_BASE[1]};
        band.setBackgroundColor(bandColor[0]);
        final TextView bandText = Ui.text(c, LearnPathView.SECTION_TITLES[1], 15,
                Ui.WHITE, true);
        bandText.setSingleLine(true);
        bandText.setIncludeFontPadding(false);
        bandText.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        FrameLayout.LayoutParams btp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        btp.leftMargin = Ui.dp(c, 18);
        band.addView(bandText, btp);
        FrameLayout.LayoutParams bandLp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(c, 30), Gravity.BOTTOM);
        bandLp.bottomMargin = 0;
        area.addView(band, bandLp);

        // floating down-arrow
        TextView down = Ui.glyph(c, "arrow_downward", 21, Ui.WHITE);
        down.setGravity(Gravity.CENTER);
        down.setBackground(Ui.round(0xE62A3225, 10, c));
        FrameLayout.LayoutParams dp = new FrameLayout.LayoutParams(
                Ui.dp(c, 42), Ui.dp(c, 42), Gravity.BOTTOM | Gravity.END);
        dp.rightMargin = Ui.dp(c, 16);
        dp.bottomMargin = Ui.dp(c, 40);
        down.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { path.smoothScrollDown(); }
        });
        area.addView(down, dp);

        path.setListener(new LearnPathView.Listener() {
            @Override public void onLessonTap(int index) {
                LessonScreen.setIndex(Math.max(0, Math.min(LESSON_COUNT - 1, index)));
                main.navigate("lesson");
            }
            @Override public void onNextTap() {
                LessonScreen.setIndex(Math.min(main.state.learnProgress,
                        LESSON_COUNT - 1));
                main.navigate("lesson");
            }
            @Override public void onSectionChanged(String sectionTitle) {
                bandText.setText(sectionTitle);
                if (LearnPathView.SECTION_TITLES[0].equals(sectionTitle)) {
                    bandColor[0] = LearnPathView.BAND_BASE[0];
                } else if (LearnPathView.SECTION_TITLES[1].equals(sectionTitle)) {
                    bandColor[0] = LearnPathView.BAND_BASE[1];
                } else {
                    bandColor[0] = LearnPathView.BAND_BASE[2];
                }
                band.setBackgroundColor(bandColor[0]);
            }
        });

        root.addView(area, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        // ---- bottom action bar ----
        LinearLayout actions = Ui.linear(c, LinearLayout.HORIZONTAL);
        actions.setGravity(Gravity.CENTER_VERTICAL);
        actions.setBackgroundColor(0xFF2B4228);
        actions.setPadding(Ui.dp(c, 15), 0, Ui.dp(c, 13), 0);

        TextView listGlyph = Ui.glyph(c, "menu", 21, 0xFFC9C7C3);
        listGlyph.setGravity(Gravity.CENTER);
        listGlyph.setBackground(Ui.round(0xFF3D533E, 10, c));
        listGlyph.setLayoutParams(new LinearLayout.LayoutParams(
                Ui.dp(c, 52), Ui.dp(c, 56)));
        listGlyph.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                Toast.makeText(c, "16 lessons: pieces, captures, check, mates & tactics",
                        Toast.LENGTH_SHORT).show();
            }
        });
        actions.addView(listGlyph);

        TextView nextLesson = Ui.text(c, "Next Lesson", 15, Ui.WHITE, true);
        nextLesson.setGravity(Gravity.CENTER);
        nextLesson.setSingleLine(true);
        nextLesson.setBackground(Ui.greenFill(c, 9));
        LinearLayout.LayoutParams nlp = new LinearLayout.LayoutParams(0, Ui.dp(c, 54), 1f);
        nlp.setMargins(Ui.dp(c, 13), 0, 0, 0);
        nextLesson.setLayoutParams(nlp);
        nextLesson.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                LessonScreen.setIndex(Math.min(main.state.learnProgress,
                        LESSON_COUNT - 1));
                main.navigate("lesson");
            }
        });
        actions.addView(nextLesson);
        root.addView(actions, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(c, 58)));

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
            case 12: return "Pins freeze pieces in place - attack two at once with a fork next!";
            case 13: return "Forks attack two pieces at once. Next: back rank mate!";
            case 14: return "Trap the king behind his own pawns with a back rank mate.";
            case 15: return "Last one! Two rooks climb like a ladder to deliver mate.";
            default: return "You've mastered the course - keep practicing!";
        }
    }

    private static Drawable bubbleBg(Context c) {
        final float tail = Ui.dp(c, 8);
        final float radius = Ui.dp(c, 9);
        return new Drawable() {
            private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            private final RectF body = new RectF();
            private final Path tailPath = new Path();

            @Override public void draw(Canvas cv) {
                android.graphics.Rect b = getBounds();
                paint.setColor(0xFFFFFFFF);
                body.set(b.left + tail, b.top, b.right, b.bottom);
                cv.drawRoundRect(body, radius, radius, paint);
                float ty = b.top + b.height() * 0.34f;
                tailPath.reset();
                tailPath.moveTo(b.left + tail + 1f, ty);
                tailPath.lineTo(b.left, ty + tail * 0.7f);
                tailPath.lineTo(b.left + tail + 1f, ty + tail * 1.4f);
                tailPath.close();
                cv.drawPath(tailPath, paint);
            }

            @Override public void setAlpha(int alpha) { paint.setAlpha(alpha); }
            @Override public void setColorFilter(ColorFilter cf) { paint.setColorFilter(cf); }
            @Override public int getOpacity() { return PixelFormat.TRANSLUCENT; }
        };
    }
}
