package com.eldar.chess;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/** Shared UI helpers - chess.com dark style, built programmatically. */
public class Ui {

    public static int dp(Context c, float v) {
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v,
                c.getResources().getDisplayMetrics()));
    }

    // ---- colors ----
    public static final int BG = 0xFF262421;
    public static final int BG_DARK = 0xFF1C1B18;
    public static final int CARD = 0xFF3B3935;
    public static final int CARD_DARK = 0xFF302E2B;
    public static final int CHIP = 0xFF403D39;
    public static final int CHIP_LIGHT = 0xFF56534E;
    public static final int GREEN = 0xFF81B64C;
    public static final int GREEN_DARK = 0xFF5D9948;
    public static final int BLUE = 0xFF3E9BDD;
    public static final int WHITE = 0xFFFFFFFF;
    public static final int GREY = 0xFFA8A29A;
    public static final int GREY_DARK = 0xFF726C65;
    public static final int YELLOW = 0xFFF1EC59;

    // ---- generic factories ----
    public static TextView text(Context c, String s, float sp, int color, boolean bold) {
        TextView t = new TextView(c);
        t.setText(s);
        t.setTextSize(sp);
        t.setTextColor(color);
        t.setTypeface(null, bold ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
        return t;
    }

    public static LinearLayout linear(Context c, int orientation) {
        LinearLayout l = new LinearLayout(c);
        l.setOrientation(orientation);
        return l;
    }

    public static LinearLayout.LayoutParams lp(int w, int h) {
        return new LinearLayout.LayoutParams(w, h);
    }

    public static LinearLayout.LayoutParams lp(int w, int h, float weight) {
        return new LinearLayout.LayoutParams(w, h, weight);
    }

    private static android.graphics.Typeface iconTypeface;

    /** Material Icons glyph shown as text (e.g. "home", "extension"). */
    public static TextView glyph(Context c, String name, float sp, int color) {
        TextView t = new TextView(c);
        if (iconTypeface == null) {
            iconTypeface = c.getResources().getFont(R.font.material_icons);
        }
        t.setText(name);
        t.setTextSize(sp);
        t.setTextColor(color);
        t.setTypeface(iconTypeface);
        t.setGravity(Gravity.CENTER);
        t.setIncludeFontPadding(false);
        return t;
    }

    /** Scaled checkerboard pattern view (used behind the Learn / first-game banners). */
    public static View checker(Context c, final int colorA, final int colorB, final int cells) {
        View v = new View(c) {
            @Override protected void onDraw(Canvas canvas) {
                float w = getWidth() / (float) cells, h = getHeight() / (float) cells;
                Paint p = new Paint();
                for (int y = 0; y < cells; y++) {
                    for (int x = 0; x < cells; x++) {
                        p.setColor(((x + y) & 1) == 0 ? colorA : colorB);
                        canvas.drawRect(x * w, y * h, (x + 1) * w, (y + 1) * h, p);
                    }
                }
            }
        };
        v.setWillNotDraw(false);
        return v;
    }

    public static GradientDrawable round(int color, float radiusDp, Context c) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(color);
        g.setCornerRadius(dp(c, radiusDp));
        return g;
    }

    /** Page backdrop of the dark screens (subtle vertical gradient seen in every SS). */
    public static GradientDrawable pageBg() {
        GradientDrawable g = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{0xFF22201E, 0xFF282523, 0xFF2B2826, 0xFF2F2C2A, 0xFF262421});
        g.setColors(new int[]{0xFF22201E, 0xFF282523, 0xFF2B2826, 0xFF2F2C2A, 0xFF262421},
                new float[]{0f, 0.42f, 0.62f, 0.84f, 1f});
        return g;
    }

    /** chess.com cards are white @ ~8% over the page gradient. */
    public static GradientDrawable cardBg(Context c, float radiusDp) {
        return round(0x15FFFFFF, radiusDp, c);
    }

    /** Pinned Play bar / green CTA fill. */
    public static GradientDrawable greenFill(Context c, float radiusDp) {
        GradientDrawable g = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{0xFF86BA52, 0xFF67A04A});
        g.setCornerRadius(dp(c, radiusDp));
        return g;
    }

    public static GradientDrawable roundStroke(int color, float radiusDp, int strokeColor,
                                               float strokeDp, Context c) {
        GradientDrawable g = round(color, radiusDp, c);
        g.setStroke(Math.max(1, dp(c, strokeDp)), strokeColor);
        return g;
    }

    /** Vertical dark scrim over the board while "Searching..." (screenshot 072139):
     *  darker at the top, fading to fully transparent by mid-board. */
    public static GradientDrawable scrim() {
        GradientDrawable g = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{0x8C3E3E42, 0x333E3E42, 0x003E3E42, 0x003E3E42});
        g.setColors(new int[]{0x8C3E3E42, 0x333E3E42, 0x003E3E42, 0x003E3E42},
                new float[]{0f, 0.38f, 0.62f, 1f});
        return g;
    }

    public static ImageView img(Context c, int resId, int sizeDp) {
        ImageView iv = new ImageView(c);
        iv.setImageResource(resId);
        int s = dp(c, sizeDp);
        iv.setLayoutParams(new LinearLayout.LayoutParams(s, s));
        return iv;
    }

    public static View spacer(Context c, int w, int h) {
        View v = new View(c);
        v.setLayoutParams(lp(w, h));
        return v;
    }

    public static View spacerWeight(Context c, float weight) {
        View v = new View(c);
        v.setLayoutParams(lp(0, 1, weight));
        return v;
    }

    public static void margins(View v, int l, int t, int r, int b) {
        ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
        if (p == null) return;
        p.setMargins(l, t, r, b);
        v.setLayoutParams(p);
    }

    public static void pad(View v, float l, float t, float r, float b, Context c) {
        v.setPadding(dp(c, l), dp(c, t), dp(c, r), dp(c, b));
    }

    /** Green pawn silhouette drawn natively (no screenshot crop). */
    public static Drawable logoPawn(Context c, int heightDp) {
        int h = dp(c, heightDp);
        int w = Math.max(1, Math.round(h * 0.62f));
        Bitmap b = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas cv = new Canvas(b);
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(0xFF7FA650);
        cv.drawRoundRect(w * 0.10f, h * 0.84f, w * 0.90f, h * 0.98f,
                w * 0.16f, h * 0.06f, p);
        cv.drawRoundRect(w * 0.32f, h * 0.44f, w * 0.68f, h * 0.90f,
                w * 0.14f, w * 0.14f, p);
        cv.drawRoundRect(w * 0.20f, h * 0.38f, w * 0.80f, h * 0.52f,
                w * 0.12f, h * 0.05f, p);
        cv.drawCircle(w * 0.5f, h * 0.24f, w * 0.24f, p);
        p.setColor(0x3383B455);
        cv.drawCircle(w * 0.43f, h * 0.19f, w * 0.09f, p);
        return new BitmapDrawable(c.getResources(), b);
    }

    /** Chess.com logo row: green pawn + "Chess.com" */
    public static LinearLayout logo(Context c, float pawnDp, float textSp) {
        LinearLayout l = linear(c, LinearLayout.HORIZONTAL);
        l.setGravity(Gravity.CENTER_VERTICAL);
        ImageView pawn = new ImageView(c);
        pawn.setImageDrawable(logoPawn(c, Math.round(pawnDp)));
        pawn.setScaleType(ImageView.ScaleType.FIT_CENTER);
        l.addView(pawn, new LinearLayout.LayoutParams(dp(c, pawnDp * 0.62f), dp(c, pawnDp)));
        TextView t = text(c, "Chess.com", textSp, WHITE, true);
        LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tp.setMargins(dp(c, 3), dp(c, 1), 0, 0);
        t.setLayoutParams(tp);
        l.addView(t);
        return l;
    }

    /** Azerbaijan flag (tiny). */
    public static Drawable flagAz(Context c) {
        int w = dp(c, 14), h = dp(c, 10);
        Bitmap b = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas cv = new Canvas(b);
        Paint p = new Paint();
        p.setColor(0xFF0092BC); cv.drawRect(0, 0, w, h / 3f, p);
        p.setColor(0xFFE4002B); cv.drawRect(0, h / 3f, w, 2 * h / 3f, p);
        p.setColor(0xFF00AF66); cv.drawRect(0, 2 * h / 3f, w, h, p);
        p.setColor(0xFFFFFFFF);
        cv.drawCircle(w * 0.44f, h * 0.5f, h * 0.20f, p);
        p.setColor(0xFFE4002B);
        cv.drawCircle(w * 0.52f, h * 0.5f, h * 0.18f, p);
        p.setColor(0xFFFFFFFF);
        cv.drawCircle(w * 0.64f, h * 0.5f, h * 0.08f, p);
        return new BitmapDrawable(c.getResources(), b);
    }

    /** Afghanistan flag (tiny, vertical stripes as in the reference screenshot). */
    public static Drawable flagAf(Context c) {
        int w = dp(c, 10), h = dp(c, 8);
        Bitmap b = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas cv = new Canvas(b);
        Paint p = new Paint();
        p.setColor(0xFF000000); cv.drawRect(0, 0, w / 3f, h, p);
        p.setColor(0xFFD32011); cv.drawRect(w / 3f, 0, 2 * w / 3f, h, p);
        p.setColor(0xFF007A36); cv.drawRect(2 * w / 3f, 0, w, h, p);
        p.setColor(0xFFFFFFFF);
        cv.drawCircle(w * 0.5f, h * 0.5f, h * 0.16f, p);
        return new BitmapDrawable(c.getResources(), b);
    }

    /** German flag (tiny, drawn natively). */
    public static Drawable flagDe(Context c) {
        int w = dp(c, 22), h = dp(c, 16);
        Bitmap b = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas cv = new Canvas(b);
        Paint p = new Paint();
        p.setColor(0xFF000000); cv.drawRect(0, 0, w, h / 3f, p);
        p.setColor(0xFFDD0000); cv.drawRect(0, h / 3f, w, 2 * h / 3f, p);
        p.setColor(0xFFFFCE00); cv.drawRect(0, 2 * h / 3f, w, h, p);
        return new BitmapDrawable(c.getResources(), b);
    }

    /** Generic red flag for opponent. */
    public static Drawable flagRed(Context c) {
        int w = dp(c, 14), h = dp(c, 10);
        Bitmap b = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas cv = new Canvas(b);
        Paint p = new Paint();
        p.setColor(0xFFD0342C); cv.drawRect(0, 0, w, h, p);
        p.setColor(0xFFF7C948);
        cv.drawCircle(w * 0.5f, h * 0.5f, h * 0.22f, p);
        return new BitmapDrawable(c.getResources(), b);
    }
}
