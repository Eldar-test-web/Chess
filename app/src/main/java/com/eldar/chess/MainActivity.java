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
    private String current = "splash";
    public GameScreen activeGame = null;
    private TextView moreBadge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        state.load(this);

        getWindow().setStatusBarColor(0x00000000);
        getWindow().setNavigationBarColor(0xFF211F1C);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Ui.BG);
        // keep content clear of the status bar / gesture bar
        root.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
            @Override public android.view.WindowInsets onApplyWindowInsets(View v,
                                                                           android.view.WindowInsets insets) {
                v.setPadding(0, insets.getSystemWindowInsetTop(),
                        0, insets.getSystemWindowInsetBottom());
                return insets;
            }
        });

        content = new FrameLayout(this);
        content.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));
        root.addView(content);

        bottomNav = buildBottomNav();
        root.addView(bottomNav, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 46)));

        setContentView(root);
        showSplash();
    }

    /** Splash (screenshot 071621): pawn logo centered on the dark canvas. */
    private void showSplash() {
        content.removeAllViews();
        bottomNav.setVisibility(View.GONE);

        FrameLayout splash = new FrameLayout(this);
        splash.setBackgroundColor(0xFF302D2B);
        ImageView art = new ImageView(this);
        art.setImageResource(R.drawable.splash_art);
        art.setAdjustViewBounds(true);
        FrameLayout.LayoutParams ap = new FrameLayout.LayoutParams(
                Ui.dp(this, 200), ViewGroup.LayoutParams.WRAP_CONTENT);
        ap.gravity = Gravity.CENTER;
        splash.addView(art, ap);
        content.addView(splash, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override public void run() {
                if ("splash".equals(current)) navigate("home");
            }
        }, 1400);
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
            // red "1" badge on More once at least one game was played (072329)
            if (moreBadge != null) {
                moreBadge.setVisibility(state.hasPlayed() ? View.VISIBLE : View.GONE);
            }
        }
    }

    public String currentScreen() { return current; }

    private LinearLayout buildBottomNav() {
        LinearLayout nav = new LinearLayout(this);
        nav.setOrientation(LinearLayout.HORIZONTAL);
        nav.setBackgroundColor(0xFF262421);
        nav.setGravity(Gravity.CENTER_VERTICAL);
        nav.setClipChildren(false);
        nav.setClipToPadding(false);

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
        item.setOrientation(LinearLayout.HORIZONTAL);
        item.setGravity(Gravity.CENTER);
        item.setClipChildren(false);
        item.setClipToPadding(false);
        item.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f));

        ImageView icon = new ImageView(this);
        icon.setImageResource(iconRes);
        icon.setScaleType(ImageView.ScaleType.FIT_CENTER);
        icon.setColorFilter(0xFF8A8780);

        FrameLayout iconWrap = new FrameLayout(this);
        iconWrap.setClipChildren(false);
        iconWrap.setLayoutParams(new LinearLayout.LayoutParams(
                Ui.dp(this, 16), Ui.dp(this, 16)));
        iconWrap.addView(icon, new FrameLayout.LayoutParams(
                Ui.dp(this, 16), Ui.dp(this, 16), Gravity.CENTER));
        if ("More".equals(label)) {
            moreBadge = new TextView(this);
            moreBadge.setText("1");
            moreBadge.setTextSize(7);
            moreBadge.setTextColor(0xFFFFFFFF);
            moreBadge.setTypeface(null, Typeface.BOLD);
            moreBadge.setGravity(Gravity.CENTER);
            moreBadge.setIncludeFontPadding(false);
            moreBadge.setBackground(Ui.round(0xFFFA412D, 5, this));
            FrameLayout.LayoutParams bp = new FrameLayout.LayoutParams(
                    Ui.dp(this, 10), Ui.dp(this, 10));
            bp.gravity = Gravity.TOP | Gravity.END;
            bp.topMargin = -Ui.dp(this, 5);
            bp.rightMargin = -Ui.dp(this, 5);
            moreBadge.setLayoutParams(bp);
            moreBadge.setVisibility(state.hasPlayed() ? View.VISIBLE : View.GONE);
            iconWrap.addView(moreBadge);
        }
        item.addView(iconWrap);

        TextView tv = Ui.text(this, label, 8.5f, 0xFF8A8780, false);
        tv.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tp.setMargins(Ui.dp(this, 4), 0, 0, 0);
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
