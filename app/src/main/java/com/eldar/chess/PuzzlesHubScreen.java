package com.eldar.chess;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Scroller;
import android.widget.TextView;
import android.widget.Toast;

import java.util.LinkedHashSet;
import java.util.Set;

/** Puzzles hub - a long scrolling diorama: cloud zone above the jungle puzzle path. */
public class PuzzlesHubScreen {

    private static final String COACH_TEXT =
            "Solving puzzles is the best way to develop pattern recognition and tactical skill.";

    public static View build(final MainActivity main) {
        final Context c = main;
        PuzzlesScreen.ensureQueue();
        final int current = PuzzlesScreen.currentIndex();
        final int total = PuzzlesScreen.totalPuzzles();

        FrameLayout root = new FrameLayout(c);

        final PathView path = new PathView(main, current, total);
        root.addView(path, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        // top scrim so the white header + coach stay readable over both zones
        View topScrim = new View(c);
        topScrim.setBackground(new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{0xE0271D13, 0xB3241B12, 0x5E221A12, 0x00221A12}));
        topScrim.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(c, 118), Gravity.TOP));
        root.addView(topScrim);

        // centered chest + title
        FrameLayout header = new FrameLayout(c);
        LinearLayout group = Ui.linear(c, LinearLayout.HORIZONTAL);
        group.setGravity(Gravity.CENTER_VERTICAL);
        group.addView(new ChestView(c), new LinearLayout.LayoutParams(
                Ui.dp(c, 34), Ui.dp(c, 29)));
        TextView title = Ui.text(c, "Puzzles", 23, Ui.WHITE, true);
        title.setIncludeFontPadding(false);
        title.setSingleLine(true);
        LinearLayout.LayoutParams tlp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tlp.setMargins(Ui.dp(c, 6), 0, 0, 0);
        title.setLayoutParams(tlp);
        group.addView(title);
        FrameLayout.LayoutParams glp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
        header.addView(group, glp);
        FrameLayout.LayoutParams hlp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(c, 62), Gravity.TOP);
        root.addView(header, hlp);

        // coach cat + bubble floating over the top of the path
        FrameLayout coach = new FrameLayout(c);
        ImageView cat = new ImageView(c);
        cat.setImageResource(R.drawable.avatar_coach_learn);
        cat.setScaleType(ImageView.ScaleType.FIT_CENTER);
        FrameLayout.LayoutParams catLp = new FrameLayout.LayoutParams(
                Ui.dp(c, 70), Ui.dp(c, 80), Gravity.START | Gravity.TOP);
        catLp.leftMargin = 0;
        coach.addView(cat, catLp);

        TextView bubble = Ui.text(c, COACH_TEXT, 8.5f, 0xFF1E1D1B, false);
        bubble.setBackground(bubbleBg(c));
        bubble.setPadding(Ui.dp(c, 11), Ui.dp(c, 7), Ui.dp(c, 11), Ui.dp(c, 7));
        bubble.setLineSpacing(Ui.dp(c, 1), 1f);
        bubble.setIncludeFontPadding(false);
        bubble.setMaxLines(3);
        FrameLayout.LayoutParams blp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.START | Gravity.TOP);
        blp.leftMargin = Ui.dp(c, 84);
        blp.rightMargin = Ui.dp(c, 16);
        blp.topMargin = Ui.dp(c, 2);
        coach.addView(bubble, blp);

        FrameLayout.LayoutParams clp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.TOP);
        clp.topMargin = Ui.dp(c, 58);
        root.addView(coach, clp);

        // bottom scrim + panel
        View bottomScrim = new View(c);
        bottomScrim.setBackground(new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
                new int[]{0xE0141810, 0xA6141810, 0x33141810, 0x00141810}));
        bottomScrim.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(c, 168), Gravity.BOTTOM));
        root.addView(bottomScrim);

        LinearLayout bottom = Ui.linear(c, LinearLayout.VERTICAL);
        bottom.setPadding(Ui.dp(c, 16), Ui.dp(c, 2), Ui.dp(c, 16), Ui.dp(c, 8));

        LinearLayout scoreRow = Ui.linear(c, LinearLayout.HORIZONTAL);
        scoreRow.setGravity(Gravity.CENTER_VERTICAL);
        TextView score = Ui.text(c, String.valueOf(PuzzlesScreen.sessionScore), 23,
                Ui.WHITE, true);
        score.setIncludeFontPadding(false);
        scoreRow.addView(score);
        scoreRow.addView(Ui.spacerWeight(c, 1f));
        bottom.addView(scoreRow);

        FrameLayout progRow = new FrameLayout(c);
        LinearLayout.LayoutParams prlp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(c, 26));
        prlp.topMargin = Ui.dp(c, 1);
        progRow.setLayoutParams(prlp);
        View bar = progressBar(c, total <= 1 ? 0f : (current + 1f) / total);
        FrameLayout.LayoutParams barLp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(c, 6),
                Gravity.START | Gravity.CENTER_VERTICAL);
        barLp.rightMargin = Ui.dp(c, 34);
        progRow.addView(bar, barLp);
        progRow.addView(new BadgeView(c), new FrameLayout.LayoutParams(
                Ui.dp(c, 32), Ui.dp(c, 26), Gravity.END | Gravity.CENTER_VERTICAL));
        bottom.addView(progRow);

        LinearLayout buttons = Ui.linear(c, LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams rowLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(c, 58));
        rowLp.topMargin = Ui.dp(c, 8);
        buttons.setLayoutParams(rowLp);

        TextView list = Ui.glyph(c, "list", 23, Ui.WHITE);
        list.setBackground(Ui.round(0xFF3F553E, 10, c));
        LinearLayout.LayoutParams listLp = new LinearLayout.LayoutParams(
                Ui.dp(c, 52), ViewGroup.LayoutParams.MATCH_PARENT);
        listLp.setMargins(0, 0, Ui.dp(c, 9), 0);
        buttons.addView(list, listLp);
        list.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { puzzleMenu(main); }
        });

        TextView solve = Ui.text(c, "Solve Puzzles", 14, Ui.WHITE, true);
        solve.setGravity(Gravity.CENTER);
        solve.setIncludeFontPadding(false);
        solve.setSingleLine(true);
        solve.setMaxLines(1);
        solve.setBackground(Ui.greenFill(c, 10));
        solve.setLayoutParams(new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.MATCH_PARENT, 1f));
        solve.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { main.navigate("puzzlesolve"); }
        });
        buttons.addView(solve);
        bottom.addView(buttons);

        FrameLayout.LayoutParams panelLp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.BOTTOM);
        panelLp.bottomMargin = Ui.dp(c, 4);
        root.addView(bottom, panelLp);

        // floating scroll-down button just above the score panel
        ArrowButton arrow = new ArrowButton(c);
        FrameLayout.LayoutParams alp = new FrameLayout.LayoutParams(
                Ui.dp(c, 44), Ui.dp(c, 44), Gravity.END | Gravity.BOTTOM);
        alp.rightMargin = Ui.dp(c, 14);
        alp.bottomMargin = Ui.dp(c, 158);
        arrow.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { path.stepToNext(); }
        });
        root.addView(arrow, alp);

        return root;
    }

    private static Drawable bubbleBg(Context c) {
        final float tail = Ui.dp(c, 8);
        final float radius = Ui.dp(c, 10);
        return new Drawable() {
            private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            private final RectF body = new RectF();
            private final Path tailPath = new Path();

            @Override public void draw(Canvas cv) {
                android.graphics.Rect b = getBounds();
                paint.setColor(0xFFFFFFFF);
                body.set(b.left + tail, b.top, b.right, b.bottom);
                cv.drawRoundRect(body, radius, radius, paint);
                float ty = b.top + b.height() * 0.30f;
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

    private static View progressBar(Context c, float fraction) {
        final float f = Math.max(0f, Math.min(1f, fraction));
        View v = new View(c) {
            private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);

            @Override protected void onDraw(Canvas canvas) {
                float h = getHeight(), r = h / 2f;
                p.setShader(null);
                p.setColor(0x5EFFFFFF);
                canvas.drawRoundRect(new RectF(0, 0, getWidth(), h), r, r, p);
                if (f > 0) {
                    int w = Math.max(getHeight(), Math.round(getWidth() * f));
                    p.setShader(new LinearGradient(0, 0, w, 0,
                            0xFFBFE27A, 0xFF86B24E, Shader.TileMode.CLAMP));
                    canvas.drawRoundRect(new RectF(0, 0, w, h), r, r, p);
                    p.setShader(null);
                }
            }
        };
        v.setWillNotDraw(false);
        return v;
    }

    private static void puzzleMenu(final MainActivity main) {
        final String[] items = {"Random puzzle", "Puzzle themes", "Puzzle stats"};
        new AlertDialog.Builder(main)
                .setTitle("Puzzle Menu")
                .setItems(items, new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface d, int which) {
                        if (which == 0) {
                            PuzzlesScreen.shuffleQueue();
                            main.navigate("puzzlesolve");
                        } else if (which == 1) {
                            themePicker(main);
                        } else {
                            new AlertDialog.Builder(main)
                                    .setTitle("Puzzle Stats")
                                    .setMessage("Rating: " + main.state.puzzleRating
                                            + "\nSolved: " + main.state.puzzlesSolved
                                            + "\nIn this run: " + PuzzlesScreen.sessionScore + " pts")
                                    .setPositiveButton("OK", null)
                                    .show();
                        }
                    }
                })
                .setNegativeButton("Close", null)
                .show();
    }

    private static void themePicker(final MainActivity main) {
        Set<String> set = new LinkedHashSet<>();
        for (PuzzleData.Puzzle p : PuzzleData.all()) set.add(p.theme);
        final String[] themes = set.toArray(new String[0]);
        new AlertDialog.Builder(main)
                .setTitle("Puzzle Themes")
                .setItems(themes, new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface d, int which) {
                        PuzzlesScreen.setTheme(themes[which]);
                        main.navigate("puzzlesolve");
                    }
                })
                .setNegativeButton("All themes", new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface d, int which) {
                        PuzzlesScreen.setTheme(null);
                        main.navigate("puzzlesolve");
                    }
                })
                .show();
    }

    /** Small wooden treasure chest drawn natively (no pale-green extension glyph). */
    private static class ChestView extends View {
        private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final RectF r = new RectF();

        ChestView(Context c) { super(c); }

        @Override protected void onDraw(Canvas cv) {
            float w = getWidth(), h = getHeight();
            if (w <= 0 || h <= 0) return;

            p.setStyle(Paint.Style.FILL);
            p.setShader(null);
            p.setColor(0x30000000);
            cv.drawOval(w * 0.06f, h * 0.88f, w * 0.94f, h, p);

            // domed lid
            r.set(w * 0.06f, h * 0.12f, w * 0.94f, h * 0.68f);
            p.setColor(0xFF9A6A38);
            cv.drawRoundRect(r, h * 0.28f, h * 0.28f, p);
            r.set(w * 0.10f, h * 0.15f, w * 0.90f, h * 0.34f);
            p.setColor(0xFFB07E45);
            cv.drawRoundRect(r, h * 0.09f, h * 0.09f, p);

            // body
            r.set(w * 0.08f, h * 0.44f, w * 0.92f, h * 0.90f);
            p.setColor(0xFF7C4A21);
            cv.drawRoundRect(r, h * 0.10f, h * 0.10f, p);
            r.set(w * 0.10f, h * 0.58f, w * 0.90f, h * 0.76f);
            p.setColor(0xFF8E5828);
            cv.drawRoundRect(r, h * 0.05f, h * 0.05f, p);

            // gold straps + lock
            p.setColor(0xFFD7A94C);
            cv.drawRect(w * 0.29f, h * 0.17f, w * 0.37f, h * 0.90f, p);
            cv.drawRect(w * 0.63f, h * 0.17f, w * 0.71f, h * 0.90f, p);
            r.set(w * 0.42f, h * 0.42f, w * 0.58f, h * 0.66f);
            p.setColor(0xFFEBCB76);
            cv.drawRoundRect(r, w * 0.035f, w * 0.035f, p);
            p.setColor(0xFF5A3A18);
            cv.drawCircle(w * 0.5f, h * 0.53f, w * 0.028f, p);
            cv.drawRect(w * 0.49f, h * 0.53f, w * 0.51f, h * 0.61f, p);

            // lid sheen
            p.setColor(0x66FFF1C4);
            r.set(w * 0.14f, h * 0.19f, w * 0.84f, h * 0.27f);
            cv.drawRoundRect(r, h * 0.04f, h * 0.04f, p);
        }
    }

    /** Brown puzzle badge with the "2" seen at the right end of the progress bar. */
    private static class BadgeView extends View {
        private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint t = new Paint(Paint.ANTI_ALIAS_FLAG);

        BadgeView(Context c) {
            super(c);
            t.setTypeface(Typeface.DEFAULT_BOLD);
            t.setTextAlign(Paint.Align.CENTER);
            t.setColor(0xFFFFFFFF);
        }

        @Override protected void onDraw(Canvas cv) {
            float w = getWidth(), h = getHeight();
            if (w <= 0 || h <= 0) return;
            float pad = h * 0.05f;

            RectF body = new RectF(pad, pad, w - pad, h - pad);
            p.setStyle(Paint.Style.FILL);
            p.setColor(0xFF7A4A22);
            cv.drawRoundRect(body, h * 0.30f, h * 0.30f, p);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(Math.max(1f, h * 0.055f));
            p.setColor(0xFFC68C48);
            cv.drawRoundRect(body, h * 0.30f, h * 0.30f, p);
            p.setStyle(Paint.Style.FILL);

            // tiny cream puzzle piece
            float s = h * 0.36f;
            float x = w * 0.32f, y = h * 0.5f;
            p.setColor(0xFFF1DCA9);
            cv.drawRoundRect(x - s * 0.45f, y - s * 0.42f, x + s * 0.30f, y + s * 0.42f,
                    s * 0.12f, s * 0.12f, p);
            cv.drawCircle(x + s * 0.34f, y, s * 0.22f, p);
            cv.drawCircle(x - s * 0.05f, y - s * 0.48f, s * 0.20f, p);

            t.setTextSize(h * 0.52f);
            Paint.FontMetrics fm = t.getFontMetrics();
            cv.drawText("2", w * 0.72f, h * 0.5f - (fm.ascent + fm.descent) / 2f, t);
        }
    }

    /** Floating dark square with a white down arrow. */
    private static class ArrowButton extends View {
        private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Path arrow = new Path();

        ArrowButton(Context c) {
            super(c);
            setClickable(true);
            setFocusable(true);
        }

        @Override protected void onDraw(Canvas cv) {
            float w = getWidth(), h = getHeight();
            if (w <= 0 || h <= 0) return;
            p.setStyle(Paint.Style.FILL);
            p.setShader(null);
            p.setColor(0xE62B3226);
            cv.drawRoundRect(new RectF(0, 0, w, h), w * 0.24f, w * 0.24f, p);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(Math.max(1f, w * 0.018f));
            p.setColor(0x22FFFFFF);
            cv.drawRoundRect(new RectF(1, 1, w - 1, h - 1), w * 0.24f, w * 0.24f, p);

            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(Math.max(2f, w * 0.075f));
            p.setStrokeCap(Paint.Cap.ROUND);
            p.setStrokeJoin(Paint.Join.ROUND);
            p.setColor(0xFFFFFFFF);
            arrow.reset();
            arrow.moveTo(w * 0.5f, h * 0.27f);
            arrow.lineTo(w * 0.5f, h * 0.60f);
            arrow.moveTo(w * 0.32f, h * 0.45f);
            arrow.lineTo(w * 0.5f, h * 0.64f);
            arrow.lineTo(w * 0.68f, h * 0.45f);
            cv.drawPath(arrow, p);
        }
    }

    /** Scrollable jungle + cloud diorama with 17 wooden and 10 pastel platforms. */
    private static class PathView extends View {

        private static final float CONTENT_DP = 2360f;
        private static final float JUNGLE_L1_Y_DP = 2150f;
        private static final float JUNGLE_PITCH_DP = 45f;
        private static final float CLOUD_L1_Y_DP = 960f;
        private static final float CLOUD_PITCH_DP = 88f;

        // level 1 .. 17, bottom to top - zigzag like the recording
        private static final float[] JX = {0.79f, 0.645f, 0.50f, 0.355f, 0.21f, 0.355f,
                0.50f, 0.645f, 0.79f, 0.645f, 0.50f, 0.355f, 0.21f, 0.355f, 0.50f, 0.645f,
                0.79f};
        // cloud level 1 .. 10, bottom to top
        private static final float[] CX = {0.79f, 0.62f, 0.46f, 0.28f, 0.16f, 0.34f,
                0.50f, 0.66f, 0.80f, 0.64f};

        private static final float[][] CLOUDS = {
                {-0.10f, 135f, 0.72f, 235f}, {0.95f, 320f, 0.62f, 228f},
                {-0.06f, 540f, 0.68f, 232f}, {0.94f, 760f, 0.58f, 222f},
                {0.36f, 36f, 0.32f, 196f}, {0.60f, 466f, 0.28f, 176f},
                {0.05f, 812f, 0.42f, 206f}
        };
        private static final float[][] BUSHES = {
                {0.02f, 1092f, 1.15f}, {0.97f, 1158f, 0.9f}, {0.04f, 1740f, 0.85f},
                {0.97f, 1790f, 1.05f}, {0.03f, 1985f, 0.8f}, {0.99f, 2050f, 1.0f},
                {0.47f, 1690f, 0.6f}, {0.90f, 1912f, 0.65f}
        };
        private static final float[][] GRASS = {
                {0.10f, 1150f, 0.8f}, {0.82f, 1208f, 0.7f}, {0.06f, 1712f, 1f},
                {0.72f, 1760f, 0.9f}, {0.93f, 1640f, 0.8f}, {0.40f, 1866f, 0.7f},
                {0.16f, 1988f, 0.9f}, {0.55f, 2052f, 0.8f}, {0.88f, 2060f, 1f},
                {0.32f, 1262f, 0.7f}, {0.62f, 1332f, 0.6f}, {0.05f, 1422f, 0.8f},
                {0.68f, 1848f, 0.8f}, {0.33f, 2118f, 0.7f}
        };
        private static final float[][] FLOWERS = {
                {0.12f, 1130f, 0f}, {0.46f, 1192f, 1f}, {0.90f, 1332f, 0f},
                {0.25f, 1462f, 1f}, {0.58f, 1620f, 0f}, {0.34f, 1682f, 2f},
                {0.72f, 1730f, 0f}, {0.90f, 1806f, 2f}, {0.52f, 1830f, 1f},
                {0.25f, 1888f, 0f}, {0.86f, 1928f, 1f}, {0.15f, 1985f, 0f},
                {0.44f, 2012f, 2f}, {0.62f, 2070f, 1f}, {0.34f, 2108f, 0f},
                {0.92f, 2052f, 0f}, {0.44f, 1560f, 0f}, {0.08f, 1820f, 1f}
        };
        private static final float[][] STONES = {
                {0.88f, 1640f, 1f}, {0.96f, 1570f, 0.7f}, {0.30f, 2072f, 0.9f},
                {0.65f, 1152f, 0.6f}
        };

        private final MainActivity main;
        private final int current, total;
        private final boolean cloudsUnlocked;

        private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint text = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final RectF rect = new RectF();

        private final Scroller scroller;
        private final VelocityTracker vt;

        private float scrollY, lastY, downX, downY;
        private boolean dragging, animating, initialized;
        private int activePointer = -1;

        PathView(MainActivity main, int current, int total) {
            super(main);
            this.main = main;
            this.current = current;
            this.total = total;
            this.cloudsUnlocked = PuzzlesScreen.allSolved();
            setWillNotDraw(false);
            scroller = new Scroller(main);
            vt = VelocityTracker.obtain();
            text.setTypeface(Typeface.DEFAULT_BOLD);
            text.setTextAlign(Paint.Align.CENTER);
            text.setColor(0xFF734A22);
        }

        private float dp(float v) {
            return v * getResources().getDisplayMetrics().density;
        }

        private float contentHeight() { return dp(CONTENT_DP); }

        private float maxScroll() {
            return Math.max(0f, contentHeight() - getHeight());
        }

        private float jungleY(int i) { return dp(JUNGLE_L1_Y_DP - i * JUNGLE_PITCH_DP); }

        private float cloudY(int i) { return dp(CLOUD_L1_Y_DP - i * CLOUD_PITCH_DP); }

        private float platformRadius(float w) {
            return Math.max(dp(28f), Math.min(w * 0.093f, dp(40f)));
        }

        private float cloudHalf(float w) {
            return Math.max(dp(18f), Math.min(w * 0.060f, dp(26f)));
        }

        private static float clamp(float v, float lo, float hi) {
            return v < lo ? lo : (v > hi ? hi : v);
        }

        @Override protected void onSizeChanged(int w, int h, int ow, int oh) {
            super.onSizeChanged(w, h, ow, oh);
            if (!initialized && h > 0) {
                initialized = true;
                // start with the current jungle platform on the lower part of the screen
                scrollY = clamp(jungleY(current) - h * 0.72f, 0f, maxScroll());
            }
        }

        @Override protected void onDetachedFromWindow() {
            super.onDetachedFromWindow();
            vt.recycle();
        }

        // ---------------------------------------------------------------- input

        @Override public boolean onTouchEvent(MotionEvent e) {
            switch (e.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    scroller.forceFinished(true);
                    animating = false;
                    downX = e.getX();
                    downY = e.getY();
                    lastY = downY;
                    dragging = false;
                    activePointer = e.getPointerId(0);
                    vt.clear();
                    vt.addMovement(e);
                    return true;
                case MotionEvent.ACTION_MOVE: {
                    vt.addMovement(e);
                    int idx = activePointer < 0 ? -1 : e.findPointerIndex(activePointer);
                    if (idx < 0) return true;
                    float y = e.getY(idx);
                    float dy = y - lastY;
                    lastY = y;
                    if (Math.abs(y - downY) > dp(7)) dragging = true;
                    if (dragging) {
                        scrollY = clamp(scrollY - dy, 0f, maxScroll());
                        invalidate();
                    }
                    return true;
                }
                case MotionEvent.ACTION_UP: {
                    vt.addMovement(e);
                    boolean tap = !dragging
                            && Math.abs(e.getX() - downX) < dp(14)
                            && Math.abs(e.getY() - downY) < dp(14);
                    if (tap) {
                        performClick();
                        handleTap(e.getX(), e.getY());
                    } else if (activePointer >= 0) {
                        vt.computeCurrentVelocity(1000);
                        float vy = vt.getYVelocity(activePointer);
                        if (Math.abs(vy) > dp(180)) {
                            scroller.fling(0, Math.round(scrollY), 0, Math.round(-vy),
                                    0, 0, 0, Math.round(maxScroll()));
                            animating = true;
                            postInvalidateOnAnimation();
                        }
                    }
                    activePointer = -1;
                    return true;
                }
                case MotionEvent.ACTION_CANCEL:
                    activePointer = -1;
                    return true;
                default:
                    return super.onTouchEvent(e);
            }
        }

        @Override public boolean performClick() {
            super.performClick();
            return true;
        }

        private void handleTap(float sx, float sy) {
            // taps behind the header/coach and the score panel must not open a level
            if (sy < dp(148f) || sy > getHeight() - dp(148f)) return;
            float x = sx;
            float y = sy + scrollY;
            float w = getWidth();
            float baseR = platformRadius(w);
            for (int i = 0; i < JX.length; i++) {
                float cx = JX[i] * w, cy = jungleY(i);
                if (Math.abs(x - cx) <= baseR * 1.20f
                        && Math.abs(y - cy) <= baseR * 1.30f) {
                    onJungleTap(i);
                    return;
                }
            }
            float half = cloudHalf(w);
            for (int i = 0; i < CX.length; i++) {
                float cx = CX[i] * w, cy = cloudY(i);
                if (Math.abs(x - cx) <= half * 1.15f && Math.abs(y - cy) <= half * 1.15f) {
                    onCloudTap(i);
                    return;
                }
            }
        }

        private void onJungleTap(int i) {
            if (i == current) {
                main.navigate("puzzlesolve");
            } else if (i < current) {
                PuzzlesScreen.jumpTo(i);
                main.navigate("puzzlesolve");
            } else {
                Toast.makeText(getContext(), "Solve the previous puzzles first",
                        Toast.LENGTH_SHORT).show();
            }
        }

        private void onCloudTap(int i) {
            if (cloudsUnlocked) {
                PuzzlesScreen.jumpTo(i % Math.max(1, total));
                main.navigate("puzzlesolve");
            } else {
                Toast.makeText(getContext(), "Solve the previous puzzles first",
                        Toast.LENGTH_SHORT).show();
            }
        }

        /** Bottom-right button: back to the current level, or up to the next zone. */
        void stepToNext() {
            float h = getHeight();
            if (h <= 0) return;
            float cur = jungleY(current);
            boolean currentVisible = cur > scrollY + h * 0.18f && cur < scrollY + h * 0.92f;
            float target = currentVisible
                    ? cloudY(0) - h * 0.52f
                    : cur - h * 0.72f;
            target = clamp(target, 0f, maxScroll());
            scroller.forceFinished(true);
            int from = Math.round(scrollY), to = Math.round(target);
            int duration = Math.min(700, 260 + Math.abs(to - from) / 3);
            scroller.startScroll(0, from, 0, to - from, duration);
            animating = true;
            postInvalidateOnAnimation();
        }

        // ---------------------------------------------------------------- draw

        @Override protected void onDraw(Canvas cv) {
            float w = getWidth(), h = getHeight();
            if (w <= 0 || h <= 0) return;

            if (animating) {
                if (scroller.computeScrollOffset()) {
                    scrollY = clamp(scroller.getCurrY(), 0f, maxScroll());
                    postInvalidateOnAnimation();
                } else {
                    animating = false;
                }
            }
            scrollY = clamp(scrollY, 0f, maxScroll());

            cv.save();
            cv.translate(0, -scrollY);

            drawSky(cv, w);
            drawCloudBackdrops(cv, w);
            drawJungleGround(cv, w);
            drawCloudBank(cv, w);
            drawCloudPlatforms(cv, w);
            drawCloudForeground(cv, w);
            drawJungleDiorama(cv, w);

            cv.restore();
        }

        private void drawSky(Canvas cv, float w) {
            p.setStyle(Paint.Style.FILL);
            p.setShader(new LinearGradient(0, 0, 0, dp(1160),
                    new int[]{0xFFBBD2D8, 0xFFCDDFE2, 0xFFE4EEED},
                    new float[]{0f, 0.55f, 1f}, Shader.TileMode.CLAMP));
            cv.drawRect(0, 0, w, dp(1160), p);
            p.setShader(new RadialGradient(w * 0.78f, dp(90), w * 0.85f,
                    new int[]{0x4DFFFFFF, 0x00FFFFFF}, null, Shader.TileMode.CLAMP));
            cv.drawRect(0, 0, w, dp(1160), p);
            p.setShader(null);
        }

        private void drawCloudBackdrops(Canvas cv, float w) {
            for (float[] cl : CLOUDS) {
                drawCloud(cv, cl[0] * w, dp(cl[1]), cl[2] * w, Math.round(cl[3]));
            }
        }

        private void drawCloud(Canvas cv, float cx, float cy, float s, int alpha) {
            p.setStyle(Paint.Style.FILL);
            p.setShader(null);
            p.setColor(0xFFAEC9CE);
            p.setAlpha(Math.round(alpha * 0.55f));
            cloudShape(cv, cx, cy + s * 0.08f, s);
            p.setColor(0xFFF4F1E3);
            p.setAlpha(alpha);
            cloudShape(cv, cx, cy, s);
            p.setColor(0xFFD3E8EA);
            p.setAlpha(Math.round(alpha * 0.5f));
            cloudShape(cv, cx + s * 0.01f, cy + s * 0.035f, s * 0.74f);
            p.setColor(0xFFFFFFFF);
            p.setAlpha(Math.round(alpha * 0.55f));
            cloudShape(cv, cx - s * 0.04f, cy - s * 0.05f, s * 0.55f);
            p.setAlpha(255);
        }

        private void cloudShape(Canvas cv, float cx, float cy, float s) {
            rect.set(cx - s * 0.90f, cy - s * 0.10f, cx + s * 0.90f, cy + s * 0.42f);
            cv.drawRoundRect(rect, s * 0.26f, s * 0.26f, p);
            cv.drawCircle(cx - s * 0.58f, cy - s * 0.04f, s * 0.30f, p);
            cv.drawCircle(cx - s * 0.24f, cy - s * 0.22f, s * 0.37f, p);
            cv.drawCircle(cx + s * 0.14f, cy - s * 0.29f, s * 0.34f, p);
            cv.drawCircle(cx + s * 0.47f, cy - s * 0.11f, s * 0.28f, p);
            cv.drawCircle(cx + s * 0.72f, cy + s * 0.05f, s * 0.21f, p);
        }

        private void drawCloudBank(Canvas cv, float w) {
            for (int i = 0; i < 7; i++) {
                float f = -0.06f + i * 0.19f;
                float s = w * (0.15f + (i % 3) * 0.025f);
                drawPuff(cv, f * w, dp(1052f) + dp((i % 2) * 9f), s, 236);
            }
        }

        private void drawPuff(Canvas cv, float cx, float cy, float s, int alpha) {
            p.setStyle(Paint.Style.FILL);
            p.setShader(null);
            p.setColor(0xFFD6E7E8);
            p.setAlpha(Math.round(alpha * 0.7f));
            rect.set(cx - s, cy - s * 0.16f, cx + s, cy + s * 0.34f);
            cv.drawRoundRect(rect, s * 0.3f, s * 0.3f, p);
            p.setColor(0xFFF7F4E8);
            p.setAlpha(alpha);
            rect.set(cx - s, cy - s * 0.24f, cx + s, cy + s * 0.30f);
            cv.drawRoundRect(rect, s * 0.3f, s * 0.3f, p);
            cv.drawCircle(cx - s * 0.45f, cy - s * 0.20f, s * 0.30f, p);
            cv.drawCircle(cx, cy - s * 0.34f, s * 0.36f, p);
            cv.drawCircle(cx + s * 0.48f, cy - s * 0.16f, s * 0.28f, p);
            p.setAlpha(255);
        }

        private void drawJungleGround(Canvas cv, float w) {
            float top = dp(1050f);
            float bottom = contentHeight();
            Path ground = new Path();
            ground.moveTo(0, top + dp(16f));
            int seg = 6;
            float sw = w / seg;
            for (int i = 0; i < seg; i++) {
                float x = i * sw;
                float up = (i % 2 == 0) ? dp(14f) : dp(2f);
                ground.cubicTo(x + sw * 0.33f, top - up, x + sw * 0.66f, top + up + dp(6f),
                        x + sw, top + dp(8f));
            }
            ground.lineTo(w, bottom);
            ground.lineTo(0, bottom);
            ground.close();

            p.setStyle(Paint.Style.FILL);
            p.setShader(new LinearGradient(0, top, 0, bottom,
                    new int[]{0xFF3C6836, 0xFF35602F, 0xFF2F562C, 0xFF2B4E2A},
                    new float[]{0f, 0.28f, 0.68f, 1f}, Shader.TileMode.CLAMP));
            cv.drawPath(ground, p);
            p.setShader(null);

            cv.save();
            cv.clipPath(ground);
            drawJungleMosaic(cv, w);
            p.setShader(new RadialGradient(w * 0.70f, dp(1260f), dp(340f),
                    new int[]{0x14FFF6C4, 0x00FFF6C4}, null, Shader.TileMode.CLAMP));
            cv.drawRect(0, top, w, bottom, p);
            p.setShader(new LinearGradient(0, dp(1900f), 0, bottom,
                    new int[]{0x00000000, 0x33000000}, null, Shader.TileMode.CLAMP));
            cv.drawRect(0, dp(1900f), w, bottom, p);
            p.setShader(new LinearGradient(0, 0, w, 0,
                    new int[]{0x3D12300F, 0x0012300F, 0x0012300F, 0x3D12300F},
                    new float[]{0f, 0.18f, 0.82f, 1f}, Shader.TileMode.CLAMP));
            cv.drawRect(0, top, w, bottom, p);
            p.setShader(null);
            cv.restore();
        }

        private void drawJungleMosaic(Canvas cv, float w) {
            float side = dp(112f);
            float half = side / 2f;
            float colStep = dp(160f);
            float rowStep = dp(80f);
            float rad = dp(6f);
            float top = dp(1050f), bottom = contentHeight();
            int firstRow = (int) Math.floor((scrollY - side - top) / rowStep) - 1;
            int lastRow = (int) Math.ceil((scrollY + getHeight() + side - top) / rowStep) + 1;
            int cols = (int) Math.ceil(w / colStep) + 3;
            for (int row = firstRow; row <= lastRow; row++) {
                float cy = top + row * rowStep;
                if (cy < top - side || cy > bottom + side) continue;
                int parity = ((row % 2) + 2) % 2;
                for (int i = -2; i < cols; i++) {
                    float cx = i * colStep + parity * colStep / 2f;
                    if (cx < -side || cx > w + side) continue;
                    int tone = (((row * 7 + i * 3) % 5) + 5) % 5;
                    int base = ((row + i) & 1) == 0 ? 0xFF386637 : 0xFF305B2E;
                    if (tone == 0) base = 0xFF3B6B39;
                    p.setStyle(Paint.Style.FILL);
                    p.setShader(null);
                    p.setColor(base);
                    cv.save();
                    cv.rotate(45f, cx, cy);
                    rect.set(cx - half, cy - half, cx + half, cy + half);
                    cv.drawRoundRect(rect, rad, rad, p);
                    cv.restore();
                }
            }
        }

        private void drawCloudPlatforms(Canvas cv, float w) {
            float half = cloudHalf(w);
            for (int i = CX.length - 1; i >= 0; i--) {
                drawCloudTile(cv, CX[i] * w, cloudY(i), half, String.valueOf(i + 1),
                        cloudsUnlocked);
            }
        }

        private void drawCloudForeground(Canvas cv, float w) {
            drawPuff(cv, w * 0.95f, cloudY(1) + dp(16f), w * 0.30f, 220);
            drawPuff(cv, w * 0.02f, cloudY(0) - dp(8f), w * 0.24f, 205);
            drawPuff(cv, w * 0.55f, dp(1010f), w * 0.20f, 190);
        }

        private void drawJungleDiorama(Canvas cv, float w) {
            float baseR = platformRadius(w);

            drawTree(cv, w * 0.11f, dp(1878f), w * 0.27f);
            for (float[] b : BUSHES) {
                drawBush(cv, b[0] * w, dp(b[1]), w * 0.055f * b[2]);
            }
            drawBench(cv, w * 0.27f, dp(1885f), w * 0.21f);
            drawPond(cv, w * 0.81f, dp(1694f), w * 0.25f);
            drawCabin(cv, w * 0.79f, dp(2075f), w * 0.37f);

            for (float[] g : GRASS) drawGrass(cv, g[0] * w, dp(g[1]), w * 0.02f * g[2]);
            for (float[] f : FLOWERS) {
                drawFlower(cv, f[0] * w, dp(f[1]), w * 0.013f, (int) f[2]);
            }
            for (float[] st : STONES) drawStone(cv, st[0] * w, dp(st[1]), w * 0.024f * st[2]);

            for (int i = JX.length - 1; i >= 0; i--) {
                float cx = JX[i] * w, cy = jungleY(i);
                boolean cur = i == current;
                drawWoodTile(cv, cx, cy, baseR, String.valueOf(i + 1),
                        i < current, cur, i > current);
            }

            // current level: white pawn bubble + big green statue
            int ci = Math.min(current, JX.length - 1);
            float cx0 = JX[ci] * w, cy0 = jungleY(ci), r0 = baseR;
            float pawnH = r0 * 2.3f;
            float baseY = cy0 + dp(4f);
            drawPawnBubble(cv, cx0, baseY - pawnH - dp(15f), dp(34f));
            drawPawn(cv, cx0, baseY, pawnH);

            // a little extra foreground grass so the bottom edge is not empty
            drawGrass(cv, w * 0.08f, contentHeight() - dp(18f), w * 0.026f);
            drawGrass(cv, w * 0.45f, contentHeight() - dp(10f), w * 0.022f);
            drawGrass(cv, w * 0.62f, contentHeight() - dp(26f), w * 0.02f);
        }

        /** Rounded isometric diamond (wider than tall) used by the wooden tiles. */
        private Path diamond(float cx, float cy, float rx, float ry) {
            Path d = new Path();
            d.moveTo(cx, cy - ry);
            d.lineTo(cx + rx, cy);
            d.lineTo(cx, cy + ry);
            d.lineTo(cx - rx, cy);
            d.close();
            return d;
        }

        private void drawWoodTile(Canvas cv, float cx, float cy, float r, String label,
                                  boolean complete, boolean current, boolean locked) {
            float rx = r;
            float ry = r * 0.78f;
            float rimX = r * 0.22f, rimY = ry * 0.22f;
            float depth = r * 0.72f;

            p.setStyle(Paint.Style.FILL);
            p.setShader(null);

            // soft drop shadow
            p.setColor(0x33000000);
            cv.drawOval(cx - rx * 1.22f, cy + ry * 0.55f, cx + rx * 1.22f,
                    cy + ry + depth + dp(7f), p);
            p.setAlpha(255);

            // green halo behind the current level
            if (current) {
                p.setShader(new RadialGradient(cx, cy + ry * 0.25f, rx * 1.75f,
                        new int[]{0x7A8ED06A, 0x338ED06A, 0x008ED06A},
                        new float[]{0f, 0.55f, 1f}, Shader.TileMode.CLAMP));
                cv.drawCircle(cx, cy + ry * 0.25f, rx * 1.75f, p);
                p.setShader(null);
            }

            float ex = rx + rimX, ey = ry + rimY;

            // extruded wooden side
            Path side = new Path();
            side.moveTo(cx - ex, cy);
            side.lineTo(cx, cy + ey);
            side.lineTo(cx + ex, cy);
            side.lineTo(cx + ex, cy + depth);
            side.lineTo(cx, cy + ey + depth);
            side.lineTo(cx - ex, cy + depth);
            side.close();
            p.setShader(new LinearGradient(0, cy, 0, cy + ey + depth,
                    0xFF6B4529, 0xFF3C2211, Shader.TileMode.CLAMP));
            cv.drawPath(side, p);
            p.setShader(null);

            // stacked wood layers on the exposed side
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(Math.max(1.2f, r * 0.07f));
            p.setColor(0x553D2413);
            for (int k = 1; k <= 3; k++) {
                float yy = cy + depth * k / 3f;
                Path band = new Path();
                band.moveTo(cx - ex * 0.94f, yy + ey * 0.55f);
                band.lineTo(cx, yy + ey * 0.94f);
                band.lineTo(cx + ex * 0.94f, yy + ey * 0.55f);
                cv.drawPath(band, p);
            }
            p.setColor(0x2EF2D79C);
            Path sideShine = new Path();
            sideShine.moveTo(cx - ex * 0.94f, cy + depth * 0.10f + ey * 0.55f);
            sideShine.lineTo(cx, cy + depth * 0.10f + ey * 0.94f);
            sideShine.lineTo(cx + ex * 0.94f, cy + depth * 0.10f + ey * 0.55f);
            cv.drawPath(sideShine, p);
            p.setStyle(Paint.Style.FILL);
            p.setAlpha(255);

            // brown rim around the cream face
            p.setShader(new LinearGradient(cx - rx, cy - ry, cx + rx, cy + ry,
                    0xFF8A5A34, 0xFF573519, Shader.TileMode.CLAMP));
            cv.drawPath(diamond(cx, cy, ex, ey), p);
            p.setShader(null);

            // cream top face
            p.setShader(new LinearGradient(cx - rx, cy - ry, cx + rx, cy + ry,
                    0xFFF9E29B, 0xFFE7C066, Shader.TileMode.CLAMP));
            cv.drawPath(diamond(cx, cy, rx, ry), p);
            p.setShader(null);

            // tree rings
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(Math.max(1.1f, r * 0.075f));
            p.setColor(0x5C5D3D2E);
            cv.drawPath(diamond(cx, cy + ry * 0.03f, rx * 0.64f, ry * 0.64f), p);
            p.setStrokeWidth(Math.max(1f, r * 0.055f));
            p.setColor(0x475D3D2E);
            cv.drawPath(diamond(cx, cy + ry * 0.05f, rx * 0.40f, ry * 0.40f), p);
            p.setStyle(Paint.Style.FILL);

            p.setColor(0x38FFFFFF);
            cv.drawPath(diamond(cx, cy - ry * 0.34f, rx * 0.52f, ry * 0.26f), p);

            text.setTypeface(Typeface.DEFAULT_BOLD);
            text.setTextAlign(Paint.Align.CENTER);
            text.setTextSize(r * 1.05f);
            Paint.FontMetrics fm = text.getFontMetrics();
            float baseline = cy - (fm.ascent + fm.descent) / 2f + r * 0.03f;
            text.setStyle(Paint.Style.STROKE);
            text.setStrokeWidth(Math.max(1.5f, r * 0.075f));
            text.setStrokeJoin(Paint.Join.ROUND);
            text.setColor(0xFF4A2A0E);
            cv.drawText(label, cx, baseline, text);
            text.setStyle(Paint.Style.FILL);
            text.setColor(0xFF6E4318);
            cv.drawText(label, cx, baseline, text);
        }

        private void drawCloudTile(Canvas cv, float cx, float cy, float half, String label,
                                   boolean unlocked) {
            float s = half * 0.72f;
            float depth = s * 0.30f;
            int alpha = unlocked ? 255 : 238;

            p.setStyle(Paint.Style.FILL);
            p.setShader(null);
            p.setColor(0x22384E52);
            cv.drawOval(cx - half * 1.15f, cy + half * 0.55f, cx + half * 1.15f,
                    cy + half * 1.02f, p);

            p.setAlpha(alpha);
            p.setShader(new LinearGradient(cx - s, cy - s, cx + s, cy + s + depth,
                    0xFFEDE8D7, 0xFFC9D4CF, Shader.TileMode.CLAMP));
            drawRotRect(cv, cx, cy + depth, s, s * 0.24f);
            p.setShader(new LinearGradient(cx - s, cy - s, cx + s, cy + s,
                    0xFFF8F3E3, 0xFFE8E3CE, Shader.TileMode.CLAMP));
            drawRotRect(cv, cx, cy, s, s * 0.24f);
            p.setShader(null);

            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(Math.max(1f, s * 0.06f));
            p.setColor(0xCCFCFAF1);
            drawRotRect(cv, cx, cy, s * 0.80f, s * 0.20f);
            p.setStrokeWidth(Math.max(1f, s * 0.045f));
            p.setColor(0x4DC8D3CB);
            drawRotRect(cv, cx, cy, s * 0.58f, s * 0.14f);
            p.setStyle(Paint.Style.FILL);

            text.setTextSize(s * 1.02f);
            Paint.FontMetrics fm = text.getFontMetrics();
            text.setColor(unlocked ? 0xFF7E8A76 : 0xFF98A18F);
            cv.drawText(label, cx, cy - (fm.ascent + fm.descent) / 2f, text);
            p.setAlpha(255);
        }

        private void drawRotRect(Canvas cv, float cx, float cy, float s, float rad) {
            cv.save();
            cv.rotate(45f, cx, cy);
            rect.set(cx - s, cy - s, cx + s, cy + s);
            cv.drawRoundRect(rect, rad, rad, p);
            cv.restore();
        }

        private void drawTree(Canvas cv, float cx, float baseY, float s) {
            p.setStyle(Paint.Style.FILL);
            p.setShader(null);
            p.setColor(0x33000000);
            cv.drawOval(cx - s * 0.55f, baseY - dp(7f), cx + s * 0.62f, baseY + dp(9f), p);

            Path trunk = new Path();
            trunk.moveTo(cx - s * 0.15f, baseY);
            trunk.lineTo(cx - s * 0.085f, baseY - s * 1.28f);
            trunk.lineTo(cx + s * 0.09f, baseY - s * 1.28f);
            trunk.lineTo(cx + s * 0.17f, baseY);
            trunk.close();
            p.setColor(0xFF7C502D);
            cv.drawPath(trunk, p);

            Path light = new Path();
            light.moveTo(cx - s * 0.15f, baseY);
            light.lineTo(cx - s * 0.085f, baseY - s * 1.26f);
            light.lineTo(cx - s * 0.015f, baseY - s * 1.26f);
            light.lineTo(cx - s * 0.045f, baseY);
            light.close();
            p.setColor(0xFF986538);
            cv.drawPath(light, p);

            p.setColor(0xFF6A4225);
            p.setStrokeWidth(Math.max(1.5f, s * 0.035f));
            p.setStyle(Paint.Style.STROKE);
            cv.drawLine(cx - s * 0.04f, baseY - s * 0.82f, cx - s * 0.34f, baseY - s * 1.10f, p);
            cv.drawLine(cx + s * 0.03f, baseY - s * 0.95f, cx + s * 0.30f, baseY - s * 1.20f, p);
            p.setStyle(Paint.Style.FILL);

            drawCanopy(cv, cx, baseY - s * 1.42f, s, 0xFF2E6737);
            drawCanopy(cv, cx - s * 0.06f, baseY - s * 1.50f, s * 0.86f, 0xFF3E7D42);
            drawCanopy(cv, cx - s * 0.13f, baseY - s * 1.58f, s * 0.55f, 0xFF53994F);

            p.setColor(0x4DE3F2B9);
            cv.drawCircle(cx - s * 0.34f, baseY - s * 1.72f, s * 0.10f, p);
            cv.drawCircle(cx + s * 0.10f, baseY - s * 1.60f, s * 0.075f, p);
        }

        private void drawCanopy(Canvas cv, float cx, float cy, float s, int color) {
            p.setColor(color);
            cv.drawCircle(cx - s * 0.52f, cy + s * 0.10f, s * 0.42f, p);
            cv.drawCircle(cx + s * 0.50f, cy + s * 0.08f, s * 0.40f, p);
            cv.drawCircle(cx - s * 0.05f, cy - s * 0.28f, s * 0.46f, p);
            cv.drawCircle(cx - s * 0.30f, cy - s * 0.02f, s * 0.40f, p);
            cv.drawCircle(cx + s * 0.28f, cy - s * 0.10f, s * 0.37f, p);
        }

        private void drawCabin(Canvas cv, float cx, float baseY, float cw) {
            float h = cw * 0.80f;
            p.setStyle(Paint.Style.FILL);
            p.setShader(null);

            p.setColor(0x3D000000);
            cv.drawOval(cx - cw * 0.62f, baseY - dp(7f), cx + cw * 0.60f, baseY + dp(11f), p);
            p.setAlpha(255);

            // log wall
            rect.set(cx - cw * 0.5f, baseY - h * 0.58f, cx + cw * 0.5f, baseY);
            p.setShader(new LinearGradient(0, rect.top, 0, baseY,
                    0xFFC08D4F, 0xFF9A6633, Shader.TileMode.CLAMP));
            cv.drawRoundRect(rect, dp(3f), dp(3f), p);
            p.setShader(null);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(Math.max(1f, dp(1.1f)));
            p.setColor(0x55713F1B);
            for (float y = rect.top + dp(8f); y < baseY - dp(2f); y += dp(9f)) {
                cv.drawLine(rect.left + dp(2f), y, rect.right - dp(2f), y, p);
            }
            p.setStyle(Paint.Style.FILL);
            p.setColor(0x1F000000);
            cv.drawRect(cx + cw * 0.22f, rect.top, rect.right, baseY, p);

            // roof
            Path roof = new Path();
            roof.moveTo(cx - cw * 0.63f, baseY - h * 0.52f);
            roof.lineTo(cx - cw * 0.34f, baseY - h * 0.62f);
            roof.lineTo(cx, baseY - h * 1.03f);
            roof.lineTo(cx + cw * 0.34f, baseY - h * 0.62f);
            roof.lineTo(cx + cw * 0.63f, baseY - h * 0.52f);
            roof.close();
            p.setColor(0xFF5C4736);
            cv.drawPath(roof, p);

            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(Math.max(1f, dp(1.1f)));
            p.setColor(0x38000000);
            for (int k = 0; k < 3; k++) {
                float ry = baseY - h * (0.90f - k * 0.105f);
                float rw = cw * (0.16f + k * 0.115f);
                cv.drawLine(cx - rw, ry, cx + rw, ry, p);
            }
            p.setColor(0x557B6653);
            p.setStrokeWidth(Math.max(1f, dp(1.6f)));
            cv.drawLine(cx, baseY - h * 1.02f, cx, baseY - h * 0.63f, p);
            p.setStyle(Paint.Style.FILL);

            // roof shingle rows
            cv.save();
            cv.clipPath(roof);
            p.setStyle(Paint.Style.FILL);
            p.setColor(0x55302318);
            for (int ry = 0; ry < 5; ry++) {
                float y = baseY - h * (0.97f - ry * 0.085f);
                float x0 = cx - cw * (0.10f + ry * 0.105f);
                float x1 = cx + cw * (0.10f + ry * 0.105f);
                for (float x = x0 + cw * 0.012f; x < x1; x += cw * 0.034f) {
                    cv.drawCircle(x, y, cw * 0.020f, p);
                }
            }
            cv.restore();

            // chimney standing on the right roof slope
            rect.set(cx + cw * 0.22f, baseY - h * 1.00f, cx + cw * 0.33f, baseY - h * 0.80f);
            p.setColor(0xFF715C4B);
            cv.drawRoundRect(rect, dp(2f), dp(2f), p);
            p.setColor(0xFF8B7663);
            cv.drawRect(rect.left - dp(2f), rect.top, rect.right + dp(2f), rect.top + dp(3.5f), p);
            p.setColor(0x33000000);
            cv.drawRect(rect.left, rect.top + dp(3.5f), rect.right, rect.top + dp(5f), p);

            // eaves beam
            rect.set(cx - cw * 0.64f, baseY - h * 0.555f, cx + cw * 0.64f, baseY - h * 0.495f);
            p.setColor(0xFF8A5A32);
            cv.drawRoundRect(rect, dp(2f), dp(2f), p);

            // door with cream frame
            rect.set(cx - cw * 0.34f, baseY - h * 0.55f, cx - cw * 0.10f, baseY);
            p.setColor(0xFF4B3220);
            cv.drawRoundRect(rect, dp(3f), dp(3f), p);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(Math.max(1.2f, dp(1.6f)));
            p.setColor(0xFFE4CE9E);
            cv.drawRoundRect(rect, dp(3f), dp(3f), p);
            p.setStyle(Paint.Style.FILL);
            p.setColor(0xFFE2B75E);
            cv.drawCircle(rect.right - cw * 0.045f, rect.centerY() + h * 0.08f, dp(1.6f), p);

            // little white bird perched on the roof ridge
            p.setColor(0xFFF4F1E6);
            cv.drawCircle(cx - cw * 0.30f, baseY - h * 0.97f, cw * 0.028f, p);
            cv.drawCircle(cx - cw * 0.275f, baseY - h * 1.00f, cw * 0.022f, p);
            Path wing = new Path();
            wing.moveTo(cx - cw * 0.318f, baseY - h * 0.975f);
            wing.lineTo(cx - cw * 0.360f, baseY - h * 0.995f);
            wing.lineTo(cx - cw * 0.300f, baseY - h * 0.990f);
            wing.close();
            cv.drawPath(wing, p);

            // cream plaque with the level number on the door
            float bs = cw * 0.115f;
            float px0 = rect.centerX() - bs / 2f, py0 = baseY - h * 0.44f;
            rect.set(px0 - dp(2.5f), py0 - dp(2.5f), px0 + bs + dp(2.5f), py0 + bs + dp(2.5f));
            p.setColor(0xFF6B4526);
            cv.drawRoundRect(rect, dp(2f), dp(2f), p);
            rect.set(px0, py0, px0 + bs, py0 + bs);
            p.setColor(0xFFF1E6C8);
            cv.drawRoundRect(rect, dp(1.5f), dp(1.5f), p);
            text.setTextSize(bs * 0.78f);
            Paint.FontMetrics pfm = text.getFontMetrics();
            text.setColor(0xFF4E7A34);
            cv.drawText("1", rect.centerX(), rect.centerY() - (pfm.ascent + pfm.descent) / 2f, text);

            // window with the white cabin cat
            rect.set(cx + cw * 0.06f, baseY - h * 0.52f, cx + cw * 0.30f, baseY - h * 0.28f);
            p.setColor(0xFFF3E2B1);
            cv.drawRoundRect(rect, dp(1.5f), dp(1.5f), p);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(Math.max(1f, dp(1.6f)));
            p.setColor(0xFF6E4A28);
            cv.drawRoundRect(rect, dp(1.5f), dp(1.5f), p);
            p.setStyle(Paint.Style.FILL);

            float catX = rect.centerX(), catY = rect.centerY() + rect.height() * 0.16f;
            float catR = rect.width() * 0.24f;
            p.setColor(0xFFF4EFE2);
            cv.drawCircle(catX, catY, catR, p);
            Path ears = new Path();
            ears.moveTo(catX - catR * 0.92f, catY - catR * 0.36f);
            ears.lineTo(catX - catR * 0.58f, catY - catR * 1.18f);
            ears.lineTo(catX - catR * 0.10f, catY - catR * 0.62f);
            ears.close();
            ears.moveTo(catX + catR * 0.92f, catY - catR * 0.36f);
            ears.lineTo(catX + catR * 0.58f, catY - catR * 1.18f);
            ears.lineTo(catX + catR * 0.10f, catY - catR * 0.62f);
            ears.close();
            cv.drawPath(ears, p);
            p.setColor(0xFFC9A77C);
            cv.drawCircle(catX - catR * 0.40f, catY - catR * 0.34f, catR * 0.16f, p);
            cv.drawCircle(catX + catR * 0.30f, catY - catR * 0.46f, catR * 0.13f, p);
        }

        private void drawChecker(Canvas cv, RectF area, int cells, int light, int dark) {
            p.setStyle(Paint.Style.FILL);
            p.setColor(light);
            cv.drawRect(area, p);
            p.setColor(dark);
            float cw = area.width() / cells, ch = area.height() / cells;
            for (int y = 0; y < cells; y++) {
                for (int x = 0; x < cells; x++) {
                    if (((x + y) & 1) == 1) {
                        cv.drawRect(area.left + x * cw, area.top + y * ch,
                                area.left + (x + 1) * cw, area.top + (y + 1) * ch, p);
                    }
                }
            }
        }

        private void drawBench(Canvas cv, float cx, float baseY, float s) {
            p.setStyle(Paint.Style.FILL);
            p.setShader(null);
            p.setColor(0x30000000);
            cv.drawOval(cx - s * 1.15f, baseY - dp(5f), cx + s * 1.15f, baseY + dp(7f), p);

            // legs
            p.setColor(0xFF674326);
            cv.drawRoundRect(cx - s * 0.84f, baseY - s * 0.44f, cx - s * 0.73f, baseY,
                    dp(1.5f), dp(1.5f), p);
            cv.drawRoundRect(cx + s * 0.73f, baseY - s * 0.44f, cx + s * 0.84f, baseY,
                    dp(1.5f), dp(1.5f), p);

            // backrest
            rect.set(cx - s * 0.98f, baseY - s * 0.88f, cx + s * 0.98f, baseY - s * 0.62f);
            p.setColor(0xFF905E33);
            cv.drawRoundRect(rect, dp(3f), dp(3f), p);
            p.setStyle(Paint.Style.STROKE);
            p.setColor(0x55713F1B);
            p.setStrokeWidth(Math.max(1f, dp(1f)));
            for (float x = rect.left + s * 0.18f; x < rect.right; x += s * 0.22f) {
                cv.drawLine(x, rect.top + dp(2f), x, rect.bottom - dp(2f), p);
            }
            p.setStyle(Paint.Style.FILL);

            // armrests
            p.setColor(0xFF7A4E2A);
            cv.drawRoundRect(cx - s * 1.02f, baseY - s * 0.60f, cx - s * 0.92f, baseY - s * 0.30f,
                    dp(1.5f), dp(1.5f), p);
            cv.drawRoundRect(cx + s * 0.92f, baseY - s * 0.60f, cx + s * 1.02f, baseY - s * 0.30f,
                    dp(1.5f), dp(1.5f), p);

            // seat
            rect.set(cx - s, baseY - s * 0.40f, cx + s, baseY - s * 0.20f);
            p.setColor(0xFFA97742);
            cv.drawRoundRect(rect, dp(3f), dp(3f), p);
            p.setColor(0x55713F1B);
            cv.drawLine(rect.left + dp(2f), rect.centerY(), rect.right - dp(2f), rect.centerY(), p);

            // chessboard on the seat
            RectF board = new RectF(cx - s * 0.58f, baseY - s * 0.385f,
                    cx + s * 0.08f, baseY - s * 0.225f);
            p.setColor(0xFF4E3118);
            cv.drawRoundRect(new RectF(board.left - dp(1.5f), board.top - dp(1.5f),
                    board.right + dp(1.5f), board.bottom + dp(1.5f)), dp(2f), dp(2f), p);
            drawChecker(cv, board, 4, 0xFFF2E6C6, 0xFF5F4A30);

            // cream pawn statue on the bench
            drawMiniPawn(cv, cx + s * 0.52f, baseY - s * 0.30f, s * 0.62f);
        }

        private void drawMiniPawn(Canvas cv, float cx, float baseY, float h) {
            float w = h * 0.52f;
            p.setColor(0x2E000000);
            cv.drawOval(cx - w * 0.55f, baseY - dp(2f), cx + w * 0.55f, baseY + dp(3f), p);
            p.setColor(0xFFE9E2CE);
            cv.drawRoundRect(cx - w * 0.48f, baseY - h * 0.15f, cx + w * 0.48f, baseY,
                    w * 0.12f, w * 0.12f, p);
            Path body = new Path();
            body.moveTo(cx - w * 0.16f, baseY - h * 0.13f);
            body.lineTo(cx - w * 0.24f, baseY - h * 0.52f);
            body.lineTo(cx + w * 0.24f, baseY - h * 0.52f);
            body.lineTo(cx + w * 0.16f, baseY - h * 0.13f);
            body.close();
            cv.drawPath(body, p);
            cv.drawRoundRect(cx - w * 0.40f, baseY - h * 0.60f, cx + w * 0.40f, baseY - h * 0.50f,
                    w * 0.08f, w * 0.08f, p);
            cv.drawCircle(cx, baseY - h * 0.73f, w * 0.27f, p);
            p.setColor(0xFFFFFFFF);
            cv.drawCircle(cx - w * 0.10f, baseY - h * 0.79f, w * 0.10f, p);
        }

        private void drawPond(Canvas cv, float cx, float cy, float s) {
            p.setStyle(Paint.Style.FILL);
            p.setShader(null);

            // grass fringe
            p.setColor(0xFF357D3B);
            for (int i = 0; i < 12; i++) {
                double a = Math.PI * 2 * i / 12.0;
                float x = cx + (float) Math.cos(a) * s * 0.98f;
                float y = cy + (float) Math.sin(a) * s * 0.62f;
                cv.drawCircle(x, y, s * 0.13f, p);
            }
            p.setColor(0xFF4C9449);
            for (int i = 0; i < 8; i++) {
                double a = Math.PI * 2 * (i + 0.5) / 8.0;
                cv.drawCircle(cx + (float) Math.cos(a) * s * 0.82f,
                        cy + (float) Math.sin(a) * s * 0.50f, s * 0.09f, p);
            }

            // water
            p.setColor(0xFF1D6F91);
            cv.drawOval(cx - s, cy - s * 0.66f, cx + s, cy + s * 0.66f, p);
            p.setColor(0xFF2E9AC6);
            cv.drawOval(cx - s * 0.95f, cy - s * 0.60f, cx + s * 0.95f, cy + s * 0.60f, p);
            p.setColor(0xFF4EC3E8);
            cv.drawOval(cx - s * 0.86f, cy - s * 0.50f, cx + s * 0.80f, cy + s * 0.46f, p);
            p.setColor(0x6688E1F5);
            cv.drawOval(cx - s * 0.62f, cy - s * 0.36f, cx + s * 0.34f, cy + s * 0.10f, p);

            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(Math.max(1f, s * 0.028f));
            p.setColor(0x66DFF7FF);
            cv.drawArc(new RectF(cx - s * 0.72f, cy - s * 0.30f, cx + s * 0.05f, cy + s * 0.30f),
                    200f, 70f, false, p);
            cv.drawArc(new RectF(cx - s * 0.15f, cy + s * 0.05f, cx + s * 0.62f, cy + s * 0.48f),
                    205f, 60f, false, p);
            p.setStyle(Paint.Style.FILL);

            // lily pads
            drawLily(cv, cx - s * 0.42f, cy - s * 0.10f, s * 0.21f);
            drawLily(cv, cx - s * 0.05f, cy + s * 0.24f, s * 0.17f);
            drawLily(cv, cx + s * 0.30f, cy - s * 0.18f, s * 0.14f);

            // pink flower on the big pad
            float fx = cx - s * 0.44f, fy = cy - s * 0.17f;
            p.setColor(0xFFF084A2);
            for (int i = 0; i < 5; i++) {
                double a = Math.PI * 2 * i / 5.0;
                cv.drawCircle(fx + (float) Math.cos(a) * s * 0.06f,
                        fy + (float) Math.sin(a) * s * 0.06f, s * 0.05f, p);
            }
            p.setColor(0xFFFFE07A);
            cv.drawCircle(fx, fy, s * 0.045f, p);

            // frog on the right rim
            float frx = cx + s * 0.60f, fry = cy - s * 0.46f;
            p.setColor(0xFF77B94C);
            cv.drawOval(frx - s * 0.15f, fry - s * 0.09f, frx + s * 0.16f, fry + s * 0.10f, p);
            cv.drawCircle(frx - s * 0.07f, fry - s * 0.10f, s * 0.055f, p);
            cv.drawCircle(frx + s * 0.07f, fry - s * 0.10f, s * 0.055f, p);
            p.setColor(0xFFF6F6EC);
            cv.drawCircle(frx - s * 0.07f, fry - s * 0.11f, s * 0.032f, p);
            cv.drawCircle(frx + s * 0.07f, fry - s * 0.11f, s * 0.032f, p);
            p.setColor(0xFF2E2E28);
            cv.drawCircle(frx - s * 0.07f, fry - s * 0.11f, s * 0.017f, p);
            cv.drawCircle(frx + s * 0.07f, fry - s * 0.11f, s * 0.017f, p);
            p.setColor(0x5572A63F);
            cv.drawOval(frx - s * 0.10f, fry - s * 0.03f, frx + s * 0.12f, fry + s * 0.07f, p);

            // rocks
            drawStone(cv, cx + s * 0.58f, cy + s * 0.32f, s * 0.13f);
            drawStone(cv, cx - s * 0.08f, cy - s * 0.55f, s * 0.07f);
        }

        private void drawLily(Canvas cv, float cx, float cy, float r) {
            p.setColor(0xFF4E9A44);
            cv.drawOval(cx - r, cy - r * 0.48f, cx + r, cy + r * 0.48f, p);
            p.setColor(0xFF3A7C36);
            Path notch = new Path();
            notch.moveTo(cx, cy);
            notch.lineTo(cx + r * 0.98f, cy - r * 0.22f);
            notch.lineTo(cx + r * 0.98f, cy + r * 0.18f);
            notch.close();
            cv.drawPath(notch, p);
            p.setColor(0x33D6F0A8);
            cv.drawOval(cx - r * 0.7f, cy - r * 0.30f, cx + r * 0.2f, cy + r * 0.04f, p);
        }

        private void drawStone(Canvas cv, float cx, float cy, float r) {
            p.setColor(0x33000000);
            cv.drawOval(cx - r * 1.05f, cy + r * 0.15f, cx + r * 1.05f, cy + r * 0.55f, p);
            p.setColor(0xFF8D948C);
            cv.drawOval(cx - r, cy - r * 0.55f, cx + r, cy + r * 0.45f, p);
            p.setColor(0xFFA9AFA5);
            cv.drawOval(cx - r * 0.72f, cy - r * 0.48f, cx + r * 0.30f, cy + r * 0.12f, p);
            p.setColor(0x66FFFFFF);
            cv.drawOval(cx - r * 0.45f, cy - r * 0.38f, cx + r * 0.02f, cy - r * 0.08f, p);
        }

        private void drawBush(Canvas cv, float cx, float cy, float r) {
            p.setColor(0x33000000);
            cv.drawOval(cx - r * 1.15f, cy + r * 0.32f, cx + r * 1.15f, cy + r * 0.70f, p);
            p.setColor(0xFF2C5F34);
            cv.drawCircle(cx, cy, r, p);
            cv.drawCircle(cx - r * 0.78f, cy + r * 0.28f, r * 0.68f, p);
            cv.drawCircle(cx + r * 0.82f, cy + r * 0.24f, r * 0.62f, p);
            p.setColor(0xFF3D7B43);
            cv.drawCircle(cx - r * 0.22f, cy - r * 0.12f, r * 0.72f, p);
            cv.drawCircle(cx + r * 0.55f, cy - r * 0.02f, r * 0.48f, p);
            p.setColor(0x665AA35B);
            cv.drawCircle(cx - r * 0.34f, cy - r * 0.38f, r * 0.30f, p);
        }

        private void drawGrass(Canvas cv, float cx, float cy, float r) {
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeCap(Paint.Cap.ROUND);
            p.setStrokeWidth(Math.max(1f, r * 0.18f));
            p.setColor(0xFF2F6D37);
            cv.drawLine(cx, cy, cx - r * 0.55f, cy - r * 1.1f, p);
            cv.drawLine(cx, cy, cx, cy - r * 1.35f, p);
            cv.drawLine(cx, cy, cx + r * 0.55f, cy - r * 1.15f, p);
            p.setColor(0xFF68A65A);
            cv.drawLine(cx + r * 0.2f, cy, cx + r * 0.75f, cy - r * 0.7f, p);
            p.setStyle(Paint.Style.FILL);
        }

        private void drawFlower(Canvas cv, float cx, float cy, float r, int kind) {
            int petal = kind == 0 ? 0xFFF7F4EA : (kind == 1 ? 0xFFF2A4B6 : 0xFFF6D36B);
            p.setColor(0xFF3E7C41);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(Math.max(1f, r * 0.28f));
            cv.drawLine(cx, cy + r * 0.2f, cx, cy + r * 1.5f, p);
            p.setStyle(Paint.Style.FILL);
            p.setColor(petal);
            for (int i = 0; i < 5; i++) {
                double a = Math.PI * 2 * i / 5.0 - Math.PI / 2;
                cv.drawCircle(cx + (float) Math.cos(a) * r * 0.62f,
                        cy + (float) Math.sin(a) * r * 0.62f, r * 0.44f, p);
            }
            p.setColor(0xFFE8B84E);
            cv.drawCircle(cx, cy, r * 0.38f, p);
        }

        private void drawPawn(Canvas cv, float cx, float baseY, float h) {
            float w = h * 0.58f;
            p.setStyle(Paint.Style.FILL);
            p.setShader(null);
            p.setColor(0x55000000);
            cv.drawOval(cx - w * 0.46f, baseY - h * 0.04f,
                    cx + w * 0.46f, baseY + h * 0.075f, p);

            p.setColor(0xFF58A043);
            cv.drawRoundRect(cx - w * 0.46f, baseY - h * 0.16f,
                    cx + w * 0.46f, baseY, w * 0.16f, w * 0.16f, p);
            Path stem = new Path();
            stem.moveTo(cx - w * 0.14f, baseY - h * 0.13f);
            stem.lineTo(cx - w * 0.24f, baseY - h * 0.46f);
            stem.lineTo(cx + w * 0.24f, baseY - h * 0.46f);
            stem.lineTo(cx + w * 0.14f, baseY - h * 0.13f);
            stem.close();
            cv.drawPath(stem, p);
            cv.drawRoundRect(cx - w * 0.40f, baseY - h * 0.545f,
                    cx + w * 0.40f, baseY - h * 0.44f, w * 0.09f, w * 0.09f, p);
            cv.drawRoundRect(cx - w * 0.11f, baseY - h * 0.63f,
                    cx + w * 0.11f, baseY - h * 0.52f, w * 0.05f, w * 0.05f, p);
            cv.drawCircle(cx, baseY - h * 0.755f, w * 0.30f, p);

            p.setColor(0xFF9ADD6D);
            cv.drawRoundRect(cx - w * 0.41f, baseY - h * 0.135f,
                    cx + w * 0.41f, baseY - h * 0.02f, w * 0.13f, w * 0.13f, p);
            Path inner = new Path();
            inner.moveTo(cx - w * 0.105f, baseY - h * 0.135f);
            inner.lineTo(cx - w * 0.195f, baseY - h * 0.44f);
            inner.lineTo(cx + w * 0.195f, baseY - h * 0.44f);
            inner.lineTo(cx + w * 0.105f, baseY - h * 0.135f);
            inner.close();
            cv.drawPath(inner, p);
            cv.drawRoundRect(cx - w * 0.36f, baseY - h * 0.525f,
                    cx + w * 0.36f, baseY - h * 0.455f, w * 0.065f, w * 0.065f, p);
            cv.drawRoundRect(cx - w * 0.085f, baseY - h * 0.615f,
                    cx + w * 0.085f, baseY - h * 0.50f, w * 0.04f, w * 0.04f, p);
            cv.drawCircle(cx - w * 0.01f, baseY - h * 0.765f, w * 0.255f, p);
            p.setShader(new RadialGradient(cx - w * 0.12f, baseY - h * 0.82f, w * 0.42f,
                    new int[]{0x77EBFFC0, 0x00EBFFC0}, null, Shader.TileMode.CLAMP));
            p.setColor(0xFFFFFFFF);
            cv.drawCircle(cx - w * 0.01f, baseY - h * 0.765f, w * 0.255f, p);
            p.setShader(null);
            p.setColor(0xFFD6F5B0);
            cv.drawCircle(cx - w * 0.11f, baseY - h * 0.84f, w * 0.075f, p);
        }

        private void drawPawnBubble(Canvas cv, float cx, float cy, float size) {
            float half = size / 2f;
            p.setStyle(Paint.Style.FILL);
            p.setShader(null);
            p.setColor(0x3D000000);
            rect.set(cx - half, cy - half + dp(1.5f), cx + half, cy + half + dp(1.5f));
            cv.drawRoundRect(rect, dp(7f), dp(7f), p);

            p.setColor(0xFFFAF9F4);
            rect.set(cx - half, cy - half, cx + half, cy + half);
            cv.drawRoundRect(rect, dp(7f), dp(7f), p);
            Path tail = new Path();
            tail.moveTo(cx - dp(5f), cy + half - dp(1f));
            tail.lineTo(cx, cy + half + dp(6f));
            tail.lineTo(cx + dp(5f), cy + half - dp(1f));
            tail.close();
            cv.drawPath(tail, p);

            // grey pawn silhouette
            float ph = size * 0.60f, pw = ph * 0.62f;
            float by = cy + size * 0.25f;
            p.setColor(0xFFA9A7A2);
            cv.drawRoundRect(cx - pw * 0.60f, by - ph * 0.10f, cx + pw * 0.60f, by,
                    pw * 0.14f, pw * 0.14f, p);
            cv.drawRoundRect(cx - pw * 0.52f, by - ph * 0.19f, cx + pw * 0.52f, by,
                    pw * 0.16f, pw * 0.16f, p);
            Path body = new Path();
            body.moveTo(cx - pw * 0.20f, by - ph * 0.17f);
            body.lineTo(cx - pw * 0.30f, by - ph * 0.50f);
            body.lineTo(cx + pw * 0.30f, by - ph * 0.50f);
            body.lineTo(cx + pw * 0.20f, by - ph * 0.17f);
            body.close();
            cv.drawPath(body, p);
            cv.drawRoundRect(cx - pw * 0.46f, by - ph * 0.58f, cx + pw * 0.46f, by - ph * 0.47f,
                    pw * 0.09f, pw * 0.09f, p);
            cv.drawCircle(cx, by - ph * 0.76f, pw * 0.30f, p);
        }
    }
}
