package com.eldar.chess;

import android.app.AlertDialog;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
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

    private TextView opponentName, userName;
    private LinearLayout opponentClock, userClock;
    private TextView opponentClockText, userClockText, opponentClockIcon, userClockIcon;
    private ImageView opponentFlag, userFlag;
    private LinearLayout opponentRow;
    private ImageView opponentAvatar, userAvatar;
    private HorizontalScrollView moveScroll;
    private LinearLayout moveRow;
    private FrameLayout boardOverlay;
    private View searchDim;
    private TextView coachBubble;
    private TextView coachText;
    private ImageView coachAvatar;
    private View resultOverlay;
    private LinearLayout bottomBar;
    private View closeButton;

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
        root.setBackgroundColor(0xFF302D2B);
        root.setClipChildren(false);

        // ---------- top bar (072146: #262421 strip continues through the move bar) ----------
        LinearLayout top = Ui.linear(c, LinearLayout.HORIZONTAL);
        top.setBackgroundColor(0xFF262421);
        top.setGravity(Gravity.CENTER_VERTICAL);
        top.setPadding(Ui.dp(c, 8), 0, Ui.dp(c, 8), 0);
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
        signal.setColorFilter(0xFFB2B2B1);
        top.addView(signal);
        root.addView(top, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(c, 32)));

        // ---------- move list bar (072146: number + white move chip right under the bar) ----------
        moveScroll = new HorizontalScrollView(c);
        moveScroll.setHorizontalScrollBarEnabled(false);
        moveScroll.setBackgroundColor(0xFF262421);
        moveScroll.setPadding(Ui.dp(c, 10), Ui.dp(c, 2), Ui.dp(c, 8), Ui.dp(c, 2));
        moveRow = Ui.linear(c, LinearLayout.HORIZONTAL);
        moveRow.setGravity(Gravity.CENTER_VERTICAL);
        moveScroll.addView(moveRow, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
        root.addView(moveScroll, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(c, 19)));

        // ---------- player rows ----------
        opponentRow = playerRow(c, true);
        LinearLayout.LayoutParams orp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        orp.setMargins(0, Ui.dp(c, 28), 0, 0);
        opponentRow.setLayoutParams(orp);
        root.addView(opponentRow);

        // ---------- coach slot: fixed footprint so the board never moves (072139/072146) ----------
        FrameLayout coachSlot = new FrameLayout(c);
        coachSlot.setClipChildren(false);
        root.addView(coachSlot, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(c, 28)));
        coachAvatar = new ImageView(c);
        coachAvatar.setImageResource(R.drawable.avatar_coach_tall);
        coachAvatar.setVisibility(View.GONE);
        FrameLayout.LayoutParams cap = new FrameLayout.LayoutParams(
                Ui.dp(c, 28), Ui.dp(c, 48), Gravity.TOP | Gravity.START);
        cap.leftMargin = Ui.dp(c, 12);
        cap.topMargin = -Ui.dp(c, 36);
        coachSlot.addView(coachAvatar, cap);

        coachText = Ui.text(c, COACH_TIPS[0], 11, 0xFF2E2C29, false);
        coachText.setSingleLine(true);
        coachText.setTextColor(0xFF2E2C29);
        coachText.setBackground(Ui.coachBubbleBg(c));
        coachText.setGravity(Gravity.CENTER_VERTICAL);
        coachText.setPadding(Ui.dp(c, 12), 0, Ui.dp(c, 8), 0);
        coachBubble = coachText;
        coachBubble.setVisibility(View.GONE);
        FrameLayout.LayoutParams bp2 = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(c, 37),
                Gravity.TOP);
        bp2.leftMargin = Ui.dp(c, 39);
        bp2.rightMargin = Ui.dp(c, 26);
        bp2.topMargin = -Ui.dp(c, 21);
        coachSlot.addView(coachBubble, bp2);

        // ---------- board ----------
        boardView = new BoardView(c);
        boardView.setListener(new BoardView.Listener() {
            @Override public void onSquareTapped(int file, int rank) { onSquare(file, rank); }
        });
        boardView.setFlipped(!session.userIsWhite);
        boardView.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        boardOverlay = new FrameLayout(c);
        boardOverlay.addView(boardView);

        // searching layer (072139): dark plateau to 45% then bright board below
        searchDim = Ui.linear(c, LinearLayout.VERTICAL);
        searchDim.setBackground(Ui.scrim());
        ((LinearLayout) searchDim).setGravity(Gravity.CENTER_HORIZONTAL | Gravity.TOP);
        float wdp = c.getResources().getDisplayMetrics().widthPixels
                / c.getResources().getDisplayMetrics().density;
        searchDim.setPadding(0, Math.round(wdp * 0.377f
                * c.getResources().getDisplayMetrics().density), 0, 0);
        TextView searching = Ui.text(c, "Searching...", 17, Ui.WHITE, true);
        searching.setGravity(Gravity.CENTER);
        ((LinearLayout) searchDim).addView(searching);
        ((LinearLayout) searchDim).addView(Ui.spacer(c, 0, Ui.dp(c, 8)));
        FrameLayout stopHalo = new FrameLayout(c);
        stopHalo.setBackground(Ui.round(0x12FFFFFF, 12, c));
        FrameLayout stopButton = new FrameLayout(c);
        stopButton.setBackground(Ui.round(0xFF2B2A27, 7, c));
        ImageView stopIcon = new ImageView(c);
        stopIcon.setImageDrawable(Ui.stopwatch(c, 18));
        stopButton.addView(stopIcon, new FrameLayout.LayoutParams(
                Ui.dp(c, 18), Ui.dp(c, 18), Gravity.CENTER));
        FrameLayout.LayoutParams sbp = new FrameLayout.LayoutParams(
                Ui.dp(c, 30), Ui.dp(c, 30), Gravity.CENTER);
        stopHalo.addView(stopButton, sbp);
        LinearLayout.LayoutParams stp = new LinearLayout.LayoutParams(
                Ui.dp(c, 40), Ui.dp(c, 40));
        stopHalo.setLayoutParams(stp);
        stopHalo.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { main.navigate("home"); }
        });
        ((LinearLayout) searchDim).addView(stopHalo);
        boardOverlay.addView(searchDim, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        root.addView(boardOverlay, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        // ---------- user row ----------
        LinearLayout userRow = playerRow(c, false);
        LinearLayout.LayoutParams urp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        urp.setMargins(0, Ui.dp(c, 26), 0, 0);
        userRow.setLayoutParams(urp);
        root.addView(userRow);

        // leftover height sits under the player row, toolbar stays pinned (072146)
        root.addView(Ui.spacerWeight(c, 1f));

        // ---------- bottom bar: four equal slots, X floats in the centre while searching ----------
        FrameLayout bottomWrap = new FrameLayout(c);
        bottomWrap.setBackgroundColor(0xFF302D2B);
        bottomBar = Ui.linear(c, LinearLayout.HORIZONTAL);
        bottomBar.setGravity(Gravity.CENTER_VERTICAL);
        bottomBar.setPadding(0, Ui.dp(c, 7), 0, Ui.dp(c, 5));
        bottomWrap.addView(bottomBar, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(c, 43)));
        root.addView(bottomWrap, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(c, 43)));

        ImageView closeBtn = new ImageView(c);
        closeBtn.setImageDrawable(Ui.bottomIcon(c, 4, 15, 0xFFB7B6B5, 0xFF302D2B));
        closeBtn.setVisibility(View.GONE);
        bottomWrap.addView(closeBtn, new FrameLayout.LayoutParams(
                Ui.dp(c, 15), Ui.dp(c, 15), Gravity.CENTER));
        closeBtn.setTag("x");
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { main.navigate("home"); }
        });
        closeButton = closeBtn;

        // persona setup
        if (session.passAndPlay) {
            setPlayerName(opponentName, "Friend", main.state.userRating);
            setPlayerName(userName, "You", main.state.userRating);
            opponentFlag.setImageDrawable(Ui.flagAz(c));
            userFlag.setImageDrawable(Ui.flagAz(c));
        } else {
            setPlayerName(opponentName, session.persona.name, session.persona.rating);
            setPlayerName(userName, session.userName, main.state.userRating);
            opponentFlag.setImageDrawable(Ui.flagCn(c));
            userFlag.setImageDrawable(Ui.flagAz(c));
        }

        // ---------- bottom bar items ----------
        bottomItem(c, 0, "Options", new Runnable() {
            @Override public void run() { showOptionsMenu(); }
        });
        bottomItem(c, 1, "Chat", new Runnable() {
            @Override public void run() { showChat(); }
        });
        bottomItem(c, 2, "Back", new Runnable() {
            @Override public void run() { session.stepBack(); }
        });
        bottomItem(c, 3, "Forward", new Runnable() {
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

    private void bottomItem(android.content.Context c, int iconKind, String label,
                            final Runnable action) {
        LinearLayout item = Ui.linear(c, LinearLayout.VERTICAL);
        item.setGravity(Gravity.CENTER_HORIZONTAL);
        item.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f));
        ImageView iv = new ImageView(c);
        iv.setImageDrawable(Ui.bottomIcon(c, iconKind, 17, 0xFFB7B6B5, 0xFF302D2B));
        iv.setLayoutParams(new LinearLayout.LayoutParams(Ui.dp(c, 17), Ui.dp(c, 17)));
        item.addView(iv);
        TextView t = Ui.text(c, label, 8.5f, 0xFFB7B6B5, false);
        t.setGravity(Gravity.CENTER);
        t.setPadding(0, Ui.dp(c, 3), 0, 0);
        item.addView(t);
        item.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { action.run(); }
        });
        bottomBar.addView(item);
    }

    private LinearLayout playerRow(android.content.Context c, boolean opponent) {
        LinearLayout row = Ui.linear(c, LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(Ui.dp(c, 12), Ui.dp(c, 6), Ui.dp(c, 10), Ui.dp(c, 6));

        ImageView av = new ImageView(c);
        av.setImageResource(R.drawable.avatar_pawn);
        av.setScaleType(ImageView.ScaleType.FIT_XY);
        row.addView(av, new LinearLayout.LayoutParams(Ui.dp(c, 28), Ui.dp(c, 28)));

        TextView name = Ui.text(c, "", 11, Ui.WHITE, false);
        LinearLayout.LayoutParams np = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        np.setMargins(Ui.dp(c, 11), 0, 0, 0);
        name.setLayoutParams(np);
        row.addView(name);

        ImageView flag = new ImageView(c);
        LinearLayout.LayoutParams fp = new LinearLayout.LayoutParams(Ui.dp(c, 10), Ui.dp(c, 7));
        fp.setMargins(Ui.dp(c, 4), 0, 0, 0);
        flag.setLayoutParams(fp);
        row.addView(flag);

        row.addView(Ui.spacerWeight(c, 1f));

        LinearLayout clockBox = Ui.linear(c, LinearLayout.HORIZONTAL);
        clockBox.setGravity(Gravity.CENTER);
        clockBox.setBackground(Ui.round(0xFF979695, 4, c));
        LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(
                Ui.dp(c, 54), Ui.dp(c, 30));
        cp.setMargins(Ui.dp(c, 8), 0, 0, 0);
        clockBox.setLayoutParams(cp);

        TextView clockIcon = Ui.glyph(c, "schedule", 8.5f, Ui.WHITE);
        clockIcon.setGravity(Gravity.CENTER);
        clockIcon.setVisibility(View.GONE);
        LinearLayout.LayoutParams ip = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ip.setMargins(Ui.dp(c, 6), 0, 0, 0);
        clockBox.addView(clockIcon, ip);

        TextView clockText = Ui.text(c, "10:00", 10.5f, 0xFF575553, true);
        clockText.setTypeface(Typeface.create("sans-serif-condensed", Typeface.BOLD));
        clockText.setSingleLine(true);
        clockText.setIncludeFontPadding(false);
        clockText.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        tp.setMargins(Ui.dp(c, 3), 0, Ui.dp(c, 6), 0);
        clockBox.addView(clockText, tp);
        row.addView(clockBox);

        if (opponent) {
            opponentAvatar = av; opponentName = name; opponentFlag = flag;
            opponentClock = clockBox; opponentClockText = clockText; opponentClockIcon = clockIcon;
        } else {
            userAvatar = av; userName = name; userFlag = flag;
            userClock = clockBox; userClockText = clockText; userClockIcon = clockIcon;
        }
        return row;
    }

    private static void setPlayerName(TextView tv, String name, int rating) {
        SpannableStringBuilder sb = new SpannableStringBuilder(name + "  (" + rating + ")");
        sb.setSpan(new StyleSpan(Typeface.BOLD), 0, name.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        sb.setSpan(new ForegroundColorSpan(0xFFA8A29A), name.length(), sb.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        tv.setText(sb);
    }

    private static final String EMPTY_BOARD_FEN = "8/8/8/8/8/8/8/8 w - - 0 1";

    private void startSearching() {
        closeButton.setVisibility(View.VISIBLE);
        // 072139: the board is empty while the opponent is being found
        boardView.setState(EMPTY_BOARD_FEN, null);
        boardView.setCoordinates(false);
        boardView.setInteractive(false);
        searchDim.setVisibility(View.VISIBLE);
        setClockStyle(opponentClock, opponentClockIcon, opponentClockText, false);
        setClockStyle(userClock, userClockIcon, userClockText, false);
        if (session.persona != null && !session.passAndPlay) {
            // 072139: the coach greeting shows during the search; the coach photo
            // sits over the opponent's avatar slot
            coachText.setText(COACH_TIPS[0]);
            coachBubble.setVisibility(View.VISIBLE);
            coachAvatar.setVisibility(View.VISIBLE);
            opponentRow.setAlpha(0.5f);
        }
        handler.postDelayed(new Runnable() {
            @Override public void run() {
                if (destroyed) return;
                beginGame();
            }
        }, 2600);
    }

    private void beginGame() {
        session.status = GameSession.Status.ACTIVE;
        searchDim.setVisibility(View.GONE);
        closeButton.setVisibility(View.GONE);
        coachBubble.setVisibility(View.GONE);
        coachAvatar.setVisibility(View.GONE);
        opponentRow.setAlpha(1f);
        boardView.setCoordinates(true);
        boardView.setInteractive(true);
        refresh();
        lastTick = System.currentTimeMillis();
        running = true;
        handler.postDelayed(tickRunnable, 100);
        // 072146: in an online game the opponent holds the white pieces
        // and opens; your clock already ran while the game was setting up
        if (!session.passAndPlay && session.board.getSideToMove() != session.userSide()) {
            if (session.userSide() == Side.BLACK) session.clockBlackMs -= 3000;
            handler.postDelayed(new Runnable() {
                @Override public void run() {
                    if (destroyed || session.status != GameSession.Status.ACTIVE) return;
                    // 072146: the bot (white) always opens 1.e4, then plays on
                    Move opener = session.findMove("E2", "E4", null);
                    if (opener != null) {
                        session.playMove(opener);
                    } else {
                        scheduleBotMove(300);
                    }
                }
            }, 500);
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
        opponentClockText.setText(session.fmtClock(session.clockOf(session.botSide())));
        userClockText.setText(session.fmtClock(session.clockOf(session.userSide())));
        setClockStyle(opponentClock, opponentClockIcon, opponentClockText, opponentToMove);
        setClockStyle(userClock, userClockIcon, userClockText, !opponentToMove);
    }

    /** 072146: the side on the move carries the black clock, white ticker icon;
     *  the waiting side shows the plain grey pill with dark digits. */
    private void setClockStyle(LinearLayout box, TextView icon, TextView text, boolean active) {
        if (active) {
            text.setTextColor(Ui.WHITE);
            icon.setTextColor(Ui.WHITE);
            icon.setVisibility(View.VISIBLE);
            box.setBackground(Ui.round(0xFF0A0A0A, 4, main));
        } else {
            text.setTextColor(0xFF575553);
            icon.setVisibility(View.GONE);
            box.setBackground(Ui.round(0xFF979695, 4, main));
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
                TextView n = Ui.text(main, moveNo + ".", 10, 0xFF7E7B76, false);
                n.setPadding(0, 0, Ui.dp(main, 7), 0);
                moveRow.addView(n);
            }
            TextView chip = Ui.text(main, san, 9, Ui.WHITE, true);
            chip.setPadding(Ui.dp(main, 5), Ui.dp(main, 1), Ui.dp(main, 5), Ui.dp(main, 1));
            chip.setBackground(Ui.round(0xFF5E5B57, 2.5f, main));
            moveRow.addView(chip);
            if (!white) moveNo++;
        }
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
        coachAvatar.setVisibility(View.VISIBLE);
        handler.postDelayed(new Runnable() {
            @Override public void run() {
                if (!destroyed) {
                    coachBubble.setVisibility(View.GONE);
                    coachAvatar.setVisibility(View.GONE);
                }
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
