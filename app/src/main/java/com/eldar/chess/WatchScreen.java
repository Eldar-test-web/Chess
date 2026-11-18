package com.eldar.chess;

import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

/** Watch screen - 1:1 with screenshot 072021. */
public class WatchScreen {

    public static View build(final MainActivity main) {
        final android.content.Context c = main;

        LinearLayout root = Ui.linear(c, LinearLayout.VERTICAL);
        root.setBackgroundColor(Ui.BG);

        // title
        LinearLayout top = Ui.linear(c, LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER);
        top.setPadding(0, Ui.dp(c, 12), 0, Ui.dp(c, 8));
        ImageView bin = new ImageView(c);
        bin.setImageResource(R.drawable.icon_watch_title);
        bin.setLayoutParams(new LinearLayout.LayoutParams(Ui.dp(c, 26), Ui.dp(c, 22)));
        top.addView(bin);
        top.addView(Ui.text(c, "  Watch", 17, Ui.WHITE, true));
        root.addView(top);

        ScrollView scroll = new ScrollView(c);
        scroll.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));
        LinearLayout body = Ui.linear(c, LinearLayout.VERTICAL);
        body.setPadding(Ui.dp(c, 10), Ui.dp(c, 4), Ui.dp(c, 10), Ui.dp(c, 10));
        scroll.addView(body, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        root.addView(scroll);

        // ---- Events card ----
        LinearLayout events = Ui.linear(c, LinearLayout.VERTICAL);
        events.setBackground(Ui.round(Ui.CARD, 8, c));
        events.setPadding(Ui.dp(c, 10), Ui.dp(c, 10), Ui.dp(c, 10), Ui.dp(c, 4));
        body.addView(events, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        LinearLayout eh = Ui.linear(c, LinearLayout.HORIZONTAL);
        eh.setGravity(Gravity.CENTER_VERTICAL);
        TextView trophy = Ui.text(c, "🏆", 18, Ui.WHITE, false);
        eh.addView(trophy);
        TextView et = Ui.text(c, "  Events Today", 15, Ui.WHITE, true);
        eh.addView(et);
        Ui.spacerWeight(c, 1f);
        ImageView chev = Ui.img(c, R.drawable.ic_chevron_right, 16);
        chev.setColorFilter(0xFF8A8780);
        eh.addView(chev);
        events.addView(eh);

        events.addView(eventRow(c, R.drawable.logo_titled, "Titled Tuesday 2026",
                "Jun 2, 2026 - Dec 30, 2026"));
        events.addView(eventRow(c, R.drawable.logo_total, "Total Chess World Championship Tour Pilot 2026",
                "Nov 10, 2026 - Nov 21, 2026"));
        events.addView(eventRow(c, R.drawable.logo_club, "Chess Club Showdown 2026",
                "Sep 30, 2026 - Dec 23, 2026"));
        events.addView(eventRow(c, R.drawable.logo_speed, "Speed Chess Championship XI",
                "Nov 16, 2026 - Feb 7, 2027"));

        // ---- News card ----
        LinearLayout news = Ui.linear(c, LinearLayout.VERTICAL);
        news.setBackground(Ui.round(Ui.CARD, 8, c));
        news.setPadding(Ui.dp(c, 10), Ui.dp(c, 10), Ui.dp(c, 10), Ui.dp(c, 12));
        LinearLayout.LayoutParams np = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        np.setMargins(0, Ui.dp(c, 12), 0, 0);
        news.setLayoutParams(np);
        body.addView(news);

        LinearLayout nh = Ui.linear(c, LinearLayout.HORIZONTAL);
        nh.setGravity(Gravity.CENTER_VERTICAL);
        TextView newsIcon = Ui.text(c, "📰", 18, Ui.WHITE, false);
        nh.addView(newsIcon);
        nh.addView(Ui.text(c, "  News", 15, Ui.WHITE, true));
        Ui.spacerWeight(c, 1f);
        ImageView chev2 = Ui.img(c, R.drawable.ic_chevron_right, 16);
        chev2.setColorFilter(0xFF8A8780);
        nh.addView(chev2);
        news.addView(nh);

        ImageView photo = new ImageView(c);
        photo.setImageResource(R.drawable.news_photo);
        photo.setScaleType(ImageView.ScaleType.FIT_XY);
        photo.setBackground(Ui.round(0xFF000000, 8, c));
        news.addView(photo, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(c, 250)));

        TextView h1 = Ui.text(c, "Firouzja, Giri Lead Total Chess Semifinals; Carlsen Forfeits Final Game",
                16, Ui.WHITE, true);
        h1.setPadding(0, Ui.dp(c, 12), 0, Ui.dp(c, 6));
        news.addView(h1);

        TextView body1 = Ui.text(c, "GM Alireza Firouzja started with a 4-0 blitz victory against GM Fabiano Caruana, lost that lead in a single rapid game, but ultimately won the day 12-8 as the grueling Semifinals of the Total Chess World Cha...",
                13, Ui.GREY, false);
        news.addView(body1);

        TextView h2 = Ui.text(c, "Tabatabaei Win Streak Earns Second Titled Tuesday", 15, Ui.WHITE, true);
        h2.setPadding(0, Ui.dp(c, 14), 0, 0);
        news.addView(h2);

        TextView h3 = Ui.text(c, "Streaks Are Freezing - From November 4 To 23, So You Can Enjoy Their Uninterrupted Chess Fun!",
                14, 0xFFB5B0A8, true);
        h3.setPadding(0, Ui.dp(c, 14), 0, 0);
        news.addView(h3);

        // clicks
        View.OnClickListener comingSoon = new View.OnClickListener() {
            @Override public void onClick(View v) {
                Toast.makeText(c, "Opening soon", Toast.LENGTH_SHORT).show();
            }
        };
        events.setOnClickListener(comingSoon);
        news.setOnClickListener(comingSoon);

        return root;
    }

    private static LinearLayout eventRow(android.content.Context c, int logoRes,
                                         String title, String dates) {
        LinearLayout row = Ui.linear(c, LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, Ui.dp(c, 8), 0, Ui.dp(c, 8));

        ImageView logo = new ImageView(c);
        logo.setImageResource(logoRes);
        logo.setScaleType(ImageView.ScaleType.FIT_XY);
        row.addView(logo, new LinearLayout.LayoutParams(Ui.dp(c, 58), Ui.dp(c, 52)));

        LinearLayout info = Ui.linear(c, LinearLayout.VERTICAL);
        LinearLayout.LayoutParams ip = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        ip.setMargins(Ui.dp(c, 10), 0, 0, 0);
        info.setLayoutParams(ip);
        TextView t = Ui.text(c, title, 14, Ui.WHITE, true);
        info.addView(t);
        TextView d = Ui.text(c, dates, 12, Ui.GREY, false);
        d.setPadding(0, Ui.dp(c, 3), 0, 0);
        info.addView(d);
        row.addView(info);
        return row;
    }
}
