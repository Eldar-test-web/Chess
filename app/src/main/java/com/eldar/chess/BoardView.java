package com.eldar.chess;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.view.View;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Custom chess board view: chess.com green board, coordinate labels inside edge
 * squares, last-move yellow highlight, legal-move dots, tap input.
 */
public class BoardView extends View {

    public interface Listener {
        /** file: 0..7 (a..h), rank: 0..7 (1..8). */
        void onSquareTapped(int file, int rank);
    }

    private static final int LIGHT = 0xFFEEEED2;
    private static final int DARK = 0xFF769656;
    private static final int HIGHLIGHT = 0xFFF7F783;
    private static final int SELECT_OVER = 0x88DDD98A;
    private static final int DOT = 0x66000000;

    private String fen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
    private int[] lastMove = null; // {ff,fr,tf,tr}
    private int[] selected = null;
    private Set<Integer> targets = new HashSet<>();
    private boolean flipped = false;
    private boolean interactive = true;
    private boolean coords = true;
    private Listener listener;

    private final HashMap<String, Bitmap> pieces = new HashMap<>();
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public BoardView(Context c) {
        super(c);
        textPaint.setColor(0x9CEBECD0);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setFakeBoldText(true);
    }

    public void setListener(Listener l) { listener = l; }
    public void setInteractive(boolean b) { interactive = b; }
    public boolean isFlipped() { return flipped; }
    public void setFlipped(boolean f) { flipped = f; invalidate(); }

    public void setCoordinates(boolean show) { coords = show; invalidate(); }

    public void setState(String fen, int[] lastMove) {
        this.fen = fen;
        this.lastMove = lastMove;
        invalidate();
    }

    public void setSelection(int[] sel, Set<Integer> targetSet) {
        this.selected = sel;
        this.targets = targetSet == null ? new HashSet<Integer>() : targetSet;
        invalidate();
    }

    private int squareSize() { return getWidth() / 8; }
    private int cellHeight() { return getHeight() / 8; }

    /** board fills the view; cells are ~2.2% taller than wide as in the 072146 SS */
    private int boardTop() { return 0; }

    @Override protected void onMeasure(int wSpec, int hSpec) {
        int w = MeasureSpec.getSize(wSpec);
        setMeasuredDimension(w, Math.round(w * 1.0224f));
    }

    @Override protected void onDraw(Canvas canvas) {
        int sqw = squareSize();
        int sqh = cellHeight();
        if (sqw <= 0 || sqh <= 0) return;
        int boardY = boardTop();
        String[] rows = parseFen(fen);

        for (int row = 0; row < 8; row++) {          // row 0 = top
            for (int col = 0; col < 8; col++) {      // col 0 = left
                int file = flipped ? 7 - col : col;
                int rank = flipped ? row : 7 - row;
                boolean lightSquare = ((file + rank) % 2) == 1;
                int x = col * sqw, y = boardY + row * sqh;
                int color = lightSquare ? LIGHT : DARK;
                if (lastMove != null && isLast(file, rank)) color = HIGHLIGHT;
                if (selected != null && selected[0] == file && selected[1] == rank) {
                    paint.setColor(color);
                    canvas.drawRect(x, y, x + sqw, y + sqh, paint);
                    paint.setColor(SELECT_OVER);
                    canvas.drawRect(x, y, x + sqw, y + sqh, paint);
                } else {
                    paint.setColor(color);
                    canvas.drawRect(x, y, x + sqw, y + sqh, paint);
                }
                if (targets.contains(key(file, rank))) {
                    String pcHere = rows[7 - rank].charAt(file) == '.' ? null
                            : String.valueOf(rows[7 - rank].charAt(file));
                    paint.setColor(DOT);
                    if (pcHere != null) {
                        // capture target: ring
                        paint.setStyle(Paint.Style.STROKE);
                        paint.setStrokeWidth(sqw * 0.07f);
                        canvas.drawCircle(x + sqw / 2f, y + sqh / 2f, sqw * 0.42f, paint);
                        paint.setStyle(Paint.Style.FILL);
                    } else {
                        canvas.drawCircle(x + sqw / 2f, y + sqh / 2f, sqw * 0.16f, paint);
                    }
                }
                // piece
                String pc = rows[7 - rank].charAt(file) == '.' ? null
                        : String.valueOf(rows[7 - rank].charAt(file));
                if (pc != null) {
                    Bitmap b = getPiece(pc);
                    if (b != null) {
                        paint.setFilterBitmap(true);
                        canvas.drawBitmap(b, null, new Rect(x, y, x + sqw, y + sqh), paint);
                    }
                }
            }
        }
        // coordinates drawn inside edge squares (chess.com style):
        // file letters always on the bottom row, rank numbers always on the left column
        if (!coords) return;
        textPaint.setTextSize(sqw * 0.22f);
        for (int col = 0; col < 8; col++) {
            int file = flipped ? 7 - col : col;
            int rank = flipped ? 7 : 0;          // rank of the bottom row
            boolean lightSquare = ((file + rank) % 2) == 1;
            String letter = String.valueOf((char) ('a' + file));
            float lx = col * sqw + sqw - sqw * 0.12f;
            float ly = boardY + 7 * sqh + sqh - sqh * 0.10f;
            boolean lastCol = lastMove != null && (lastMove[2] == file && lastMove[3] == rank);
            textPaint.setColor(lastCol ? 0xFF4A4A3A : (lightSquare ? 0x8C769656 : 0x9CEEEED2));
            canvas.drawText(letter, lx, ly, textPaint);
        }
        for (int row = 0; row < 8; row++) {
            int rank = flipped ? row : 7 - row;
            int file = flipped ? 7 : 0;          // file of the left column
            boolean lightSquare = ((file + rank) % 2) == 1;
            String num = String.valueOf(rank + 1);
            float lx = sqw * 0.14f;
            float ly = boardY + row * sqh + sqh * 0.20f;
            boolean lastRow = lastMove != null && (lastMove[2] == file && lastMove[3] == rank);
            textPaint.setColor(lastRow ? 0xFF4A4A3A : (lightSquare ? 0x8C769656 : 0x9CEEEED2));
            canvas.drawText(num, lx, ly, textPaint);
        }
    }

    private boolean isLast(int file, int rank) {
        return (lastMove[0] == file && lastMove[1] == rank)
                || (lastMove[2] == file && lastMove[3] == rank);
    }

    private static int key(int file, int rank) { return file * 8 + rank; }

    private Bitmap getPiece(String code) {
        Bitmap b = pieces.get(code);
        if (b == null) {
            String name;
            switch (code) {
                case "K": name = "piece_neo_wk"; break;
                case "Q": name = "piece_neo_wq"; break;
                case "R": name = "piece_neo_wr"; break;
                case "B": name = "piece_neo_wb"; break;
                case "N": name = "piece_neo_wn"; break;
                case "P": name = "piece_neo_wp"; break;
                case "k": name = "piece_neo_bk"; break;
                case "q": name = "piece_neo_bq"; break;
                case "r": name = "piece_neo_br"; break;
                case "b": name = "piece_neo_bb"; break;
                case "n": name = "piece_neo_bn"; break;
                default: name = "piece_neo_bp"; break;
            }
            int id = getResources().getIdentifier(name, "drawable", getContext().getPackageName());
            b = BitmapFactory.decodeResource(getResources(), id);
            pieces.put(code, b);
        }
        return b;
    }

    /** returns 8 strings rank8..rank1, '.' = empty. */
    private static String[] parseFen(String fen) {
        String placement = fen.split(" ")[0];
        String[] parts = placement.split("/");
        String[] out = new String[8];
        for (int i = 0; i < 8; i++) {
            StringBuilder sb = new StringBuilder();
            for (char ch : parts[i].toCharArray()) {
                if (Character.isDigit(ch)) {
                    for (int k = 0; k < ch - '0'; k++) sb.append('.');
                } else sb.append(ch);
            }
            out[i] = sb.toString();
        }
        return out;
    }

    @Override public boolean onTouchEvent(android.view.MotionEvent ev) {
        if (!interactive || listener == null) return false;
        if (ev.getAction() != android.view.MotionEvent.ACTION_UP) return true;
        int sqw = squareSize();
        int sqh = cellHeight();
        if (sqw <= 0 || sqh <= 0) return true;
        int col = (int) (ev.getX() / sqw);
        int row = (int) ((ev.getY() - boardTop()) / sqh);
        if (col < 0 || col > 7 || row < 0 || row > 7) return true;
        int file = flipped ? 7 - col : col;
        int rank = flipped ? row : 7 - row;
        listener.onSquareTapped(file, rank);
        return true;
    }
}
