package com.eldar.chess;

import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

/** More screen - 1:1 with screenshots 072029 / 072038. */
public class MoreScreen {

    private static final String[][] SECTIONS = {
            {"Account", "💎|Membership|premium", "👤|Profile", "📊|Stats", "🎨|Theme",
                    "⭐|Awards", "👥|Friends|friends", "✉️|Messages", "⚙️|Settings", "❓|Support"},
            {"Learn", "🔍|Analysis", "🧭|Explorer", "♟️|Endgames", "🎯|Practice",
                    "💡|Insights", "🤸|Vision", "▶️|Videos", "♚|Solo Chess"},
            {"Connect", "🏆|Events", "🏅|Leaderboard", "🛡️|Players League", "📺|ChessTV|tv",
                    "📰|Articles", "👀|Watch|watch", "💬|Forums", "🚩|Clubs", "📰|News", "♟️|Other Apps"}
    };

    public static View build(final MainActivity main) {
        final android.content.Context c = main;

        ScrollView scroll = new ScrollView(c);
        scroll.setBackgroundColor(Ui.BG);
        LinearLayout root = Ui.linear(c, LinearLayout.VERTICAL);
        root.setPadding(0, Ui.dp(c, 10), 0, Ui.dp(c, 10));
        scroll.addView(root, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        LinearLayout logoWrap = Ui.linear(c, LinearLayout.HORIZONTAL);
        logoWrap.setGravity(Gravity.CENTER);
        logoWrap.setPadding(0, Ui.dp(c, 6), 0, Ui.dp(c, 4));
        logoWrap.addView(Ui.logo(c, 14, 12));
        root.addView(logoWrap);
        root.addView(Ui.spacer(c, 1, Ui.dp(c, 18)));

        // Try Premium banner
        LinearLayout banner = Ui.linear(c, LinearLayout.HORIZONTAL);
        banner.setGravity(Gravity.CENTER_VERTICAL);
        banner.setBackground(Ui.round(0xFF1C7FC4, 6, c));
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(c, 38));
        bp.setMargins(Ui.dp(c, 10), 0, Ui.dp(c, 10), 0);
        banner.setLayoutParams(bp);
        banner.setPadding(Ui.dp(c, 12), 0, Ui.dp(c, 12), 0);
        TextView gem = Ui.text(c, "💎", 14, Ui.WHITE, false);
        banner.addView(gem);
        LinearLayout bt = Ui.linear(c, LinearLayout.VERTICAL);
        bt.setLayoutParams(new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        ((LinearLayout.LayoutParams) bt.getLayoutParams()).setMargins(Ui.dp(c, 10), 0, 0, 0);
        bt.addView(Ui.text(c, "Try Premium", 12, Ui.WHITE, true));
        bt.addView(Ui.text(c, "Play your best chess!", 9, 0xFFD8EDFA, false));
        banner.addView(bt);
        banner.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { main.navigate("premium"); }
        });
        root.addView(banner);

        for (String[] section : SECTIONS) {
            TextView label = Ui.text(c, section[0], 10.7f, Ui.GREY, true);
            label.setPadding(Ui.dp(c, 16), Ui.dp(c, 14), 0, Ui.dp(c, 4));
            root.addView(label);

            for (int i = 1; i < section.length; i++) {
                final String[] parts = section[i].split("\\|");
                LinearLayout row = Ui.linear(c, LinearLayout.HORIZONTAL);
                row.setGravity(Gravity.CENTER_VERTICAL);
                row.setPadding(Ui.dp(c, 16), 0, Ui.dp(c, 16), 0);
                row.setLayoutParams(new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(c, 34)));

                TextView icon = Ui.text(c, parts[0], 13, Ui.WHITE, false);
                icon.setGravity(Gravity.CENTER);
                row.addView(icon, new LinearLayout.LayoutParams(
                        Ui.dp(c, 26), Ui.dp(c, 26)));
                TextView t = Ui.text(c, "  " + parts[1], 10.7f, Ui.WHITE, true);
                row.addView(t);
                row.addView(Ui.spacerWeight(c, 1f));
                if (parts.length > 2 && parts[2].equals("tv")) {
                    TextView live = Ui.text(c, "LIVE", 8, Ui.WHITE, true);
                    live.setBackground(Ui.round(0xFFD0342C, 3, c));
                    live.setPadding(Ui.dp(c, 5), Ui.dp(c, 1), Ui.dp(c, 5), Ui.dp(c, 1));
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

        return scroll;
    }
}
