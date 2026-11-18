package com.eldar.chess;

import android.app.AlertDialog;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/** New Game screen - 1:1 with screenshots 072117 / 072125. */
public class NewGameScreen {

    public static final String[] TIMES = {"1 min", "3 min", "5 min", "10 min", "15 min", "30 min"};

    public static View build(final MainActivity main) {
        final android.content.Context c = main;
        final long[] selectedMs = {600000};
        final String[] selectedLabel = {"10 min"};

        LinearLayout root = Ui.linear(c, LinearLayout.VERTICAL);
        root.setBackgroundColor(Ui.BG);

        // top bar
        LinearLayout top = Ui.linear(c, LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);
        top.setPadding(Ui.dp(c, 8), Ui.dp(c, 8), Ui.dp(c, 8), Ui.dp(c, 4));

        ImageView back = Ui.img(c, R.drawable.ic_back, 22);
        back.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { main.navigate("home"); }
        });
        top.addView(back);

        LinearLayout title = Ui.linear(c, LinearLayout.HORIZONTAL);
        title.setGravity(Gravity.CENTER);
        title.setLayoutParams(new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        TextView hand = Ui.glyph(c, "touch_app", 16, 0xFFD5D2CC);
        hand.setLayoutParams(new LinearLayout.LayoutParams(Ui.dp(c, 18), Ui.dp(c, 18)));
        title.addView(hand);
        TextView tt = Ui.text(c, "New Game", 13, Ui.WHITE, true);
        LinearLayout.LayoutParams ttp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ttp.setMargins(Ui.dp(c, 4), 0, 0, 0);
        tt.setLayoutParams(ttp);
        title.addView(tt);
        top.addView(title);
        top.addView(Ui.spacer(c, Ui.dp(c, 22), 1));
        root.addView(top);

        LinearLayout body = Ui.linear(c, LinearLayout.VERTICAL);
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        bp.setMargins(Ui.dp(c, 47), Ui.dp(c, 4), Ui.dp(c, 47), 0);
        body.setLayoutParams(bp);

        // ---- time pill ----
        final LinearLayout timePill = Ui.linear(c, LinearLayout.HORIZONTAL);
        timePill.setGravity(Gravity.CENTER_VERTICAL);
        timePill.setBackground(Ui.round(0xFF403D39, 6, c));
        LinearLayout.LayoutParams tpp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(c, 37));
        tpp.setMargins(0, Ui.dp(c, 6), 0, 0);
        timePill.setLayoutParams(tpp);
        timePill.setPadding(Ui.dp(c, 10), 0, Ui.dp(c, 10), 0);

        TextView sw = Ui.glyph(c, "timer", 15, 0xFFD5D2CC);
        sw.setLayoutParams(new LinearLayout.LayoutParams(Ui.dp(c, 16), Ui.dp(c, 16)));
        timePill.addView(sw);
        final TextView timeText = Ui.text(c, "  10 min", 12, Ui.WHITE, true);
        timePill.addView(timeText);
        timePill.addView(Ui.spacerWeight(c, 1f));
        ImageView chev = Ui.img(c, R.drawable.ic_chevron_down, 12);
        chev.setColorFilter(0xFFA8A29A);
        timePill.addView(chev);
        timePill.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                showTimeDialog(main, selectedLabel, selectedMs, timeText);
            }
        });
        body.addView(timePill);

        // ---- Start Game ----
        TextView start = Ui.text(c, "Start Game", 13, Ui.WHITE, true);
        start.setGravity(Gravity.CENTER);
        start.setBackground(Ui.round(Ui.GREEN, 6, c));
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(c, 37));
        sp.setMargins(0, Ui.dp(c, 10), 0, 0);
        start.setLayoutParams(sp);
        start.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                startGame(main, "online", selectedMs[0]);
            }
        });
        body.addView(start);

        // ---- option rows ----
        body.addView(optionRow(c, "emoji_events", "Tournaments", new Runnable() {
            @Override public void run() {
                toast(c, "Tournaments are not available yet");
            }
        }));
        body.addView(optionRow(c, "person", "Play a Friend", new Runnable() {
            @Override public void run() { startGame(main, "passplay", selectedMs[0]); }
        }));
        body.addView(optionRow(c, "smart_toy", "Play Bots", new Runnable() {
            @Override public void run() { startGame(main, "bots", selectedMs[0]); }
        }));
        body.addView(optionRow(c, "psychology", "Play Coach", new Runnable() {
            @Override public void run() { startGame(main, "coach", selectedMs[0]); }
        }));

        // ---- expandable section ----
        final LinearLayout extra = Ui.linear(c, LinearLayout.VERTICAL);
        extra.setVisibility(View.GONE);
        extra.addView(optionRow(c, "tune", "Custom Game", new Runnable() {
            @Override public void run() { showTimeDialog(main, selectedLabel, selectedMs, timeText); }
        }));
        extra.addView(optionRow(c, "groups", "Play in Person", new Runnable() {
            @Override public void run() { startGame(main, "passplay", selectedMs[0]); }
        }));

        final ImageView chevBig = Ui.img(c, R.drawable.ic_chevron_down, 18);
        chevBig.setColorFilter(0xFF8A8780);
        LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, Ui.dp(c, 22));
        cp.setMargins(0, Ui.dp(c, 6), 0, 0);
        cp.gravity = Gravity.CENTER_HORIZONTAL;
        chevBig.setLayoutParams(cp);
        chevBig.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                boolean show = extra.getVisibility() != View.VISIBLE;
                extra.setVisibility(show ? View.VISIBLE : View.GONE);
                chevBig.setImageResource(show ? R.drawable.ic_chevron_up : R.drawable.ic_chevron_down);
            }
        });
        body.addView(chevBig);
        body.addView(extra);

        root.addView(body);
        return root;
    }

    private static void toast(final android.content.Context c, String s) {
        android.widget.Toast.makeText(c, s, android.widget.Toast.LENGTH_SHORT).show();
    }

    private static LinearLayout optionRow(android.content.Context c, String glyphName,
                                          String label, final Runnable action) {
        LinearLayout row = Ui.linear(c, LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER);
        row.setBackground(Ui.round(0xFF403D39, 6, c));
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(c, 37.5f));
        p.setMargins(0, Ui.dp(c, 10), 0, 0);
        row.setLayoutParams(p);

        TextView icon = Ui.glyph(c, glyphName, 15, 0xFFDAD7D2);
        icon.setLayoutParams(new LinearLayout.LayoutParams(Ui.dp(c, 16), Ui.dp(c, 16)));
        row.addView(icon);
        row.addView(Ui.text(c, "  " + label, 12, Ui.WHITE, true));

        row.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { action.run(); }
        });
        return row;
    }

    private static void showTimeDialog(final MainActivity main, final String[] label,
                                       final long[] ms, final TextView timeText) {
        final android.content.Context c = main;
        AlertDialog.Builder b = new AlertDialog.Builder(c, R.style.DarkDialog);
        final String[] items = TIMES;
        b.setTitle("Select time").setItems(items, new android.content.DialogInterface.OnClickListener() {
            @Override public void onClick(android.content.DialogInterface d, int which) {
                label[0] = items[which];
                ms[0] = Long.parseLong(items[which].split(" ")[0]) * 60000;
                timeText.setText("  " + items[which]);
                d.dismiss();
            }
        }).show();
    }

    /** persona key: online | bots | coach | passplay */
    public static void startGame(MainActivity main, String kind, long timeMs) {
        GameSession.Persona persona;
        boolean passPlay = false;
        switch (kind) {
            case "bots":
                // Marko thinks slowly and rarely grabs material
                persona = new GameSession.Persona("Marko", 1123, 2, 90,
                        false, 1600, 3400, 0.22, 0.35);
                break;
            case "coach":
                persona = new GameSession.Persona("David", 847, 1, 200,
                        true, 1100, 2400, 0.18, 0.30);
                break;
            case "passplay":
                persona = new GameSession.Persona("Friend", main.state.userRating, 0, 0, false);
                passPlay = true;
                break;
            default:
                // quick, grabby online opponent (screenshots 072139 / 072146)
                persona = new GameSession.Persona("codon201100", 1472, 3, 25,
                        false, 700, 1500, 0.35, 0.55);
        }
        GameScreen.pendingPersona = persona;
        GameScreen.pendingTime = timeMs;
        GameScreen.pendingPassPlay = passPlay;
        GameScreen.pendingUserBlack = !passPlay && "online".equals(kind);
        main.navigate("game");
    }
}
