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

        LinearLayout top = Ui.linear(c, LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);
        top.setPadding(Ui.dp(c, 8), Ui.dp(c, 10), Ui.dp(c, 8), Ui.dp(c, 4));
        ImageView back = Ui.img(c, R.drawable.ic_back, 20);
        back.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { main.navigate("home"); }
        });
        top.addView(back);
        TextView title = Ui.text(c, "Friends", 17, Ui.WHITE, true);
        title.setLayoutParams(new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        title.setGravity(Gravity.CENTER);
        top.addView(title);
        ImageView grid = Ui.img(c, R.drawable.ic_grid, 18);
        grid.setColorFilter(0xFF8A8780);
        grid.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                Toast.makeText(c, "Grid view", Toast.LENGTH_SHORT).show();
            }
        });
        top.addView(grid);
        root.addView(top);

        LinearLayout body = Ui.linear(c, LinearLayout.VERTICAL);
        body.setPadding(Ui.dp(c, 10), Ui.dp(c, 8), Ui.dp(c, 10), 0);
        root.addView(body);

        // 2x2 buttons
        LinearLayout r1 = Ui.linear(c, LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams r1p = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(c, 32));
        r1p.setMargins(0, 0, 0, Ui.dp(c, 6));
        r1.setLayoutParams(r1p);
        LinearLayout b1 = friendBtn(c, "person_search", "Search Contacts");
        LinearLayout.LayoutParams b1p = (LinearLayout.LayoutParams) b1.getLayoutParams();
        b1p.setMargins(0, 0, Ui.dp(c, 6), 0);
        r1.addView(b1);
        r1.addView(friendBtn(c, "facebook", "Facebook Friends"));
        body.addView(r1);
        LinearLayout r2 = Ui.linear(c, LinearLayout.HORIZONTAL);
        r2.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(c, 32)));
        LinearLayout b3 = friendBtn(c, "person_add", "Invite Friends");
        LinearLayout.LayoutParams b3p = (LinearLayout.LayoutParams) b3.getLayoutParams();
        b3p.setMargins(0, 0, Ui.dp(c, 6), 0);
        r2.addView(b3);
        r2.addView(friendBtn(c, "link", "Send Challenge Link"));
        body.addView(r2);

        body.addView(Ui.spacer(c, 1, Ui.dp(c, 10)));

        // search bar
        LinearLayout search = Ui.linear(c, LinearLayout.HORIZONTAL);
        search.setGravity(Gravity.CENTER_VERTICAL);
        search.setBackground(Ui.roundStroke(0xFF2E2C29, 6, 0xFF4A4844, 1, c));
        search.setPadding(Ui.dp(c, 8), 0, Ui.dp(c, 10), 0);
        search.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(c, 30)));
        TextView mag = Ui.glyph(c, "search", 13, 0xFF8A867E);
        mag.setLayoutParams(new LinearLayout.LayoutParams(Ui.dp(c, 14), Ui.dp(c, 14)));
        search.addView(mag);
        EditText input = new EditText(c);
        input.setHint("Search by name or username");
        input.setHintTextColor(0xFF8A867E);
        input.setTextColor(Ui.WHITE);
        input.setTextSize(11);
        input.setBackground(null);
        input.setSingleLine(true);
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
        flp.setMargins(0, Ui.dp(c, 14), 0, 0);
        fl.setLayoutParams(flp);
        fl.addView(Ui.text(c, "Friends", 13, Ui.WHITE, true));
        TextView count = Ui.text(c, "0", 11, Ui.GREY, false);
        count.setBackground(Ui.round(0xFF3B3935, 8, c));
        count.setPadding(Ui.dp(c, 7), Ui.dp(c, 1), Ui.dp(c, 7), Ui.dp(c, 1));
        LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        cp.setMargins(Ui.dp(c, 8), 0, 0, 0);
        count.setLayoutParams(cp);
        fl.addView(count);
        body.addView(fl);

        // empty state
        TextView empty = Ui.text(c, "You haven't added any Friends yet", 12, 0xFFC9C6BE, false);
        empty.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams ep = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(c, 120));
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

    private static LinearLayout friendBtn(final android.content.Context c, String glyphName,
                                          String label) {
        LinearLayout b = Ui.linear(c, LinearLayout.HORIZONTAL);
        b.setGravity(Gravity.CENTER);
        b.setBackground(Ui.round(0xFF3B3935, 6, c));
        b.setLayoutParams(new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.MATCH_PARENT, 1f));
        TextView icon = Ui.glyph(c, glyphName, 14, 0xFFD5D2CC);
        icon.setLayoutParams(new LinearLayout.LayoutParams(Ui.dp(c, 15), Ui.dp(c, 15)));
        b.addView(icon);
        b.addView(Ui.text(c, " " + label, 10, Ui.WHITE, true));
        b.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                Toast.makeText(c, "Coming soon", Toast.LENGTH_SHORT).show();
            }
        });
        return b;
    }
}
