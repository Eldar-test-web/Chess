package com.eldar.chess;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

/** More screen - 1:1 with screenshots 072029 / 072038. */
public class MoreScreen {

    // {section label, item, item, ...}; item = "icon|label|action"
    private static final String[][] SECTIONS = {
            {"Account",
                    "membership|Membership|premium", "profile|Profile", "stats|Stats",
                    "theme|Theme", "awards|Awards", "friends|Friends|friends",
                    "messages|Messages", "settings|Settings", "support|Support"},
            {"Learn",
                    "analysis|Analysis", "explorer|Explorer", "endgames|Endgames",
                    "practice|Practice", "insights|Insights", "vision|Vision",
                    "videos|Videos", "solochess|Solo Chess"},
            {"Connect",
                    "events|Events", "leaderboard|Leaderboard", "playersleague|Players League",
                    "chesstv|ChessTV|tv", "articles|Articles", "watch|Watch|watch",
                    "forums|Forums", "clubs|Clubs", "news|News", "otherapps|Other Apps"}
    };

    public static View build(final MainActivity main) {
        final Context c = main;

        ScrollView scroll = new ScrollView(c);
        scroll.setBackgroundColor(0xFF262421);
        LinearLayout root = Ui.linear(c, LinearLayout.VERTICAL);
        scroll.addView(root, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        // ---- logo ----
        LinearLayout logoWrap = Ui.linear(c, LinearLayout.HORIZONTAL);
        logoWrap.setGravity(Gravity.CENTER);
        logoWrap.setPadding(0, Ui.dp(c, 22), 0, 0);
        logoWrap.addView(Ui.logo(c, 22, 16));
        root.addView(logoWrap);
        root.addView(Ui.spacer(c, 1, Ui.dp(c, 16)));

        // ---- Try Premium banner ----
        LinearLayout banner = Ui.linear(c, LinearLayout.HORIZONTAL);
        banner.setGravity(Gravity.CENTER_VERTICAL);
        GradientDrawable bg = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{0xFF0070B1, 0xFF0085C3});
        bg.setCornerRadius(Ui.dp(c, 7));
        bg.setCornerRadius(Ui.dp(c, 10));
        banner.setBackground(bg);
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(c, 96));
        bp.setMargins(Ui.dp(c, 16), 0, Ui.dp(c, 16), 0);
        banner.setLayoutParams(bp);
        banner.setPadding(Ui.dp(c, 14), 0, Ui.dp(c, 14), 0);

        ImageView gem = new ImageView(c);
        gem.setImageResource(R.drawable.more_gem);
        gem.setScaleType(ImageView.ScaleType.FIT_CENTER);
        banner.addView(gem, new LinearLayout.LayoutParams(
                Ui.dp(c, 60), Ui.dp(c, 56)));
        LinearLayout bt = Ui.linear(c, LinearLayout.VERTICAL);
        bt.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams btp = new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        btp.setMargins(Ui.dp(c, 10), 0, 0, 0);
        bt.setLayoutParams(btp);
        bt.addView(Ui.text(c, "Try Premium", 20, Ui.WHITE, true));
        bt.addView(Ui.text(c, "Play your best chess!", 12, 0xFFCFE8F8, false));
        banner.addView(bt);
        banner.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { main.navigate("premium"); }
        });
        root.addView(banner);

        // ---- sections ----
        for (String[] section : SECTIONS) {
            TextView label = Ui.text(c, section[0], 13.5f, 0xFFF0F0F0, true);
            label.setPadding(Ui.dp(c, 24), Ui.dp(c, 30), 0, Ui.dp(c, 10));
            root.addView(label);

            for (int i = 1; i < section.length; i++) {
                final String[] parts = section[i].split("\\|");
                LinearLayout row = Ui.linear(c, LinearLayout.HORIZONTAL);
                row.setGravity(Gravity.CENTER_VERTICAL);
                row.setPadding(Ui.dp(c, 24), 0, Ui.dp(c, 24), 0);
                row.setLayoutParams(new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(c, 68)));

                int res = c.getResources().getIdentifier(
                        "more_" + parts[0], "drawable", c.getPackageName());
                ImageView icon = new ImageView(c);
                icon.setImageResource(res);
                icon.setScaleType(ImageView.ScaleType.FIT_XY);
                row.addView(icon, new LinearLayout.LayoutParams(
                        Ui.dp(c, 32), Ui.dp(c, 32)));

                TextView t = Ui.text(c, parts[1], 16, 0xFFF0EFED, true);
                LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                tp.setMargins(Ui.dp(c, 16), 0, 0, 0);
                t.setLayoutParams(tp);
                row.addView(t);
                row.addView(Ui.spacerWeight(c, 1f));
                if (parts.length > 2 && parts[2].equals("tv")) {
                    TextView live = Ui.text(c, "LIVE", 9, Ui.WHITE, true);
                    live.setGravity(Gravity.CENTER);
                    live.setBackground(Ui.round(0xFFE02828, 3, c));
                    live.setPadding(Ui.dp(c, 6), Ui.dp(c, 2), Ui.dp(c, 6), Ui.dp(c, 2));
                    row.addView(live);
                }

                row.setOnClickListener(new View.OnClickListener() {
                    @Override public void onClick(View v) {
                        if (parts.length > 2) {
                            if (parts[2].equals("premium")) main.navigate("premium");
                            else if (parts[2].equals("friends")) main.navigate("friends");
                            else if (parts[2].equals("watch")) main.navigate("watch");
                            else Toast.makeText(c, "ChessTV is live! Opening soon",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(c, parts[1] + " - coming soon", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                root.addView(row);
            }
        }
        root.addView(Ui.spacer(c, 1, Ui.dp(c, 24)));

        return scroll;
    }
}
