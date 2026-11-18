package com.eldar.chess;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Puzzles hub - the winding jungle path of puzzle levels, drawn natively. */
public class PuzzlesHubScreen {

    public static View build(final MainActivity main) {
        final Context c = main;
        PuzzlesScreen.ensureQueue();

        FrameLayout root = new FrameLayout(c);

        FrameLayout pathWrap = new FrameLayout(c);
        pathWrap.addView(new PathView(c), new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        root.addView(pathWrap, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        // top scrim + title
        View topScrim = new View(c);
        topScrim.setBackground(new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{0x7A000000, 0x33000000, 0x00000000}));
        FrameLayout.LayoutParams tsp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(c, 110), Gravity.TOP);
        root.addView(topScrim, tsp);

        LinearLayout header = Ui.linear(c, LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(Ui.dp(c, 14), Ui.dp(c, 8), Ui.dp(c, 12), 0);
        TextView icon = Ui.glyph(c, "extension", 26, 0xFFBFD98A);
        header.addView(icon, new LinearLayout.LayoutParams(Ui.dp(c, 28), Ui.dp(c, 30)));
        TextView title = Ui.text(c, " Puzzles", 21, Ui.WHITE, true);
        title.setGravity(Gravity.CENTER_VERTICAL);
        header.addView(title);
        header.addView(Ui.spacerWeight(c, 1f));
        TextView trophy = Ui.glyph(c, "emoji_events", 22, 0xFFE8C87E);
        trophy.setPadding(Ui.dp(c, 10), Ui.dp(c, 6), Ui.dp(c, 10), Ui.dp(c, 6));
        trophy.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                MoreDetailScreen.open(main, "leaderboard");
            }
        });
        header.addView(trophy);
        root.addView(header, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(c, 52), Gravity.TOP));

        // bottom scrim
        View bottomScrim = new View(c);
        bottomScrim.setBackground(new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
                new int[]{0xE0000000, 0xB8000000, 0x00000000}));
        root.addView(bottomScrim, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(c, 250), Gravity.BOTTOM));

        final int number = PuzzlesScreen.progressIndex(), total = PuzzlesScreen.totalPuzzles();

        LinearLayout bottom = Ui.linear(c, LinearLayout.VERTICAL);
        bottom.setPadding(Ui.dp(c, 16), 0, Ui.dp(c, 16), Ui.dp(c, 8));

        LinearLayout scoreRow = Ui.linear(c, LinearLayout.HORIZONTAL);
        scoreRow.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout scoreCol = Ui.linear(c, LinearLayout.VERTICAL);
        scoreCol.addView(Ui.text(c, "Score", 10.5f, 0xFFD7D2CA, true));
        scoreCol.addView(Ui.text(c, String.valueOf(PuzzlesScreen.sessionScore), 25,
                Ui.WHITE, true));
        scoreRow.addView(scoreCol);
        scoreRow.addView(Ui.spacerWeight(c, 1f));

        LinearLayout badge = Ui.linear(c, LinearLayout.HORIZONTAL);
        badge.setGravity(Gravity.CENTER_VERTICAL);
        badge.setBackground(Ui.round(0xCC6E4C29, 8, c));
        badge.setPadding(Ui.dp(c, 9), Ui.dp(c, 5), Ui.dp(c, 11), Ui.dp(c, 6));
        badge.addView(Ui.glyph(c, "extension", 15, 0xFFF0DCA9));
        TextView count = Ui.text(c, "  " + (number + 1) + "/" + total, 13, Ui.WHITE, true);
        badge.addView(count);
        scoreRow.addView(badge);
        bottom.addView(scoreRow);

        bottom.addView(progressBar(c, total <= 1 ? 0f : (number + 1f) / total),
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        Ui.dp(c, 7)));

        LinearLayout buttons = Ui.linear(c, LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams bRowLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(c, 58));
        bRowLp.setMargins(0, Ui.dp(c, 10), 0, 0);
        buttons.setLayoutParams(bRowLp);

        TextView list = Ui.glyph(c, "list", 24, Ui.WHITE);
        list.setBackground(Ui.round(0xFF3B3935, 10, c));
        LinearLayout.LayoutParams listLp = new LinearLayout.LayoutParams(
                Ui.dp(c, 58), ViewGroup.LayoutParams.MATCH_PARENT);
        listLp.setMargins(0, 0, Ui.dp(c, 9), 0);
        list.setLayoutParams(listLp);
        list.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { puzzleMenu(main); }
        });
        buttons.addView(list);

        TextView solve = Ui.text(c, "Solve Puzzles", 18.5f, Ui.WHITE, true);
        solve.setGravity(Gravity.CENTER);
        solve.setBackground(Ui.greenFill(c, 10));
        solve.setLayoutParams(new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.MATCH_PARENT, 1f));
        solve.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { main.navigate("puzzlesolve"); }
        });
        buttons.addView(solve);
        bottom.addView(buttons);

        FrameLayout.LayoutParams blp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.BOTTOM);
        blp.bottomMargin = Ui.dp(c, 4);
        root.addView(bottom, blp);

        return root;
    }

    private static View progressBar(Context c, float fraction) {
        final float f = Math.max(0f, Math.min(1f, fraction));
        View v = new View(c) {
            @Override protected void onDraw(Canvas canvas) {
                float h = getHeight(), r = h / 2f;
                Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
                p.setColor(0x59FFFFFF);
                canvas.drawRoundRect(new RectF(0, 0, getWidth(), h), r, r, p);
                if (f > 0) {
                    int w = Math.max(getHeight(), Math.round(getWidth() * f));
                    p.setShader(new LinearGradient(0, 0, w, 0,
                            0xFFBFE27A, 0xFF86B24E, Shader.TileMode.CLAMP));
                    canvas.drawRoundRect(new RectF(0, 0, w, h), r, r, p);
                }
            }
        };
        v.setWillNotDraw(false);
        ViewGroup.MarginLayoutParams lp = new ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(c, 6));
        lp.topMargin = Ui.dp(c, 9);
        v.setLayoutParams(lp);
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

    /** Green jungle board with a winding sandy trail, wooden levels and a pawn on the current one. */
    private static class PathView extends View {

        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        private final float[][] nodes = {
                {0.735f, 0.610f}, // 1 (current)
                {0.545f, 0.555f},
                {0.360f, 0.485f},
                {0.245f, 0.390f},
                {0.345f, 0.310f},
                {0.505f, 0.290f},
                {0.660f, 0.245f},
                {0.790f, 0.168f},
                {0.660f, 0.100f},
                {0.560f, 0.042f}
        };

        PathView(Context c) {
            super(c);
            setWillNotDraw(false);
        }

        @Override protected void onDraw(Canvas cv) {
            float w = getWidth(), h = getHeight();
            if (w <= 0 || h <= 0) return;

            paint.setShader(new LinearGradient(0, 0, 0, h,
                    new int[]{0xFF5B9455, 0xFF447C47, 0xFF2E5F37, 0xFF1F4A2B},
                    new float[]{0f, 0.45f, 0.8f, 1f}, Shader.TileMode.CLAMP));
            cv.drawRect(0, 0, w, h, paint);
            paint.setShader(null);

            // faint diamond lattice
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(Math.max(1f, w * 0.002f));
            paint.setColor(0x12FFFFFF);
            float s = w * 0.19f;
            for (float x = -h; x < w + h; x += s) {
                cv.drawLine(x, 0, x + h, h, paint);
                cv.drawLine(x, h, x + h, 0, paint);
            }
            paint.setStyle(Paint.Style.FILL);

            // soft light from the top + vignette at the bottom
            paint.setShader(new RadialGradient(w * 0.5f, h * 0.12f, h * 0.8f,
                    new int[]{0x2EFFF8C8, 0x00000000}, null, Shader.TileMode.CLAMP));
            cv.drawRect(0, 0, w, h, paint);
            paint.setShader(new LinearGradient(0, h * 0.55f, 0, h,
                    new int[]{0x00000000, 0x40000000}, null, Shader.TileMode.CLAMP));
            cv.drawRect(0, h * 0.55f, w, h, paint);
            paint.setShader(null);

            drawBushes(cv, w, h);

            float px[][] = new float[nodes.length][2];
            float baseR = w * 0.061f;
            for (int i = 0; i < nodes.length; i++) {
                px[i][0] = nodes[i][0] * w;
                px[i][1] = nodes[i][1] * h;
            }

            // trail
            Path road = new Path();
            road.moveTo(px[0][0], px[0][1]);
            for (int i = 1; i < px.length; i++) {
                float mx = (px[i - 1][0] + px[i][0]) / 2f;
                float my = (px[i - 1][1] + px[i][1]) / 2f;
                road.quadTo(px[i - 1][0], px[i - 1][1], mx, my);
            }
            road.lineTo(px[px.length - 1][0], px[px.length - 1][1]);

            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setColor(0x33000000);
            paint.setStrokeWidth(baseR * 0.86f);
            cv.save();
            cv.translate(baseR * 0.14f, baseR * 0.2f);
            cv.drawPath(road, paint);
            cv.restore();
            paint.setColor(0xFF8A6A42);
            paint.setStrokeWidth(baseR * 0.78f);
            cv.drawPath(road, paint);
            paint.setColor(0xFFDCC291);
            paint.setStrokeWidth(baseR * 0.60f);
            cv.drawPath(road, paint);
            paint.setStrokeWidth(baseR * 0.14f);
            paint.setColor(0x66FFF3D2);
            paint.setPathEffect(new android.graphics.DashPathEffect(
                    new float[]{baseR * 0.5f, baseR * 0.85f}, 0));
            cv.drawPath(road, paint);
            paint.setPathEffect(null);
            paint.setStyle(Paint.Style.FILL);

            // levels, far ones first so the near path overlaps
            for (int i = px.length - 1; i >= 2; i--) {
                boolean dim = i >= 8;
                drawTile(cv, px[i][0], px[i][1], baseR * (dim ? 0.80f : 0.9f),
                        String.valueOf(i + 1), false, dim, 0);
            }
            drawTile(cv, px[1][0], px[1][1], baseR, "2", false, false, 0);

            // current level: green platform + statue pawn
            float cr = baseR * 1.28f;
            paint.setShader(new RadialGradient(px[0][0], px[0][1], cr * 2.1f,
                    new int[]{0x60EFFFCF, 0x00EFFFCF}, null, Shader.TileMode.CLAMP));
            cv.drawCircle(px[0][0], px[0][1], cr * 2.1f, paint);
            paint.setShader(null);
            drawTile(cv, px[0][0], px[0][1], cr, null, true, false,
                    Ui.dp(getContext(), 2));
            drawPawn(cv, px[0][0], px[0][1] + cr * 0.30f, cr * 2.25f);
        }

        private void drawPawn(Canvas cv, float cx, float baseY, float h) {
            float w = h * 0.58f;
            paint.setColor(0x55000000);
            cv.drawOval(cx - w * 0.46f, baseY - h * 0.04f,
                    cx + w * 0.46f, baseY + h * 0.075f, paint);

            paint.setColor(0xFF4C843C);
            cv.drawRoundRect(cx - w * 0.46f, baseY - h * 0.16f,
                    cx + w * 0.46f, baseY, w * 0.16f, w * 0.16f, paint);
            Path stem = new Path();
            stem.moveTo(cx - w * 0.14f, baseY - h * 0.13f);
            stem.lineTo(cx - w * 0.24f, baseY - h * 0.46f);
            stem.lineTo(cx + w * 0.24f, baseY - h * 0.46f);
            stem.lineTo(cx + w * 0.14f, baseY - h * 0.13f);
            stem.close();
            cv.drawPath(stem, paint);
            cv.drawRoundRect(cx - w * 0.40f, baseY - h * 0.545f,
                    cx + w * 0.40f, baseY - h * 0.44f, w * 0.09f, w * 0.09f, paint);
            cv.drawRoundRect(cx - w * 0.11f, baseY - h * 0.63f,
                    cx + w * 0.11f, baseY - h * 0.52f, w * 0.05f, w * 0.05f, paint);
            cv.drawCircle(cx, baseY - h * 0.755f, w * 0.275f, paint);

            paint.setColor(0xFF8FD269);
            cv.drawRoundRect(cx - w * 0.41f, baseY - h * 0.135f,
                    cx + w * 0.41f, baseY - h * 0.02f, w * 0.13f, w * 0.13f, paint);
            Path inner = new Path();
            inner.moveTo(cx - w * 0.105f, baseY - h * 0.135f);
            inner.lineTo(cx - w * 0.195f, baseY - h * 0.44f);
            inner.lineTo(cx + w * 0.195f, baseY - h * 0.44f);
            inner.lineTo(cx + w * 0.105f, baseY - h * 0.135f);
            inner.close();
            cv.drawPath(inner, paint);
            cv.drawRoundRect(cx - w * 0.36f, baseY - h * 0.525f,
                    cx + w * 0.36f, baseY - h * 0.455f, w * 0.065f, w * 0.065f, paint);
            cv.drawRoundRect(cx - w * 0.085f, baseY - h * 0.615f,
                    cx + w * 0.085f, baseY - h * 0.52f, w * 0.04f, w * 0.04f, paint);
            cv.drawCircle(cx - w * 0.01f, baseY - h * 0.765f, w * 0.235f, paint);
            paint.setColor(0xFFC9EDA3);
            cv.drawCircle(cx - w * 0.09f, baseY - h * 0.83f, w * 0.08f, paint);
        }

        private void drawBushes(Canvas cv, float w, float h) {
            float[][] bushes = {{0.055f, 0.62f, 1f}, {0.94f, 0.52f, 0.8f},
                    {0.10f, 0.28f, 0.72f}, {0.86f, 0.10f, 0.6f}};
            for (float[] b : bushes) {
                float cx = b[0] * w, cy = b[1] * h, r = w * 0.05f * b[2];
                paint.setColor(0xFF2C5735);
                cv.drawCircle(cx, cy, r, paint);
                cv.drawCircle(cx - r * 0.7f, cy + r * 0.25f, r * 0.68f, paint);
                cv.drawCircle(cx + r * 0.75f, cy + r * 0.2f, r * 0.6f, paint);
                paint.setColor(0x336DAE62);
                cv.drawCircle(cx - r * 0.25f, cy - r * 0.3f, r * 0.5f, paint);
            }
        }

        private void drawTile(Canvas cv, float cx, float cy, float r, String label,
                              boolean current, boolean dim, int elevationDp) {
            float depth = r * 0.20f;
            Path side = diamond(cx, cy + depth, r);
            paint.setColor(dim ? 0xFF4E3A24 : 0xFF7A532E);
            cv.drawPath(side, paint);

            Path top = diamond(cx, cy, r);
            if (current) {
                paint.setShader(new LinearGradient(cx - r, cy - r, cx + r, cy + r,
                        0xFF93C97B, 0xFF5E9D4E, Shader.TileMode.CLAMP));
                cv.drawPath(top, paint);
                paint.setShader(null);
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(Math.max(2f, r * 0.07f));
                paint.setColor(0xFFB6DE9F);
                cv.drawPath(top, paint);
                paint.setStyle(Paint.Style.FILL);
            } else {
                paint.setColor(dim ? 0xFFB7A98C : 0xFFF0DFBD);
                cv.drawPath(top, paint);
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(Math.max(1.5f, r * 0.055f));
                paint.setColor(dim ? 0xFF8C7A5C : 0xFFC49A63);
                cv.drawPath(top, paint);
                paint.setStyle(Paint.Style.FILL);
            }

            if (label != null) {
                paint.setColor(dim ? 0xFF8A7856 : 0xFF79502A);
                paint.setTextAlign(Paint.Align.CENTER);
                paint.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
                paint.setTextSize(r * 0.82f);
                Paint.FontMetrics fm = paint.getFontMetrics();
                cv.drawText(label, cx, cy - (fm.ascent + fm.descent) / 2f, paint);
            }
        }

        private Path diamond(float cx, float cy, float r) {
            Path p = new Path();
            p.moveTo(cx, cy - r);
            p.lineTo(cx + r, cy);
            p.lineTo(cx, cy + r);
            p.lineTo(cx - r, cy);
            p.close();
            return p;
        }
    }
}
