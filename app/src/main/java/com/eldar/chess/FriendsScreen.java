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
        r1.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(c, 44)));
        r1.addView(friendBtn(c, R.drawable.icon_search_contacts, "Search Contacts", 1f));
        Ui.spacer(c, Ui.dp(c, 8), 1);
        r1.addView(friendBtn(c, R.drawable.icon_fb, "Facebook Friends", 1.4f));
        body.addView(r1);
        Ui.spacer(c, 1, Ui.dp(c, 8));
        LinearLayout r2 = Ui.linear(c, LinearLayout.HORIZONTAL);
        r2.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(c, 44)));
        r2.addView(friendBtn(c, R.drawable.icon_invite, "Invite Friends", 1f));
        Ui.spacer(c, Ui.dp(c, 8), 1);
        r2.addView(friendBtn(c, R.drawable.icon_link, "Send Challenge Link", 1.4f));
        body.addView(r2);

        Ui.spacer(c, 1, Ui.dp(c, 14));

        // search bar
        LinearLayout search = Ui.linear(c, LinearLayout.HORIZONTAL);
        search.setGravity(Gravity.CENTER_VERTICAL);
        search.setBackground(Ui.round(0xFF2E2C29, 8, c));
        search.setPadding(Ui.dp(c, 10), 0, Ui.dp(c, 10), 0);
        search.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(c, 44)));
        ImageView mag = new ImageView(c);
        mag.setImageResource(R.drawable.icon_search_bar);
        mag.setLayoutParams(new LinearLayout.LayoutParams(Ui.dp(c, 18), Ui.dp(c, 18)));
        search.addView(mag);
        EditText input = new EditText(c);
        input.setHint("Search by name or username");
        input.setHintTextColor(0xFF8A867E);
        input.setTextColor(Ui.WHITE);
        input.setTextSize(13);
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
        fl.addView(Ui.text(c, "Friends", 15, Ui.WHITE, true));
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
        TextView empty = Ui.text(c, "You haven't added any Friends yet", 14, 0xFFC9C6BE, false);
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

    private static LinearLayout friendBtn(final android.content.Context c, int iconRes,
                                          String label, float weight) {
        LinearLayout b = Ui.linear(c, LinearLayout.HORIZONTAL);
        b.setGravity(Gravity.CENTER);
        b.setBackground(Ui.round(0xFF3B3935, 8, c));
        b.setLayoutParams(new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.MATCH_PARENT, weight));
        ImageView icon = new ImageView(c);
        icon.setImageResource(iconRes);
        icon.setLayoutParams(new LinearLayout.LayoutParams(Ui.dp(c, 20), Ui.dp(c, 20)));
        b.addView(icon);
        b.addView(Ui.text(c, " " + label, 14, Ui.WHITE, true));
        b.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                Toast.makeText(c, "Coming soon", Toast.LENGTH_SHORT).show();
            }
        });
        return b;
    }
}
