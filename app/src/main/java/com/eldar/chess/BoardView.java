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

    private static final int LIGHT = 0xFFEBECD0;
    private static final int DARK = 0xFF739552;
    private static final int HIGHLIGHT = 0xFFEFE96E;
    private static final int SELECT_OVER = 0x88DDD98A;
    private static final int DOT = 0x66000000;

    private String fen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
    private int[] lastMove = null; // {ff,fr,tf,tr}
    private int[] selected = null;
    private Set<Integer> targets = new HashSet<>();
    private boolean flipped = false;
    private boolean interactive = true;
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

    /** the 8x8 board is square; centre it vertically in the view */
    private int boardTop() { return Math.max(0, (getHeight() - getWidth()) / 2); }

    @Override protected void onDraw(Canvas canvas) {
        int sq = squareSize();
        if (sq <= 0) return;
        int boardY = boardTop();
        String[] rows = parseFen(fen);

        for (int row = 0; row < 8; row++) {          // row 0 = top
            for (int col = 0; col < 8; col++) {      // col 0 = left
                int file = flipped ? 7 - col : col;
                int rank = flipped ? row : 7 - row;
                boolean lightSquare = ((file + rank) % 2) == 1;
                int x = col * sq, y = boardY + row * sq;
                int color = lightSquare ? LIGHT : DARK;
                if (lastMove != null && isLast(file, rank)) color = HIGHLIGHT;
                if (selected != null && selected[0] == file && selected[1] == rank) {
                    paint.setColor(color);
                    canvas.drawRect(x, y, x + sq, y + sq, paint);
                    paint.setColor(SELECT_OVER);
                    canvas.drawRect(x, y, x + sq, y + sq, paint);
                } else {
                    paint.setColor(color);
                    canvas.drawRect(x, y, x + sq, y + sq, paint);
                }
                if (targets.contains(key(file, rank))) {
                    String pcHere = rows[7 - rank].charAt(file) == '.' ? null
                            : String.valueOf(rows[7 - rank].charAt(file));
                    paint.setColor(DOT);
                    if (pcHere != null) {
                        // capture target: ring
                        paint.setStyle(Paint.Style.STROKE);
                        paint.setStrokeWidth(sq * 0.07f);
                        canvas.drawCircle(x + sq / 2f, y + sq / 2f, sq * 0.42f, paint);
                        paint.setStyle(Paint.Style.FILL);
                    } else {
                        canvas.drawCircle(x + sq / 2f, y + sq / 2f, sq * 0.16f, paint);
                    }
                }
                // piece
                String pc = rows[7 - rank].charAt(file) == '.' ? null
                        : String.valueOf(rows[7 - rank].charAt(file));
                if (pc != null) {
                    Bitmap b = getPiece(pc);
                    if (b != null) {
                        int size = (int) (sq * 0.98f);
                        float left = x + (sq - size) / 2f, top = y + (sq - size) / 2f;
                        canvas.drawBitmap(b, null, new Rect((int) left, (int) top,
                                (int) left + size, (int) top + size), paint);
                    }
                }
            }
        }
        // coordinates drawn inside edge squares (chess.com style):
        // file letters always on the bottom row, rank numbers always on the left column
        textPaint.setTextSize(sq * 0.22f);
        for (int col = 0; col < 8; col++) {
            int file = flipped ? 7 - col : col;
            int rank = flipped ? 7 : 0;          // rank of the bottom row
            boolean lightSquare = ((file + rank) % 2) == 1;
            String letter = String.valueOf((char) ('a' + file));
            float lx = col * sq + sq - sq * 0.12f;
            float ly = boardY + 7 * sq + sq - sq * 0.10f;
            boolean lastCol = lastMove != null && (lastMove[2] == file && lastMove[3] == rank);
            textPaint.setColor(lastCol ? 0xFF4A4A3A : (lightSquare ? 0x8C739552 : 0x9CEBECD0));
            canvas.drawText(letter, lx, ly, textPaint);
        }
        for (int row = 0; row < 8; row++) {
            int rank = flipped ? row : 7 - row;
            int file = flipped ? 7 : 0;          // file of the left column
            boolean lightSquare = ((file + rank) % 2) == 1;
            String num = String.valueOf(rank + 1);
            float lx = sq * 0.14f;
            float ly = boardY + row * sq + sq * 0.20f;
            boolean lastRow = lastMove != null && (lastMove[2] == file && lastMove[3] == rank);
            textPaint.setColor(lastRow ? 0xFF4A4A3A : (lightSquare ? 0x8C739552 : 0x9CEBECD0));
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
                case "K": name = "piece_wk"; break;
                case "Q": name = "piece_wq"; break;
                case "R": name = "piece_wr"; break;
                case "B": name = "piece_wb"; break;
                case "N": name = "piece_wn"; break;
                case "P": name = "piece_wp"; break;
                case "k": name = "piece_bk"; break;
                case "q": name = "piece_bq"; break;
                case "r": name = "piece_br"; break;
                case "b": name = "piece_bb"; break;
                case "n": name = "piece_bn"; break;
                default: name = "piece_bp"; break;
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
        int sq = squareSize();
        if (sq <= 0) return true;
        int col = (int) (ev.getX() / sq);
        int row = (int) ((ev.getY() - boardTop()) / sq);
        if (col < 0 || col > 7 || row < 0 || row > 7) return true;
        int file = flipped ? 7 - col : col;
        int rank = flipped ? row : 7 - row;
        listener.onSquareTapped(file, rank);
        return true;
    }
}
