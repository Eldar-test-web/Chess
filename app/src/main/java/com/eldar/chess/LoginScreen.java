package com.eldar.chess;

import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

/** Login / welcome screen - 1:1 with screenshot 071700. */
public class LoginScreen {

    public static View build(final MainActivity main) {
        final android.content.Context c = main;

        ScrollView scroll = new ScrollView(c);
        scroll.setBackgroundColor(0xFF1E1D1A);
        scroll.setFillViewport(true);

        LinearLayout root = Ui.linear(c, LinearLayout.VERTICAL);
        scroll.addView(root, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        // ---- top bar: logo + Log In ----
        LinearLayout top = Ui.linear(c, LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);
        top.setPadding(0, Ui.dp(c, 14), 0, 0);

        top.addView(Ui.spacerWeight(c, 1f));
        LinearLayout logo = Ui.logo(c, 20, 15);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1.2f);
        logo.setLayoutParams(lp);
        logo.setGravity(Gravity.CENTER);
        top.addView(logo);

        TextView login = Ui.text(c, "Log In", 13, Ui.WHITE, true);
        login.setPadding(0, 0, Ui.dp(c, 16), 0);
        LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        login.setLayoutParams(llp);
        login.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                Toast.makeText(c, "Sign in is not available - use Play as Guest", Toast.LENGTH_SHORT).show();
            }
        });
        top.addView(login);
        root.addView(top);

        // ---- hero image: green pawn on checkered floor (from screenshot) ----
        ImageView hero = new ImageView(c);
        hero.setImageResource(R.drawable.login_hero);
        hero.setScaleType(ImageView.ScaleType.FIT_XY);
        int heroH = (int) ((float) main.getResources().getDisplayMetrics().widthPixels
                * (525f / 532f));
        root.addView(hero, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, heroH));

        // ---- headline ----
        TextView t1 = Ui.text(c, "Play. Learn.", 24, Ui.WHITE, true);
        t1.setGravity(Gravity.CENTER);
        t1.setPadding(0, Ui.dp(c, 20), 0, 0);
        root.addView(t1);
        TextView t2 = Ui.text(c, "Have Fun!", 24, Ui.WHITE, true);
        t2.setGravity(Gravity.CENTER);
        root.addView(t2);

        root.addView(Ui.spacer(c, 1, Ui.dp(c, 16)));

        // ---- phone row ----
        LinearLayout phoneRow = Ui.linear(c, LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams prp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(c, 30));
        prp.setMargins(Ui.dp(c, 24), 0, Ui.dp(c, 24), 0);
        phoneRow.setLayoutParams(prp);

        LinearLayout cc = Ui.linear(c, LinearLayout.HORIZONTAL);
        cc.setGravity(Gravity.CENTER_VERTICAL);
        cc.setBackground(Ui.round(0xFF3A3835, 6, c));
        cc.setPadding(Ui.dp(c, 8), 0, Ui.dp(c, 8), 0);
        LinearLayout.LayoutParams ccp = new LinearLayout.LayoutParams(
                Ui.dp(c, 80), ViewGroup.LayoutParams.MATCH_PARENT);
        cc.setLayoutParams(ccp);

        ImageView flag = new ImageView(c);
        flag.setImageDrawable(Ui.flagDe(c));
        flag.setLayoutParams(new LinearLayout.LayoutParams(Ui.dp(c, 17), Ui.dp(c, 12)));
        cc.addView(flag);
        TextView plus = Ui.text(c, "  +49 ", 11, Ui.WHITE, false);
        cc.addView(plus);
        ImageView chev = Ui.img(c, R.drawable.ic_chevron_down, 10);
        chev.setColorFilter(0xFFA8A29A);
        cc.addView(chev);
        phoneRow.addView(cc);

        phoneRow.addView(Ui.spacer(c, Ui.dp(c, 8), 1));

        EditText phone = new EditText(c);
        phone.setHint("Phone Number");
        phone.setHintTextColor(0xFF8A867E);
        phone.setTextColor(Ui.WHITE);
        phone.setTextSize(11);
        phone.setBackground(Ui.round(0xFF3A3835, 6, c));
        phone.setSingleLine(true);
        phone.setPadding(Ui.dp(c, 10), 0, Ui.dp(c, 10), 0);
        LinearLayout.LayoutParams pp = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.MATCH_PARENT, 1f);
        phone.setLayoutParams(pp);
        phoneRow.addView(phone);
        root.addView(phoneRow);

        // ---- OR divider ----
        LinearLayout orRow = Ui.linear(c, LinearLayout.HORIZONTAL);
        orRow.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams orp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        orp.setMargins(Ui.dp(c, 24), Ui.dp(c, 10), Ui.dp(c, 24), Ui.dp(c, 10));
        orRow.setLayoutParams(orp);
        View line1 = new View(c);
        line1.setBackgroundColor(0xFF3C3A36);
        orRow.addView(line1, new LinearLayout.LayoutParams(0, Ui.dp(c, 1), 1f));
        TextView or = Ui.text(c, " OR ", 11, 0xFF8A867E, false);
        or.setGravity(Gravity.CENTER);
        orRow.addView(or);
        View line2 = new View(c);
        line2.setBackgroundColor(0xFF3C3A36);
        orRow.addView(line2, new LinearLayout.LayoutParams(0, Ui.dp(c, 1), 1f));
        root.addView(orRow);

        // ---- Continue with Email ----
        root.addView(continueBtn(c, "Continue with Email", new BtnIcon() {
            @Override public View make(android.content.Context ctx) {
                ImageView e = new ImageView(ctx);
                e.setImageResource(R.drawable.login_mail);
                e.setScaleType(ImageView.ScaleType.FIT_CENTER);
                return e;
            }
        }, new View.OnClickListener() {
            @Override public void onClick(View v) {
                Toast.makeText(c, "Use Play as Guest to start", Toast.LENGTH_SHORT).show();
            }
        }));

        root.addView(Ui.spacer(c, 1, Ui.dp(c, 10)));

        // ---- Continue with Google ----
        root.addView(continueBtn(c, "Continue with Google", new BtnIcon() {
            @Override public View make(android.content.Context ctx) {
                ImageView g = new ImageView(ctx);
                g.setImageResource(R.drawable.google_g);
                return g;
            }
        }, new View.OnClickListener() {
            @Override public void onClick(View v) {
                Toast.makeText(c, "Use Play as Guest to start", Toast.LENGTH_SHORT).show();
            }
        }));

        root.addView(Ui.spacer(c, 1, Ui.dp(c, 16)));

        // ---- Play as Guest ----
        TextView guest = Ui.text(c, "Play as Guest", 12, Ui.WHITE, true);
        guest.setGravity(Gravity.CENTER);
        guest.setPadding(0, 0, 0, Ui.dp(c, 12));
        guest.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { main.navigate("home"); }
        });
        root.addView(guest);

        return scroll;
    }

    private interface BtnIcon { View make(android.content.Context c); }

    private static LinearLayout continueBtn(final android.content.Context c, String label,
                                            BtnIcon iconFactory, View.OnClickListener onClick) {
        LinearLayout b = Ui.linear(c, LinearLayout.HORIZONTAL);
        b.setGravity(Gravity.CENTER_VERTICAL);
        b.setBackground(Ui.round(0xFF3A3835, 6, c));
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(c, 30));
        bp.setMargins(Ui.dp(c, 24), 0, Ui.dp(c, 24), 0);
        b.setLayoutParams(bp);
        b.setPadding(Ui.dp(c, 10), 0, Ui.dp(c, 10), 0);

        View icon = iconFactory.make(c);
        LinearLayout.LayoutParams ip = new LinearLayout.LayoutParams(
                Ui.dp(c, 16), Ui.dp(c, 16));
        icon.setLayoutParams(ip);
        b.addView(icon);

        TextView t = Ui.text(c, label, 11, Ui.WHITE, true);
        LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        tp.setMargins(Ui.dp(c, 8), 0, 0, 0);
        t.setLayoutParams(tp);
        b.addView(t);

        b.setOnClickListener(onClick);
        return b;
    }
}
