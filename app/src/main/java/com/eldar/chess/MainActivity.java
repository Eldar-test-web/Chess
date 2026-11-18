package com.eldar.chess;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

/** Single-activity navigator. Hosts content screens + chess.com bottom nav. */
public class MainActivity extends AppCompatActivity {

    public AppState state = new AppState();

    private FrameLayout content;
    private LinearLayout bottomNav;
    private LinearLayout[] navItems = new LinearLayout[5];
    private String[] navLabels = {"Home", "Puzzles", "Learn", "Watch", "More"};
    private String current = "login";
    public GameScreen activeGame = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        state.load(this);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Ui.BG);

        content = new FrameLayout(this);
        content.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));
        root.addView(content);

        bottomNav = buildBottomNav();
        root.addView(bottomNav, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 54)));

        setContentView(root);
        navigate("login");
    }

    public void navigate(String id) {
        if (activeGame != null && !id.equals("game")) {
            activeGame.destroy();
            activeGame = null;
        }
        current = id;
        content.removeAllViews();
        View v;
        switch (id) {
            case "login": v = LoginScreen.build(this); break;
            case "home": v = HomeScreen.build(this); break;
            case "newgame": v = NewGameScreen.build(this); break;
            case "game": v = GameScreen.build(this); break;
            case "learn": v = LearnScreen.build(this); break;
            case "watch": v = WatchScreen.build(this); break;
            case "more": v = MoreScreen.build(this); break;
            case "premium": v = PremiumScreen.build(this); break;
            case "friends": v = FriendsScreen.build(this); break;
            case "puzzles": v = PuzzlesScreen.build(this); break;
            default: v = new View(this);
        }
        content.addView(v, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        boolean showNav = id.equals("home") || id.equals("learn") || id.equals("watch")
                || id.equals("more") || id.equals("puzzles");
        bottomNav.setVisibility(showNav ? View.VISIBLE : View.GONE);
        if (showNav) {
            for (int i = 0; i < navItems.length; i++) {
                styleNav(navItems[i], navLabels[i].toLowerCase().equals(id));
            }
        }
    }

    public String currentScreen() { return current; }

    private LinearLayout buildBottomNav() {
        LinearLayout nav = new LinearLayout(this);
        nav.setOrientation(LinearLayout.HORIZONTAL);
        nav.setBackgroundColor(0xFF211F1C);
        nav.setGravity(Gravity.CENTER_VERTICAL);

        int[] icons = {R.drawable.ic_nav_pawn, R.drawable.ic_nav_puzzle, R.drawable.ic_nav_cap,
                R.drawable.ic_nav_binoculars, R.drawable.ic_nav_more};
        for (int i = 0; i < 5; i++) {
            navItems[i] = navItem(navLabels[i], icons[i]);
            nav.addView(navItems[i]);
        }
        return nav;
    }

    private LinearLayout navItem(final String label, int iconRes) {
        final LinearLayout item = new LinearLayout(this);
        item.setOrientation(LinearLayout.VERTICAL);
        item.setGravity(Gravity.CENTER);
        item.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f));
        item.setPadding(0, Ui.dp(this, 4), 0, Ui.dp(this, 3));

        ImageView icon = new ImageView(this);
        icon.setImageResource(iconRes);
        icon.setLayoutParams(new LinearLayout.LayoutParams(Ui.dp(this, 20), Ui.dp(this, 20)));
        item.addView(icon);

        TextView tv = Ui.text(this, label, 10, Ui.WHITE, true);
        tv.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tp.setMargins(0, Ui.dp(this, 2), 0, 0);
        tv.setLayoutParams(tp);
        item.addView(tv);

        item.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                navigate(label.toLowerCase());
            }
        });
        item.setTag(new Object[]{icon, tv});
        return item;
    }

    private void styleNav(LinearLayout item, boolean active) {
        Object[] tag = (Object[]) item.getTag();
        ImageView icon = (ImageView) tag[0];
        TextView tv = (TextView) tag[1];
        if (active) {
            icon.setColorFilter(Ui.WHITE);
            tv.setTextColor(Ui.WHITE);
            tv.setTypeface(null, Typeface.BOLD);
        } else {
            icon.setColorFilter(0xFF8A8780);
            tv.setTextColor(0xFF8A8780);
            tv.setTypeface(null, Typeface.NORMAL);
        }
    }

    @Override public void onBackPressed() {
        switch (current) {
            case "home": finish(); break;
            case "newgame": case "premium": case "friends": navigate("home"); break;
            case "learn": case "watch": case "puzzles": navigate("home"); break;
            case "game": if (activeGame != null) activeGame.onBackPressed(); break;
            case "more": navigate("home"); break;
            default: navigate("home");
        }
    }
}
