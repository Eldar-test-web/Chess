package com.eldar.chess;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Piece;
import com.github.bhlangonijr.chesslib.PieceType;
import com.github.bhlangonijr.chesslib.Side;
import com.github.bhlangonijr.chesslib.Square;
import com.github.bhlangonijr.chesslib.move.Move;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/** Every row of the More tab opens a real, working sub-screen. */
public class MoreDetailScreen {

    private static final String START_FEN =
            "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
    private static final String SOLO_FEN = "4k3/8/8/3p4/2Q1p3/8/8/4K3 w - - 0 1";

    private static final int[][] PALETTES = {
            {0xFFEEEED2, 0xFF769656},
            {0xFFEED9B0, 0xFFB58863},
            {0xFFD6E0E9, 0xFF7D9BB8},
            {0xFFE9E9E7, 0xFF8A8A87},
    };
    private static final String[] THEME_NAMES = {"Green", "Wood", "Blue", "Grey"};

    private static MainActivity main;
    private static String key = "profile";
    private static String sub = null;

    public static void open(MainActivity m, String k) {
        main = m;
        key = k;
        sub = null;
        m.navigate("moredetail");
    }

    public static void back(MainActivity m) {
        if (sub != null) {
            sub = null;
            m.navigate("moredetail");
        } else {
            m.navigate("more");
        }
    }

    public static View build(MainActivity m) {
        main = m;
        if (sub != null) {
            try {
                if (sub.startsWith("chat:")) return chatScreen(Integer.parseInt(sub.substring(5)));
                if (sub.startsWith("eg:")) return endgamePlay(Integer.parseInt(sub.substring(3)));
                if (sub.startsWith("solo:")) return analysisScreen("Solo Chess", SOLO_FEN, true);
                if (sub.startsWith("video:")) return videoScreen(Integer.parseInt(sub.substring(6)));
                if (sub.startsWith("tv:")) return tvStreamScreen(Integer.parseInt(sub.substring(3)));
                if (sub.startsWith("article:")) return articleScreen(Integer.parseInt(sub.substring(8)));
                if (sub.startsWith("forum:")) return forumThread(Integer.parseInt(sub.substring(6)));
                if (sub.startsWith("news:")) return newsScreen(Integer.parseInt(sub.substring(5)));
            } catch (Exception ignored) { }
            sub = null;
        }
        switch (key) {
            case "profile": return profileScreen();
            case "stats": return statsScreen();
            case "theme": return themeScreen();
            case "awards": return awardsScreen();
            case "messages": return messagesScreen();
            case "settings": return settingsScreen();
            case "support": return supportScreen();
            case "analysis": return analysisScreen("Analysis", START_FEN, false);
            case "explorer": return explorerScreen();
            case "endgames": return endgamesScreen();
            case "practice": return practiceScreen();
            case "insights": return insightsScreen();
            case "vision": return visionScreen();
            case "videos": return videosScreen();
            case "solochess": return soloIntroScreen();
            case "leaderboard": return leaderboardScreen();
            case "playersleague": return leagueScreen();
            case "chesstv": return tvScreen();
            case "articles": return articlesScreen();
            case "forums": return forumsScreen();
            case "clubs": return clubsScreen();
            case "news": return newsScreenList();
            case "otherapps": return otherAppsScreen();
            default: return profileScreen();
        }
    }

    // ---------------------------------------------------------------- helpers

    private static class Page {
        ScrollView scroll;
        LinearLayout root;
    }

    private static Page newPage(String title) {
        Context c = main;
        Page pg = new Page();
        pg.scroll = new ScrollView(c);
        pg.scroll.setFillViewport(true);
        pg.scroll.setBackground(Ui.pageBg());
        pg.root = Ui.linear(c, LinearLayout.VERTICAL);
        pg.root.setPadding(Ui.dp(c, 16), Ui.dp(c, 10), Ui.dp(c, 16), Ui.dp(c, 28));
        pg.scroll.addView(pg.root, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        LinearLayout head = Ui.linear(c, LinearLayout.HORIZONTAL);
        head.setGravity(Gravity.CENTER_VERTICAL);
        TextView backBtn = Ui.glyph(c, "arrow_back", 21, Ui.WHITE);
        backBtn.setPadding(Ui.dp(c, 2), Ui.dp(c, 8), Ui.dp(c, 12), Ui.dp(c, 8));
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { back(main); }
        });
        head.addView(backBtn);
        head.addView(Ui.text(c, title, 20, Ui.WHITE, true));
        pg.root.addView(head);
        return pg;
    }

    private static LinearLayout card(Context c) {
        LinearLayout cc = Ui.linear(c, LinearLayout.VERTICAL);
        cc.setBackground(Ui.round(0x1FFFFFFF, 12, c));
        cc.setPadding(Ui.dp(c, 14), Ui.dp(c, 14), Ui.dp(c, 14), Ui.dp(c, 14));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.topMargin = Ui.dp(c, 12);
        cc.setLayoutParams(lp);
        return cc;
    }

    private static TextView section(Context c, String s) {
        TextView t = Ui.text(c, s, 14, 0xFFCFC9C1, true);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.topMargin = Ui.dp(c, 20);
        lp.bottomMargin = Ui.dp(c, 2);
        t.setLayoutParams(lp);
        return t;
    }

    private static TextView body(Context c, String s) {
        TextView t = Ui.text(c, s, 12.5f, Ui.GREY, false);
        t.setLineSpacing(Ui.dp(c, 3), 1f);
        return t;
    }

    private static TextView button(Context c, String label, boolean primary,
                                   View.OnClickListener l) {
        TextView b = Ui.text(c, label, 15, Ui.WHITE, true);
        b.setGravity(Gravity.CENTER);
        b.setBackground(primary ? Ui.greenFill(c, 9) : Ui.round(0xFF43403B, 9, c));
        b.setPadding(Ui.dp(c, 10), Ui.dp(c, 12), Ui.dp(c, 10), Ui.dp(c, 12));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.topMargin = Ui.dp(c, 12);
        b.setLayoutParams(lp);
        b.setOnClickListener(l);
        return b;
    }

    private static LinearLayout listRow(Context c, String iconRes, String title, String subText,
                                        View.OnClickListener click) {
        LinearLayout row = Ui.linear(c, LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setBackground(Ui.round(0x1FFFFFFF, 12, c));
        row.setPadding(Ui.dp(c, 14), Ui.dp(c, 12), Ui.dp(c, 14), Ui.dp(c, 12));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.topMargin = Ui.dp(c, 10);
        row.setLayoutParams(lp);
        if (iconRes != null) {
            int res = c.getResources().getIdentifier(iconRes, "drawable", c.getPackageName());
            if (res != 0) {
                ImageView icon = new ImageView(c);
                icon.setImageResource(res);
                icon.setScaleType(ImageView.ScaleType.FIT_CENTER);
                row.addView(icon, new LinearLayout.LayoutParams(
                        Ui.dp(c, 34), Ui.dp(c, 34)));
            }
        }
        LinearLayout texts = Ui.linear(c, LinearLayout.VERTICAL);
        LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        tp.setMargins(iconRes != null ? Ui.dp(c, 12) : 0, 0, Ui.dp(c, 8), 0);
        texts.setLayoutParams(tp);
        texts.addView(Ui.text(c, title, 14.5f, Ui.WHITE, true));
        if (subText != null) texts.addView(Ui.text(c, subText, 11.5f, Ui.GREY, false));
        row.addView(texts);
        row.addView(Ui.glyph(c, "chevron_right", 18, Ui.GREY));
        if (click != null) row.setOnClickListener(click);
        return row;
    }

    private static LinearLayout bar(Context c, String label, String value, float fraction,
                                    int color) {
        LinearLayout box = Ui.linear(c, LinearLayout.VERTICAL);
        LinearLayout.LayoutParams blp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        blp.topMargin = Ui.dp(c, 12);
        box.setLayoutParams(blp);
        LinearLayout head = Ui.linear(c, LinearLayout.HORIZONTAL);
        head.addView(Ui.text(c, label, 12.5f, Ui.WHITE, true));
        head.addView(Ui.spacerWeight(c, 1f));
        head.addView(Ui.text(c, value, 12.5f, Ui.GREY, false));
        box.addView(head);
        LinearLayout track = Ui.linear(c, LinearLayout.HORIZONTAL);
        track.setBackground(Ui.round(0xFF45423C, 5, c));
        LinearLayout.LayoutParams tlp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(c, 10));
        tlp.topMargin = Ui.dp(c, 6);
        track.setLayoutParams(tlp);
        float f = Math.max(0.04f, Math.min(0.96f, fraction));
        View fill = new View(c);
        fill.setBackground(Ui.round(color, 5, c));
        track.addView(fill, new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.MATCH_PARENT, f));
        track.addView(new View(c), new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.MATCH_PARENT, 1f - f));
        box.addView(track);
        return box;
    }

    private static TextView circle(Context c, String label, int color, int sizeDp, float sp) {
        TextView t = Ui.text(c, label, sp, Ui.WHITE, true);
        t.setGravity(Gravity.CENTER);
        t.setBackground(Ui.round(color, sizeDp / 2f, c));
        t.setLayoutParams(new LinearLayout.LayoutParams(Ui.dp(c, sizeDp), Ui.dp(c, sizeDp)));
        return t;
    }

    private static LinearLayout.LayoutParams weightLp(Context c, float weight) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, weight);
        lp.setMargins(Ui.dp(c, 4), Ui.dp(c, 4), Ui.dp(c, 4), Ui.dp(c, 4));
        return lp;
    }

    // ---------------------------------------------------------------- profile

    private static View profileScreen() {
        final Context c = main;
        final AppState st = main.state;
        Page pg = newPage("Profile");

        LinearLayout head = card(c);
        head.setGravity(Gravity.CENTER_HORIZONTAL);
        head.addView(circle(c, st.playerName.isEmpty() ? "E"
                : st.playerName.substring(0, 1).toUpperCase(), Ui.GREEN_DARK, 76, 30));
        TextView name = Ui.text(c, st.playerName, 19, Ui.WHITE, true);
        name.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams np = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        np.topMargin = Ui.dp(c, 8);
        name.setLayoutParams(np);
        head.addView(name);
        TextView meta = Ui.text(c, "Azerbaijan  •  Club player", 12, Ui.GREY, false);
        meta.setGravity(Gravity.CENTER);
        head.addView(meta);
        head.addView(button(c, "Edit name", false, new View.OnClickListener() {
            @Override public void onClick(View v) { editName(); }
        }));
        pg.root.addView(head);

        pg.root.addView(section(c, "Ratings"));
        LinearLayout row1 = Ui.linear(c, LinearLayout.HORIZONTAL);
        row1.addView(ratingBox(c, "Bullet", st.bulletRating, "bolt"), weightLp(c, 1f));
        row1.addView(ratingBox(c, "Blitz", st.blitzRating, "flash_on"), weightLp(c, 1f));
        pg.root.addView(row1);
        LinearLayout row2 = Ui.linear(c, LinearLayout.HORIZONTAL);
        row2.addView(ratingBox(c, "Rapid", st.rapidRating, "schedule"), weightLp(c, 1f));
        row2.addView(ratingBox(c, "Daily", st.dailyRating, "today"), weightLp(c, 1f));
        pg.root.addView(row2);

        pg.root.addView(section(c, "Stats"));
        LinearLayout stats = card(c);
        statsRow(c, stats, "Games played", String.valueOf(st.gamesPlayed));
        statsRow(c, stats, "Puzzles solved", String.valueOf(st.puzzlesSolved));
        statsRow(c, stats, "Lessons finished", String.valueOf(st.learnProgress));
        statsRow(c, stats, "Current streak", st.streak + " days");
        pg.root.addView(stats);

        return pg.scroll;
    }

    private static LinearLayout ratingBox(Context c, String label, int rating, String glyph) {
        LinearLayout box = Ui.linear(c, LinearLayout.VERTICAL);
        box.setBackground(Ui.round(0x1FFFFFFF, 12, c));
        box.setPadding(Ui.dp(c, 12), Ui.dp(c, 12), Ui.dp(c, 12), Ui.dp(c, 12));
        box.setGravity(Gravity.CENTER_HORIZONTAL);
        LinearLayout titleRow = Ui.linear(c, LinearLayout.HORIZONTAL);
        titleRow.setGravity(Gravity.CENTER);
        titleRow.addView(Ui.glyph(c, glyph, 13, Ui.GREEN));
        TextView lt = Ui.text(c, " " + label, 11.5f, Ui.GREY, false);
        titleRow.addView(lt);
        box.addView(titleRow);
        TextView val = Ui.text(c, String.valueOf(rating), 21, Ui.WHITE, true);
        val.setGravity(Gravity.CENTER);
        box.addView(val);
        return box;
    }

    private static void statsRow(Context c, LinearLayout parent, String label, String value) {
        LinearLayout row = Ui.linear(c, LinearLayout.HORIZONTAL);
        row.setPadding(0, Ui.dp(c, 5), 0, Ui.dp(c, 5));
        row.addView(Ui.text(c, label, 12.5f, Ui.GREY, false));
        row.addView(Ui.spacerWeight(c, 1f));
        row.addView(Ui.text(c, value, 12.5f, Ui.WHITE, true));
        parent.addView(row);
    }

    private static void editName() {
        final EditText in = new EditText(main);
        in.setText(main.state.playerName);
        in.setSingleLine(true);
        in.setInputType(InputType.TYPE_CLASS_TEXT);
        in.setPadding(Ui.dp(main, 16), Ui.dp(main, 8), Ui.dp(main, 16), Ui.dp(main, 8));
        new AlertDialog.Builder(main)
                .setTitle("Player name")
                .setView(in)
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface d, int w) {
                        String n = in.getText().toString().trim();
                        if (!n.isEmpty()) {
                            main.state.playerName = n;
                            main.state.save(main);
                            main.navigate("moredetail");
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ---------------------------------------------------------------- stats

    private static View statsScreen() {
        Context c = main;
        AppState st = main.state;
        Page pg = newPage("Stats");
        pg.root.addView(section(c, "Rating progress"));
        LinearLayout card = card(c);
        card.addView(bar(c, "Bullet", String.valueOf(st.bulletRating), st.bulletRating / 2800f, 0xFFE0A23C));
        card.addView(bar(c, "Blitz", String.valueOf(st.blitzRating), st.blitzRating / 2800f, 0xFFE0672C));
        card.addView(bar(c, "Rapid", String.valueOf(st.rapidRating), st.rapidRating / 2800f, 0xFF67A04A));
        card.addView(bar(c, "Daily", String.valueOf(st.dailyRating), st.dailyRating / 2800f, 0xFF3E9BDD));
        pg.root.addView(card);

        pg.root.addView(section(c, "Activity"));
        LinearLayout act = card(c);
        statsRow(c, act, "Games played", String.valueOf(st.gamesPlayed));
        statsRow(c, act, "Puzzles solved", String.valueOf(st.puzzlesSolved));
        statsRow(c, act, "Puzzle rating", String.valueOf(st.puzzleRating));
        statsRow(c, act, "Lessons finished", String.valueOf(st.learnProgress));
        statsRow(c, act, "Day streak", String.valueOf(st.streak));
        statsRow(c, act, "Win rate", "54%");
        pg.root.addView(act);

        if (!st.lastOpponent.isEmpty()) {
            pg.root.addView(section(c, "Last game"));
            LinearLayout last = card(c);
            statsRow(c, last, "Opponent", st.lastOpponent);
            statsRow(c, last, "Result", st.lastResult);
            statsRow(c, last, "Accuracy", st.lastAccuracy + "%");
            pg.root.addView(last);
        }
        return pg.scroll;
    }

    // ---------------------------------------------------------------- theme

    private static View themeScreen() {
        final Context c = main;
        Page pg = newPage("Board theme");
        pg.root.addView(body(c, "Pick the board colours. Every board in the app updates instantly."));
        for (int i = 0; i < PALETTES.length; i++) {
            final int idx = i;
            LinearLayout item = card(c);
            final int[] pal = PALETTES[i];
            View preview = new View(c) {
                private final Paint p = new Paint();
                @Override protected void onDraw(Canvas canvas) {
                    float w = getWidth() / 8f, h = getHeight() / 8f;
                    for (int y = 0; y < 8; y++) {
                        for (int x = 0; x < 8; x++) {
                            p.setColor(((x + y) & 1) == 0 ? pal[0] : pal[1]);
                            canvas.drawRect(x * w, y * h, (x + 1) * w, (y + 1) * h, p);
                        }
                    }
                }
            };
            preview.setWillNotDraw(false);
            preview.setBackground(Ui.round(0, 8, c));
            item.addView(preview, new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(c, 96)));
            LinearLayout titleRow = Ui.linear(c, LinearLayout.HORIZONTAL);
            titleRow.setGravity(Gravity.CENTER_VERTICAL);
            LinearLayout.LayoutParams trp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            trp.topMargin = Ui.dp(c, 10);
            titleRow.setLayoutParams(trp);
            titleRow.addView(Ui.text(c, THEME_NAMES[i], 15, Ui.WHITE, true));
            titleRow.addView(Ui.spacerWeight(c, 1f));
            if (AppState.theme == i) {
                titleRow.addView(Ui.text(c, "Selected", 12.5f, Ui.GREEN, true));
            }
            item.addView(titleRow);
            item.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    AppState.theme = idx;
                    main.state.save(main);
                    Toast.makeText(c, THEME_NAMES[idx] + " board selected", Toast.LENGTH_SHORT).show();
                    main.navigate("moredetail");
                }
            });
            pg.root.addView(item);
        }
        return pg.scroll;
    }

    // ---------------------------------------------------------------- awards

    private static View awardsScreen() {
        Context c = main;
        AppState st = main.state;
        Page pg = newPage("Awards");
        String[][] awards = {
                {"First Victory", "Play your first game", st.gamesPlayed >= 1 ? "1" : "0"},
                {"Puzzle Rookie", "Solve 100 puzzles", st.puzzlesSolved >= 100 ? "1" : "0"},
                {"Lesson Starter", "Finish your first lesson", st.learnProgress >= 1 ? "1" : "0"},
                {"On Fire", "Reach a 7 day streak", st.streak >= 7 ? "1" : "0"},
                {"Tactician", "Solve 500 puzzles", st.puzzlesSolved >= 500 ? "1" : "0"},
                {"Century Club", "Reach a 1800 rating", st.userRating >= 1800 ? "1" : "0"},
                {"Puzzle Master", "Reach 2400 puzzle rating", st.puzzleRating >= 2400 ? "1" : "0"},
                {"Veteran", "Play 1000 games", st.gamesPlayed >= 1000 ? "1" : "0"},
        };
        pg.root.addView(body(c, "Earn awards by playing, solving and learning."));
        for (String[] a : awards) {
            boolean earned = "1".equals(a[2]);
            LinearLayout row = card(c);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            TextView badge = Ui.glyph(c, earned ? "emoji_events" : "lock", 22,
                    earned ? 0xFFF1C40F : Ui.GREY_DARK);
            row.addView(badge, new LinearLayout.LayoutParams(Ui.dp(c, 40), Ui.dp(c, 40)));
            LinearLayout texts = Ui.linear(c, LinearLayout.VERTICAL);
            LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            tp.setMargins(Ui.dp(c, 12), 0, 0, 0);
            texts.setLayoutParams(tp);
            texts.addView(Ui.text(c, a[0], 15, earned ? Ui.WHITE : Ui.GREY, true));
            texts.addView(Ui.text(c, a[1], 11.5f, Ui.GREY, false));
            row.addView(texts);
            row.addView(Ui.text(c, earned ? "EARNED" : "LOCKED", 10.5f,
                    earned ? Ui.GREEN : Ui.GREY_DARK, true));
            pg.root.addView(row);
        }
        return pg.scroll;
    }

    // ---------------------------------------------------------------- messages

    private static final String[][] DEFAULT_CHATS = {
            {"Coach Andrei", "Great game yesterday! Your opening play keeps improving."},
            {"Chess.com Team", "Welcome back! Keep your lesson streak going."},
            {"Elena (Club)", "Rematch on Saturday? I need my revenge."},
    };
    private static ArrayList<String[]> chats;
    private static ArrayList<ArrayList<String[]>> chatsMsgs;

    private static void ensureChats() {
        if (chats != null) return;
        chats = new ArrayList<>();
        chatsMsgs = new ArrayList<>();
        for (String[] d : DEFAULT_CHATS) {
            chats.add(new String[]{d[0], d[1]});
            ArrayList<String[]> msgs = new ArrayList<>();
            msgs.add(new String[]{"them", d[1]});
            chatsMsgs.add(msgs);
        }
    }

    private static View messagesScreen() {
        ensureChats();
        Context c = main;
        Page pg = newPage("Messages");
        pg.root.addView(body(c, "Your coaches and club friends."));
        for (int i = 0; i < chats.size(); i++) {
            final int idx = i;
            String[] ch = chats.get(i);
            LinearLayout row = listRow(c, null, ch[0], ch[1], new View.OnClickListener() {
                @Override public void onClick(View v) {
                    sub = "chat:" + idx;
                    main.navigate("moredetail");
                }
            });
            int color = idx == 0 ? 0xFF6C4B8E : idx == 1 ? 0xFF2C6E49 : 0xFF8E4B4B;
            row.addView(circle(c, ch[0].substring(0, 1).toUpperCase(), color, 44, 18), 0);
            pg.root.addView(row);
        }
        return pg.scroll;
    }

    private static View chatScreen(int idx) {
        ensureChats();
        if (idx < 0 || idx >= chatsMsgs.size()) { sub = null; return messagesScreen(); }
        final Context c = main;
        String name = chats.get(idx)[0];
        final int chatIdx = idx;
        Page pg = newPage(name);
        pg.root.addView(body(c, name + " is online"));
        ArrayList<String[]> msgs = chatsMsgs.get(idx);
        for (String[] msg : msgs) {
            boolean you = "you".equals(msg[0]);
            LinearLayout row = Ui.linear(c, LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams rlp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            rlp.topMargin = Ui.dp(c, 8);
            row.setLayoutParams(rlp);
            TextView bubble = Ui.text(c, msg[1], 12.5f, you ? 0xFF10240B : Ui.WHITE, false);
            bubble.setTextColor(you ? Ui.WHITE : Ui.WHITE);
            bubble.setBackground(you ? Ui.greenFill(c, 12) : Ui.round(0xFF3B3935, 12, c));
            bubble.setPadding(Ui.dp(c, 12), Ui.dp(c, 9), Ui.dp(c, 12), Ui.dp(c, 9));
            bubble.setMaxWidth((int) (c.getResources().getDisplayMetrics().widthPixels * 0.76f));
            if (you) {
                row.addView(Ui.spacerWeight(c, 1f));
                row.addView(bubble);
            } else {
                row.addView(bubble);
                row.addView(Ui.spacerWeight(c, 1f));
            }
            pg.root.addView(row);
        }
        final EditText input = new EditText(c);
        input.setHint("Message " + name.split(" ")[0]);
        input.setSingleLine(true);
        input.setTextSize(12.5f);
        input.setTextColor(Ui.WHITE);
        input.setHintTextColor(Ui.GREY);
        input.setPadding(Ui.dp(c, 12), 0, Ui.dp(c, 12), 0);
        input.setBackground(Ui.round(0xFF3B3935, 10, c));
        LinearLayout inputRow = Ui.linear(c, LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams irlp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(c, 52));
        irlp.topMargin = Ui.dp(c, 16);
        inputRow.setLayoutParams(irlp);
        inputRow.addView(input, new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.MATCH_PARENT, 1f));
        TextView send = Ui.text(c, "Send", 13.5f, Ui.WHITE, true);
        send.setGravity(Gravity.CENTER);
        send.setBackground(Ui.greenFill(c, 10));
        LinearLayout.LayoutParams slp = new LinearLayout.LayoutParams(
                Ui.dp(c, 84), ViewGroup.LayoutParams.MATCH_PARENT);
        slp.setMargins(Ui.dp(c, 8), 0, 0, 0);
        send.setLayoutParams(slp);
        send.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                String text = input.getText().toString().trim();
                if (text.isEmpty()) return;
                chatsMsgs.get(chatIdx).add(new String[]{"you", text});
                chats.get(chatIdx)[1] = text;
                main.navigate("moredetail");
            }
        });
        inputRow.addView(send);
        pg.root.addView(inputRow);
        final ScrollView sc = pg.scroll;
        sc.post(new Runnable() {
            @Override public void run() { sc.fullScroll(View.FOCUS_DOWN); }
        });
        return pg.scroll;
    }

    // ---------------------------------------------------------------- settings

    private interface OnToggle { void changed(boolean value); }

    private static LinearLayout toggleRow(Context c, String title, String desc,
                                          boolean checked, final OnToggle setter) {
        LinearLayout row = Ui.linear(c, LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setBackground(Ui.round(0x1FFFFFFF, 12, c));
        row.setPadding(Ui.dp(c, 14), Ui.dp(c, 10), Ui.dp(c, 14), Ui.dp(c, 10));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.topMargin = Ui.dp(c, 10);
        row.setLayoutParams(lp);
        LinearLayout texts = Ui.linear(c, LinearLayout.VERTICAL);
        LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        tp.setMargins(0, 0, Ui.dp(c, 10), 0);
        texts.setLayoutParams(tp);
        texts.addView(Ui.text(c, title, 14.5f, Ui.WHITE, true));
        texts.addView(Ui.text(c, desc, 11.5f, Ui.GREY, false));
        row.addView(texts);
        final Switch sw = new Switch(c);
        sw.setChecked(checked);
        sw.setThumbTintList(ColorStateList.valueOf(0xFFFFFFFF));
        sw.setTrackTintList(ColorStateList.valueOf(checked ? Ui.GREEN : 0xFF4A4742));
        sw.setOnCheckedChangeListener(new android.widget.CompoundButton.OnCheckedChangeListener() {
            @Override public void onCheckedChanged(android.widget.CompoundButton b, boolean v) {
                sw.setTrackTintList(ColorStateList.valueOf(v ? Ui.GREEN : 0xFF4A4742));
                setter.changed(v);
            }
        });
        row.addView(sw);
        return row;
    }

    private static View settingsScreen() {
        final Context c = main;
        Page pg = newPage("Settings");
        pg.root.addView(section(c, "Board"));
        pg.root.addView(toggleRow(c, "Coordinates",
                "Show file letters and rank numbers", AppState.coords,
                new OnToggle() {
                    @Override public void changed(boolean v) {
                        AppState.coords = v;
                        main.state.save(main);
                    }
                }));
        pg.root.addView(toggleRow(c, "Legal move dots",
                "Show dots on possible moves", AppState.dots,
                new OnToggle() {
                    @Override public void changed(boolean v) {
                        AppState.dots = v;
                        main.state.save(main);
                    }
                }));
        pg.root.addView(listRow(c, "more_theme", "Board theme",
                THEME_NAMES[Math.max(0, Math.min(3, AppState.theme))],
                new View.OnClickListener() {
                    @Override public void onClick(View v) {
                        key = "theme";
                        sub = null;
                        main.navigate("moredetail");
                    }
                }));

        pg.root.addView(section(c, "Gameplay"));
        pg.root.addView(toggleRow(c, "Sound effects",
                "Beeps for moves and alerts", AppState.sound,
                new OnToggle() {
                    @Override public void changed(boolean v) {
                        AppState.sound = v;
                        main.state.save(main);
                        if (v) {
                            try {
                                ToneGenerator tg = new ToneGenerator(AudioManager.STREAM_MUSIC, 60);
                                tg.startTone(ToneGenerator.TONE_PROP_BEEP, 140);
                            } catch (Exception ignored) { }
                        }
                    }
                }));

        pg.root.addView(section(c, "About"));
        LinearLayout about = card(c);
        statsRow(c, about, "App", "Chess");
        statsRow(c, about, "Version", "1.4.0");
        statsRow(c, about, "Engine", "chesslib");
        pg.root.addView(about);
        return pg.scroll;
    }

    // ---------------------------------------------------------------- support

    private static final String[][] FAQ = {
            {"How is my rating calculated?",
             "Ratings follow the Glicko system. Winning against a stronger opponent gives more points, and the pool rating (bullet, blitz, rapid, daily) is shown on your profile."},
            {"Why did I lose rating after a wrong puzzle?",
             "Puzzles work like rated games: a correct solution adds points, a wrong move costs a few. The puzzle rating shown under Puzzles is your tactical rating."},
            {"Can I change the board look?",
             "Yes. Open More > Theme (or Settings > Board theme) and pick one of the four boards. The choice is saved and applied everywhere."},
            {"How do I turn off coordinates?",
             "More > Settings > Coordinates. You can also toggle legal-move dots and sound there."},
    };
    private static final boolean[] faqOpen = new boolean[FAQ.length];

    private static View supportScreen() {
        final Context c = main;
        Page pg = newPage("Support");
        pg.root.addView(body(c, "Answers to the most common questions."));
        for (int i = 0; i < FAQ.length; i++) {
            final int idx = i;
            LinearLayout item = card(c);
            LinearLayout head = Ui.linear(c, LinearLayout.HORIZONTAL);
            head.setGravity(Gravity.CENTER_VERTICAL);
            head.addView(Ui.text(c, FAQ[i][0], 13.5f, Ui.WHITE, true),
                    new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
            head.addView(Ui.glyph(c, faqOpen[i] ? "expand_less" : "expand_more", 18, Ui.GREY));
            item.addView(head);
            if (faqOpen[i]) {
                TextView ans = body(c, FAQ[i][1]);
                ans.setPadding(0, Ui.dp(c, 8), 0, 0);
                item.addView(ans);
            }
            item.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    faqOpen[idx] = !faqOpen[idx];
                    main.navigate("moredetail");
                }
            });
            pg.root.addView(item);
        }
        pg.root.addView(button(c, "Contact support", true, new View.OnClickListener() {
            @Override public void onClick(View v) { contactSupport(); }
        }));
        return pg.scroll;
    }

    private static void contactSupport() {
        final EditText in = new EditText(main);
        in.setHint("Describe your issue");
        in.setMinLines(3);
        in.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        in.setPadding(Ui.dp(main, 16), Ui.dp(main, 8), Ui.dp(main, 16), Ui.dp(main, 8));
        new AlertDialog.Builder(main)
                .setTitle("Contact support")
                .setView(in)
                .setPositiveButton("Send", new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface d, int w) {
                        Toast.makeText(main, "Message sent. We reply within 24h.",
                                Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ---------------------------------------------------------------- analysis / solo

    private static int countSidePieces(Board b, Side side, boolean excludeKing) {
        int n = 0;
        for (int f = 0; f < 8; f++) {
            for (int r = 0; r < 8; r++) {
                Piece p = b.getPiece(Square.valueOf("" + (char) ('A' + f) + (r + 1)));
                if (p == Piece.NONE || p.getPieceSide() != side) continue;
                if (excludeKing && p.getPieceType() == PieceType.KING) continue;
                n++;
            }
        }
        return n;
    }

    private static View analysisScreen(String title, final String startFen,
                                       final boolean solo) {
        final Context c = main;
        Page pg = newPage(title);
        final Board board = new Board();
        board.loadFromFen(startFen);
        final BoardView view = new BoardView(c);
        final ArrayDeque<String> history = new ArrayDeque<>();
        final int[] sel = {-1, -1};
        final int[][] last = {null};
        final TextView status = Ui.text(c, "", 13.5f, Ui.WHITE, true);
        final TextView hint = body(c, solo
                ? "Solo Chess: you play White and only captures are allowed. Capture every black piece except the king."
                : "Free play board. Tap a piece, then tap a destination. Both sides move.");

        final Runnable refresh = new Runnable() {
            @Override public void run() {
                view.setState(board.getFen(), last[0]);
                try {
                    if (board.isMated()) {
                        status.setText("Checkmate! "
                                + (board.getSideToMove() == Side.WHITE ? "Black" : "White") + " wins");
                        return;
                    }
                } catch (Exception ignored) { }
                String side = board.getSideToMove() == Side.WHITE ? "White" : "Black";
                status.setText((solo ? "" : side + " to move")
                        + (solo ? "Solo Chess - white to move" : ""));
            }
        };

        view.setListener(new BoardView.Listener() {
            @Override public void onSquareTapped(int file, int rank) {
                String sqName = String.valueOf((char) ('A' + file)) + (rank + 1);
                Square sq = Square.valueOf(sqName);
                if (sel[0] >= 0) {
                    String fromName = String.valueOf((char) ('A' + sel[0])) + (sel[1] + 1);
                    Square from = Square.valueOf(fromName);
                    Move chosen = null;
                    for (Move m : board.legalMoves()) {
                        if (m.getFrom() == from && m.getTo() == sq) { chosen = m; break; }
                    }
                    boolean capture = board.getPiece(sq) != Piece.NONE;
                    if (chosen != null && solo && !capture) {
                        Toast.makeText(c, "Solo Chess: captures only!",
                                Toast.LENGTH_SHORT).show();
                    } else if (chosen != null) {
                        history.push(board.getFen());
                        board.doMove(chosen);
                        last[0] = new int[]{
                                chosen.getFrom().getFile().ordinal(),
                                chosen.getFrom().getRank().ordinal(),
                                chosen.getTo().getFile().ordinal(),
                                chosen.getTo().getRank().ordinal()};
                        if (solo) {
                            int left = countSidePieces(board, Side.BLACK, true);
                            if (left == 0) {
                                Toast.makeText(c, "Solo Chess complete! All pieces captured.",
                                        Toast.LENGTH_LONG).show();
                            }
                            String[] f = board.getFen().split(" ");
                            board.loadFromFen(f[0] + " w " + f[2] + " " + f[3] + " "
                                    + f[4] + " " + f[5]);
                        }
                    }
                    sel[0] = -1; sel[1] = -1;
                    view.setSelection(null, null);
                    refresh.run();
                } else {
                    Piece p = board.getPiece(sq);
                    boolean okSide = p != Piece.NONE && (solo
                            ? p.getPieceSide() == Side.WHITE
                            : p.getPieceSide() == board.getSideToMove());
                    if (okSide) {
                        Set<Integer> targets = new HashSet<>();
                        for (Move m : board.legalMoves()) {
                            if (m.getFrom() == sq) {
                                targets.add(m.getTo().getFile().ordinal() * 8
                                        + m.getTo().getRank().ordinal());
                            }
                        }
                        sel[0] = file; sel[1] = rank;
                        view.setSelection(new int[]{file, rank}, targets);
                    }
                }
            }
        });
        refresh.run();

        FrameLayout wrap = new FrameLayout(c);
        LinearLayout.LayoutParams wlp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        wlp.topMargin = Ui.dp(c, 12);
        wrap.setLayoutParams(wlp);
        wrap.addView(view, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        pg.root.addView(wrap);
        pg.root.addView(statusRow(c, status));
        if (solo) pg.root.addView(hint);

        LinearLayout controls = Ui.linear(c, LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        clp.topMargin = Ui.dp(c, 10);
        controls.setLayoutParams(clp);
        TextView undo = Ui.text(c, "Undo", 13.5f, Ui.WHITE, true);
        undo.setGravity(Gravity.CENTER);
        undo.setBackground(Ui.round(0xFF43403B, 9, c));
        undo.setPadding(0, Ui.dp(c, 11), 0, Ui.dp(c, 11));
        undo.setLayoutParams(new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        undo.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (history.isEmpty()) {
                    Toast.makeText(c, "No moves yet", Toast.LENGTH_SHORT).show();
                    return;
                }
                board.loadFromFen(history.pop());
                sel[0] = -1; sel[1] = -1;
                last[0] = null;
                view.setSelection(null, null);
                refresh.run();
            }
        });
        TextView flip = Ui.text(c, "Flip", 13.5f, Ui.WHITE, true);
        flip.setGravity(Gravity.CENTER);
        flip.setBackground(Ui.round(0xFF43403B, 9, c));
        flip.setPadding(0, Ui.dp(c, 11), 0, Ui.dp(c, 11));
        LinearLayout.LayoutParams flp = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        flp.setMargins(Ui.dp(c, 8), 0, Ui.dp(c, 8), 0);
        flip.setLayoutParams(flp);
        flip.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { view.setFlipped(!view.isFlipped()); }
        });
        TextView reset = Ui.text(c, "Reset", 13.5f, Ui.WHITE, true);
        reset.setGravity(Gravity.CENTER);
        reset.setBackground(Ui.round(0xFF43403B, 9, c));
        reset.setPadding(0, Ui.dp(c, 11), 0, Ui.dp(c, 11));
        reset.setLayoutParams(new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        reset.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                board.loadFromFen(startFen);
                history.clear();
                sel[0] = -1; sel[1] = -1;
                last[0] = null;
                view.setSelection(null, null);
                refresh.run();
            }
        });
        controls.addView(undo);
        controls.addView(flip);
        controls.addView(reset);
        pg.root.addView(controls);
        return pg.scroll;
    }

    private static LinearLayout statusRow(Context c, TextView status) {
        LinearLayout row = Ui.linear(c, LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setBackground(Ui.round(0x1FFFFFFF, 10, c));
        row.setPadding(Ui.dp(c, 12), Ui.dp(c, 9), Ui.dp(c, 12), Ui.dp(c, 9));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.topMargin = Ui.dp(c, 10);
        row.setLayoutParams(lp);
        row.addView(Ui.glyph(c, "visibility", 15, Ui.GREEN));
        status.setPadding(Ui.dp(c, 8), 0, 0, 0);
        row.addView(status);
        return row;
    }

    // ---------------------------------------------------------------- explorer / endgames / practice

    private static final String[][] OPENINGS = {
            {"Italian Game", "1.e4 e5 2.Nf3 Nc6 3.Bc4",
             "Fast development and pressure on f7. Perfect for club players."},
            {"Sicilian Defense", "1.e4 c5",
             "The most popular answer to 1.e4 - sharp, counter-attacking play."},
            {"French Defense", "1.e4 e6",
             "Solid pawn chains and a queenside attack plan for Black."},
            {"Ruy Lopez", "1.e4 e5 2.Nf3 Nc6 3.Bb5",
             "Classical pressure on the knight that defends e5."},
            {"Queen's Gambit", "1.d4 d5 2.c4",
             "Offer a pawn to dominate the centre - a timeless classic."},
            {"London System", "1.d4 2.Nf3 3.Bf4",
             "Easy, reliable setup with a kingside attacking scheme."},
    };

    private static View explorerScreen() {
        final Context c = main;
        Page pg = newPage("Explorer");
        pg.root.addView(body(c, "Six openings every club player should know."));
        for (final String[] o : OPENINGS) {
            LinearLayout item = card(c);
            item.addView(Ui.text(c, o[0], 15.5f, Ui.WHITE, true));
            item.addView(Ui.text(c, o[1], 12.5f, Ui.GREEN, true));
            TextView desc = body(c, o[2]);
            desc.setPadding(0, Ui.dp(c, 6), 0, 0);
            item.addView(desc);
            TextView practice = Ui.text(c, "Practice on the board  ▸", 12, 0xFFA8CD85, true);
            practice.setPadding(0, Ui.dp(c, 10), 0, 0);
            item.addView(practice);
            item.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    key = "analysis";
                    sub = null;
                    main.navigate("moredetail");
                    Toast.makeText(c, o[0] + ": play the moves on the board",
                            Toast.LENGTH_SHORT).show();
                }
            });
            pg.root.addView(item);
        }
        return pg.scroll;
    }

    private static final String[][] ENDGAMES = {
            {"King + Pawn vs King", "8/8/8/4k3/8/4K3/4P3/8 w - - 0 1",
             "Escort the pawn to promotion using the king."},
            {"Rook vs King", "8/8/8/4k3/8/4K3/8/7R w - - 0 1",
             "Cut the king off and deliver the ladder mate."},
            {"Lucena Position", "1K6/1P6/8/8/8/8/r6k/8 w - - 0 1",
             "The most important rook endgame - build the bridge."},
            {"Two Rooks Mate", "8/8/8/4k3/8/8/8/R6R w - - 0 1",
             "Ladder mate with two rooks in a few moves."},
    };

    private static View endgamesScreen() {
        final Context c = main;
        Page pg = newPage("Endgames");
        pg.root.addView(body(c, "Practice the positions that win club games."));
        for (int i = 0; i < ENDGAMES.length; i++) {
            final int idx = i;
            LinearLayout item = card(c);
            item.addView(Ui.text(c, ENDGAMES[i][0], 15.5f, Ui.WHITE, true));
            TextView desc = body(c, ENDGAMES[i][2]);
            desc.setPadding(0, Ui.dp(c, 6), 0, 0);
            item.addView(desc);
            TextView play = Ui.text(c, "Practice  ▸", 12.5f, 0xFFA8CD85, true);
            play.setPadding(0, Ui.dp(c, 10), 0, 0);
            item.addView(play);
            item.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    sub = "eg:" + idx;
                    main.navigate("moredetail");
                }
            });
            pg.root.addView(item);
        }
        return pg.scroll;
    }

    private static View endgamePlay(int idx) {
        if (idx < 0 || idx >= ENDGAMES.length) { sub = null; return endgamesScreen(); }
        return analysisScreen(ENDGAMES[idx][0], ENDGAMES[idx][1], false);
    }

    private static View practiceScreen() {
        final Context c = main;
        Page pg = newPage("Practice");
        pg.root.addView(body(c, "Pick a training mode."));
        pg.root.addView(listRow(c, "more_practice", "Solve puzzles", "Mate in 1 tactics",
                new View.OnClickListener() {
                    @Override public void onClick(View v) { main.navigate("puzzles"); }
                }));
        pg.root.addView(listRow(c, "more_analysis", "Analysis board", "Free play, undo, flip",
                new View.OnClickListener() {
                    @Override public void onClick(View v) {
                        key = "analysis"; sub = null; main.navigate("moredetail");
                    }
                }));
        pg.root.addView(listRow(c, null, "Vision trainer", "Find squares fast",
                new View.OnClickListener() {
                    @Override public void onClick(View v) {
                        key = "vision"; sub = null; main.navigate("moredetail");
                    }
                }));
        pg.root.addView(listRow(c, null, "Endgames", "Convert winning positions",
                new View.OnClickListener() {
                    @Override public void onClick(View v) {
                        key = "endgames"; sub = null; main.navigate("moredetail");
                    }
                }));
        pg.root.addView(button(c, "Start a new game", true, new View.OnClickListener() {
            @Override public void onClick(View v) { main.navigate("newgame"); }
        }));
        return pg.scroll;
    }

    // ---------------------------------------------------------------- insights

    private static View insightsScreen() {
        Context c = main;
        AppState st = main.state;
        Page pg = newPage("Insights");
        String bestPool = "Rapid";
        int best = st.rapidRating;
        if (st.bulletRating > best) { best = st.bulletRating; bestPool = "Bullet"; }
        if (st.blitzRating > best) { best = st.blitzRating; bestPool = "Blitz"; }
        if (st.dailyRating > best) { best = st.dailyRating; bestPool = "Daily"; }

        LinearLayout top = card(c);
        top.addView(Ui.text(c, "Your strongest pool is " + bestPool, 15.5f, Ui.WHITE, true));
        TextView d = body(c, "You score best in " + bestPool + " (" + best
                + "). Keep playing there to build confidence, and spend study time on the others.");
        d.setPadding(0, Ui.dp(c, 6), 0, 0);
        top.addView(d);
        pg.root.addView(top);

        pg.root.addView(section(c, "Progress"));
        LinearLayout bars = card(c);
        bars.addView(bar(c, "Games (goal 1500)", st.gamesPlayed + "/1500",
                st.gamesPlayed / 1500f, 0xFF67A04A));
        bars.addView(bar(c, "Puzzles (goal 2000)", st.puzzlesSolved + "/2000",
                st.puzzlesSolved / 2000f, 0xFFE0A23C));
        bars.addView(bar(c, "Lessons (goal 30)", st.learnProgress + "/30",
                st.learnProgress / 30f, 0xFF3E9BDD));
        pg.root.addView(bars);

        pg.root.addView(section(c, "Last game"));
        LinearLayout last = card(c);
        statsRow(c, last, "Accuracy", st.lastAccuracy.isEmpty() ? "-" : st.lastAccuracy + "%");
        statsRow(c, last, "Result vs " + st.lastOpponent, st.lastResult.isEmpty() ? "-" : st.lastResult);
        pg.root.addView(last);
        return pg.scroll;
    }

    // ---------------------------------------------------------------- vision

    private static int[] visionTarget = {4, 3};
    private static int visionScore = 0;
    private static int visionRound = 0;

    private static void newVisionTarget() {
        java.util.Random r = new java.util.Random();
        visionTarget[0] = r.nextInt(8);
        visionTarget[1] = r.nextInt(8);
    }

    private static View visionScreen() {
        final Context c = main;
        Page pg = newPage("Vision");
        pg.root.addView(body(c, "Find the named square on the board. 10 rounds."));
        final TextView prompt = Ui.text(c, "", 24, Ui.WHITE, true);
        prompt.setGravity(Gravity.CENTER);
        prompt.setPadding(0, Ui.dp(c, 12), 0, Ui.dp(c, 4));
        pg.root.addView(prompt);
        final TextView score = Ui.text(c, "", 13, Ui.GREY, true);
        score.setGravity(Gravity.CENTER);
        score.setPadding(0, 0, 0, Ui.dp(c, 10));
        pg.root.addView(score);
        if (visionRound >= 10) {
            prompt.setText("Result: " + visionScore + " / 10");
            score.setText("Tap the board to start again");
        } else {
            prompt.setText(squareName(visionTarget[0], visionTarget[1]));
            score.setText("Score " + visionScore + "  •  Round " + (visionRound + 1) + "/10");
        }

        LinearLayout grid = Ui.linear(c, LinearLayout.VERTICAL);
        for (int row = 0; row < 8; row++) {
            LinearLayout line = Ui.linear(c, LinearLayout.HORIZONTAL);
            for (int col = 0; col < 8; col++) {
                final int file = col;
                final int rank = 7 - row;
                TextView cell = new TextView(c);
                boolean lightSq = ((file + rank) % 2) == 1;
                cell.setBackgroundColor(lightSq ? 0xFFEEEED2 : 0xFF769656);
                cell.setLayoutParams(new LinearLayout.LayoutParams(
                        0, Ui.dp(c, 42), 1f));
                cell.setOnClickListener(new View.OnClickListener() {
                    @Override public void onClick(View v) {
                        if (visionRound >= 10) {
                            visionRound = 0;
                            visionScore = 0;
                            newVisionTarget();
                            main.navigate("moredetail");
                            return;
                        }
                        if (file == visionTarget[0] && rank == visionTarget[1]) {
                            visionScore++;
                            Toast.makeText(c, "Correct!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(c, "That was " + squareName(visionTarget[0], visionTarget[1]),
                                    Toast.LENGTH_SHORT).show();
                        }
                        visionRound++;
                        newVisionTarget();
                        main.navigate("moredetail");
                    }
                });
                line.addView(cell);
            }
            grid.addView(line);
        }
        pg.root.addView(grid);
        return pg.scroll;
    }

    private static String squareName(int file, int rank) {
        return String.valueOf((char) ('a' + file)) + (rank + 1);
    }

    // ---------------------------------------------------------------- videos / TV

    private static final String[][] VIDEOS = {
            {"Getting Started with Openings", "FM Anna Cramling", "12:40"},
            {"Tactics: Pins and Skewers", "IM Levy Rozman", "08:15"},
            {"Endgame Essentials", "GM Daniel Naroditsky", "15:02"},
            {"How to Calculate Like a Master", "GM Hikaru Nakamura", "21:37"},
            {"Speedrun to 2000 - Episode 4", "GM Daniel Naroditsky", "31:08"},
    };

    private static View videosScreen() {
        final Context c = main;
        Page pg = newPage("Videos");
        pg.root.addView(body(c, "Lessons from the world's best coaches."));
        for (int i = 0; i < VIDEOS.length; i++) {
            final int idx = i;
            LinearLayout row = listRow(c, null, VIDEOS[i][0],
                    VIDEOS[i][1] + "  •  " + VIDEOS[i][2], new View.OnClickListener() {
                        @Override public void onClick(View v) {
                            sub = "video:" + idx;
                            main.navigate("moredetail");
                        }
                    });
            row.addView(circle(c, "▶", 0xFF5D9948, 44, 16), 0);
            pg.root.addView(row);
        }
        return pg.scroll;
    }

    private static View videoScreen(int idx) {
        final Context c = main;
        if (idx < 0 || idx >= VIDEOS.length) { sub = null; return videosScreen(); }
        Page pg = newPage(VIDEOS[idx][0]);
        pg.root.addView(body(c, VIDEOS[idx][1] + "  •  " + VIDEOS[idx][2]));

        FrameLayout player = new FrameLayout(c);
        player.setBackground(Ui.round(0xFF141312, 12, c));
        LinearLayout.LayoutParams plp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(c, 190));
        plp.topMargin = Ui.dp(c, 12);
        player.setLayoutParams(plp);
        TextView glyph = Ui.glyph(c, "play_arrow", 52, 0xCCFFFFFF);
        player.addView(glyph, new FrameLayout.LayoutParams(
                Ui.dp(c, 80), Ui.dp(c, 80), Gravity.CENTER));
        pg.root.addView(player);

        LinearLayout track = Ui.linear(c, LinearLayout.HORIZONTAL);
        track.setBackground(Ui.round(0xFF45423C, 4, c));
        LinearLayout.LayoutParams tlp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(c, 8));
        tlp.topMargin = Ui.dp(c, 10);
        track.setLayoutParams(tlp);
        final View fill = new View(c);
        fill.setBackground(Ui.round(Ui.GREEN, 4, c));
        track.addView(fill, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 0.02f));
        track.addView(new View(c), new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 0.98f));
        pg.root.addView(track);

        final TextView subLine = Ui.text(c, "Playing - 0:00", 12.5f, Ui.GREY, false);
        subLine.setPadding(0, Ui.dp(c, 6), 0, 0);
        pg.root.addView(subLine);

        final Handler handler = new Handler(Looper.getMainLooper());
        final float[] progress = {0.02f};
        final boolean[] playing = {true};
        final TextView pause = button(c, "Pause", false, null);
        pause.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                playing[0] = !playing[0];
                pause.setText(playing[0] ? "Pause" : "Play");
            }
        });
        pg.root.addView(pause);

        handler.post(new Runnable() {
            @Override public void run() {
                if (!fill.isAttachedToWindow()) return;
                if (playing[0]) {
                    progress[0] += 0.004f;
                    if (progress[0] > 0.99f) progress[0] = 0.02f;
                    LinearLayout parent = (LinearLayout) fill.getParent();
                    ((LinearLayout.LayoutParams) fill.getLayoutParams()).weight = progress[0];
                    ((LinearLayout.LayoutParams) parent.getChildAt(1).getLayoutParams()).weight =
                            1f - progress[0];
                    parent.requestLayout();
                    int totalSec = Integer.parseInt(VIDEOS[idx][2].split(":")[0]) * 60
                            + Integer.parseInt(VIDEOS[idx][2].split(":")[1]);
                    int sec = (int) (totalSec * progress[0]);
                    subLine.setText("Playing - " + (sec / 60) + ":"
                            + String.format("%02d", sec % 60));
                }
                handler.postDelayed(this, 120);
            }
        });
        return pg.scroll;
    }

    private static final String[][] STREAMS = {
            {"Titled Tuesday - Round 7", "GM Hikaru Nakamura", "24.1K watching"},
            {"Blitz Arena with Anna", "WGM Anna Rudolf", "6.8K watching"},
            {"Endgame class: rook endings", "IM Eric Rosen", "3.2K watching"},
    };

    private static View tvScreen() {
        final Context c = main;
        Page pg = newPage("ChessTV");
        pg.root.addView(body(c, "Live streams and broadcasts."));
        for (int i = 0; i < STREAMS.length; i++) {
            final int idx = i;
            LinearLayout row = listRow(c, null, STREAMS[i][0],
                    STREAMS[i][1] + "  •  " + STREAMS[i][2], new View.OnClickListener() {
                        @Override public void onClick(View v) {
                            sub = "tv:" + idx;
                            main.navigate("moredetail");
                        }
                    });
            TextView live = Ui.text(c, "LIVE", 10, Ui.WHITE, true);
            live.setGravity(Gravity.CENTER);
            live.setBackground(Ui.round(0xFFE02828, 3, c));
            live.setPadding(Ui.dp(c, 6), Ui.dp(c, 2), Ui.dp(c, 6), Ui.dp(c, 2));
            row.addView(live, 0);
            pg.root.addView(row);
        }
        return pg.scroll;
    }

    private static View tvStreamScreen(int idx) {
        final Context c = main;
        if (idx < 0 || idx >= STREAMS.length) { sub = null; return tvScreen(); }
        Page pg = newPage(STREAMS[idx][0]);
        pg.root.addView(body(c, STREAMS[idx][1] + "  •  " + STREAMS[idx][2]));
        FrameLayout player = new FrameLayout(c);
        player.setBackground(Ui.round(0xFF141312, 12, c));
        LinearLayout.LayoutParams plp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(c, 190));
        plp.topMargin = Ui.dp(c, 12);
        player.setLayoutParams(plp);
        player.addView(Ui.glyph(c, "play_arrow", 52, 0xCCFFFFFF),
                new FrameLayout.LayoutParams(Ui.dp(c, 80), Ui.dp(c, 80), Gravity.CENTER));
        TextView live = Ui.text(c, "LIVE", 11, Ui.WHITE, true);
        live.setGravity(Gravity.CENTER);
        live.setBackground(Ui.round(0xFFE02828, 3, c));
        live.setPadding(Ui.dp(c, 8), Ui.dp(c, 2), Ui.dp(c, 8), Ui.dp(c, 2));
        FrameLayout.LayoutParams llp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        llp.gravity = Gravity.TOP | Gravity.START;
        llp.topMargin = Ui.dp(c, 10);
        llp.leftMargin = Ui.dp(c, 10);
        player.addView(live, llp);
        pg.root.addView(player);

        pg.root.addView(section(c, "Live chat"));
        String[] chat = {"ChessFan99: what an attack!", "Baku_GM: Qxf7 is coming",
                "Anya: this endgame is so clean", "chessnerd: stream quality is great"};
        LinearLayout chatCard = card(c);
        for (String line : chat) {
            TextView t = body(c, line);
            t.setPadding(0, Ui.dp(c, 3), 0, Ui.dp(c, 3));
            chatCard.addView(t);
        }
        pg.root.addView(chatCard);
        return pg.scroll;
    }

    // ---------------------------------------------------------------- articles

    private static final String[][] ARTICLES = {
            {"How to Improve at Chess Quickly", "5 min read",
             "Improvement comes from playing slow games and reviewing them.\n\n"
                     + "Play one longer game instead of five blitz games, then write down every "
                     + "mistake you can find. If you cannot explain a move, that is your next lesson topic.\n\n"
                     + "Solve fifteen minutes of tactics every day. Consistency beats marathon sessions."},
            {"The Art of the Endgame", "7 min read",
             "Most club games are decided after move 30.\n\n"
                     + "Learn the Lucena and Philidor positions, king activity, and the rule of the "
                     + "square. These few ideas win more points than any opening trap.\n\n"
                     + "Practice each endgame against a friend until you can explain every move."},
            {"Building a Repertoire with Confidence", "6 min read",
             "A repertoire is only useful if you remember it under pressure.\n\n"
                     + "Choose one opening for White and two defenses for Black. Play them in every "
                     + "game for a month before adding anything new.\n\n"
                     + "Depth beats breadth at club level."},
    };

    private static View articlesScreen() {
        final Context c = main;
        Page pg = newPage("Articles");
        pg.root.addView(body(c, "Short reads from our coaches."));
        for (int i = 0; i < ARTICLES.length; i++) {
            final int idx = i;
            LinearLayout row = listRow(c, null, ARTICLES[i][0], ARTICLES[i][1],
                    new View.OnClickListener() {
                        @Override public void onClick(View v) {
                            sub = "article:" + idx;
                            main.navigate("moredetail");
                        }
                    });
            pg.root.addView(row);
        }
        return pg.scroll;
    }

    private static View articleScreen(int idx) {
        final Context c = main;
        if (idx < 0 || idx >= ARTICLES.length) { sub = null; return articlesScreen(); }
        Page pg = newPage("Article");
        pg.root.addView(Ui.text(c, ARTICLES[idx][0], 18, Ui.WHITE, true));
        TextView meta = Ui.text(c, ARTICLES[idx][1] + "  •  Chess staff", 11.5f, Ui.GREY, false);
        meta.setPadding(0, Ui.dp(c, 4), 0, Ui.dp(c, 6));
        pg.root.addView(meta);
        TextView content = body(c, ARTICLES[idx][2]);
        content.setTextSize(13);
        content.setTextColor(0xFFDDD8D0);
        pg.root.addView(content);
        pg.root.addView(button(c, "Share article", false, new View.OnClickListener() {
            @Override public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(Intent.EXTRA_SUBJECT, ARTICLES[idx][0]);
                i.putExtra(Intent.EXTRA_TEXT, ARTICLES[idx][0] + "\n\n" + ARTICLES[idx][2]);
                main.startActivity(Intent.createChooser(i, "Share article"));
            }
        }));
        return pg.scroll;
    }

    // ---------------------------------------------------------------- forums

    private static final String[][] FORUM_TOPICS = {
            {"Best openings for club players", "Eldar", "12 replies"},
            {"How do you train tactics?", "Anya", "8 replies"},
            {"Rook endgame help", "Teimour", "5 replies"},
            {"What is your favourite board theme?", "Elena", "20 replies"},
    };
    private static ArrayList<ArrayList<String[]>> forumPosts;

    private static void ensureForums() {
        if (forumPosts != null) return;
        forumPosts = new ArrayList<>();
        for (String[] t : FORUM_TOPICS) {
            ArrayList<String[]> posts = new ArrayList<>();
            posts.add(new String[]{t[1], "Starting the discussion: " + t[0]
                    + ". Share your experience!"});
            forumPosts.add(posts);
        }
        forumPosts.get(0).add(new String[]{"Anya", "The Italian is perfect - simple plans and quick development."});
        forumPosts.get(0).add(new String[]{"Teimour", "I switched to the London and my results improved immediately."});
        forumPosts.get(1).add(new String[]{"Elena", "15 minutes of puzzles before every session. No exceptions."});
        forumPosts.get(2).add(new String[]{"Coach Andrei", "Start with the Lucena and Philidor positions."});
    }

    private static View forumsScreen() {
        ensureForums();
        final Context c = main;
        Page pg = newPage("Forums");
        pg.root.addView(body(c, "Talk chess with the community."));
        for (int i = 0; i < FORUM_TOPICS.length; i++) {
            final int idx = i;
            LinearLayout row = listRow(c, null, FORUM_TOPICS[i][0],
                    "by " + FORUM_TOPICS[i][1] + "  •  " + forumPosts.get(i).size() + " posts",
                    new View.OnClickListener() {
                        @Override public void onClick(View v) {
                            sub = "forum:" + idx;
                            main.navigate("moredetail");
                        }
                    });
            pg.root.addView(row);
        }
        return pg.scroll;
    }

    private static View forumThread(int idx) {
        ensureForums();
        final Context c = main;
        if (idx < 0 || idx >= FORUM_TOPICS.length) { sub = null; return forumsScreen(); }
        final int topicIdx = idx;
        Page pg = newPage("Thread");
        pg.root.addView(Ui.text(c, FORUM_TOPICS[idx][0], 17.5f, Ui.WHITE, true));
        TextView meta = Ui.text(c, "Started by " + FORUM_TOPICS[idx][1], 11.5f, Ui.GREY, false);
        meta.setPadding(0, 0, 0, Ui.dp(c, 4));
        pg.root.addView(meta);
        ArrayList<String[]> posts = forumPosts.get(idx);
        for (String[] post : posts) {
            LinearLayout item = card(c);
            LinearLayout head = Ui.linear(c, LinearLayout.HORIZONTAL);
            head.setGravity(Gravity.CENTER_VERTICAL);
            head.addView(circle(c, post[0].substring(0, 1).toUpperCase(), 0xFF4A6E8E, 34, 14));
            TextView author = Ui.text(c, "  " + post[0], 12.5f, Ui.GREEN, true);
            head.addView(author);
            item.addView(head);
            TextView text = body(c, post[1]);
            text.setPadding(0, Ui.dp(c, 8), 0, 0);
            text.setTextColor(0xFFE6E1DA);
            item.addView(text);
            pg.root.addView(item);
        }
        final EditText reply = new EditText(c);
        reply.setHint("Write a reply");
        reply.setSingleLine(true);
        reply.setTextSize(12.5f);
        reply.setTextColor(Ui.WHITE);
        reply.setHintTextColor(Ui.GREY);
        reply.setPadding(Ui.dp(c, 12), 0, Ui.dp(c, 12), 0);
        reply.setBackground(Ui.round(0xFF3B3935, 10, c));
        LinearLayout inputRow = Ui.linear(c, LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams irlp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(c, 52));
        irlp.topMargin = Ui.dp(c, 16);
        inputRow.setLayoutParams(irlp);
        inputRow.addView(reply, new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.MATCH_PARENT, 1f));
        TextView post = Ui.text(c, "Post", 13.5f, Ui.WHITE, true);
        post.setGravity(Gravity.CENTER);
        post.setBackground(Ui.greenFill(c, 10));
        LinearLayout.LayoutParams plp = new LinearLayout.LayoutParams(
                Ui.dp(c, 84), ViewGroup.LayoutParams.MATCH_PARENT);
        plp.setMargins(Ui.dp(c, 8), 0, 0, 0);
        post.setLayoutParams(plp);
        post.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                String text = reply.getText().toString().trim();
                if (text.isEmpty()) return;
                forumPosts.get(topicIdx).add(new String[]{main.state.playerName, text});
                main.navigate("moredetail");
            }
        });
        inputRow.addView(post);
        pg.root.addView(inputRow);
        return pg.scroll;
    }

    // ---------------------------------------------------------------- clubs

    private static final String[][] CLUBS = {
            {"Baku Chess Club", "12,480 members",
             "Azerbaijan's friendliest club - daily arenas and team matches."},
            {"Tactics Addicts", "48,210 members",
             "All tactics, all day. Daily puzzle races and mate-in-1 sprints."},
            {"Weekend Warriors", "9,340 members",
             "Casual rapid arenas every Saturday and Sunday."},
            {"Endgame Lab", "21,760 members",
             "Study rook endings and practise with the community."},
    };

    private static boolean clubJoined(int idx) {
        String s = main.getSharedPreferences("chess_state", Context.MODE_PRIVATE)
                .getString("clubs", "");
        if (!s.isEmpty()) {
            for (String part : s.split(",")) {
                try { if (Integer.parseInt(part) == idx) return true; }
                catch (Exception ignored) { }
            }
        }
        return false;
    }

    private static void toggleClub(int idx) {
        SharedPreferences sp = main.getSharedPreferences("chess_state", Context.MODE_PRIVATE);
        String s = sp.getString("clubs", "");
        HashSet<Integer> set = new HashSet<>();
        if (!s.isEmpty()) {
            for (String part : s.split(",")) {
                try { set.add(Integer.parseInt(part)); } catch (Exception ignored) { }
            }
        }
        if (!set.add(idx)) set.remove(idx);
        StringBuilder sb = new StringBuilder();
        for (int x : set) {
            if (sb.length() > 0) sb.append(',');
            sb.append(x);
        }
        sp.edit().putString("clubs", sb.toString()).apply();
        main.navigate("moredetail");
    }

    private static View clubsScreen() {
        final Context c = main;
        Page pg = newPage("Clubs");
        pg.root.addView(body(c, "Join a club to meet players at your level."));
        for (int i = 0; i < CLUBS.length; i++) {
            final int idx = i;
            LinearLayout item = card(c);
            item.addView(Ui.text(c, CLUBS[i][0], 15.5f, Ui.WHITE, true));
            item.addView(Ui.text(c, CLUBS[i][1], 11.5f, Ui.GREEN, true));
            TextView desc = body(c, CLUBS[i][2]);
            desc.setPadding(0, Ui.dp(c, 6), 0, Ui.dp(c, 10));
            item.addView(desc);
            final boolean joined = clubJoined(i);
            TextView join = Ui.text(c, joined ? "Joined  ✓" : "Join club", 13, Ui.WHITE, true);
            join.setGravity(Gravity.CENTER);
            join.setBackground(joined ? Ui.round(0xFF43403B, 8, c) : Ui.greenFill(c, 8));
            join.setPadding(0, Ui.dp(c, 9), 0, Ui.dp(c, 9));
            join.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) { toggleClub(idx); }
            });
            item.addView(join, new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            pg.root.addView(item);
        }
        return pg.scroll;
    }

    // ---------------------------------------------------------------- leaderboard / league

    private static final String[][] LEADERS = {
            {"Magnus Carlsen", "Norway", "2839"},
            {"Hikaru Nakamura", "USA", "2802"},
            {"Fabiano Caruana", "USA", "2795"},
            {"Alireza Firouzja", "France", "2789"},
            {"Wesley So", "USA", "2778"},
            {"Anish Giri", "Netherlands", "2776"},
            {"Ding Liren", "China", "2774"},
            {"Ian Nepomniachtchi", "Russia", "2770"},
            {"Levon Aronian", "USA", "2765"},
            {"Teimour Radjabov", "Azerbaijan", "2762"},
    };

    private static View leaderboardScreen() {
        final Context c = main;
        Page pg = newPage("Leaderboard");
        pg.root.addView(body(c, "Top players this week."));
        for (int i = 0; i < LEADERS.length; i++) {
            final int idx = i;
            LinearLayout row = Ui.linear(c, LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setBackground(Ui.round(0x1FFFFFFF, 12, c));
            row.setPadding(Ui.dp(c, 14), Ui.dp(c, 11), Ui.dp(c, 14), Ui.dp(c, 11));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.topMargin = Ui.dp(c, 8);
            row.setLayoutParams(lp);
            TextView rank = Ui.text(c, String.valueOf(i + 1), 13, Ui.GREY, true);
            rank.setGravity(Gravity.CENTER);
            row.addView(rank, new LinearLayout.LayoutParams(Ui.dp(c, 30),
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            LinearLayout texts = Ui.linear(c, LinearLayout.VERTICAL);
            LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            tp.setMargins(Ui.dp(c, 10), 0, 0, 0);
            texts.setLayoutParams(tp);
            texts.addView(Ui.text(c, LEADERS[i][0], 13.5f, Ui.WHITE, true));
            texts.addView(Ui.text(c, LEADERS[i][1], 11, Ui.GREY, false));
            row.addView(texts);
            row.addView(Ui.text(c, LEADERS[i][2], 14, Ui.GREEN, true));
            row.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    new AlertDialog.Builder(main)
                            .setTitle(LEADERS[idx][0])
                            .setMessage(LEADERS[idx][1] + "\nRating: " + LEADERS[idx][2])
                            .setPositiveButton("Close", null)
                            .show();
                }
            });
            pg.root.addView(row);
        }
        LinearLayout me = card(c);
        me.setBackground(Ui.round(0x3381B64C, 12, c));
        me.addView(Ui.text(c, "You - rank #1284", 13.5f, Ui.WHITE, true));
        me.addView(Ui.text(c, "Rating " + main.state.userRating, 12.5f, 0xFFCBE5AF, false));
        pg.root.addView(me);
        return pg.scroll;
    }

    private static final String[][] LEAGUE_TIERS = {
            {"Wood", "0 - 800", "8B5A2B"},
            {"Stone", "800 - 1200", "808080"},
            {"Bronze", "1200 - 1600", "CD7F32"},
            {"Silver", "1600 - 1900", "C0C0C0"},
            {"Gold", "1900 - 2200", "D4A017"},
            {"Crystal", "2200 - 2500", "7FD4E8"},
            {"Elite", "2500+", "E02828"},
    };

    private static View leagueScreen() {
        Context c = main;
        Page pg = newPage("Players League");
        pg.root.addView(body(c, "Climb the tiers by winning rated games."));
        LinearLayout progress = card(c);
        progress.addView(Ui.text(c, "Your league: Bronze II", 15.5f, Ui.WHITE, true));
        progress.addView(bar(c, "Progress to Silver", "62%", 0.62f, 0xFFCD7F32));
        pg.root.addView(progress);
        pg.root.addView(section(c, "Tiers"));
        for (String[] t : LEAGUE_TIERS) {
            LinearLayout row = Ui.linear(c, LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(Ui.dp(c, 6), Ui.dp(c, 8), Ui.dp(c, 6), Ui.dp(c, 8));
            int color = (int) Long.parseLong(t[2], 16);
            View dot = new View(c);
            dot.setBackground(Ui.round(0xFF000000 | color, 10, c));
            row.addView(dot, new LinearLayout.LayoutParams(Ui.dp(c, 20), Ui.dp(c, 20)));
            TextView name = Ui.text(c, "  " + t[0], 13.5f, Ui.WHITE, true);
            row.addView(name);
            row.addView(Ui.spacerWeight(c, 1f));
            row.addView(Ui.text(c, t[1], 11.5f, Ui.GREY, false));
            if ("Bronze".equals(t[0])) {
                row.addView(Ui.text(c, "  YOU", 10.5f, Ui.GREEN, true));
            }
            pg.root.addView(row);
        }
        return pg.scroll;
    }

    // ---------------------------------------------------------------- events / news / other apps

    private static View soloIntroScreen() {
        final Context c = main;
        Page pg = newPage("Solo Chess");
        LinearLayout intro = card(c);
        intro.addView(Ui.text(c, "One army, one goal", 15.5f, Ui.WHITE, true));
        TextView d = body(c, "In Solo Chess you play alone with White. Every move must be a "
                + "capture, and you win by capturing every black piece except the king.\n\n"
                + "The black army never moves - solve the capture chain in as few moves as possible.");
        d.setPadding(0, Ui.dp(c, 8), 0, 0);
        intro.addView(d);
        pg.root.addView(intro);
        pg.root.addView(button(c, "Start Solo Chess", true, new View.OnClickListener() {
            @Override public void onClick(View v) {
                sub = "solo:0";
                main.navigate("moredetail");
            }
        }));
        pg.root.addView(button(c, "Back to More", false, new View.OnClickListener() {
            @Override public void onClick(View v) { back(main); }
        }));
        return pg.scroll;
    }

    private static final String[][] NEWS = {
            {"World Rapid Championship starts today",
             "The strongest rapid event of the year begins with 180 players, including four "
                     + "Azerbaijani grandmasters. Follow the games live in Watch.\n\nRounds start "
                     + "at 15:00 local time every day."},
            {"New puzzle rush record",
             "A 13-year-old from Ganja solved 58 puzzles in three minutes, beating the national "
                     + "record by six.\n\nWatch the full attempt in ChessTV."},
            {"Club arena this weekend",
             "Baku Chess Club hosts a 5+0 arena on Saturday. Everyone above 1200 is welcome.\n\n"
                     + "Join the club in More > Clubs to get the invite."},
    };

    private static View newsScreenList() {
        final Context c = main;
        Page pg = newPage("News");
        pg.root.addView(body(c, "What is happening in the chess world."));
        for (int i = 0; i < NEWS.length; i++) {
            final int idx = i;
            LinearLayout row = listRow(c, null, NEWS[i][0], "3 min read",
                    new View.OnClickListener() {
                        @Override public void onClick(View v) {
                            sub = "news:" + idx;
                            main.navigate("moredetail");
                        }
                    });
            pg.root.addView(row);
        }
        return pg.scroll;
    }

    private static View newsScreen(int idx) {
        final Context c = main;
        if (idx < 0 || idx >= NEWS.length) { sub = null; return newsScreenList(); }
        Page pg = newPage("News");
        pg.root.addView(Ui.text(c, NEWS[idx][0], 18, Ui.WHITE, true));
        TextView content = body(c, NEWS[idx][1]);
        content.setTextSize(13);
        content.setTextColor(0xFFDDD8D0);
        content.setPadding(0, Ui.dp(c, 8), 0, 0);
        pg.root.addView(content);
        return pg.scroll;
    }

    private static final String[][] OTHER_APPS = {
            {"Chessable", "Science-based courses and spaced repetition."},
            {"Chess.com Puzzles", "Daily puzzle streaks in a standalone app."},
            {"Aimchess", "Find the weaknesses in your own games."},
    };

    private static View otherAppsScreen() {
        final Context c = main;
        Page pg = newPage("Other Apps");
        pg.root.addView(body(c, "More from the Chess team."));
        for (final String[] app : OTHER_APPS) {
            LinearLayout item = card(c);
            item.addView(Ui.text(c, app[0], 15.5f, Ui.WHITE, true));
            TextView d = body(c, app[1]);
            d.setPadding(0, Ui.dp(c, 6), 0, 0);
            item.addView(d);
            TextView open = Ui.text(c, "Open  ▸", 12.5f, 0xFFA8CD85, true);
            open.setPadding(0, Ui.dp(c, 10), 0, 0);
            item.addView(open);
            item.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    new AlertDialog.Builder(main)
                            .setTitle(app[0])
                            .setMessage(app[1] + "\n\nThis demo build does not link to external apps.")
                            .setPositiveButton("Close", null)
                            .show();
                }
            });
            pg.root.addView(item);
        }
        return pg.scroll;
    }
}
