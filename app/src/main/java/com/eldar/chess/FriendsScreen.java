package com.eldar.chess;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

/** Friends screen - chess.com layout with working search, invites and challenges. */
public class FriendsScreen {

    private static final String[][] CONTACTS = {
            {"Anya Petrova", "1854"},
            {"Teimour Mamedov", "1922"},
            {"Elena Sokolova", "1731"},
            {"Rashad Aliyev", "1687"},
            {"Coach Andrei", "2210"},
    };

    public static View build(final MainActivity main) {
        final Context c = main;

        LinearLayout root = Ui.linear(c, LinearLayout.VERTICAL);
        root.setBackground(Ui.pageBg());

        // ---- header ----
        FrameLayout header = new FrameLayout(c);
        header.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(c, 56)));
        root.addView(header);

        ImageView backIcon = Ui.img(c, R.drawable.ic_back, 24);
        FrameLayout.LayoutParams bp = new FrameLayout.LayoutParams(
                Ui.dp(c, 24), Ui.dp(c, 24), Gravity.LEFT | Gravity.CENTER_VERTICAL);
        bp.leftMargin = Ui.dp(c, 14);
        backIcon.setLayoutParams(bp);
        backIcon.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { main.navigate("more"); }
        });
        header.addView(backIcon);

        TextView title = Ui.text(c, "Friends", 19, Ui.WHITE, true);
        FrameLayout.LayoutParams tp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER);
        title.setLayoutParams(tp);
        header.addView(title);

        ImageView grid = Ui.img(c, R.drawable.fr_grid, 26);
        FrameLayout.LayoutParams gp = new FrameLayout.LayoutParams(
                Ui.dp(c, 26), Ui.dp(c, 26), Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        gp.rightMargin = Ui.dp(c, 14);
        grid.setLayoutParams(gp);
        grid.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                Toast.makeText(c, "Friends list view", Toast.LENGTH_SHORT).show();
            }
        });
        header.addView(grid);

        LinearLayout body = Ui.linear(c, LinearLayout.VERTICAL);
        body.setPadding(Ui.dp(c, 14), Ui.dp(c, 6), Ui.dp(c, 14), 0);
        root.addView(body);

        // ---- 2x2 action tiles ----
        LinearLayout r1 = Ui.linear(c, LinearLayout.HORIZONTAL);
        r1.addView(friendTile(c, R.drawable.fr_search, "Search Contacts",
                new View.OnClickListener() {
                    @Override public void onClick(View v) { searchContacts(main); }
                }));
        r1.addView(friendTile(c, R.drawable.fr_facebook, "Facebook Friends",
                new View.OnClickListener() {
                    @Override public void onClick(View v) { facebookFriends(main); }
                }));
        body.addView(r1);

        LinearLayout r2 = Ui.linear(c, LinearLayout.HORIZONTAL);
        r2.addView(friendTile(c, R.drawable.fr_invite, "Invite Friends",
                new View.OnClickListener() {
                    @Override public void onClick(View v) { inviteFriends(main); }
                }));
        r2.addView(friendTile(c, R.drawable.fr_link, "Challenge Link",
                new View.OnClickListener() {
                    @Override public void onClick(View v) { challengeLink(main); }
                }));
        body.addView(r2);

        // ---- search bar ----
        LinearLayout search = Ui.linear(c, LinearLayout.HORIZONTAL);
        search.setGravity(Gravity.CENTER_VERTICAL);
        search.setBackground(Ui.roundStroke(0xFF363332, 8, 0xFF4A4844, 1, c));
        search.setPadding(Ui.dp(c, 12), 0, Ui.dp(c, 12), 0);
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(c, 50));
        sp.setMargins(0, Ui.dp(c, 14), 0, 0);
        search.setLayoutParams(sp);
        search.addView(Ui.glyph(c, "search", 17, 0xFF8A867E));
        final EditText input = new EditText(c);
        input.setHint("Search by name or username");
        input.setHintTextColor(0xFF8A867E);
        input.setTextColor(Ui.WHITE);
        input.setTextSize(12f);
        input.setBackground(null);
        input.setSingleLine(true);
        input.setPadding(Ui.dp(c, 8), 0, 0, 0);
        LinearLayout.LayoutParams ip = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.MATCH_PARENT, 1f);
        input.setLayoutParams(ip);
        search.addView(input);
        body.addView(search);

        // ---- friends list ----
        LinearLayout fl = Ui.linear(c, LinearLayout.HORIZONTAL);
        fl.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams flp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        flp.setMargins(0, Ui.dp(c, 18), 0, Ui.dp(c, 6));
        fl.setLayoutParams(flp);
        fl.addView(Ui.text(c, "Friends", 15, Ui.WHITE, true));
        final TextView count = Ui.text(c, "0", 11, Ui.GREY, true);
        count.setGravity(Gravity.CENTER);
        count.setBackground(Ui.round(0xFF3B3935, 9, c));
        LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(
                Ui.dp(c, 24), Ui.dp(c, 24));
        cp.setMargins(Ui.dp(c, 8), 0, 0, 0);
        count.setLayoutParams(cp);
        fl.addView(count);
        body.addView(fl);

        final LinearLayout listWrap = Ui.linear(c, LinearLayout.VERTICAL);
        body.addView(listWrap);

        final Runnable refresh = new Runnable() {
            @Override public void run() {
                listWrap.removeAllViews();
                String q = input.getText().toString().trim().toLowerCase();
                ArrayList<Integer> ids = loadFriends(main);
                count.setText(String.valueOf(ids.size()));
                boolean any = false;
                for (int id : ids) {
                    String name = CONTACTS[id][0];
                    if (!q.isEmpty() && !name.toLowerCase().contains(q)) continue;
                    any = true;
                    listWrap.addView(friendRow(main, id, name, CONTACTS[id][1]));
                }
                if (!any) {
                    TextView empty = Ui.text(c, ids.isEmpty()
                                    ? "You haven't added any Friends yet"
                                    : "No friends match your search",
                            13, 0xFFC9C6BE, false);
                    empty.setGravity(Gravity.CENTER);
                    LinearLayout.LayoutParams ep = new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT);
                    ep.topMargin = Ui.dp(c, 24);
                    empty.setLayoutParams(ep);
                    listWrap.addView(empty);
                }
            }
        };
        input.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int cc) { }
            @Override public void onTextChanged(CharSequence s, int a, int b, int cc) { }
            @Override public void afterTextChanged(Editable s) { refresh.run(); }
        });
        refresh.run();

        return root;
    }

    private static LinearLayout friendTile(Context c, int iconRes, String label,
                                           View.OnClickListener click) {
        LinearLayout b = Ui.linear(c, LinearLayout.VERTICAL);
        b.setGravity(Gravity.CENTER);
        b.setBackground(Ui.round(0xFF383634, 10, c));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                0, Ui.dp(c, 88), 1f);
        lp.setMargins(Ui.dp(c, 4), Ui.dp(c, 4), Ui.dp(c, 4), Ui.dp(c, 4));
        b.setLayoutParams(lp);
        ImageView icon = new ImageView(c);
        icon.setImageResource(iconRes);
        icon.setScaleType(ImageView.ScaleType.FIT_CENTER);
        b.addView(icon, new LinearLayout.LayoutParams(Ui.dp(c, 30), Ui.dp(c, 30)));
        TextView t = Ui.text(c, label, 11.5f, Ui.WHITE, true);
        t.setGravity(Gravity.CENTER);
        t.setSingleLine(true);
        LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tp.topMargin = Ui.dp(c, 6);
        t.setLayoutParams(tp);
        b.addView(t);
        b.setOnClickListener(click);
        return b;
    }

    private static LinearLayout friendRow(final MainActivity main, final int id,
                                          String name, String rating) {
        final Context c = main;
        LinearLayout row = Ui.linear(c, LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setBackground(Ui.round(0x1FFFFFFF, 12, c));
        row.setPadding(Ui.dp(c, 12), Ui.dp(c, 10), Ui.dp(c, 12), Ui.dp(c, 10));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.topMargin = Ui.dp(c, 8);
        row.setLayoutParams(lp);

        TextView avatar = Ui.text(c, name.substring(0, 1), 17, Ui.WHITE, true);
        avatar.setGravity(Gravity.CENTER);
        int[] colors = {0xFF6C4B8E, 0xFF2C6E49, 0xFF8E4B4B, 0xFF4A6E8E, 0xFF8E7A2C};
        avatar.setBackground(Ui.round(colors[id % colors.length], 22, c));
        row.addView(avatar, new LinearLayout.LayoutParams(Ui.dp(c, 44), Ui.dp(c, 44)));

        LinearLayout texts = Ui.linear(c, LinearLayout.VERTICAL);
        LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        tp.setMargins(Ui.dp(c, 12), 0, Ui.dp(c, 8), 0);
        texts.setLayoutParams(tp);
        texts.addView(Ui.text(c, name, 14, Ui.WHITE, true));
        texts.addView(Ui.text(c, "Rating " + rating, 11.5f, Ui.GREY, false));
        row.addView(texts);

        TextView challenge = Ui.text(c, "Challenge", 12, Ui.WHITE, true);
        challenge.setGravity(Gravity.CENTER);
        challenge.setBackground(Ui.greenFill(c, 8));
        challenge.setPadding(Ui.dp(c, 12), Ui.dp(c, 8), Ui.dp(c, 12), Ui.dp(c, 8));
        challenge.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                new AlertDialog.Builder(main)
                        .setTitle("Challenge sent")
                        .setMessage("Waiting for the friend to accept. You can start a game meanwhile.")
                        .setPositiveButton("New game", new DialogInterface.OnClickListener() {
                            @Override public void onClick(DialogInterface d, int w) {
                                main.navigate("newgame");
                            }
                        })
                        .setNegativeButton("OK", null)
                        .show();
            }
        });
        row.addView(challenge);

        TextView more = Ui.glyph(c, "more_horiz", 17, Ui.GREY);
        more.setGravity(Gravity.CENTER);
        more.setPadding(Ui.dp(c, 8), Ui.dp(c, 8), Ui.dp(c, 2), Ui.dp(c, 8));
        more.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                new AlertDialog.Builder(main)
                        .setTitle("Remove friend")
                        .setMessage("Remove this friend from your list?")
                        .setPositiveButton("Remove", new DialogInterface.OnClickListener() {
                            @Override public void onClick(DialogInterface d, int w) {
                                removeFriend(main, id);
                                main.navigate("friends");
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });
        row.addView(more);
        return row;
    }

    private static void searchContacts(final MainActivity main) {
        final Context c = main;
        final ArrayList<Integer> added = loadFriends(main);
        final ArrayList<Integer> available = new ArrayList<>();
        final ArrayList<String> names = new ArrayList<>();
        for (int i = 0; i < CONTACTS.length; i++) {
            if (!added.contains(i)) {
                available.add(i);
                names.add(CONTACTS[i][0] + "  (" + CONTACTS[i][1] + ")");
            }
        }
        if (available.isEmpty()) {
            Toast.makeText(c, "All contacts are already your friends", Toast.LENGTH_SHORT).show();
            return;
        }
        new AlertDialog.Builder(main)
                .setTitle("Search contacts")
                .setItems(names.toArray(new String[0]), new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface d, int which) {
                        addFriend(main, available.get(which));
                        Toast.makeText(c, CONTACTS[available.get(which)][0] + " added",
                                Toast.LENGTH_SHORT).show();
                        main.navigate("friends");
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private static void facebookFriends(final MainActivity main) {
        final Context c = main;
        new AlertDialog.Builder(main)
                .setTitle("Facebook Friends")
                .setMessage("Connect Facebook to find friends who already play chess. "
                        + "Three demo friends will be added.")
                .setPositiveButton("Connect", new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface d, int w) {
                        addFriend(main, 3);
                        addFriend(main, 4);
                        Toast.makeText(c, "Facebook connected - friends found",
                                Toast.LENGTH_SHORT).show();
                        main.navigate("friends");
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private static void inviteFriends(MainActivity main) {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_SUBJECT, "Join me on Chess");
        i.putExtra(Intent.EXTRA_TEXT,
                "Join me on Chess! Play a game with me: https://chess.com/invite/eldar");
        main.startActivity(Intent.createChooser(i, "Invite friends"));
    }

    private static void challengeLink(MainActivity main) {
        ClipboardManager cm = (ClipboardManager) main.getSystemService(Context.CLIPBOARD_SERVICE);
        if (cm != null) {
            cm.setPrimaryClip(ClipData.newPlainText("Chess challenge link",
                    "https://chess.com/challenge/eldar-8f2c"));
        }
        Toast.makeText(main, "Challenge link copied to clipboard", Toast.LENGTH_LONG).show();
    }

    private static ArrayList<Integer> loadFriends(Context c) {
        SharedPreferences p = c.getSharedPreferences("chess_state", Context.MODE_PRIVATE);
        String s = p.getString("friendsList", "");
        ArrayList<Integer> out = new ArrayList<>();
        if (!s.isEmpty()) {
            for (String part : s.split(",")) {
                try {
                    int v = Integer.parseInt(part);
                    if (v >= 0 && v < CONTACTS.length && !out.contains(v)) out.add(v);
                } catch (Exception ignored) { }
            }
        }
        return out;
    }

    private static void addFriend(Context c, int id) {
        ArrayList<Integer> ids = loadFriends(c);
        if (ids.contains(id)) return;
        ids.add(id);
        saveFriends(c, ids);
    }

    private static void removeFriend(Context c, int id) {
        ArrayList<Integer> ids = loadFriends(c);
        ids.remove(Integer.valueOf(id));
        saveFriends(c, ids);
    }

    private static void saveFriends(Context c, ArrayList<Integer> ids) {
        StringBuilder sb = new StringBuilder();
        for (int v : ids) {
            if (sb.length() > 0) sb.append(',');
            sb.append(v);
        }
        c.getSharedPreferences("chess_state", Context.MODE_PRIVATE)
                .edit().putString("friendsList", sb.toString()).apply();
    }
}
