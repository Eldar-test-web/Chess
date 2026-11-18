package com.eldar.chess;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
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

        // ---- logo (full-width wrapper so the group really sits in the middle) ----
        LinearLayout logoWrap = Ui.linear(c, LinearLayout.HORIZONTAL);
        logoWrap.setGravity(Gravity.CENTER);
        logoWrap.setPadding(0, Ui.dp(c, 12), 0, 0);
        logoWrap.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        logoWrap.addView(Ui.logo(c, 20, 11.5f));
        root.addView(logoWrap);
        root.addView(Ui.spacer(c, 1, Ui.dp(c, 12)));

        // ---- Try Premium banner ----
        LinearLayout banner = Ui.linear(c, LinearLayout.HORIZONTAL);
        banner.setGravity(Gravity.CENTER_VERTICAL);
        GradientDrawable bg = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{0xFF0070B1, 0xFF0085C3});
        bg.setCornerRadius(Ui.dp(c, 10));
        banner.setBackground(bg);
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        bp.setMargins(Ui.dp(c, 16), 0, Ui.dp(c, 16), 0);
        banner.setLayoutParams(bp);
        banner.setMinimumHeight(Ui.dp(c, 78));
        banner.setPadding(Ui.dp(c, 10), Ui.dp(c, 4), Ui.dp(c, 14), Ui.dp(c, 4));

        View gem = new GemView(c);
        banner.addView(gem, new LinearLayout.LayoutParams(
                Ui.dp(c, 54), Ui.dp(c, 52)));
        LinearLayout bt = Ui.linear(c, LinearLayout.VERTICAL);
        bt.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams btp = new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        btp.setMargins(Ui.dp(c, 10), 0, 0, 0);
        bt.setLayoutParams(btp);
        bt.addView(Ui.text(c, "Try Premium", 16, Ui.WHITE, true));
        bt.addView(Ui.text(c, "Play your best chess!", 9, 0xFFCFE8F8, false));
        banner.addView(bt);
        banner.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { main.navigate("premium"); }
        });
        root.addView(banner);

        // ---- sections ----
        for (String[] section : SECTIONS) {
            TextView label = Ui.text(c, section[0], 11.5f, 0xFFF0F0F0, true);
            label.setSingleLine(true);
            label.setPadding(Ui.dp(c, 12), Ui.dp(c, 14), Ui.dp(c, 16), Ui.dp(c, 2));
            root.addView(label);

            for (int i = 1; i < section.length; i++) {
                final String[] parts = section[i].split("\\|");
                LinearLayout row = Ui.linear(c, LinearLayout.HORIZONTAL);
                row.setGravity(Gravity.CENTER_VERTICAL);
                row.setPadding(Ui.dp(c, 16), 0, Ui.dp(c, 16), 0);
                row.setMinimumHeight(Ui.dp(c, 54));
                row.setLayoutParams(new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                int res = c.getResources().getIdentifier(
                        "more_" + parts[0], "drawable", c.getPackageName());
                ImageView icon = new ImageView(c);
                icon.setImageResource(res);
                icon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                row.addView(icon, new LinearLayout.LayoutParams(
                        Ui.dp(c, 24), Ui.dp(c, 24)));

                TextView t = Ui.text(c, parts[1], 10.5f, 0xFFF0EFED, true);
                t.setSingleLine(true);
                LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                tp.setMargins(Ui.dp(c, 14), 0, 0, 0);
                t.setLayoutParams(tp);
                row.addView(t);
                row.addView(Ui.spacerWeight(c, 1f));
                if (parts.length > 2 && parts[2].equals("tv")) {
                    TextView live = Ui.text(c, "LIVE", 7.5f, Ui.WHITE, true);
                    live.setGravity(Gravity.CENTER);
                    live.setBackground(Ui.round(0xFFE02828, 3, c));
                    live.setPadding(Ui.dp(c, 5), Ui.dp(c, 1), Ui.dp(c, 5), Ui.dp(c, 1));
                    row.addView(live);
                }

                row.setOnClickListener(new View.OnClickListener() {
                    @Override public void onClick(View v) {
                        if (parts.length > 2) {
                            String act = parts[2];
                            if (act.equals("premium")) { main.navigate("premium"); return; }
                            if (act.equals("friends")) { main.navigate("friends"); return; }
                            if (act.equals("watch")) { main.navigate("watch"); return; }
                        }
                        MoreDetailScreen.open(main, parts[0]);
                    }
                });
                root.addView(row);
            }
        }
        root.addView(Ui.spacer(c, 1, Ui.dp(c, 24)));

        return scroll;
    }

    /** Crisp faceted gem drawn in code, replacing the opaque bitmap. */
    private static class GemView extends View {
        private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Path body = new Path();
        private final Path facet = new Path();

        GemView(Context c) { super(c); }

        @Override protected void onDraw(Canvas cv) {
            float w = getWidth(), h = getHeight();
            if (w <= 0 || h <= 0) return;
            float cx = w / 2f;
            float top = h * 0.10f, bottom = h * 0.94f;
            float lx = w * 0.04f, rx = w * 0.96f;
            float tableY = h * 0.30f;
            float tl = w * 0.26f, tr = w * 0.74f;

            body.reset();
            body.moveTo(tl, top);
            body.lineTo(tr, top);
            body.lineTo(rx, tableY);
            body.lineTo(cx, bottom);
            body.lineTo(lx, tableY);
            body.close();
            p.setStyle(Paint.Style.FILL);
            p.setShader(new LinearGradient(lx, top, rx, bottom,
                    0xFF8FE6F8, 0xFF2E9BD3, Shader.TileMode.CLAMP));
            cv.drawPath(body, p);
            p.setShader(null);

            facet.reset();
            facet.moveTo(tl + w * 0.03f, top + h * 0.02f);
            facet.lineTo(tr - w * 0.03f, top + h * 0.02f);
            facet.lineTo(cx + w * 0.13f, tableY);
            facet.lineTo(cx - w * 0.13f, tableY);
            facet.close();
            p.setColor(0xFFC9F4FF);
            cv.drawPath(facet, p);

            facet.reset();
            facet.moveTo(cx - w * 0.13f, tableY);
            facet.lineTo(cx + w * 0.13f, tableY);
            facet.lineTo(cx, bottom - h * 0.04f);
            facet.close();
            p.setColor(0xFF5CC3E9);
            cv.drawPath(facet, p);

            facet.reset();
            facet.moveTo(lx + w * 0.02f, tableY);
            facet.lineTo(cx - w * 0.13f, tableY);
            facet.lineTo(cx, bottom - h * 0.04f);
            facet.close();
            p.setColor(0xFF49B4E0);
            cv.drawPath(facet, p);

            facet.reset();
            facet.moveTo(cx + w * 0.13f, tableY);
            facet.lineTo(rx - w * 0.02f, tableY);
            facet.lineTo(cx, bottom - h * 0.04f);
            facet.close();
            p.setColor(0xFF7AD7F1);
            cv.drawPath(facet, p);

            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(Math.max(1.5f, w * 0.018f));
            p.setColor(0xFFFFFFFF);
            cv.drawPath(body, p);

            drawSparkle(cv, w * 0.16f, h * 0.14f, w * 0.10f);
            drawSparkle(cv, w * 0.86f, h * 0.78f, w * 0.055f);
            drawSparkle(cv, w * 0.22f, h * 0.72f, w * 0.035f);
            p.setStyle(Paint.Style.FILL);
        }

        private void drawSparkle(Canvas cv, float cx, float cy, float r) {
            Path s = new Path();
            s.moveTo(cx, cy - r);
            s.quadTo(cx + r * 0.18f, cy - r * 0.18f, cx + r, cy);
            s.quadTo(cx + r * 0.18f, cy + r * 0.18f, cx, cy + r);
            s.quadTo(cx - r * 0.18f, cy + r * 0.18f, cx - r, cy);
            s.quadTo(cx - r * 0.18f, cy - r * 0.18f, cx, cy - r);
            s.close();
            p.setStyle(Paint.Style.FILL);
            p.setColor(0xFFFFFFFF);
            cv.drawPath(s, p);
        }
    }
}
