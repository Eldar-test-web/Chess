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
        ImageView bin = Ui.img(c, R.drawable.watch_bino, 19);
        bin.setScaleType(ImageView.ScaleType.FIT_CENTER);
        top.addView(bin);
        TextView wt = Ui.text(c, "Watch", 15, Ui.WHITE, true);
        LinearLayout.LayoutParams wtp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        wtp.setMargins(Ui.dp(c, 4), 0, 0, 0);
        wt.setLayoutParams(wtp);
        top.addView(wt);
        root.addView(top);

        ScrollView scroll = new ScrollView(c);
        scroll.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));
        LinearLayout body = Ui.linear(c, LinearLayout.VERTICAL);
        body.setPadding(Ui.dp(c, 35), Ui.dp(c, 3), Ui.dp(c, 33), Ui.dp(c, 10));
        scroll.addView(body, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        root.addView(scroll);

        // ---- Events card ----
        LinearLayout events = Ui.linear(c, LinearLayout.VERTICAL);
        events.setBackground(Ui.round(0xFF1C1A1A, 7, c));
        events.setPadding(Ui.dp(c, 10), Ui.dp(c, 10), Ui.dp(c, 11), Ui.dp(c, 5));
        body.addView(events, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        LinearLayout eh = Ui.linear(c, LinearLayout.HORIZONTAL);
        eh.setGravity(Gravity.CENTER_VERTICAL);
        ImageView trophy = Ui.img(c, R.drawable.watch_trophy, 17);
        trophy.setScaleType(ImageView.ScaleType.FIT_CENTER);
        eh.addView(trophy);
        TextView et = Ui.text(c, "  Events Today", 12, Ui.WHITE, true);
        eh.addView(et);
        eh.addView(Ui.spacerWeight(c, 1f));
        ImageView chev = Ui.img(c, R.drawable.ic_chevron_right, 12);
        chev.setColorFilter(0xFF8A8780);
        eh.addView(chev);
        events.addView(eh, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(c, 37.3f)));

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
        news.setBackground(Ui.round(0xFF1C1A1A, 7, c));
        news.setPadding(Ui.dp(c, 10), Ui.dp(c, 4), Ui.dp(c, 11), Ui.dp(c, 10));
        LinearLayout.LayoutParams np = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        np.setMargins(0, Ui.dp(c, 11), 0, 0);
        news.setLayoutParams(np);
        body.addView(news);

        LinearLayout nh = Ui.linear(c, LinearLayout.HORIZONTAL);
        nh.setGravity(Gravity.CENTER_VERTICAL);
        ImageView newsIcon = Ui.img(c, R.drawable.watch_news, 16);
        nh.addView(newsIcon);
        TextView nt = Ui.text(c, "  News", 12, Ui.WHITE, true);
        LinearLayout.LayoutParams ntp = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        nt.setLayoutParams(ntp);
        nh.addView(nt);
        ImageView chev2 = Ui.img(c, R.drawable.ic_chevron_right, 12);
        chev2.setColorFilter(0xFF8A8780);
        nh.addView(chev2);
        news.addView(nh, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(c, 28.7f)));

        ImageView photo = new ImageView(c);
        photo.setImageResource(R.drawable.news_photo);
        photo.setScaleType(ImageView.ScaleType.FIT_XY);
        float cardW = c.getResources().getDisplayMetrics().widthPixels
                / c.getResources().getDisplayMetrics().density - 35 - 33 - 10 - 11;
        LinearLayout.LayoutParams php = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(c, (int) (cardW * 0.5636f)));
        php.setMargins(-Ui.dp(c, 10), Ui.dp(c, 4), -Ui.dp(c, 11), 0);
        news.addView(photo, php);

        TextView h1 = Ui.text(c, "Firouzja, Giri Lead Total Chess Semifinals; Carlsen Forfeits Final Game",
                13.2f, Ui.WHITE, true);
        h1.setPadding(0, Ui.dp(c, 12), 0, 0);
        news.addView(h1);

        TextView body1 = Ui.text(c, "GM Alireza Firouzja started with a 4-0 blitz victory against GM Fabiano Caruana, lost that lead in a single rapid game, but ultimately won the day 12-8 as the grueling Semifinals of the Total Chess World Cha...",
                8f, Ui.GREY, false);
        body1.setPadding(0, Ui.dp(c, 4), 0, 0);
        news.addView(body1);

        TextView h2 = Ui.text(c, "Tabatabaei Win Streak Earns Second Titled Tuesday", 11, Ui.WHITE, true);
        h2.setSingleLine(true);
        h2.setEllipsize(android.text.TextUtils.TruncateAt.END);
        h2.setPadding(0, Ui.dp(c, 26), 0, 0);
        news.addView(h2);

        TextView h3 = Ui.text(c, "Streaks Are Freezing - From November 4 To 23, So You Can Enjoy Their Uninterrupted Chess Fun!",
                10.5f, 0xFFB5B0A8, true);
        h3.setPadding(0, Ui.dp(c, 12), 0, 0);
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
        row.setPadding(0, Ui.dp(c, 6), 0, Ui.dp(c, 6));
        row.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        ImageView logo = new ImageView(c);
        logo.setImageResource(logoRes);
        logo.setScaleType(ImageView.ScaleType.CENTER_CROP);
        row.addView(logo, new LinearLayout.LayoutParams(Ui.dp(c, 47), Ui.dp(c, 28.7f)));

        LinearLayout info = Ui.linear(c, LinearLayout.VERTICAL);
        LinearLayout.LayoutParams ip = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        ip.setMargins(Ui.dp(c, 11), 0, 0, 0);
        info.setLayoutParams(ip);
        TextView t = Ui.text(c, title, 10, Ui.WHITE, true);
        t.setIncludeFontPadding(false);
        info.addView(t);
        TextView d = Ui.text(c, dates, 8, Ui.GREY, false);
        d.setIncludeFontPadding(false);
        d.setPadding(0, Ui.dp(c, 2), 0, 0);
        info.addView(d);
        row.addView(info);
        return row;
    }
}
