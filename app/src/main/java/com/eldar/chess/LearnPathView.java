package com.eldar.chess;

import android.animation.ValueAnimator;
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
import android.view.animation.DecelerateInterpolator;
import android.widget.Toast;

/** Long chess.com-style learn journey: 3 sections x 48 diamonds on one canvas path. */
public class LearnPathView extends View {

    public interface Listener {
        void onLessonTap(int lessonIndex);
        void onNextTap();
        void onSectionChanged(String title);
    }

    public static final String[] SECTION_TITLES = {"Learn To Play", "Playing The Game", "Endgames"};

    private static final int SECTION_COUNT = 3;
    private static final int PER_SECTION = 48;
    private static final int TOTAL = SECTION_COUNT * PER_SECTION;
    private static final int REAL_LESSONS = 16;
    private static final int BASE_CURRENT = 48;

    private static final float PITCH = 70f;
    private static final float SIDE = 86f;
    private static final float RADIUS = 16f;
    private static final float COL_OFFSET = 82f;
    private static final float TOP = 200f;
    private static final float SECTION_GAP = 140f;
    private static final float BOTTOM_PAD = 260f;

    private static final float MOSAIC_SIDE = 76f;
    private static final float MOSAIC_COL = 107f;
    private static final float MOSAIC_ROW = 53.5f;

    private static final int MEDAL_R = 45;

    private static final int[] COLUMN = {1, 2, 1, 0, 1, 2, 1, 0, 1, 2, 1, 0, 1, 2, 1, 0};
    private static final int[] GLYPH = {0, 2, 3, 4, 7, 8, 7, 5, 6, 7, 8, 5, 7, 4, 6, 1};

    private static final String[] LESSON_NAMES = {"Move the King", "The Knight", "The Rook",
            "The Bishop", "The Queen", "The Pawn", "Capturing", "Check", "Checkmate",
            "Castling", "En Passant", "Promotion", "The Pin", "The Fork",
            "Back Rank Mate", "Ladder Mate"};

    static final int[] BAND_BASE = {0xFF26301F, 0xFF2D4526, 0xFF33291E};
    private static final int[] UNDER = {0xFF242C1E, 0xFF222C1C, 0xFF2B2419};
    private static final int[] MOSAIC_A = {0xFF2C3627, 0xFF2D3829, 0xFF352E24};
    private static final int[] MOSAIC_B = {0xFF293322, 0xFF293424, 0xFF312A20};
    private static final int[] MEDAL_FILL = {0xFF32422C, 0xFF2D4933, 0xFF3A3226};
    private static final int[] GLYPH_TINT = {0xFFAEB8A4, 0xFFB4BDA9, 0xFFC0B8A6};

    private static final int[][] TILE = {
            {0xFF41543A, 0xFF2F3C2A},   // section 0 future
            {0xFF394A32, 0xFF2A3522},   // section 0 completed
            {0xFF425640, 0xFF2F3C2B},   // section 1 future
            {0xFF3A4C38, 0xFF2A3523},   // section 1 completed
            {0xFF4C4A38, 0xFF383325},   // section 2 future
            {0xFF433F30, 0xFF332E22},   // section 2 completed
    };

    private final float density;
    private final float contentH;

    private final Paint solid = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint imgPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Paint text = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint[] tilePaints = new Paint[6];
    private final PorterDuffColorFilter[] glyphFilters = new PorterDuffColorFilter[3];
    private final RectF rect = new RectF();
    private final Paint.FontMetrics fontMetrics = new Paint.FontMetrics();

    private Bitmap wPawn, wQueen, wRook, wBishop, wKnight;
    private Bitmap gKing, gPawn, gKnight, gBishop, gQueen, gRook;

    private float scrollY, lastY, touchX, touchY;
    private boolean dragging;
    private boolean initialScrollDone;
    private int lessonProgress;
    private int currentIndex = BASE_CURRENT;
    private int lastSection = -1;
    private Listener listener;
    private ValueAnimator scrollAnimator;

    public LearnPathView(Context c) {
        super(c);
        density = c.getResources().getDisplayMetrics().density;
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

        contentH = dp(TOP + (TOTAL - 1) * PITCH
                + (SECTION_COUNT - 1) * SECTION_GAP + BOTTOM_PAD);

        float half = dp(SIDE) / 2f;
        for (int i = 0; i < tilePaints.length; i++) {
            Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
            p.setShader(new LinearGradient(-half, -half, half * 0.7f, half,
                    TILE[i][0], TILE[i][1], Shader.TileMode.CLAMP));
            tilePaints[i] = p;
        }
        for (int i = 0; i < SECTION_COUNT; i++) {
            glyphFilters[i] = new PorterDuffColorFilter(GLYPH_TINT[i], PorterDuff.Mode.SRC_IN);
        }
    }

    public void setListener(Listener l) {
        listener = l;
        lastSection = -1;
        post(new Runnable() {
            @Override public void run() { updateSectionFromScroll(); }
        });
    }

    public void setProgress(int p) {
        lessonProgress = Math.max(0, Math.min(REAL_LESSONS, p));
        currentIndex = Math.min(TOTAL - 1, BASE_CURRENT + lessonProgress);
        invalidate();
    }

    public void smoothScrollDown() {
        float vh = getHeight();
        if (vh <= 0f) return;
        float max = Math.max(0f, contentH - vh);
        float target = Math.min(max, scrollY + vh * 0.82f);
        if (target <= scrollY + dp(2f)) return;
        if (scrollAnimator != null) scrollAnimator.cancel();
        ValueAnimator anim = ValueAnimator.ofFloat(scrollY, target);
        anim.setDuration(430);
        anim.setInterpolator(new DecelerateInterpolator());
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override public void onAnimationUpdate(ValueAnimator animation) {
                scrollY = (Float) animation.getAnimatedValue();
                invalidate();
                updateSectionFromScroll();
            }
        });
        scrollAnimator = anim;
        anim.start();
    }

    private float dp(float v) { return v * density; }

    private float nodeY(int index) {
        return dp(TOP + index * PITCH + (index / PER_SECTION) * SECTION_GAP);
    }

    private float medallionY(int section) {
        if (section == 0) return dp(TOP - 100f);
        float first = nodeY(section * PER_SECTION);
        float prev = nodeY(section * PER_SECTION - 1);
        return (first + prev) / 2f;
    }

    private float columnX(int column, float w) {
        float offset = Math.min(dp(COL_OFFSET), w / 2f - dp(36f));
        offset = Math.max(dp(34f), offset);
        return w / 2f + (column - 1) * offset;
    }

    private float maxScroll() {
        return Math.max(0f, contentH - getHeight());
    }

    @Override protected void onSizeChanged(int w, int h, int ow, int oh) {
        if (!initialScrollDone) {
            float top = nodeY(currentIndex) - h * 0.42f;
            scrollY = Math.max(0f, Math.min(contentH - h, top));
            initialScrollDone = true;
        } else {
            scrollY = Math.max(0f, Math.min(contentH - h, scrollY));
        }
        updateSectionFromScroll();
    }

    @Override protected void onDraw(Canvas cv) {
        float w = getWidth();
        float vh = getHeight();

        solid.setShader(null);
        solid.setStyle(Paint.Style.FILL);
        solid.setColor(BAND_BASE[1]);
        cv.drawRect(0f, 0f, w, vh, solid);

        cv.save();
        cv.translate(0f, -scrollY);

        drawSectionBackgrounds(cv, w, vh);
        drawDecorations(cv, w, vh);
        drawNodes(cv, w, vh);
        drawAmbientPieces(cv, w);
        drawCurrentMedallion(cv, w);
        cv.restore();

        // warm dark veil over the pinned coach area, like the recording
        solid.setShader(new LinearGradient(0f, 0f, 0f, dp(170f),
                new int[]{0xE026201A, 0xB3231E18, 0x50221E18, 0x00221E18},
                new float[]{0f, 0.42f, 0.74f, 1f}, Shader.TileMode.CLAMP));
        cv.drawRect(0f, 0f, w, dp(170f), solid);
        solid.setShader(null);
    }

    private void drawSectionBackgrounds(Canvas cv, float w, float vh) {
        for (int s = 0; s < SECTION_COUNT; s++) {
            float top = s == 0 ? 0f : medallionY(s);
            float bottom = s == SECTION_COUNT - 1 ? contentH : medallionY(s + 1);
            if (bottom < scrollY - dp(40f) || top > scrollY + vh + dp(40f)) continue;
            cv.save();
            cv.clipRect(0f, top, w, bottom);

            solid.setShader(null);
            solid.setStyle(Paint.Style.FILL);
            solid.setColor(UNDER[s]);
            cv.drawRect(0f, top, w, bottom, solid);

            drawMosaic(cv, w, vh, s);

            cv.restore();
        }
    }

    private void drawMosaic(Canvas cv, float w, float vh, int section) {
        float side = dp(MOSAIC_SIDE);
        float colStep = dp(MOSAIC_COL);
        float rowStep = dp(MOSAIC_ROW);
        float half = side / 2f;
        float rad = dp(4f);
        int firstRow = (int) Math.floor((scrollY - side) / rowStep) - 1;
        int lastRow = (int) Math.ceil((scrollY + vh + side) / rowStep) + 1;
        int cols = (int) Math.ceil(w / colStep) + 3;
        for (int row = firstRow; row <= lastRow; row++) {
            float cy = row * rowStep;
            int parity = ((row % 2) + 2) % 2;
            for (int i = -2; i < cols; i++) {
                float cx = i * colStep + parity * colStep / 2f;
                if (cx < -side || cx > w + side) continue;
                int tone = (((row * 7 + i * 3) % 5) + 5) % 5;
                solid.setShader(null);
                solid.setStyle(Paint.Style.FILL);
                int base = ((row + i) & 1) == 0 ? MOSAIC_A[section] : MOSAIC_B[section];
                solid.setColor(tone == 0 ? lighten(base, 4) : base);
                cv.save();
                cv.rotate(45f, cx, cy);
                rect.set(cx - half, cy - half, cx + half, cy + half);
                cv.drawRoundRect(rect, rad, rad, solid);
                cv.restore();
            }
        }
    }

    private static int lighten(int color, int amt) {
        int r = Math.min(255, ((color >> 16) & 0xFF) + amt);
        int g = Math.min(255, ((color >> 8) & 0xFF) + amt);
        int b = Math.min(255, (color & 0xFF) + amt);
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    private void drawAmbientPieces(Canvas cv, float w) {
        float cx = printCurrentX(w), cy = nodeY(currentIndex);
        ambient(cv, wRook, cx - dp(84f), cy + dp(62f), 92f);
        ambient(cv, wBishop, cx + dp(92f), cy + dp(225f), 92f);
    }

    private void ambient(Canvas cv, Bitmap b, float cx, float cy, float hDp) {
        if (b == null) return;
        float ph = dp(hDp);
        float pw = ph * b.getWidth() / b.getHeight();
        solid.setShader(null);
        solid.setStyle(Paint.Style.FILL);
        solid.setColor(0x33000000);
        cv.drawOval(cx - pw * 0.34f, cy + ph / 2f - dp(3f),
                cx + pw * 0.34f, cy + ph / 2f + dp(3f), solid);
        piece(cv, b, cx, cy, hDp, 255);
    }

    private float printCurrentX(float w) {
        return columnX(COLUMN[currentIndex % 16], w);
    }

    private void drawCurrentMedallion(Canvas cv, float w) {
        float cx = printCurrentX(w);
        float cy = nodeY(currentIndex);
        float r = dp(MEDAL_R);
        int section = Math.min(SECTION_COUNT - 1, currentIndex / PER_SECTION);
        solid.setShader(null);
        solid.setStyle(Paint.Style.FILL);
        solid.setColor(0x40000000);
        cv.drawCircle(cx, cy + dp(5f), r, solid);
        solid.setColor(MEDAL_FILL[section]);
        cv.drawCircle(cx, cy, r, solid);
        solid.setStyle(Paint.Style.STROKE);
        solid.setStrokeWidth(dp(2.5f));
        solid.setColor(0x1F000000);
        cv.drawCircle(cx, cy, r - dp(3f), solid);
        solid.setStyle(Paint.Style.FILL);
        drawCap3d(cv, cx, cy);
    }

    private void drawCap3d(Canvas cv, float cx, float cy) {
        float s = dp(48f);
        solid.setShader(null);
        solid.setStyle(Paint.Style.FILL);

        Path base = new Path();
        base.moveTo(cx - s * 0.42f, cy + s * 0.06f);
        base.lineTo(cx + s * 0.42f, cy + s * 0.06f);
        base.lineTo(cx + s * 0.33f, cy + s * 0.34f);
        base.lineTo(cx - s * 0.33f, cy + s * 0.34f);
        base.close();
        solid.setColor(0xFF1B5E8A);
        cv.drawPath(base, solid);

        solid.setColor(0xFF2E86C1);
        Path left = new Path();
        left.moveTo(cx, cy - s * 0.36f);
        left.lineTo(cx + s * 0.62f, cy - s * 0.10f);
        left.lineTo(cx, cy + s * 0.14f);
        left.lineTo(cx - s * 0.62f, cy - s * 0.10f);
        left.close();
        cv.drawPath(left, solid);

        Path shine = new Path();
        shine.moveTo(cx, cy - s * 0.36f);
        shine.lineTo(cx + s * 0.62f, cy - s * 0.10f);
        shine.lineTo(cx, cy - s * 0.02f);
        shine.lineTo(cx - s * 0.62f, cy - s * 0.10f);
        shine.close();
        solid.setColor(0xFF4FA8DE);
        cv.drawPath(shine, solid);

        solid.setColor(0xFF123F5C);
        cv.drawCircle(cx, cy - s * 0.20f, s * 0.055f, solid);

        solid.setStyle(Paint.Style.STROKE);
        solid.setStrokeWidth(dp(2.2f));
        solid.setStrokeCap(Paint.Cap.ROUND);
        solid.setColor(0xFFE9C46A);
        cv.drawLine(cx + s * 0.10f, cy - s * 0.26f, cx + s * 0.40f, cy + s * 0.16f, solid);
        solid.setStyle(Paint.Style.FILL);
        cv.drawCircle(cx + s * 0.40f, cy + s * 0.18f, dp(3f), solid);
    }

    private void drawDecorations(Canvas cv, float w, float vh) {
        for (int k = 0; k < 24; k++) {
            float cy = dp(330f + k * 285f + (k % 3) * 38f);
            if (cy < scrollY - dp(130f) || cy > scrollY + vh + dp(130f)) continue;
            float h = 62f + (k % 4) * 8f;
            float cx = w / 2f + ((k % 3) - 1) * dp(96f) + dp(((k * 7) % 11) - 5);
            if (coversCard(cx, cy, w)) continue;
            solid.setShader(null);
            solid.setStyle(Paint.Style.FILL);
            solid.setColor(0x2E000000);
            float base = cy + dp(h) / 2f - dp(4f);
            cv.drawOval(cx - dp(h) * 0.30f, base, cx + dp(h) * 0.30f, base + dp(h) * 0.10f, solid);
            piece(cv, decoration(k), cx, cy, h, 255);
        }
    }

    private boolean coversCard(float cx, float cy, float w) {
        int from = Math.max(0, currentIndex - 6);
        int to = Math.min(TOTAL - 1, currentIndex + 8);
        for (int i = from; i <= to; i++) {
            if (Math.abs(cy - nodeY(i)) < dp(46f)
                    && Math.abs(cx - columnX(COLUMN[i % 16], w)) < dp(64f)) return true;
        }
        return false;
    }

    private Bitmap decoration(int k) {
        switch (k % 6) {
            case 0: return wPawn;
            case 1: return wQueen;
            case 2: return wRook;
            case 3: return wBishop;
            case 4: return wKnight;
            default: return wPawn;
        }
    }

    private void drawNodes(Canvas cv, float w, float vh) {
        for (int i = 0; i < TOTAL; i++) {
            float cy = nodeY(i);
            if (cy < scrollY - dp(100f) || cy > scrollY + vh + dp(60f)) continue;
            if (i == currentIndex) continue;
            int section = i / PER_SECTION;
            float cx = columnX(COLUMN[i % 16], w);
            boolean done = i < currentIndex;
            drawTile(cv, cx, cy, section, done);
            drawGlyph(cv, cx, cy, GLYPH[i % 16], section, done);
        }
    }

    private void drawTile(Canvas cv, float cx, float cy, int section, boolean done) {
        float half = dp(SIDE) / 2f;
        cv.save();
        cv.translate(cx, cy);
        cv.rotate(45f);
        solid.setShader(null);
        solid.setStyle(Paint.Style.FILL);
        solid.setColor(0x33000000);
        rect.set(-half * 0.96f, -half * 0.96f + dp(5f),
                half * 0.96f, half * 0.96f + dp(5f));
        cv.drawRoundRect(rect, dp(RADIUS), dp(RADIUS), solid);
        solid.setColor(0xFF283322);
        rect.set(-half * 0.99f, -half * 0.99f + dp(3.5f),
                half * 0.99f, half * 0.99f + dp(3.5f));
        cv.drawRoundRect(rect, dp(RADIUS), dp(RADIUS), solid);
        rect.set(-half, -half, half, half);
        cv.drawRoundRect(rect, dp(RADIUS), dp(RADIUS),
                tilePaints[section * 2 + (done ? 1 : 0)]);
        solid.setStyle(Paint.Style.STROKE);
        solid.setStrokeWidth(dp(2f));
        solid.setColor(0x12FFFFFF);
        rect.set(-half * 0.84f, -half * 0.84f, half * 0.84f, half * 0.84f);
        cv.drawRoundRect(rect, dp(RADIUS) * 0.8f, dp(RADIUS) * 0.8f, solid);
        solid.setStyle(Paint.Style.FILL);
        cv.restore();
    }

    private void drawGlyph(Canvas cv, float cx, float cy, int id, int section, boolean done) {
        int alpha = done ? 120 : 165;
        imgPaint.setColorFilter(glyphFilters[section]);
        imgPaint.setAlpha(alpha);
        solid.setShader(null);
        solid.setStyle(Paint.Style.FILL);
        solid.setColor(GLYPH_TINT[section]);
        solid.setAlpha(alpha);
        switch (id) {
            case 0: glyphBitmap(cv, gKing, cx, cy, 44f); break;
            case 1: {
                float arm = dp(8.5f), len = dp(39f);
                rect.set(cx - arm / 2f, cy - len / 2f, cx + arm / 2f, cy + len / 2f);
                cv.drawRoundRect(rect, arm / 2f, arm / 2f, solid);
                rect.set(cx - len / 2f, cy - arm / 2f, cx + len / 2f, cy + arm / 2f);
                cv.drawRoundRect(rect, arm / 2f, arm / 2f, solid);
                break;
            }
            case 2: drawAxe(cv, cx, cy); break;
            case 3: {
                float bar = dp(8f), len = dp(44f), gap = dp(12.5f);
                rect.set(cx - gap - bar / 2f, cy - len / 2f, cx - gap + bar / 2f, cy + len / 2f);
                cv.drawRoundRect(rect, bar / 2f, bar / 2f, solid);
                rect.set(cx + gap - bar / 2f, cy - len / 2f, cx + gap + bar / 2f, cy + len / 2f);
                cv.drawRoundRect(rect, bar / 2f, bar / 2f, solid);
                rect.set(cx - len / 2f, cy - gap - bar / 2f, cx + len / 2f, cy - gap + bar / 2f);
                cv.drawRoundRect(rect, bar / 2f, bar / 2f, solid);
                rect.set(cx - len / 2f, cy + gap - bar / 2f, cx + len / 2f, cy + gap + bar / 2f);
                cv.drawRoundRect(rect, bar / 2f, bar / 2f, solid);
                break;
            }
            case 4: glyphBitmap(cv, gPawn, cx, cy, 36f); break;
            case 5: glyphBitmap(cv, gKnight, cx, cy, 42f); break;
            case 6: glyphBitmap(cv, gBishop, cx, cy, 42f); break;
            case 7: glyphBitmap(cv, gRook, cx, cy, 40f); break;
            default: glyphBitmap(cv, gQueen, cx, cy, 42f); break;
        }
        imgPaint.setColorFilter(null);
        imgPaint.setAlpha(255);
        solid.setAlpha(255);
    }

    private void glyphBitmap(Canvas cv, Bitmap b, float cx, float cy, float hDp) {
        if (b == null) return;
        float ph = dp(hDp);
        float pw = ph * b.getWidth() / b.getHeight();
        rect.set(cx - pw / 2f, cy - ph / 2f, cx + pw / 2f, cy + ph / 2f);
        cv.drawBitmap(b, null, rect, imgPaint);
    }

    private void drawAxe(Canvas cv, float cx, float cy) {
        Path blade = new Path();
        float top = cy - dp(21f), bot = cy + dp(4f);
        blade.moveTo(cx - dp(2f), top);
        blade.lineTo(cx + dp(22f), top - dp(2f));
        blade.lineTo(cx + dp(22f), bot);
        blade.lineTo(cx - dp(2f), bot - dp(2f));
        blade.close();
        cv.drawPath(blade, solid);
        cv.drawRect(cx - dp(4.5f), top - dp(1f), cx + dp(1.5f), cy + dp(21f), solid);
        Path crown = new Path();
        float by = cy + dp(21f), wHalf = dp(9.5f);
        crown.moveTo(cx - wHalf, by);
        crown.lineTo(cx - wHalf, by - dp(6.5f));
        crown.lineTo(cx - wHalf / 2f, by - dp(3.2f));
        crown.lineTo(cx, by - dp(6.5f));
        crown.lineTo(cx + wHalf / 2f, by - dp(3.2f));
        crown.lineTo(cx + wHalf, by - dp(6.5f));
        crown.lineTo(cx + wHalf, by);
        crown.close();
        cv.drawPath(crown, solid);
    }

    private void piece(Canvas cv, Bitmap b, float cx, float cy, float hDp, int alpha) {
        if (b == null) return;
        float ph = dp(hDp);
        float pw = ph * b.getWidth() / b.getHeight();
        imgPaint.setColorFilter(null);
        imgPaint.setAlpha(alpha);
        rect.set(cx - pw / 2f, cy - ph / 2f, cx + pw / 2f, cy + ph / 2f);
        cv.drawBitmap(b, null, rect, imgPaint);
        imgPaint.setAlpha(255);
    }

    private void nextLesson() {
        if (listener != null) {
            listener.onNextTap();
        } else {
            Toast.makeText(getContext(), "Ready for the next lesson!",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override public boolean onTouchEvent(MotionEvent e) {
        switch (e.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                if (scrollAnimator != null) {
                    scrollAnimator.cancel();
                    scrollAnimator = null;
                }
                touchX = e.getX();
                touchY = e.getY();
                lastY = touchY;
                dragging = false;
                return true;
            case MotionEvent.ACTION_MOVE: {
                float dy = e.getY() - lastY;
                lastY = e.getY();
                if (Math.abs(e.getY() - touchY) > dp(6f)) dragging = true;
                if (dragging) {
                    scrollY = Math.max(0f, Math.min(maxScroll(), scrollY - dy));
                    invalidate();
                    updateSectionFromScroll();
                }
                return true;
            }
            case MotionEvent.ACTION_UP:
                if (!dragging && Math.abs(e.getX() - touchX) < dp(12f)
                        && Math.abs(e.getY() - touchY) < dp(12f)) {
                    performClick();
                    float tx = e.getX();
                    float ty = e.getY() + scrollY;
                    float w = getWidth();
                    float curX = printCurrentX(w);
                    float curY = nodeY(currentIndex);
                    if (Math.hypot(tx - curX, ty - curY) <= dp(MEDAL_R + 8f)) {
                        nextLesson();
                        return true;
                    }
                    for (int i = 0; i < TOTAL; i++) {
                        float cy = nodeY(i);
                        if (Math.abs(ty - cy) > dp(52f)) continue;
                        float cx = columnX(COLUMN[i % 16], w);
                        if (Math.abs(tx - cx) + Math.abs(ty - cy) <= dp(50f)) {
                            if (i == currentIndex) {
                                nextLesson();
                            } else if (listener != null) {
                                listener.onLessonTap(i % REAL_LESSONS);
                            } else {
                                Toast.makeText(getContext(),
                                        LESSON_NAMES[i % REAL_LESSONS],
                                        Toast.LENGTH_SHORT).show();
                            }
                            return true;
                        }
                    }
                }
                return true;
            default:
                return super.onTouchEvent(e);
        }
    }

    private void updateSectionFromScroll() {
        if (getHeight() == 0) return;
        float probe = scrollY + getHeight() * 0.62f;
        int section = probe >= medallionY(2) ? 2 : probe >= medallionY(1) ? 1 : 0;
        if (section == lastSection) return;
        lastSection = section;
        if (listener != null) listener.onSectionChanged(SECTION_TITLES[section]);
    }

    @Override protected void onDetachedFromWindow() {
        if (scrollAnimator != null) {
            scrollAnimator.cancel();
            scrollAnimator = null;
        }
        super.onDetachedFromWindow();
    }

    @Override public boolean performClick() {
        super.performClick();
        return true;
    }
}
