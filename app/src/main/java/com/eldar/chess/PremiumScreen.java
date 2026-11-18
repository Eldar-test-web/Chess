package com.eldar.chess;

import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

/** Premium paywall - 1:1 with screenshots 072258 / 072307 / 072315 / 072320. */
public class PremiumScreen {

    static class Plan {
        String name; String subtitle; int subtitleColor;
        int border; int iconRes;
        String[] features; int[] featIdx;
        String priceOld; String priceYearly; String yearlyTotal;
        float featTop; boolean yearlyOnly; String members;

        Plan(String name, String subtitle, int subtitleColor, int border, int iconRes,
             String[] features, int[] featIdx, String priceOld, String priceYearly,
             String yearlyTotal, float featTop, boolean yearlyOnly, String members) {
            this.name = name; this.subtitle = subtitle; this.subtitleColor = subtitleColor;
            this.border = border; this.iconRes = iconRes; this.features = features;
            this.featIdx = featIdx; this.priceOld = priceOld; this.priceYearly = priceYearly;
            this.yearlyTotal = yearlyTotal; this.featTop = featTop;
            this.yearlyOnly = yearlyOnly; this.members = members;
        }
    }

    private static final String[] FULL_FEAT = {"Unlimited Game Review", "Unlimited Move Explanations",
            "Advanced Stats", "Unlimited Puzzles", "Unlimited Lessons", "Unlimited Coach Games",
            "Unlock All Bots", "No Ads"};
    private static final int[] FULL_IDX = {0, 1, 2, 3, 4, 5, 6, 7};
    private static final int[] PLAT_IDX = {0, 3, 4, 5, 6, 7};
    private static final int[] GOLD_IDX = {3, 4, 5, 6, 7};

    private static Plan[] plans() {
        return new Plan[]{
                new Plan("Diamond", "MOST POPULAR", 0xFF009FD9, 0xFF009FD9,
                        R.drawable.prem_icon_diamond, FULL_FEAT, FULL_IDX,
                        "AZN 13.58", "AZN 7.51", "AZN 90.08", 90.1f, false, null),
                new Plan("Platinum", "", 0, 0xFF7FA8C9,
                        R.drawable.prem_icon_platinum,
                        new String[]{"Unlimited Game Review", "Unlimited Puzzles",
                                "Unlimited Lessons", "Unlimited Coach Games", "Unlock All Bots",
                                "No Ads"}, PLAT_IDX,
                        "AZN 8.48", "AZN 4.96", "AZN 59.48", 98.3f, false, null),
                new Plan("Gold", "", 0, 0xFFE2A924,
                        R.drawable.prem_icon_gold,
                        new String[]{"Unlimited Puzzles", "Unlimited Lessons",
                                "Unlimited Coach Games", "Unlock All Bots", "No Ads"}, GOLD_IDX,
                        "AZN 5.08", "AZN 3.12", "AZN 37.38", 107.0f, false, null),
                new Plan("Friends & Family", "SAVE 72%", 0xFF81B64C, 0xFF81B64C,
                        R.drawable.prem_icon_friends, FULL_FEAT, FULL_IDX,
                        "", "AZN 12.61", "AZN 151.28", 103.3f, true, "2\u20136 members")
        };
    }

    public static View build(final MainActivity main) {
        final android.content.Context c = main;

        LinearLayout root = Ui.linear(c, LinearLayout.VERTICAL);
        root.setBackgroundColor(Ui.BG);

        // header row (24dp): centred title, back arrow left
        FrameLayout header = new FrameLayout(c);
        root.addView(header, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(c, 10)));
        header.setClipChildren(false);
        TextView title = Ui.text(c, "Unlock Your Potential", 15.5f, Ui.WHITE, true);
        title.setGravity(Gravity.CENTER);
        title.setIncludeFontPadding(false);
        header.addView(title, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(c, 17),
                Gravity.CENTER));
        ImageView back = new ImageView(c);
        back.setImageResource(R.drawable.ic_back);
        FrameLayout.LayoutParams bp = new FrameLayout.LayoutParams(
                Ui.dp(c, 11), Ui.dp(c, 11), Gravity.START | Gravity.TOP);
        bp.leftMargin = Ui.dp(c, 7);
        bp.topMargin = Ui.dp(c, 1);
        header.addView(back, bp);
        back.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { main.navigate("home"); }
        });

        final ViewPager2 pager = new ViewPager2(c);
        pager.setAdapter(new PlanAdapter(main));
        pager.setClipToPadding(false);
        root.addView(pager, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        // page dots: 4 cells 10.8dp apart, active #E0E0DF 6.8dp, idle 4dp
        final LinearLayout dots = Ui.linear(c, LinearLayout.HORIZONTAL);
        dots.setGravity(Gravity.CENTER);
        root.addView(dots, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(c, 20)));
        for (int i = 0; i < 4; i++) {
            FrameLayout cell = new FrameLayout(c);
            cell.setLayoutParams(new LinearLayout.LayoutParams(Ui.dp(c, 10.8f), Ui.dp(c, 10.8f)));
            View d = new View(c);
            int sz = Ui.dp(c, i == 0 ? 6.8f : 4);
            cell.addView(d, new FrameLayout.LayoutParams(sz, sz, Gravity.CENTER));
            dots.addView(cell);
        }
        styleDots(dots, 0, c);

        pager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override public void onPageSelected(int position) {
                styleDots(dots, position, c);
            }
        });
        return root;
    }

    private static void styleDots(LinearLayout dots, int active, android.content.Context c) {
        for (int i = 0; i < dots.getChildCount(); i++) {
            FrameLayout cell = (FrameLayout) dots.getChildAt(i);
            cell.removeAllViews();
            View d = new View(c);
            int sz = Ui.dp(c, i == active ? 6.8f : 4);
            d.setBackground(Ui.round(i == active ? 0xFFE0E0DF : 0xFF444140, 4, c));
            cell.addView(d, new FrameLayout.LayoutParams(sz, sz, Gravity.CENTER));
        }
    }

    static class PlanAdapter extends RecyclerView.Adapter<PlanHolder> {
        MainActivity main;
        Plan[] plans = plans();

        PlanAdapter(MainActivity main) { this.main = main; }

        @NonNull @Override public PlanHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            FrameLayout page = new FrameLayout(main);
            page.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            page.setClipChildren(false);
            return new PlanHolder(page, main);
        }

        @Override public void onBindViewHolder(@NonNull PlanHolder holder, int position) {
            holder.bind(plans[position], position, plans);
        }

        @Override public int getItemCount() { return plans.length; }
    }

    static class PlanHolder extends RecyclerView.ViewHolder {
        FrameLayout page; MainActivity main;

        PlanHolder(FrameLayout page, MainActivity main) {
            super(page);
            this.page = page;
            this.main = main;
        }

        void bind(final Plan plan, int position, Plan[] all) {
            final android.content.Context c = main;
            page.removeAllViews();

            if (position > 0) {
                // right edge of the previous card peeking in from the left (072307)
                FrameLayout sliver = new FrameLayout(c);
                sliver.setClipChildren(false);
                View fill = new View(c);
                android.graphics.drawable.GradientDrawable sliverBg =
                        new android.graphics.drawable.GradientDrawable();
                sliverBg.setColor(0xFF3A3937);
                float r = Ui.dp(c, 8);
                sliverBg.setCornerRadii(new float[]{0, 0, r, r, r, r, 0, 0});
                fill.setBackground(sliverBg);
                sliver.addView(fill, new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                View stroke = new View(c);
                android.graphics.drawable.GradientDrawable sg =
                        new android.graphics.drawable.GradientDrawable();
                sg.setColor(0x00000000);
                sg.setCornerRadii(new float[]{0, 0, r, r, r, r, 0, 0});
                sg.setStroke(Math.max(1, Ui.dp(c, 1.4f)), all[position - 1].border);
                stroke.setBackground(sg);
                sliver.addView(stroke, new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                FrameLayout.LayoutParams slp = new FrameLayout.LayoutParams(
                        Ui.dp(c, 24.6f), Ui.dp(c, 416));
                slp.setMargins(Ui.dp(c, 31.2f), Ui.dp(c, 105.4f), 0, 0);
                page.addView(sliver, slp);
            }

            FrameLayout card = new FrameLayout(c);
            card.setClipChildren(false);
            GradientDrawable cardBg = new GradientDrawable();
            cardBg.setColor(0xFF3A3937);
            cardBg.setCornerRadius(Ui.dp(c, 8));
            cardBg.setStroke(Math.max(1, Ui.dp(c, 1.4f)), plan.border);
            card.setBackground(cardBg);
            FrameLayout.LayoutParams cw = new FrameLayout.LayoutParams(
                    Ui.dp(c, 260.5f), Ui.dp(c, 416));
            cw.setMargins(Ui.dp(c, 69.9f), Ui.dp(c, 105.4f), 0, 0);
            page.addView(card, cw);

            // inner column: left 50dp / right 14dp -> button width 196.5dp
            FrameLayout col = new FrameLayout(c);
            col.setClipChildren(false);
            col.setPadding(Ui.dp(c, 50), 0, Ui.dp(c, 14), 0);
            card.addView(col, new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

            TextView name = Ui.text(c, plan.name, 15, Ui.WHITE, true);
            name.setGravity(Gravity.CENTER);
            name.setIncludeFontPadding(false);
            placeTop(col, name, 21.2f, ViewGroup.LayoutParams.WRAP_CONTENT, c);

            if (!plan.subtitle.isEmpty()) {
                TextView sub = Ui.text(c, plan.subtitle, 8, plan.subtitleColor, true);
                sub.setGravity(Gravity.CENTER);
                sub.setIncludeFontPadding(false);
                sub.setLetterSpacing(0.03f);
                placeTop(col, sub, 41.3f, ViewGroup.LayoutParams.WRAP_CONTENT, c);
            }

            if (plan.members != null) {
                LinearLayout row = Ui.linear(c, LinearLayout.HORIZONTAL);
                row.setGravity(Gravity.CENTER_VERTICAL);
                ImageView mi = new ImageView(c);
                mi.setImageResource(R.drawable.prem_members);
                row.addView(mi, new LinearLayout.LayoutParams(Ui.dp(c, 12.2f), Ui.dp(c, 10.9f)));
                TextView mt = Ui.text(c, plan.members, 9, 0xFFAAA8A5, false);
                mt.setIncludeFontPadding(false);
                LinearLayout.LayoutParams mp = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                mp.leftMargin = Ui.dp(c, 4);
                row.addView(mt, mp);
                FrameLayout.LayoutParams rp = new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, Ui.dp(c, 12),
                        Gravity.TOP | Gravity.CENTER_HORIZONTAL);
                rp.topMargin = Ui.dp(c, 54.5f);
                col.addView(row, rp);
            }

            // feature list: left-aligned column, icon x = card+82.9dp
            LinearLayout feats = Ui.linear(c, LinearLayout.VERTICAL);
            FrameLayout.LayoutParams fp = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                    Gravity.TOP | Gravity.CENTER_HORIZONTAL);
            fp.topMargin = Ui.dp(c, plan.featTop);
            col.addView(feats, fp);
            for (int i = 0; i < plan.features.length; i++) {
                LinearLayout fr = Ui.linear(c, LinearLayout.HORIZONTAL);
                fr.setGravity(Gravity.CENTER_VERTICAL);
                fr.setLayoutParams(new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, Ui.dp(c, 17.3f)));
                ImageView fi = new ImageView(c);
                fi.setImageResource(Res.feat(plan.featIdx[i]));
                fr.addView(fi, new LinearLayout.LayoutParams(
                        Ui.dp(c, 13.6f), Ui.dp(c, 13.6f)));
                TextView ft = Ui.text(c, plan.features[i], 9, Ui.WHITE, true);
                ft.setIncludeFontPadding(false);
                ft.setSingleLine(true);
                LinearLayout.LayoutParams ftp = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                ftp.leftMargin = Ui.dp(c, 4);
                fr.addView(ft, ftp);
                feats.addView(fr);
            }

            if (!plan.priceOld.isEmpty()) {
                TextView old = Ui.text(c, plan.priceOld + " / month", 7.5f, 0xFFA8A7A5, false);
                old.setPaintFlags(old.getPaintFlags()
                        | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
                old.setGravity(Gravity.CENTER);
                old.setIncludeFontPadding(false);
                placeTop(col, old, 267.5f, ViewGroup.LayoutParams.WRAP_CONTENT, c);
            }

            TextView price = Ui.text(c, plan.priceYearly + " / month", 11.3f, 0xFF4DC3EA, true);
            price.setGravity(Gravity.CENTER);
            price.setIncludeFontPadding(false);
            placeTop(col, price, 277.5f, ViewGroup.LayoutParams.WRAP_CONTENT, c);

            TextView total = Ui.text(c, "12 months \u2022 " + plan.yearlyTotal, 11, Ui.WHITE, true);
            total.setGravity(Gravity.CENTER);
            total.setIncludeFontPadding(false);
            placeTop(col, total, 290.5f, ViewGroup.LayoutParams.WRAP_CONTENT, c);

            if (plan.yearlyOnly) {
                TextView chip = Ui.text(c, "YEARLY PLAN ONLY", 7.5f, 0xFF9A9793, true);
                chip.setLetterSpacing(0.05f);
                chip.setSingleLine(true);
                chip.setGravity(Gravity.CENTER);
                chip.setIncludeFontPadding(false);
                chip.setBackground(Ui.round(0xFF3B3935, 5, c));
                FrameLayout.LayoutParams cp = new FrameLayout.LayoutParams(
                        Ui.dp(c, 78.8f), Ui.dp(c, 22.4f),
                        Gravity.TOP | Gravity.CENTER_HORIZONTAL);
                cp.topMargin = Ui.dp(c, 310.9f);
                col.addView(chip, cp);
            } else {
                LinearLayout toggle = Ui.linear(c, LinearLayout.HORIZONTAL);
                toggle.setBackground(Ui.round(0xFF363434, 5, c));
                FrameLayout.LayoutParams tp = new FrameLayout.LayoutParams(
                        Ui.dp(c, 108.6f), Ui.dp(c, 22.4f),
                        Gravity.TOP | Gravity.CENTER_HORIZONTAL);
                tp.topMargin = Ui.dp(c, 310.9f);
                col.addView(toggle, tp);

                TextView yearly = Ui.text(c, "Yearly", 9.5f, Ui.WHITE, true);
                yearly.setGravity(Gravity.CENTER);
                yearly.setIncludeFontPadding(false);
                yearly.setBackground(Ui.round(0xFF413E3E, 5, c));
                toggle.addView(yearly, new LinearLayout.LayoutParams(
                        Ui.dp(c, 54.3f), ViewGroup.LayoutParams.MATCH_PARENT));
                TextView monthly = Ui.text(c, "Monthly", 9.5f, 0xFF9A9793, false);
                monthly.setGravity(Gravity.CENTER);
                monthly.setIncludeFontPadding(false);
                toggle.addView(monthly, new LinearLayout.LayoutParams(
                        0, ViewGroup.LayoutParams.MATCH_PARENT, 1f));

                yearly.setOnClickListener(new View.OnClickListener() {
                    @Override public void onClick(View v) {
                        Toast.makeText(c, "Yearly plan selected", Toast.LENGTH_SHORT).show();
                    }
                });
                monthly.setOnClickListener(new View.OnClickListener() {
                    @Override public void onClick(View v) {
                        Toast.makeText(c, "Monthly plan selected", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            TextView tryBtn = Ui.text(c, "Try for AZN 0.00", 13.5f, Ui.WHITE, true);
            tryBtn.setGravity(Gravity.CENTER);
            tryBtn.setIncludeFontPadding(false);
            tryBtn.setBackground(Ui.round(0xFF0090D3, 6, c));
            FrameLayout.LayoutParams tp2 = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(c, 33));
            tp2.topMargin = Ui.dp(c, 344.2f);
            col.addView(tryBtn, tp2);
            tryBtn.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    Toast.makeText(c, "This is a demo - enjoy your free trial!",
                            Toast.LENGTH_SHORT).show();
                }
            });

            // fine print: two single lines, clipped by the card edge like the SS
            FrameLayout fineWrap = new FrameLayout(c);
            fineWrap.setClipChildren(true);
            FrameLayout.LayoutParams fwp = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(c, 20));
            fwp.topMargin = Ui.dp(c, 389.2f);
            card.addView(fineWrap, fwp);
            LinearLayout lines = Ui.linear(c, LinearLayout.VERTICAL);
            lines.setClipChildren(true);
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                    Gravity.START | Gravity.TOP);
            lp.leftMargin = Ui.dp(c, 32.6f);
            fineWrap.addView(lines, lp);
            String[] fineLines = {
                    "You will be billed at the end of your free trial and your Premium membership will renew",
                    "automatically unless you cancel beforehand. You may cancel anytime in Google Play."};
            for (String line : fineLines) {
                TextView ft = Ui.text(c, line, 6, 0xFF8A8780, false);
                ft.setSingleLine(true);
                ft.setIncludeFontPadding(false);
                ft.setGravity(Gravity.START);
                lines.addView(ft);
            }

            // plan icon overlapping the top border
            ImageView icon = new ImageView(c);
            icon.setImageResource(plan.iconRes);
            FrameLayout.LayoutParams ip = new FrameLayout.LayoutParams(
                    Ui.dp(c, 40.7f), Ui.dp(c, 50.9f));
            ip.leftMargin = Ui.dp(c, 127.6f);
            ip.topMargin = -Ui.dp(c, 27.1f);
            card.addView(icon, ip);
        }

        private static void placeTop(FrameLayout col, View v, float topDp,
                                     int height, android.content.Context c) {
            FrameLayout.LayoutParams p = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, height,
                    Gravity.TOP | Gravity.CENTER_HORIZONTAL);
            p.topMargin = Ui.dp(c, topDp);
            col.addView(v, p);
        }
    }

    static class Res {
        static int feat(int i) {
            switch (i) {
                case 0: return R.drawable.prem_feat_0;
                case 1: return R.drawable.prem_feat_1;
                case 2: return R.drawable.prem_feat_2;
                case 3: return R.drawable.prem_feat_3;
                case 4: return R.drawable.prem_feat_4;
                case 5: return R.drawable.prem_feat_5;
                case 6: return R.drawable.prem_feat_6;
                default: return R.drawable.prem_feat_7;
            }
        }
    }
}
