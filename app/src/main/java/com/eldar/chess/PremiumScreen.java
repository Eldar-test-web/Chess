package com.eldar.chess;

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
        String name; String subtitle; String subtitleColor; int icon; int border;
        String[] features; String[] featIcons;
        String priceOld; String priceYearly; String yearlyTotal; boolean yearlyOnly;

        Plan(String name, String subtitle, String subtitleColor, int icon, int border,
             String[] features, String[] featIcons, String priceOld, String priceYearly,
             String yearlyTotal, boolean yearlyOnly) {
            this.name = name; this.subtitle = subtitle; this.subtitleColor = subtitleColor;
            this.icon = icon; this.border = border; this.features = features;
            this.featIcons = featIcons; this.priceOld = priceOld; this.priceYearly = priceYearly;
            this.yearlyTotal = yearlyTotal; this.yearlyOnly = yearlyOnly;
        }
    }

    private static Plan[] plans() {
        String[] fullFeat = {"Unlimited Game Review", "Unlimited Move Explanations", "Advanced Stats",
                "Unlimited Puzzles", "Unlimited Lessons", "Unlimited Coach Games",
                "Unlock All Bots", "No Ads"};
        String[] featIcons = {"🧭", "😄", "📊", "🧩", "🎓", "🧑", "🤖", "🚫"};
        String[] goldFeat = {"Unlimited Puzzles", "Unlimited Lessons", "Unlimited Coach Games",
                "Unlock All Bots", "No Ads"};
        String[] platinumFeat = {"Unlimited Game Review", "Unlimited Puzzles", "Unlimited Lessons",
                "Unlimited Coach Games", "Unlock All Bots", "No Ads"};

        return new Plan[]{
                new Plan("Diamond", "MOST POPULAR", "#3E9BDD", R.drawable.icon_diamond, 0xFF3E9BDD,
                        fullFeat, featIcons, "AZN 13.58", "AZN 7.51", "AZN 90.08", false),
                new Plan("Platinum", "", "", R.drawable.icon_crown, 0xFF7FA8C9,
                        platinumFeat, featIcons, "AZN 8.48", "AZN 4.96", "AZN 59.48", false),
                new Plan("Gold", "", "", R.drawable.icon_star, 0xFFE8B430,
                        goldFeat, featIcons, "AZN 5.08", "AZN 3.12", "AZN 37.38", false),
                new Plan("Friends & Family", "SAVE 72%", "#81B64C", R.drawable.icon_heart, 0xFF81B64C,
                        fullFeat, featIcons, "AZN 12.61", "AZN 12.61", "AZN 151.28", true)
        };
    }

    public static View build(final MainActivity main) {
        final android.content.Context c = main;

        LinearLayout root = Ui.linear(c, LinearLayout.VERTICAL);
        root.setBackgroundColor(Ui.BG);

        LinearLayout top = Ui.linear(c, LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER);
        top.setPadding(Ui.dp(c, 8), Ui.dp(c, 10), Ui.dp(c, 8), Ui.dp(c, 4));
        ImageView back = Ui.img(c, R.drawable.ic_back, 20);
        back.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { main.navigate("home"); }
        });
        FrameLayout fl = new FrameLayout(c);
        fl.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        fl.addView(back, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER_VERTICAL | Gravity.START));
        TextView title = Ui.text(c, "Unlock Your Potential", 18, Ui.WHITE, true);
        title.setGravity(Gravity.CENTER);
        fl.addView(title, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER));
        top.addView(fl);
        root.addView(top);

        final ViewPager2 pager = new ViewPager2(c);
        pager.setAdapter(new PlanAdapter(main));
        root.addView(pager, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        // dots
        final LinearLayout dots = Ui.linear(c, LinearLayout.HORIZONTAL);
        dots.setGravity(Gravity.CENTER);
        dots.setPadding(0, Ui.dp(c, 8), 0, Ui.dp(c, 12));
        for (int i = 0; i < 4; i++) {
            View d = new View(c);
            d.setLayoutParams(new LinearLayout.LayoutParams(Ui.dp(c, 7), Ui.dp(c, 7)));
            LinearLayout.LayoutParams dp2 = (LinearLayout.LayoutParams) d.getLayoutParams();
            dp2.setMargins(Ui.dp(c, 4), 0, Ui.dp(c, 4), 0);
            d.setLayoutParams(dp2);
            d.setBackground(Ui.round(i == 0 ? Ui.WHITE : 0xFF56534E, 4, c));
            dots.addView(d);
        }
        root.addView(dots);

        pager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override public void onPageSelected(int position) {
                for (int i = 0; i < dots.getChildCount(); i++) {
                    View d = dots.getChildAt(i);
                    d.setBackground(Ui.round(i == position ? Ui.WHITE : 0xFF56534E, 4, c));
                }
            }
        });
        return root;
    }

    static class PlanAdapter extends RecyclerView.Adapter<PlanHolder> {
        MainActivity main;
        Plan[] plans = plans();

        PlanAdapter(MainActivity main) { this.main = main; }

        @NonNull @Override public PlanHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            FrameLayout page = new FrameLayout(main);
            page.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            return new PlanHolder(page, main);
        }

        @Override public void onBindViewHolder(@NonNull PlanHolder holder, int position) {
            holder.bind(plans[position], position);
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

        void bind(final Plan plan, int position) {
            final android.content.Context c = main;
            page.removeAllViews();

            if (position > 0) {
                // right edge of the previous card peeking in from the left (072307)
                FrameLayout sliver = new FrameLayout(c);
                View fill = new View(c);
                fill.setBackground(Ui.round(0xFF2E2C29, 12, c));
                sliver.addView(fill, new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                View stroke = new View(c);
                stroke.setBackground(Ui.roundStroke(0x00000000, 12, plans()[position - 1].border, 2, c));
                stroke.setAlpha(0.62f);
                sliver.addView(stroke, new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                FrameLayout.LayoutParams slp = new FrameLayout.LayoutParams(
                        Ui.dp(c, 33), ViewGroup.LayoutParams.MATCH_PARENT);
                slp.setMargins(Ui.dp(c, 46), Ui.dp(c, 172), 0, Ui.dp(c, 24));
                page.addView(sliver, slp);
            }

            LinearLayout cardWrap = Ui.linear(c, LinearLayout.VERTICAL);
            FrameLayout.LayoutParams cw = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            cw.setMargins(Ui.dp(c, 102), Ui.dp(c, 150), -Ui.dp(c, 4), Ui.dp(c, 24));
            cardWrap.setLayoutParams(cw);

            FrameLayout cardArea = new FrameLayout(c);
            LinearLayout card = Ui.linear(c, LinearLayout.VERTICAL);
            card.setBackground(Ui.roundStroke(0xFF2E2C29, 12, plan.border, 2, c));
            card.setPadding(Ui.dp(c, 14), Ui.dp(c, 28), Ui.dp(c, 14), Ui.dp(c, 12));
            cardArea.addView(card, new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

            // icon overlapping top border
            ImageView icon = new ImageView(c);
            icon.setImageResource(plan.icon);
            FrameLayout.LayoutParams ip = new FrameLayout.LayoutParams(
                    Ui.dp(c, 44), Ui.dp(c, 48), Gravity.CENTER_HORIZONTAL | Gravity.TOP);
            ip.setMargins(0, Ui.dp(c, 22), 0, 0);
            icon.setLayoutParams(ip);
            icon.setTranslationY(-Ui.dp(c, 24));
            cardArea.addView(icon);

            LinearLayout inner = Ui.linear(c, LinearLayout.VERTICAL);
            inner.setGravity(Gravity.CENTER_HORIZONTAL);
            inner.setPadding(Ui.dp(c, 8), Ui.dp(c, 6), Ui.dp(c, 8), Ui.dp(c, 4));
            LinearLayout.LayoutParams inp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            inp.setMargins(0, Ui.dp(c, 64), 0, 0);
            inner.setLayoutParams(inp);

            TextView name = Ui.text(c, plan.name, 20, Ui.WHITE, true);
            name.setGravity(Gravity.CENTER);
            inner.addView(name);

            if (plan.subtitle != null && !plan.subtitle.isEmpty()) {
                int subColor = plan.subtitleColor.isEmpty() ? Ui.GREY
                        : (int) Long.parseLong(plan.subtitleColor.replace("#", "FF"), 16);
                TextView sub = Ui.text(c, plan.subtitle, 11, subColor, true);
                sub.setGravity(Gravity.CENTER);
                sub.setPadding(0, Ui.dp(c, 4), 0, 0);
                inner.addView(sub);
            } else {
                Ui.spacer(c, 1, Ui.dp(c, 4));
            }

            Ui.spacer(c, 1, Ui.dp(c, 18));

            for (int i = 0; i < plan.features.length; i++) {
                LinearLayout fr = Ui.linear(c, LinearLayout.HORIZONTAL);
                fr.setGravity(Gravity.CENTER_VERTICAL);
                fr.setLayoutParams(new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, Ui.dp(c, 26)));
                TextView fi = Ui.text(c, plan.featIcons[i], 13, Ui.WHITE, false);
                fr.addView(fi);
                TextView ft = Ui.text(c, " " + plan.features[i], 13, Ui.WHITE, true);
                fr.addView(ft);
                inner.addView(fr);
            }

            Ui.spacerWeight(c, 1f);

            if (!plan.priceOld.isEmpty()) {
                TextView old = Ui.text(c, plan.priceOld + " / month", 12, Ui.GREY, false);
                old.setPaintFlags(old.getPaintFlags()
                        | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
                old.setGravity(Gravity.CENTER);
                inner.addView(old);
            }

            TextView price = Ui.text(c, plan.priceYearly + " / month", 17, 0xFF3E9BDD, true);
            price.setGravity(Gravity.CENTER);
            price.setPadding(0, Ui.dp(c, 2), 0, 0);
            inner.addView(price);
            TextView total = Ui.text(c, "12 months • " + plan.yearlyTotal, 15, Ui.WHITE, true);
            total.setGravity(Gravity.CENTER);
            inner.addView(total);

            if (plan.yearlyOnly) {
                TextView chip = Ui.text(c, "YEARLY PLAN ONLY", 11, Ui.GREY, false);
                chip.setBackground(Ui.round(0xFF3B3935, 4, c));
                chip.setPadding(Ui.dp(c, 10), Ui.dp(c, 5), Ui.dp(c, 10), Ui.dp(c, 5));
                chip.setGravity(Gravity.CENTER);
                LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                cp.setMargins(0, Ui.dp(c, 8), 0, 0);
                chip.setLayoutParams(cp);
                inner.addView(chip);
            } else {
                LinearLayout toggle = Ui.linear(c, LinearLayout.HORIZONTAL);
                LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, Ui.dp(c, 30));
                tp.setMargins(0, Ui.dp(c, 8), 0, 0);
                toggle.setLayoutParams(tp);
                TextView yearly = Ui.text(c, "  Yearly  ", 13, Ui.WHITE, false);
                yearly.setGravity(Gravity.CENTER);
                yearly.setBackground(Ui.round(0xFF3B3935, 6, c));
                toggle.addView(yearly);
                final TextView monthly = Ui.text(c, "  Monthly  ", 13, Ui.GREY, false);
                monthly.setGravity(Gravity.CENTER);
                toggle.addView(monthly);
                inner.addView(toggle);

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

            TextView tryBtn = Ui.text(c, "Try for AZN 0.00", 16, Ui.WHITE, true);
            tryBtn.setGravity(Gravity.CENTER);
            tryBtn.setBackground(Ui.round(0xFF1C7FC4, 8, c));
            LinearLayout.LayoutParams tp2 = new LinearLayout.LayoutParams(
                    Ui.dp(c, 250), Ui.dp(c, 44));
            tp2.setMargins(0, Ui.dp(c, 14), 0, 0);
            tp2.gravity = Gravity.CENTER_HORIZONTAL;
            tryBtn.setLayoutParams(tp2);
            tryBtn.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    Toast.makeText(c, "This is a demo - enjoy your free trial!",
                            Toast.LENGTH_SHORT).show();
                }
            });
            inner.addView(tryBtn);

            TextView fine = Ui.text(c, "You will be billed at the end of your free trial and your " +
                    "Premium membership will renew automatically unless you cancel beforehand. " +
                    "You may cancel anytime in Google Play.", 9, 0xFF8A8780, false);
            fine.setGravity(Gravity.CENTER);
            fine.setPadding(0, Ui.dp(c, 8), 0, 0);
            inner.addView(fine);

            cardArea.addView(inner);
            cardWrap.addView(cardArea, new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            page.addView(cardWrap);
        }
    }
}
