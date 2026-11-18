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
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

/** Learn screen lesson path with 16 large touch-friendly level diamonds. */
public class LearnPathView extends View {

    public interface Listener {
        void onLessonTap(int index);
        void onNextTap();
    }

    // column centres widened so the bigger diamonds keep their spacing
    private static final float COL_A = 110f, COL_B = 180f, COL_C = 250f;
    private static final float PITCH = 52.6f;
    private static final float SIDE = 64f;
    private static final float RADIUS = 9f;
    private static final float LATTICE_SIDE = 53.3f;
    private static final float LATTICE_RADIUS = 8f;
    public static final float CONTENT_H = 1090f;      // scrollable path height
    private static final float LESSON_TOP = 130f, LESSON_PITCH = 55f;
    private static final float MINT_Y = 1010f;

    private static final float[] COLS = {COL_B, COL_A, COL_B, COL_C, COL_B, COL_A,
            COL_B, COL_C, COL_B, COL_A, COL_B, COL_C, COL_B, COL_A, COL_B, COL_C};
    private static final int[] GLYPH = {0, 1, 2, 3, 4, 5, 6, 7, 8, 0, 7, 8, 7, 5, 7, 7};

    public static final String[] LESSON_NAMES = {"Move the King", "The Knight", "The Rook",
            "The Bishop", "The Queen", "The Pawn", "Capturing", "Check", "Checkmate",
            "Castling", "En Passant", "Promotion", "The Pin", "The Fork",
            "Back Rank Mate", "Ladder Mate"};

    private static final int LATTICE = 0xFF1B1917;
    private static final int TEAL_TOP = 0xFF265050;
    private static final int TEAL_BOT = 0xFF163A3A;
    private static final int MINT_TOP = 0xFF7DF3D3;
    private static final int MINT_BOT = 0xFF46E2B7;
    private static final int GLYPH_TINT = 0xFF6E908B;

    private final Paint solid = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint imgPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Paint text = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();

    private Bitmap wPawn, wQueen, wRook, wBishop, wKnight;
    private Bitmap gKing, gPawn, gKnight, gBishop, gQueen, gRook;

    private float touchX, touchY;
    private float scrollY, lastY;
    private boolean dragging;
    private int progress = 0;
    private Listener listener;

    public LearnPathView(Context c) {
        super(c);
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

    public void setListener(Listener l) { listener = l; }

    public void setProgress(int p) { progress = p; invalidate(); }

    private float dp(float v) { return v * getResources().getDisplayMetrics().density; }

    @Override protected void onSizeChanged(int w, int h, int ow, int oh) {
        scrollY = Math.max(0, dp(CONTENT_H) - h);
    }

    @Override protected void onDraw(Canvas cv) {
        float w = getWidth();
        float h = dp(CONTENT_H);

        // viewport backdrop (shows through if content is shorter than the view)
        solid.setShader(null);
        solid.setStyle(Paint.Style.FILL);
        solid.setColor(0xFF262421);
        cv.drawRect(0, 0, w, getHeight(), solid);

        cv.save();
        cv.translate(0, -scrollY);

        solid.setColor(0xFF262421);
        cv.drawRect(0, 0, w, h, solid);

        // dark checkerboard lattice
        solid.setColor(LATTICE);
        int cols = (int) Math.ceil(w / dp(PITCH)) + 4;
        float colPitch = dp(PITCH), side = dp(LATTICE_SIDE), rad = dp(LATTICE_RADIUS);
        for (int i = -3; i < cols; i++) {
            float cx = dp(COL_B) + (i - 2) * colPitch;
            if (cx < -side || cx > w + side) continue;
            float rowOff = ((i - 2) & 1) == 0 ? 0 : dp(40.2f);
            for (float cy = -side + rowOff; cy < h + side; cy += dp(80.4f)) {
                cv.save();
                cv.rotate(45f, cx, cy);
                rect.set(cx - side / 2, cy - side / 2, cx + side / 2, cy + side / 2);
                cv.drawRoundRect(rect, rad, rad, solid);
                cv.restore();
            }
        }

        // teal lesson diamonds (completed ones glow brighter)
        for (int i = 0; i < COLS.length; i++) {
            float cx = dp(COLS[i]);
            float cy = dp(LESSON_TOP + i * LESSON_PITCH);
            drawTeal(cv, cx, cy, i < progress);
            drawGlyph(cv, cx, cy, GLYPH[i]);
        }

        // current lesson: mint diamond with glow
        drawMint(cv, dp(COL_A), dp(MINT_Y));

        // 3D decorations
        piece(cv, wPawn, dp(COL_C), dp(160f), 52f);
        piece(cv, wQueen, dp(COL_A), dp(270f), 73f);
        piece(cv, wRook, dp(COL_C), dp(380f), 61f);
        piece(cv, wBishop, dp(COL_A), dp(490f), 67f);
        piece(cv, wKnight, dp(COL_C), dp(600f), 54f);
        piece(cv, wPawn, dp(COL_A), dp(710f), 52f);
        piece(cv, wQueen, dp(COL_C), dp(820f), 73f);

        cv.restore();
    }

    private float hitRadius() { return dp(SIDE * 0.7071f); }

    @Override public boolean onTouchEvent(MotionEvent e) {
        switch (e.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                touchX = e.getX();
                touchY = e.getY();
                lastY = touchY;
                dragging = false;
                return true;
            case MotionEvent.ACTION_MOVE: {
                float dy = e.getY() - lastY;
                lastY = e.getY();
                if (Math.abs(e.getY() - touchY) > dp(6)) dragging = true;
                if (dragging) {
                    float max = Math.max(0, dp(CONTENT_H) - getHeight());
                    scrollY = Math.max(0, Math.min(max, scrollY - dy));
                    invalidate();
                }
                return true;
            }
            case MotionEvent.ACTION_UP:
                if (!dragging && Math.abs(e.getX() - touchX) < dp(12)
                        && Math.abs(e.getY() - touchY) < dp(12)) {
                    performClick();
                    float ty = e.getY() + scrollY;
                    for (int i = 0; i < COLS.length; i++) {
                        float dx = Math.abs(e.getX() - dp(COLS[i]));
                        float dy = Math.abs(ty - dp(LESSON_TOP + i * LESSON_PITCH));
                        if (dx + dy <= hitRadius()) {
                            if (listener != null) {
                                listener.onLessonTap(i);
                            } else {
                                Toast.makeText(getContext(), "Lesson " + (i + 1) + ": "
                                        + LESSON_NAMES[i], Toast.LENGTH_SHORT).show();
                            }
                            return true;
                        }
                    }
                    float dx = Math.abs(e.getX() - dp(COL_A));
                    float dy = Math.abs(ty - dp(MINT_Y));
                    if (dx + dy <= hitRadius() + dp(2)) {
                        if (listener != null) {
                            listener.onNextTap();
                        } else {
                            Toast.makeText(getContext(), "Ready for the next lesson!",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                return true;
            default:
                return super.onTouchEvent(e);
        }
    }

    @Override public boolean performClick() {
        super.performClick();
        return true;
    }

    private void drawTeal(Canvas cv, float cx, float cy, boolean completed) {
        float s = dp(SIDE) / 2;
        solid.setShader(new LinearGradient(cx - s, cy - s, cx + s, cy + s,
                completed ? 0xFF356262 : TEAL_TOP, completed ? 0xFF204C4C : TEAL_BOT,
                Shader.TileMode.CLAMP));
        cv.save();
        cv.rotate(45f, cx, cy);
        rect.set(cx - s, cy - s, cx + s, cy + s);
        cv.drawRoundRect(rect, dp(RADIUS), dp(RADIUS), solid);
        cv.restore();
        solid.setShader(null);
    }

    private void drawGlyph(Canvas cv, float cx, float cy, int id) {
        imgPaint.setColorFilter(new PorterDuffColorFilter(GLYPH_TINT, PorterDuff.Mode.SRC_IN));
        imgPaint.setAlpha(150);
        solid.setColor(GLYPH_TINT);
        solid.setAlpha(150);
        solid.setStyle(Paint.Style.FILL);
        switch (id) {
            case 0: glyphBitmap(cv, gKing, cx, cy, 38); break;
            case 1: { // thick plus
                float a = dp(8), l = dp(35);
                rect.set(cx - a / 2, cy - l / 2, cx + a / 2, cy + l / 2);
                cv.drawRoundRect(rect, a / 2, a / 2, solid);
                rect.set(cx - l / 2, cy - a / 2, cx + l / 2, cy + a / 2);
                cv.drawRoundRect(rect, a / 2, a / 2, solid);
                break;
            }
            case 2: drawAxe(cv, cx, cy); break;
            case 3: { // knight + cross + x
                glyphBitmap(cv, gKnight, cx - dp(4), cy, 31);
                float l = dp(9);
                solid.setStrokeWidth(dp(2.8f));
                solid.setStrokeCap(Paint.Cap.ROUND);
                solid.setStyle(Paint.Style.STROKE);
                cv.drawLine(cx + dp(14), cy - dp(7), cx + dp(14), cy - dp(16), solid);
                cv.drawLine(cx + dp(9), cy - dp(13), cx + dp(19), cy - dp(13), solid);
                cv.drawLine(cx + dp(11), cy + dp(4), cx + dp(18), cy + dp(11), solid);
                cv.drawLine(cx + dp(18), cy + dp(4), cx + dp(11), cy + dp(11), solid);
                solid.setStyle(Paint.Style.FILL);
                break;
            }
            case 4: glyphBitmap(cv, gPawn, cx, cy, 29); break;
            case 5: glyphBitmap(cv, gKnight, cx, cy, 34); break;
            case 6: glyphBitmap(cv, gBishop, cx, cy, 34); break;
            case 7: glyphBitmap(cv, gRook, cx, cy, 33); break;
            default: glyphBitmap(cv, gQueen, cx, cy, 35); break;
        }
        imgPaint.setColorFilter(null);
        imgPaint.setAlpha(255);
        solid.setAlpha(255);
    }

    private void glyphBitmap(Canvas cv, Bitmap b, float cx, float cy, float hDp) {
        if (b == null) return;
        float ph = dp(hDp), pw = ph * b.getWidth() / b.getHeight();
        rect.set(cx - pw / 2, cy - ph / 2, cx + pw / 2, cy + ph / 2);
        cv.drawBitmap(b, null, rect, imgPaint);
    }

    private void drawAxe(Canvas cv, float cx, float cy) {
        Path blade = new Path();
        float top = cy - dp(17), bot = cy + dp(3);
        blade.moveTo(cx - dp(2), top);
        blade.lineTo(cx + dp(18), top - dp(2));
        blade.lineTo(cx + dp(18), bot);
        blade.lineTo(cx - dp(2), bot - dp(2));
        blade.close();
        cv.drawPath(blade, solid);
        cv.drawRect(cx - dp(4), top - dp(1), cx + dp(1), cy + dp(17), solid);
        // tiny crown
        Path crown = new Path();
        float by = cy + dp(17), wHalf = dp(8);
        crown.moveTo(cx - wHalf, by);
        crown.lineTo(cx - wHalf, by - dp(6));
        crown.lineTo(cx - wHalf / 2, by - dp(2.5f));
        crown.lineTo(cx, by - dp(6));
        crown.lineTo(cx + wHalf / 2, by - dp(2.5f));
        crown.lineTo(cx + wHalf, by - dp(6));
        crown.lineTo(cx + wHalf, by);
        crown.close();
        cv.drawPath(crown, solid);
    }

    private void drawMint(Canvas cv, float cx, float cy) {
        float s = dp(SIDE) / 2 + dp(2);
        // outer glow
        Paint glow = new Paint(Paint.ANTI_ALIAS_FLAG);
        glow.setShader(new RadialGradient(cx, cy, dp(76),
                new int[]{0x7A5CF0C9, 0x335CF0C9, 0x005CF0C9},
                new float[]{0f, 0.55f, 1f}, Shader.TileMode.CLAMP));
        cv.drawCircle(cx, cy, dp(76), glow);
        // darker under-edge for depth
        solid.setShader(null);
        solid.setColor(0xFF2FA98F);
        drawRotRect(cv, cx, cy + dp(6), s, dp(RADIUS));
        // mint diamond
        solid.setShader(new LinearGradient(cx - s, cy - s, cx - s, cy + s,
                MINT_TOP, MINT_BOT, Shader.TileMode.CLAMP));
        drawRotRect(cv, cx, cy, s, dp(RADIUS));
        solid.setShader(null);
        // white king icon
        solid.setColor(0xFFFFFFFF);
        float kw = dp(33), kh = dp(33);
        float l = cx - kw / 2, r = cx + kw / 2;
        float t = cy - kh / 2 - dp(1), b = cy + kh / 2;
        rect.set(l + dp(3), b - dp(5), r - dp(3), b);
        cv.drawRoundRect(rect, dp(1.8f), dp(1.8f), solid);
        // crown body: two rounded shoulders with curled tips
        Path king = new Path();
        king.moveTo(l + dp(6), b - dp(7));
        king.lineTo(l + dp(6), t + dp(13));
        king.cubicTo(l + dp(6), t + dp(17), l + dp(11), t + dp(17.5f), l + dp(13), t + dp(14));
        king.lineTo(l + dp(15), t + dp(11));
        king.cubicTo(l + dp(15.5f), t + dp(7), cx - dp(2.5f), t + dp(6), cx, t + dp(6.5f));
        king.cubicTo(cx + dp(2.5f), t + dp(6), r - dp(15.5f), t + dp(7), r - dp(15), t + dp(11));
        king.lineTo(r - dp(13), t + dp(14));
        king.cubicTo(r - dp(11), t + dp(17.5f), r - dp(6), t + dp(17), r - dp(6), t + dp(13));
        king.lineTo(r - dp(6), b - dp(7));
        king.close();
        cv.drawPath(king, solid);
        // cross
        rect.set(cx - dp(1.7f), t - dp(6), cx + dp(1.7f), t + dp(7));
        cv.drawRoundRect(rect, dp(0.8f), dp(0.8f), solid);
        rect.set(cx - dp(6), t - dp(1.7f), cx + dp(6), t + dp(1.7f));
        cv.drawRoundRect(rect, dp(0.8f), dp(0.8f), solid);
        // Next pill
        float pw = dp(94), ph = dp(40), py = cy - dp(82);
        solid.setColor(0xFFF8F8F8);
        rect.set(cx - pw / 2, py - ph / 2, cx + pw / 2, py + ph / 2);
        cv.drawRoundRect(rect, dp(9), dp(9), solid);
        // tail
        cv.save();
        cv.rotate(45f, cx, py + ph / 2 + dp(2));
        cv.drawRect(cx - dp(7), py + ph / 2 - dp(9), cx + dp(7), py + ph / 2 + dp(3), solid);
        cv.restore();
        // label
        text.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        text.setTextSize(dp(18.5f));
        text.setColor(0xFF1F1F1F);
        text.setTextAlign(Paint.Align.CENTER);
        Paint.FontMetrics fm = text.getFontMetrics();
        cv.drawText("Next", cx, py - (fm.ascent + fm.descent) / 2f, text);
    }

    private void drawRotRect(Canvas cv, float cx, float cy, float s, float rad) {
        cv.save();
        cv.rotate(45f, cx, cy);
        rect.set(cx - s, cy - s, cx + s, cy + s);
        cv.drawRoundRect(rect, rad, rad, solid);
        cv.restore();
    }

    private void piece(Canvas cv, Bitmap b, float cx, float cy, float hDp) {
        if (b == null) return;
        float ph = dp(hDp), pw = ph * b.getWidth() / b.getHeight();
        imgPaint.setColorFilter(null);
        imgPaint.setAlpha(255);
        rect.set(cx - pw / 2, cy - ph / 2, cx + pw / 2, cy + ph / 2);
        cv.drawBitmap(b, null, rect, imgPaint);
    }
}
