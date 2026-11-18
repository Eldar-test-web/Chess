package com.eldar.chess;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.view.View;

/** Diagonal lesson lattice behind the Learn screen (screenshot 072013). */
public class LearnPathView extends View {

    // column centres + lattice pitch measured on the reference (dp @ 1.473 px/dp)
    private static final float COL_A = 125.6f, COL_B = 181.8f, COL_C = 234.4f;
    private static final float PITCH = 52.6f;
    private static final float SIDE = 53.3f;
    private static final float RADIUS = 8f;
    private static final float SS_H = 535f;      // path height in the screenshot
    private static final float MINT_Y = 471.8f;  // mint diamond centre in the screenshot

    // lesson chain: {column x, y, glyph id}
    private static final float[][] LESSONS = {
            {COL_B, 69.2f}, {COL_A, 115.4f}, {COL_B, 149.4f}, {COL_C, 196.9f},
            {COL_B, 237.6f}, {COL_A, 285.1f}, {COL_B, 325.9f}, {COL_C, 366.6f},
            {COL_B, 407.3f}
    };
    private static final int[] GLYPH = {0, 1, 2, 3, 4, 5, 6, 7, 8};

    private static final int LATTICE = 0xFF1B1917;
    private static final int TEAL_TOP = 0xFF265050;
    private static final int TEAL_BOT = 0xFF163A3A;
    private static final int MINT_TOP = 0xFF7DF3D3;
    private static final int MINT_BOT = 0xFF46E2B7;
    private static final int GLYPH_TINT = 0xFF6E908B;

    private final Paint solid = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint imgPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Paint text = new Paint(Paint.ANTI_ALIAS_FLAG);

    private Bitmap wPawn, wQueen, wRook, wBishop, wKnight;
    private Bitmap gKing, gPawn, gKnight, gBishop, gQueen, gRook;

    public LearnPathView(Context c) {
        super(c);
        setLayerType(LAYER_TYPE_SOFTWARE, null);
        wPawn = BitmapFactory.decodeResource(getResources(), R.drawable.piece3d_pawn_a);
        wQueen = BitmapFactory.decodeResource(getResources(), R.drawable.piece3d_queen_a);
        wRook = BitmapFactory.decodeResource(getResources(), R.drawable.piece3d_rook_a);
        wBishop = BitmapFactory.decodeResource(getResources(), R.drawable.piece3d_bishop_a);
        wKnight = BitmapFactory.decodeResource(getResources(), R.drawable.piece3d_knight_a);
        gKing = BitmapFactory.decodeResource(getResources(), R.drawable.piece_neo_wk);
        gPawn = BitmapFactory.decodeResource(getResources(), R.drawable.piece_neo_wp);
        gKnight = BitmapFactory.decodeResource(getResources(), R.drawable.piece_neo_wn);
        gBishop = BitmapFactory.decodeResource(getResources(), R.drawable.piece_neo_wb);
        gQueen = BitmapFactory.decodeResource(getResources(), R.drawable.piece_neo_wq);
        gRook = BitmapFactory.decodeResource(getResources(), R.drawable.piece_neo_wr);
    }

    private float dp(float v) { return v * getResources().getDisplayMetrics().density; }

    @Override protected void onDraw(Canvas cv) {
        float w = getWidth(), h = getHeight();
        float shift = h - dp(SS_H);
        float mintY = dp(MINT_Y) + shift;

        // page backdrop
        solid.setShader(null);
        solid.setStyle(Paint.Style.FILL);
        solid.setColor(0xFF262421);
        cv.drawRect(0, 0, w, h, solid);

        // dark checkerboard lattice
        solid.setColor(LATTICE);
        solid.setShadowLayer(dp(4), 0, dp(1.5f), 0x38000000);
        int cols = (int) Math.ceil(w / dp(PITCH)) + 4;
        float colPitch = dp(PITCH), side = dp(SIDE), rad = dp(RADIUS);
        for (int i = -3; i < cols; i++) {
            float cx = dp(COL_B) + (i - 2) * colPitch;
            if (cx < -side || cx > w + side) continue;
            float rowOff = ((i - 2) & 1) == 0 ? 0 : dp(40.2f);
            for (float cy = -side + rowOff; cy < h + side; cy += dp(80.4f)) {
                cv.save();
                cv.rotate(45f, cx, cy);
                cv.drawRoundRect(new RectF(cx - side / 2, cy - side / 2,
                        cx + side / 2, cy + side / 2), rad, rad, solid);
                cv.restore();
            }
        }
        solid.setShadowLayer(0, 0, 0, 0);

        // teal lesson diamonds
        for (int i = 0; i < LESSONS.length; i++) {
            float cx = dp(LESSONS[i][0]);
            float cy = dp(LESSONS[i][1]) + shift;
            drawTeal(cv, cx, cy, i == 1);
            drawGlyph(cv, cx, cy, GLYPH[i]);
        }

        // current lesson: mint diamond with glow
        drawMint(cv, dp(COL_A), mintY);

        // 3D decorations
        piece(cv, wPawn, dp(COL_C), dp(111.3f) + shift, 47.5f);
        piece(cv, wQueen, dp(COL_A), dp(198.2f) + shift, 66.5f);
        piece(cv, wRook, dp(COL_C), dp(283.8f) + shift, 56.3f);
        piece(cv, wBishop, dp(COL_A), dp(371.3f) + shift, 61.8f);
        piece(cv, wKnight, dp(COL_C), dp(461.0f) + shift, 50.2f);
    }

    private void drawTeal(Canvas cv, float cx, float cy, boolean bright) {
        float s = dp(SIDE) / 2;
        solid.setShader(new LinearGradient(cx - s, cy - s, cx + s, cy + s,
                bright ? 0xFF356262 : TEAL_TOP, bright ? 0xFF204C4C : TEAL_BOT, Shader.TileMode.CLAMP));
        solid.setShadowLayer(dp(7), 0, dp(3), 0x59000000);
        cv.save();
        cv.rotate(45f, cx, cy);
        cv.drawRoundRect(new RectF(cx - s, cy - s, cx + s, cy + s),
                dp(RADIUS), dp(RADIUS), solid);
        cv.restore();
        solid.setShadowLayer(0, 0, 0, 0);
        solid.setShader(null);
    }

    private void drawGlyph(Canvas cv, float cx, float cy, int id) {
        imgPaint.setColorFilter(new PorterDuffColorFilter(GLYPH_TINT, PorterDuff.Mode.SRC_IN));
        imgPaint.setAlpha(150);
        imgPaint.setShadowLayer(0, 0, 0, 0);
        solid.setColor(GLYPH_TINT);
        solid.setAlpha(150);
        solid.setStyle(Paint.Style.FILL);
        switch (id) {
            case 0: glyphBitmap(cv, gKing, cx, cy, 32); break;
            case 1: { // thick plus
                float a = dp(7), l = dp(30);
                RectF v = new RectF(cx - a / 2, cy - l / 2, cx + a / 2, cy + l / 2);
                RectF hbar = new RectF(cx - l / 2, cy - a / 2, cx + l / 2, cy + a / 2);
                cv.drawRoundRect(v, a / 2, a / 2, solid);
                cv.drawRoundRect(hbar, a / 2, a / 2, solid);
                break;
            }
            case 2: drawAxe(cv, cx, cy); break;
            case 3: { // knight + cross + x
                glyphBitmap(cv, gKnight, cx - dp(4), cy, 26);
                float l = dp(8);
                solid.setStrokeWidth(dp(2.4f));
                solid.setStrokeCap(Paint.Cap.ROUND);
                solid.setStyle(Paint.Style.STROKE);
                cv.drawLine(cx + dp(12), cy - dp(6), cx + dp(12), cy - dp(14), solid);
                cv.drawLine(cx + dp(8), cy - dp(11), cx + dp(16), cy - dp(11), solid);
                cv.drawLine(cx + dp(9), cy + dp(3), cx + dp(15), cy + dp(9), solid);
                cv.drawLine(cx + dp(15), cy + dp(3), cx + dp(9), cy + dp(9), solid);
                solid.setStyle(Paint.Style.FILL);
                break;
            }
            case 4: glyphBitmap(cv, gPawn, cx, cy, 24); break;
            case 5: glyphBitmap(cv, gKnight, cx, cy, 28); break;
            case 6: glyphBitmap(cv, gBishop, cx, cy, 28); break;
            case 7: glyphBitmap(cv, gRook, cx, cy, 27); break;
            default: glyphBitmap(cv, gQueen, cx, cy, 29); break;
        }
        imgPaint.setColorFilter(null);
        imgPaint.setAlpha(255);
        solid.setAlpha(255);
    }

    private void glyphBitmap(Canvas cv, Bitmap b, float cx, float cy, float hDp) {
        if (b == null) return;
        float ph = dp(hDp), pw = ph * b.getWidth() / b.getHeight();
        cv.drawBitmap(b, null, new RectF(cx - pw / 2, cy - ph / 2, cx + pw / 2, cy + ph / 2),
                imgPaint);
    }

    private void drawAxe(Canvas cv, float cx, float cy) {
        Path blade = new Path();
        float top = cy - dp(14), bot = cy + dp(2);
        blade.moveTo(cx - dp(2), top);
        blade.lineTo(cx + dp(15), top - dp(2));
        blade.lineTo(cx + dp(15), bot);
        blade.lineTo(cx - dp(2), bot - dp(2));
        blade.close();
        cv.drawPath(blade, solid);
        cv.drawRect(cx - dp(3), top - dp(1), cx + dp(1), cy + dp(14), solid);
        // tiny crown
        Path crown = new Path();
        float by = cy + dp(14), wHalf = dp(7);
        crown.moveTo(cx - wHalf, by);
        crown.lineTo(cx - wHalf, by - dp(5));
        crown.lineTo(cx - wHalf / 2, by - dp(2));
        crown.lineTo(cx, by - dp(5));
        crown.lineTo(cx + wHalf / 2, by - dp(2));
        crown.lineTo(cx + wHalf, by - dp(5));
        crown.lineTo(cx + wHalf, by);
        crown.close();
        cv.drawPath(crown, solid);
    }

    private void drawMint(Canvas cv, float cx, float cy) {
        float s = dp(SIDE) / 2 + dp(1.5f);
        // outer glow
        Paint glow = new Paint(Paint.ANTI_ALIAS_FLAG);
        glow.setShader(new RadialGradient(cx, cy, dp(64),
                new int[]{0x7A5CF0C9, 0x335CF0C9, 0x005CF0C9},
                new float[]{0f, 0.55f, 1f}, Shader.TileMode.CLAMP));
        cv.drawCircle(cx, cy, dp(64), glow);
        // darker under-edge for depth
        solid.setShader(null);
        solid.setColor(0xFF2FA98F);
        drawRotRect(cv, cx, cy + dp(5), s, dp(RADIUS));
        // mint diamond
        solid.setShader(new LinearGradient(cx - s, cy - s, cx - s, cy + s,
                MINT_TOP, MINT_BOT, Shader.TileMode.CLAMP));
        drawRotRect(cv, cx, cy, s, dp(RADIUS));
        solid.setShader(null);
        // white king icon
        solid.setColor(0xFFFFFFFF);
        float kw = dp(27), kh = dp(27);
        float l = cx - kw / 2, r = cx + kw / 2;
        float t = cy - kh / 2 - dp(1), b = cy + kh / 2;
        // base bar
        cv.drawRoundRect(new RectF(l + dp(2.5f), b - dp(4), r - dp(2.5f), b),
                dp(1.5f), dp(1.5f), solid);
        // crown body: two rounded shoulders with curled tips
        Path king = new Path();
        king.moveTo(l + dp(5), b - dp(5.5f));
        king.lineTo(l + dp(5), t + dp(11));
        king.cubicTo(l + dp(5), t + dp(14), l + dp(9), t + dp(14.5f), l + dp(10.5f), t + dp(11.5f));
        king.lineTo(l + dp(12), t + dp(9));
        king.cubicTo(l + dp(12.5f), t + dp(6), cx - dp(2), t + dp(5), cx, t + dp(5.5f));
        king.cubicTo(cx + dp(2), t + dp(5), r - dp(12.5f), t + dp(6), r - dp(12), t + dp(9));
        king.lineTo(r - dp(10.5f), t + dp(11.5f));
        king.cubicTo(r - dp(9), t + dp(14.5f), r - dp(5), t + dp(14), r - dp(5), t + dp(11));
        king.lineTo(r - dp(5), b - dp(5.5f));
        king.close();
        cv.drawPath(king, solid);
        // cross
        cv.drawRoundRect(new RectF(cx - dp(1.4f), t - dp(5), cx + dp(1.4f), t + dp(5.5f)),
                dp(0.7f), dp(0.7f), solid);
        cv.drawRoundRect(new RectF(cx - dp(5), t - dp(1.4f), cx + dp(5), t + dp(1.4f)),
                dp(0.7f), dp(0.7f), solid);
        // Next pill
        float pw = dp(58), ph = dp(24.5f), py = cy - dp(56.3f);
        solid.setShadowLayer(dp(7), 0, dp(2), 0x66000000);
        solid.setColor(0xFFF8F8F8);
        cv.drawRoundRect(new RectF(cx - pw / 2, py - ph / 2, cx + pw / 2, py + ph / 2),
                dp(6), dp(6), solid);
        solid.setShadowLayer(0, 0, 0, 0);
        // tail
        cv.save();
        cv.rotate(45f, cx, py + ph / 2 + dp(2));
        cv.drawRect(cx - dp(4), py + ph / 2 - dp(6), cx + dp(4), py + ph / 2 + dp(2), solid);
        cv.restore();
        // label
        text.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        text.setTextSize(dp(11.5f));
        text.setColor(0xFF1F1F1F);
        text.setTextAlign(Paint.Align.CENTER);
        Paint.FontMetrics fm = text.getFontMetrics();
        cv.drawText("Next", cx, py - (fm.ascent + fm.descent) / 2f, text);
    }

    private void drawRotRect(Canvas cv, float cx, float cy, float s, float rad) {
        cv.save();
        cv.rotate(45f, cx, cy);
        cv.drawRoundRect(new RectF(cx - s, cy - s, cx + s, cy + s), rad, rad, solid);
        cv.restore();
    }

    private void piece(Canvas cv, Bitmap b, float cx, float cy, float hDp) {
        if (b == null) return;
        float ph = dp(hDp), pw = ph * b.getWidth() / b.getHeight();
        imgPaint.setColorFilter(null);
        imgPaint.setAlpha(255);
        imgPaint.setShadowLayer(dp(9), 0, dp(4), 0x73000000);
        cv.drawBitmap(b, null, new RectF(cx - pw / 2, cy - ph / 2, cx + pw / 2, cy + ph / 2),
                imgPaint);
        imgPaint.setShadowLayer(0, 0, 0, 0);
    }
}
