package com.eldar.chess;

import android.app.AlertDialog;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.bhlangonijr.chesslib.Piece;
import com.github.bhlangonijr.chesslib.Side;
import com.github.bhlangonijr.chesslib.Square;
import com.github.bhlangonijr.chesslib.move.Move;

import java.util.HashSet;
import java.util.Set;

/** In-game screen: searching -> live game with clocks, coach tips, result overlay. */
public class GameScreen {

    public static GameSession.Persona pendingPersona = null;
    public static long pendingTime = 600000;
    public static boolean pendingPassPlay = false;
    /** 072139/072146: online games seat the player on the black side */
    public static boolean pendingUserBlack = false;

    private GameSession session;
    private BoardView boardView;
    private final MainActivity main;

    private TextView opponentClock, userClock, opponentName, userName;
    private ImageView opponentFlag, userFlag;
    private LinearLayout opponentRow;
    private FrameLayout opponentAvatar;
    private HorizontalScrollView moveScroll;
    private LinearLayout moveRow;
    private FrameLayout boardOverlay;
    private View searchDim;
    private LinearLayout coachBubble;
    private TextView coachText;
    private View resultOverlay;
    private LinearLayout bottomBar;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private long lastTick = 0;
    private boolean running = false;
    private int selectedFile = -1, selectedRank = -1;
    private boolean destroyed = false;

    private static final String[] COACH_TIPS = {
            "Don't forget to bring all of your pieces into the game!",
            "Control the center early in the game!",
            "Castle your king to keep it safe!",
            "Think before you move - what does the opponent want?",
            "Look for checks, captures and threats!"
    };

    public GameScreen(MainActivity main) {
        this.main = main;
    }

    public static View build(final MainActivity main) {
        GameScreen gs = new GameScreen(main);
        main.activeGame = gs;
        return gs.create();
    }

    private View create() {
        session = new GameSession(pendingPersona, pendingTime);
        session.passAndPlay = pendingPassPlay;
        session.userIsWhite = !pendingPassPlay && !pendingUserBlack;
        final android.content.Context c = main;

        LinearLayout root = Ui.linear(c, LinearLayout.VERTICAL);
        root.setBackgroundColor(Ui.BG);

        // ---------- top bar ----------
        LinearLayout top = Ui.linear(c, LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);
        top.setPadding(Ui.dp(c, 8), Ui.dp(c, 6), Ui.dp(c, 8), Ui.dp(c, 4));
        ImageView back = Ui.img(c, R.drawable.ic_back, 20);
        back.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { onBackPressed(); }
        });
        top.addView(back);
        LinearLayout logo = Ui.logo(c, 16, 13);
        logo.setLayoutParams(new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        logo.setGravity(Gravity.CENTER);
        top.addView(logo);
        ImageView signal = Ui.img(c, R.drawable.ic_signal, 16);
        signal.setColorFilter(0xFFB5B0A8);
        top.addView(signal);
        root.addView(top);

        // ---------- move list bar ----------
        moveScroll = new HorizontalScrollView(c);
        moveScroll.setHorizontalScrollBarEnabled(false);
        moveScroll.setBackgroundColor(0xFF211F1C);
        moveScroll.setVisibility(View.GONE);
        moveScroll.setPadding(Ui.dp(c, 8), Ui.dp(c, 3), Ui.dp(c, 8), Ui.dp(c, 3));
        moveRow = Ui.linear(c, LinearLayout.HORIZONTAL);
        moveRow.setGravity(Gravity.CENTER_VERTICAL);
        moveScroll.addView(moveRow, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
        root.addView(moveScroll, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        // ---------- opponent row ----------
        opponentRow = Ui.linear(c, LinearLayout.HORIZONTAL);
        opponentRow.setGravity(Gravity.CENTER_VERTICAL);
        opponentRow.setPadding(Ui.dp(c, 8), Ui.dp(c, 8), Ui.dp(c, 8), Ui.dp(c, 8));
        opponentAvatar = new FrameLayout(c);
        opponentAvatar.setBackground(Ui.round(0xFFD9D5CC, 4, c));
        opponentAvatar.addView(Ui.glyph(c, "person", 17, 0xFF3B3935),
                new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        opponentRow.addView(opponentAvatar, new LinearLayout.LayoutParams(
                Ui.dp(c, 30), Ui.dp(c, 30)));

        opponentName = Ui.text(c, "", 11, Ui.WHITE, false);
        LinearLayout.LayoutParams np = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        np.setMargins(Ui.dp(c, 7), 0, 0, 0);
        opponentName.setLayoutParams(np);
        opponentRow.addView(opponentName);

        opponentFlag = new ImageView(c);
        opponentFlag.setLayoutParams(new LinearLayout.LayoutParams(Ui.dp(c, 10), Ui.dp(c, 7)));
        opponentRow.addView(opponentFlag);

        opponentClock = Ui.text(c, "10:00", 12, Ui.WHITE, true);
        opponentClock.setGravity(Gravity.CENTER);
        opponentClock.setBackground(Ui.round(0xFF56534E, 4, c));
        LinearLayout.LayoutParams ocp = new LinearLayout.LayoutParams(
                Ui.dp(c, 53), Ui.dp(c, 29));
        ocp.setMargins(Ui.dp(c, 8), 0, 0, 0);
        opponentClock.setLayoutParams(ocp);
        opponentRow.addView(opponentClock);
        root.addView(opponentRow);

        // ---------- coach bubble ----------
        coachBubble = Ui.linear(c, LinearLayout.HORIZONTAL);
        coachBubble.setVisibility(View.GONE);
        coachBubble.setGravity(Gravity.CENTER_VERTICAL);
        coachBubble.setPadding(Ui.dp(c, 6), Ui.dp(c, 0), Ui.dp(c, 8), Ui.dp(c, 4));
        FrameLayout coachAv = new FrameLayout(c);
        View coachBg = new View(c);
        coachBg.setBackground(Ui.round(0xFFD9D5CC, 17, c));
        coachAv.addView(coachBg, new FrameLayout.LayoutParams(
                Ui.dp(c, 34), Ui.dp(c, 34), Gravity.CENTER));
        coachAv.addView(Ui.glyph(c, "person", 21, 0xFF3B3935),
                new FrameLayout.LayoutParams(
                        Ui.dp(c, 34), Ui.dp(c, 34), Gravity.CENTER));
        coachBubble.addView(coachAv, new LinearLayout.LayoutParams(
                Ui.dp(c, 38), Ui.dp(c, 46)));
        coachAv.setTranslationY(-Ui.dp(c, 10));   // overlaps the name row (072139)
        coachText = Ui.text(c, COACH_TIPS[0], 12, 0xFF2E2C29, false);
        coachText.setTextColor(0xFF2E2C29);
        coachText.setBackground(Ui.round(0xFFB8B7B6, 8, c));
        coachText.setPadding(Ui.dp(c, 10), Ui.dp(c, 7), Ui.dp(c, 10), Ui.dp(c, 7));
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        bp.setMargins(0, 0, Ui.dp(c, 10), 0);
        coachText.setLayoutParams(bp);
        coachBubble.addView(coachText);
        root.addView(coachBubble);

        // ---------- board ----------
        boardView = new BoardView(c);
        boardView.setListener(new BoardView.Listener() {
            @Override public void onSquareTapped(int file, int rank) { onSquare(file, rank); }
        });
        boardView.setFlipped(!session.userIsWhite);
        boardView.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        boardOverlay = new FrameLayout(c);
        boardOverlay.addView(boardView);

        // searching layer (072139): gradient scrim fading down the empty board
        searchDim = Ui.linear(c, LinearLayout.VERTICAL);
        searchDim.setBackground(Ui.scrim());
        ((LinearLayout) searchDim).setGravity(Gravity.CENTER);
        TextView searching = Ui.text(c, "Searching...", 24, Ui.WHITE, true);
        searching.setGravity(Gravity.CENTER);
        ((LinearLayout) searchDim).addView(searching);
        FrameLayout stopHalo = new FrameLayout(c);
        stopHalo.setBackground(Ui.round(0x2EFFFFFF, 13, c));
        FrameLayout stopButton = new FrameLayout(c);
        stopButton.setBackground(Ui.round(0xFF2E2C29, 8, c));
        stopButton.addView(Ui.glyph(c, "timer", 19, Ui.WHITE),
                new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        FrameLayout.LayoutParams sbp = new FrameLayout.LayoutParams(
                Ui.dp(c, 44), Ui.dp(c, 44), Gravity.CENTER);
        stopHalo.addView(stopButton, sbp);
        LinearLayout.LayoutParams stp = new LinearLayout.LayoutParams(
                Ui.dp(c, 60), Ui.dp(c, 60));
        stp.setMargins(0, Ui.dp(c, 10), 0, 0);
        stopHalo.setLayoutParams(stp);
        stopHalo.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { main.navigate("home"); }
        });
        ((LinearLayout) searchDim).addView(stopHalo);
        boardOverlay.addView(searchDim, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        root.addView(boardOverlay, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        // ---------- user row ----------
        LinearLayout userRow = Ui.linear(c, LinearLayout.HORIZONTAL);
        userRow.setGravity(Gravity.CENTER_VERTICAL);
        userRow.setPadding(Ui.dp(c, 8), Ui.dp(c, 8), Ui.dp(c, 8), Ui.dp(c, 8));
        FrameLayout userAv = new FrameLayout(c);
        userAv.setBackground(Ui.round(0xFFD9D5CC, 4, c));
        userAv.addView(Ui.glyph(c, "person", 17, 0xFF3B3935),
                new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        userRow.addView(userAv, new LinearLayout.LayoutParams(Ui.dp(c, 30), Ui.dp(c, 30)));

        userName = Ui.text(c, "", 11, Ui.WHITE, false);
        LinearLayout.LayoutParams unp = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        unp.setMargins(Ui.dp(c, 7), 0, 0, 0);
        userName.setLayoutParams(unp);
        userRow.addView(userName);

        userFlag = new ImageView(c);
        userFlag.setLayoutParams(new LinearLayout.LayoutParams(Ui.dp(c, 10), Ui.dp(c, 7)));
        userRow.addView(userFlag);

        userClock = Ui.text(c, "10:00", 12, Ui.WHITE, true);
        userClock.setGravity(Gravity.CENTER);
        userClock.setBackground(Ui.round(0xFF56534E, 4, c));
        LinearLayout.LayoutParams ucp = new LinearLayout.LayoutParams(
                Ui.dp(c, 53), Ui.dp(c, 29));
        ucp.setMargins(Ui.dp(c, 8), 0, 0, 0);
        userClock.setLayoutParams(ucp);
        userRow.addView(userClock);
        root.addView(userRow);

        // ---------- bottom bar ----------
        bottomBar = Ui.linear(c, LinearLayout.HORIZONTAL);
        bottomBar.setBackgroundColor(0xFF211F1C);
        bottomBar.setGravity(Gravity.CENTER_VERTICAL);
        bottomBar.setPadding(0, Ui.dp(c, 4), 0, Ui.dp(c, 4));
        root.addView(bottomBar, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(c, 50)));

        // persona setup
        if (session.passAndPlay) {
            opponentName.setText("Friend  (" + main.state.userRating + ")");
            userName.setText("You  (" + main.state.userRating + ")");
            opponentFlag.setImageDrawable(Ui.flagAz(c));
            userFlag.setImageDrawable(Ui.flagAz(c));
        } else {
            opponentName.setText(session.persona.name + "  (" + session.persona.rating + ")");
            userName.setText(session.userName + "  (" + main.state.userRating + ")");
            opponentFlag.setImageDrawable(Ui.flagRed(c));
            userFlag.setImageDrawable(Ui.flagAz(c));
        }

        // ---------- bottom bar items ----------
        bottomItem(c, "Options", R.drawable.ic_list_options, new Runnable() {
            @Override public void run() { showOptionsMenu(); }
        });
        bottomItem(c, "Chat", R.drawable.ic_chat, new Runnable() {
            @Override public void run() { showChat(); }
        });
        ImageView x = new ImageView(c);
        x.setImageResource(R.drawable.ic_close);
        x.setColorFilter(0xFFA8A29A);
        LinearLayout.LayoutParams xp = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        x.setLayoutParams(xp);
        x.setPadding(Ui.dp(c, 14), Ui.dp(c, 4), Ui.dp(c, 14), Ui.dp(c, 4));
        x.setTag("x");
        x.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { main.navigate("home"); }
        });
        bottomBar.addView(x);
        bottomItem(c, "Back", R.drawable.ic_chevron_left, new Runnable() {
            @Override public void run() { session.stepBack(); }
        });
        bottomItem(c, "Forward", R.drawable.ic_chevron_right, new Runnable() {
            @Override public void run() { session.stepForward(); }
        });

        // ---------- session listener ----------
        session.listener = new GameSession.Listener() {
            @Override public void onPositionChanged() { refresh(); }
            @Override public void onGameEnded(GameSession.Result result, String reason) {
                endGame(result, reason);
            }
        };

        startSearching();
        return root;
    }

    private void bottomItem(android.content.Context c, String label, int iconRes,
                            final Runnable action) {
        LinearLayout item = Ui.linear(c, LinearLayout.VERTICAL);
        item.setGravity(Gravity.CENTER);
        item.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f));
        ImageView iv = new ImageView(c);
        iv.setImageResource(iconRes);
        iv.setColorFilter(0xFFB5B0A8);
        iv.setLayoutParams(new LinearLayout.LayoutParams(Ui.dp(c, 22), Ui.dp(c, 22)));
        item.addView(iv);
        TextView t = Ui.text(c, label, 11, 0xFFB5B0A8, false);
        t.setGravity(Gravity.CENTER);
        item.addView(t);
        item.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { action.run(); }
        });
        bottomBar.addView(item);
    }

    private static final String EMPTY_BOARD_FEN = "8/8/8/8/8/8/8/8 w - - 0 1";

    private void startSearching() {
        showTag("x", View.VISIBLE);
        // 072139: the board is empty while the opponent is being found
        boardView.setState(EMPTY_BOARD_FEN, null);
        boardView.setInteractive(false);
        searchDim.setVisibility(View.VISIBLE);
        setClockStyle(opponentClock, false);
        setClockStyle(userClock, false);
        if (session.persona != null && !session.passAndPlay) {
            // 072139: the coach greeting shows during the search, online included
            coachText.setText(COACH_TIPS[0]);
            coachBubble.setVisibility(View.VISIBLE);
            coachBubble.setAlpha(0.92f);
            opponentRow.setAlpha(0.5f);
        }
        handler.postDelayed(new Runnable() {
            @Override public void run() {
                if (destroyed) return;
                beginGame();
            }
        }, 2600);
    }

    private void showTag(String tag, int vis) {
        for (int i = 0; i < bottomBar.getChildCount(); i++) {
            View child = bottomBar.getChildAt(i);
            if (tag.equals(child.getTag())) child.setVisibility(vis);
        }
    }

    private void beginGame() {
        session.status = GameSession.Status.ACTIVE;
        searchDim.setVisibility(View.GONE);
        showTag("x", View.GONE);
        coachBubble.setVisibility(View.GONE);
        opponentRow.setAlpha(1f);
        boardView.setInteractive(true);
        refresh();
        lastTick = System.currentTimeMillis();
        running = true;
        handler.postDelayed(tickRunnable, 100);
        // 072146: in an online game the opponent holds the white pieces
        // and opens; your clock already ran while the game was setting up
        if (!session.passAndPlay && session.board.getSideToMove() != session.userSide()) {
            if (session.userSide() == Side.BLACK) session.clockBlackMs -= 3000;
            scheduleBotMove(500);
        }
    }

    private final Runnable tickRunnable = new Runnable() {
        @Override public void run() {
            if (destroyed || !running) return;
            long now = System.currentTimeMillis();
            long delta = now - lastTick;
            lastTick = now;
            session.tickClock(delta);
            updateClocks();
            if (session.status == GameSession.Status.ACTIVE) {
                handler.postDelayed(this, 100);
            }
        }
    };

    private void updateClocks() {
        boolean whiteToMove = session.board.getSideToMove() == Side.WHITE;
        boolean opponentToMove = whiteToMove == (session.botSide() == Side.WHITE);
        opponentClock.setText(session.fmtClock(session.clockOf(session.botSide())));
        userClock.setText(session.fmtClock(session.clockOf(session.userSide())));
        setClockStyle(opponentClock, opponentToMove);
        setClockStyle(userClock, !opponentToMove);
    }

    /** 072146: the side on the move carries the black clock, white ring and ticker icon. */
    private void setClockStyle(TextView clock, boolean active) {
        if (active) {
            clock.setTextColor(Ui.WHITE);
            clock.setBackground(Ui.roundStroke(0xFF000000, 4, 0xFFFFFFFF, 1.5f, main));
            android.graphics.drawable.Drawable icon =
                    main.getResources().getDrawable(R.drawable.ic_clock_small, null);
            icon.setTint(0xFFFFFFFF);
            icon.setBounds(0, 0, Ui.dp(main, 12), Ui.dp(main, 14));
            clock.setCompoundDrawables(icon, null, null, null);
            clock.setCompoundDrawablePadding(Ui.dp(main, 4));
        } else {
            clock.setTextColor(0xFF575553);
            clock.setBackground(Ui.round(0xFF979695, 4, main));
            clock.setCompoundDrawables(null, null, null, null);
        }
    }

    private void refresh() {
        Move last = session.lastRawMove();
        int[] lastSq = null;
        if (last != null) {
            lastSq = new int[]{
                    last.getFrom().getFile().ordinal(),
                    last.getFrom().getRank().ordinal(),
                    last.getTo().getFile().ordinal(),
                    last.getTo().getRank().ordinal()};
        }
        boardView.setState(session.fenAtView(), lastSq);
        updateClocks();
        updateMoveBar();
        deselect();
    }

    private void updateMoveBar() {
        moveRow.removeAllViews();
        int moveNo = 1;
        for (int i = 0; i < session.sanHistory.size(); i++) {
            String san = session.sanHistory.get(i);
            boolean white = i % 2 == 0;
            if (white) {
                TextView n = Ui.text(main, moveNo + ".", 12, 0xFF8A8780, false);
                n.setPadding(Ui.dp(main, 2), 0, Ui.dp(main, 2), 0);
                moveRow.addView(n);
            }
            TextView chip = Ui.text(main, san, 12, Ui.WHITE, false);
            chip.setPadding(Ui.dp(main, 5), Ui.dp(main, 2), Ui.dp(main, 5), Ui.dp(main, 2));
            moveRow.addView(chip);
            if (!white) moveNo++;
        }
        moveScroll.setVisibility(session.sanHistory.isEmpty() ? View.GONE : View.VISIBLE);
        if (!session.sanHistory.isEmpty()) {
            moveScroll.post(new Runnable() {
                @Override public void run() { moveScroll.fullScroll(View.FOCUS_RIGHT); }
            });
        }
    }

    // ---------- input ----------
    private void onSquare(int file, int rank) {
        if (!session.humanTurn()) return;
        String sq = squareName(file, rank);

        if (selectedFile == -1) {
            Piece p = session.board.getPiece(Square.valueOf(sq));
            boolean mySide = session.passAndPlay
                    ? p.getPieceSide() == session.board.getSideToMove()
                    : p.getPieceSide() == session.userSide();
            if (p != Piece.NONE && mySide) select(file, rank);
            return;
        }

        if (selectedFile == file && selectedRank == rank) {
            deselect();
            return;
        }

        String from = squareName(selectedFile, selectedRank);
        Move m = session.findMove(from, sq, null);
        if (m == null) m = session.findMove(from, sq, "q");
        deselect();
        if (m != null) {
            session.playMove(m);
            afterUserMove();
        } else {
            Piece p = session.board.getPiece(Square.valueOf(sq));
            boolean mySide = session.passAndPlay
                    ? p.getPieceSide() == session.board.getSideToMove()
                    : p.getPieceSide() == session.userSide();
            if (p != Piece.NONE && mySide) select(file, rank);
        }
    }

    private void afterUserMove() {
        if (session.status == GameSession.Status.ENDED) return;
        if (session.passAndPlay) return;
        scheduleBotMove(250);
    }

    private void scheduleBotMove(long delayMs) {
        handler.postDelayed(new Runnable() {
            @Override public void run() {
                if (destroyed || session.status != GameSession.Status.ACTIVE) return;
                BotPlayer.think(session, new BotPlayer.Callback() {
                    @Override public void onMove(Move m) {
                        if (destroyed || m == null
                                || session.status != GameSession.Status.ACTIVE) return;
                        session.playMove(m);
                        maybeCoachTip();
                    }
                });
            }
        }, delayMs);
    }

    private void maybeCoachTip() {
        if (session.persona == null || !session.persona.coach) return;
        int plies = session.sanHistory.size();
        if (plies == 6 || plies == 10 || plies == 14) {
            showCoachBubble(COACH_TIPS[(plies / 4) % COACH_TIPS.length], 4000);
        }
    }

    private void showCoachBubble(String tip, long ms) {
        coachText.setText(tip);
        coachBubble.setVisibility(View.VISIBLE);
        handler.postDelayed(new Runnable() {
            @Override public void run() {
                if (!destroyed) coachBubble.setVisibility(View.GONE);
            }
        }, ms);
    }

    private void select(int file, int rank) {
        selectedFile = file;
        selectedRank = rank;
        Set<Integer> targets = new HashSet<>();
        String from = squareName(file, rank);
        Square f = Square.valueOf(from);
        for (Move m : session.board.legalMoves()) {
            if (m.getFrom() == f) {
                targets.add(m.getTo().getFile().ordinal() * 8 + m.getTo().getRank().ordinal());
            }
        }
        boardView.setSelection(new int[]{file, rank}, targets);
    }

    private void deselect() {
        selectedFile = -1;
        selectedRank = -1;
        boardView.setSelection(null, null);
    }

    private static String squareName(int file, int rank) {
        return String.valueOf((char) ('A' + file)) + (rank + 1);
    }

    // ---------- end / menus ----------
    private void endGame(GameSession.Result result, String reason) {
        running = false;
        boardView.setInteractive(false);
        String resStr = result == GameSession.Result.WIN ? "Win"
                : result == GameSession.Result.LOSS ? "Loss" : "Draw";
        int delta = result == GameSession.Result.WIN ? 8 + (int) (Math.random() * 9)
                : result == GameSession.Result.LOSS ? -(8 + (int) (Math.random() * 9)) : 0;
        String accuracy = String.valueOf(75 + (int) (Math.random() * 20)) + "."
                + (int) (Math.random() * 9);
        String opp = session.passAndPlay ? "Friend" : session.persona.name;
        main.state.recordGame(opp, resStr, accuracy, delta);
        main.state.save(main);
        showResultOverlay(result, reason, delta);
    }

    private void showResultOverlay(GameSession.Result result, String reason, int delta) {
        final android.content.Context c = main;
        if (resultOverlay != null) return;

        FrameLayout dim = new FrameLayout(c);
        dim.setBackgroundColor(0xCC000000);

        LinearLayout card = Ui.linear(c, LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER);
        card.setBackground(Ui.round(Ui.CARD, 12, c));
        FrameLayout.LayoutParams cp = new FrameLayout.LayoutParams(
                Ui.dp(c, 280), ViewGroup.LayoutParams.WRAP_CONTENT);
        cp.gravity = Gravity.CENTER;
        card.setLayoutParams(cp);
        card.setPadding(Ui.dp(c, 16), Ui.dp(c, 20), Ui.dp(c, 16), Ui.dp(c, 16));

        String title = result == GameSession.Result.WIN ? "You Win!"
                : result == GameSession.Result.LOSS ? "You Lose" : "Draw";
        TextView t = Ui.text(c, title, 26, Ui.WHITE, true);
        t.setGravity(Gravity.CENTER);
        card.addView(t);
        TextView r = Ui.text(c, reason + "  •  " + (delta > 0 ? "+" + delta : delta + " rating"),
                13, Ui.GREY, false);
        r.setGravity(Gravity.CENTER);
        r.setPadding(0, Ui.dp(c, 6), 0, Ui.dp(c, 12));
        card.addView(r);

        card.addView(overlayBtn(c, "Rematch", Ui.GREEN, new Runnable() {
            @Override public void run() {
                main.navigate("home");
                NewGameScreen.startGame(main,
                        session.passAndPlay ? "passplay"
                                : session.persona.coach ? "coach"
                                : session.persona.name.equals("Marko") ? "bots" : "online",
                        pendingTime);
            }
        }));
        card.addView(overlayBtn(c, "New Game", 0xFF403D39, new Runnable() {
            @Override public void run() { main.navigate("newgame"); }
        }));
        card.addView(overlayBtn(c, "Home", 0xFF403D39, new Runnable() {
            @Override public void run() { main.navigate("home"); }
        }));

        dim.addView(card);
        resultOverlay = dim;
        ((ViewGroup) main.findViewById(android.R.id.content)).addView(dim,
                new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    private TextView overlayBtn(android.content.Context c, String label, int color,
                                final Runnable action) {
        TextView b = Ui.text(c, label, 15, Ui.WHITE, true);
        b.setGravity(Gravity.CENTER);
        b.setBackground(Ui.round(color, 8, c));
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(c, 42));
        p.setMargins(0, Ui.dp(c, 8), 0, 0);
        b.setLayoutParams(p);
        b.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { action.run(); }
        });
        return b;
    }

    private void showOptionsMenu() {
        final android.content.Context c = main;
        final String[] items = {"Resign", "Draw offer", "Flip board"};
        new AlertDialog.Builder(c, R.style.DarkDialog).setTitle("Options")
                .setItems(items, new android.content.DialogInterface.OnClickListener() {
                    @Override public void onClick(android.content.DialogInterface d, int which) {
                        if (which == 0) {
                            new AlertDialog.Builder(c, R.style.DarkDialog)
                                    .setTitle("Resign the game?")
                                    .setPositiveButton("Resign",
                                            new android.content.DialogInterface.OnClickListener() {
                                                @Override public void onClick(
                                                        android.content.DialogInterface dlg, int w) {
                                                    session.resign();
                                                }
                                            })
                                    .setNegativeButton("Cancel", null).show();
                        } else if (which == 1) {
                            toast("Draw offer declined");
                        } else {
                            boardView.setFlipped(!boardView.isFlipped());
                        }
                        d.dismiss();
                    }
                }).show();
    }

    private void showChat() {
        toast(session.persona != null && !session.passAndPlay
                ? session.persona.name + ": Good luck, have fun!"
                : "Chat is empty");
    }

    private void toast(String s) {
        Toast.makeText(main, s, Toast.LENGTH_SHORT).show();
    }

    public void onBackPressed() {
        if (session.status == GameSession.Status.ACTIVE && session.sanHistory.size() >= 2) {
            session.resign();
        } else {
            main.navigate("home");
        }
    }

    public void destroy() {
        destroyed = true;
        running = false;
        handler.removeCallbacksAndMessages(null);
        if (resultOverlay != null) {
            ViewGroup parent = (ViewGroup) resultOverlay.getParent();
            if (parent != null) parent.removeView(resultOverlay);
            resultOverlay = null;
        }
    }
}
