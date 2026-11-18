package com.eldar.chess;

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

/** Friends screen - 1:1 with screenshot 072242. */
public class FriendsScreen {

    public static View build(final MainActivity main) {
        final android.content.Context c = main;

        LinearLayout root = Ui.linear(c, LinearLayout.VERTICAL);
        root.setBackgroundColor(Ui.BG);

        // ---- header (same compact style as Premium) ----
        FrameLayout header = new FrameLayout(c);
        header.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(c, 10)));
        header.setClipChildren(false);
        root.addView(header);

        ImageView back = Ui.img(c, R.drawable.ic_back, 19);
        FrameLayout.LayoutParams bp = new FrameLayout.LayoutParams(
                Ui.dp(c, 19), Ui.dp(c, 19), Gravity.LEFT | Gravity.CENTER_VERTICAL);
        bp.leftMargin = Ui.dp(c, 10);
        back.setLayoutParams(bp);
        back.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { main.navigate("home"); }
        });
        header.addView(back);

        TextView title = Ui.text(c, "Friends", 10, Ui.WHITE, true);
        title.setGravity(Gravity.CENTER);
        title.setIncludeFontPadding(false);
        FrameLayout.LayoutParams tp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(c, 17), Gravity.CENTER);
        title.setLayoutParams(tp);
        header.addView(title);

        ImageView grid = Ui.img(c, R.drawable.fr_grid, 18);
        FrameLayout.LayoutParams gp = new FrameLayout.LayoutParams(
                Ui.dp(c, 18), Ui.dp(c, 18), Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        gp.rightMargin = Ui.dp(c, 11);
        gp.topMargin = Ui.dp(c, 2);
        grid.setLayoutParams(gp);
        grid.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                Toast.makeText(c, "Grid view", Toast.LENGTH_SHORT).show();
            }
        });
        header.addView(grid);

        // ---- body ----
        LinearLayout body = Ui.linear(c, LinearLayout.VERTICAL);
        body.setPadding(Ui.dp(c, 12), Ui.dp(c, 22), Ui.dp(c, 12), 0);
        root.addView(body);

        // 2x2 buttons
        LinearLayout r1 = Ui.linear(c, LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams r1p = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(c, 33.4f));
        r1p.setMargins(0, 0, 0, Ui.dp(c, 5.4f));
        r1.setLayoutParams(r1p);
        LinearLayout b1 = friendBtn(c, R.drawable.fr_search, "Search Contacts", 19);
        LinearLayout.LayoutParams b1p = (LinearLayout.LayoutParams) b1.getLayoutParams();
        b1p.setMargins(0, 0, Ui.dp(c, 5.4f), 0);
        r1.addView(b1);
        r1.addView(friendBtn(c, R.drawable.fr_facebook, "Facebook Friends", 19));
        body.addView(r1);

        LinearLayout r2 = Ui.linear(c, LinearLayout.HORIZONTAL);
        r2.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(c, 34)));
        LinearLayout b3 = friendBtn(c, R.drawable.fr_invite, "Invite Friends", 18);
        LinearLayout.LayoutParams b3p = (LinearLayout.LayoutParams) b3.getLayoutParams();
        b3p.setMargins(0, 0, Ui.dp(c, 5.4f), 0);
        r2.addView(b3);
        r2.addView(friendBtn(c, R.drawable.fr_link, "Send Challenge Link", 25));
        body.addView(r2);

        // search bar
        LinearLayout search = Ui.linear(c, LinearLayout.HORIZONTAL);
        search.setGravity(Gravity.CENTER_VERTICAL);
        search.setBackground(Ui.roundStroke(0xFF363332, 6, 0xFF4A4844, 1, c));
        search.setPadding(Ui.dp(c, 8), 0, Ui.dp(c, 10), 0);
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(c, 26.8f));
        sp.setMargins(0, Ui.dp(c, 10), 0, 0);
        search.setLayoutParams(sp);
        TextView mag = Ui.glyph(c, "search", 11, 0xFF8A867E);
        mag.setLayoutParams(new LinearLayout.LayoutParams(Ui.dp(c, 12), Ui.dp(c, 12)));
        search.addView(mag);
        EditText input = new EditText(c);
        input.setHint("Search by name or username");
        input.setHintTextColor(0xFF8A867E);
        input.setTextColor(Ui.WHITE);
        input.setTextSize(9f);
        input.setBackground(null);
        input.setSingleLine(true);
        input.setPadding(0, 0, 0, 0);
        LinearLayout.LayoutParams ip = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        ip.setMargins(Ui.dp(c, 8), 0, 0, 0);
        input.setLayoutParams(ip);
        search.addView(input);
        body.addView(search);

        // Friends label + count
        LinearLayout fl = Ui.linear(c, LinearLayout.HORIZONTAL);
        fl.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams flp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        flp.setMargins(0, Ui.dp(c, 11), 0, 0);
        fl.setLayoutParams(flp);
        fl.addView(Ui.text(c, "Friends", 9f, Ui.WHITE, true));
        TextView count = Ui.text(c, "0", 8, Ui.GREY, false);
        count.setGravity(Gravity.CENTER);
        count.setBackground(Ui.round(0xFF3B3935, 8, c));
        LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(
                Ui.dp(c, 11), Ui.dp(c, 11));
        cp.setMargins(Ui.dp(c, 8), 0, 0, 0);
        count.setLayoutParams(cp);
        fl.addView(count);
        body.addView(fl);

        // empty state
        TextView empty = Ui.text(c, "You haven't added any Friends yet", 10, 0xFFC9C6BE, false);
        empty.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams ep = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ep.setMargins(0, Ui.dp(c, 20), 0, 0);
        empty.setLayoutParams(ep);
        body.addView(empty);

        input.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int cc) { }
            @Override public void onTextChanged(CharSequence s, int a, int b, int cc) { }
            @Override public void afterTextChanged(Editable s) {
                Toast.makeText(c, s.length() == 0 ? "Search friends" : "No results for " + s,
                        Toast.LENGTH_SHORT).show();
            }
        });

        return root;
    }

    private static LinearLayout friendBtn(final android.content.Context c, int iconRes,
                                          String label, int iconDp) {
        LinearLayout b = Ui.linear(c, LinearLayout.HORIZONTAL);
        b.setGravity(Gravity.CENTER);
        b.setBackground(Ui.round(0xFF383634, 6, c));
        b.setLayoutParams(new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.MATCH_PARENT, 1f));
        ImageView icon = Ui.img(c, iconRes, iconDp);
        b.addView(icon);
        TextView t = Ui.text(c, " " + label, 11, Ui.WHITE, true);
        t.setSingleLine(true);
        b.addView(t);
        b.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                Toast.makeText(c, "Coming soon", Toast.LENGTH_SHORT).show();
            }
        });
        return b;
    }
}
