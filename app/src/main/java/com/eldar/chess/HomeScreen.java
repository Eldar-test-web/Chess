package com.eldar.chess;

import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

/** Home screen - 1:1 with screenshots 071929 (new user), 072058 (learn done), 072329 (played). */
public class HomeScreen {

    public static View build(final MainActivity main) {
        final android.content.Context c = main;
        boolean played = main.state.hasPlayed();

        LinearLayout root = Ui.linear(c, LinearLayout.VERTICAL);
        root.setBackground(Ui.pageBg());

        root.addView(topBar(c, main));

        FrameLayout stage = new FrameLayout(c);
        stage.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        ScrollView scroll = new ScrollView(c);
        scroll.setVerticalScrollBarEnabled(false);
        scroll.setClipToPadding(false);
        scroll.setBackgroundColor(0x00000000);

        LinearLayout body = Ui.linear(c, LinearLayout.VERTICAL);
        body.setPadding(0, Ui.dp(c, 12), 0, Ui.dp(c, 78));
        scroll.addView(body, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        if (played) buildPlayed(c, main, body);
        else buildUnplayed(c, main, body);

        stage.addView(scroll, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        View scrim = new View(c);
        scrim.setBackgroundColor(0xFF262421);
        FrameLayout.LayoutParams scp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(c, 64));
        scp.gravity = Gravity.BOTTOM;
        stage.addView(scrim, scp);

        TextView play = Ui.text(c, "Play", 21, Ui.WHITE, true);
        play.setGravity(Gravity.CENTER);
        play.setBackground(Ui.greenFill(c, 8));
        FrameLayout.LayoutParams pp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(c, 52));
        pp.gravity = Gravity.BOTTOM;
        pp.setMargins(Ui.dp(c, 8), 0, Ui.dp(c, 12), Ui.dp(c, 10));
        play.setLayoutParams(pp);
        play.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { main.navigate("newgame"); }
        });
        stage.addView(play);

        root.addView(stage);
        return root;
    }

    // ---------------------------------------------------------------- played state (072329)

    private static void buildPlayed(final android.content.Context c, final MainActivity main,
                                    LinearLayout body) {
        // ---- Puzzles artwork hero + Daily Puzzle ----
        LinearLayout row = Ui.linear(c, LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(c, 138));
        rp.setMargins(Ui.dp(c, 6), 0, 0, 0);
        row.setLayoutParams(rp);

        FrameLayout hero = new FrameLayout(c);
        hero.setBackground(Ui.round(0xFF2F2D2B, 6, c));
        hero.setClipToOutline(true);
        ImageView art = new ImageView(c);
        art.setImageResource(R.drawable.puzzle_card);
        art.setScaleType(ImageView.ScaleType.FIT_XY);
        hero.addView(art, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        hero.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { main.navigate("puzzles"); }
        });
        row.addView(hero, new LinearLayout.LayoutParams(Ui.dp(c, 249),
                ViewGroup.LayoutParams.MATCH_PARENT));

        row.addView(Ui.spacer(c, Ui.dp(c, 7), 1));

        LinearLayout daily = Ui.linear(c, LinearLayout.VERTICAL);
        daily.setGravity(Gravity.CENTER_HORIZONTAL);
        daily.setBackground(Ui.cardBg(c, 6));
        daily.setPadding(0, Ui.dp(c, 36), 0, 0);
        LinearLayout.LayoutParams dlp = new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.MATCH_PARENT, 1f);
        dlp.setMargins(0, 0, -Ui.dp(c, 22), 0);
        daily.setLayoutParams(dlp);

        ImageView dIcon = new ImageView(c);
        dIcon.setImageResource(R.drawable.daily_icon);
        dIcon.setScaleType(ImageView.ScaleType.FIT_CENTER);
        daily.addView(dIcon, new LinearLayout.LayoutParams(Ui.dp(c, 38), Ui.dp(c, 34)));

        TextView dTitle = Ui.text(c, "Daily Puzzle", 11, Ui.WHITE, true);
        dTitle.setGravity(Gravity.CENTER);
        dTitle.setPadding(0, Ui.dp(c, 5), 0, 0);
        daily.addView(dTitle);
        TextView dSub = Ui.text(c, "Solved by 118,355", 9.5f, 0xFFA5A19B, false);
        dSub.setGravity(Gravity.CENTER);
        dSub.setPadding(0, Ui.dp(c, 4), 0, 0);
        daily.addView(dSub);
        daily.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { main.navigate("puzzles"); }
        });
        row.addView(daily);
        body.addView(row);

        // ---- Recommended match ----
        final LinearLayout recSection = Ui.linear(c, LinearLayout.VERTICAL);
        LinearLayout head = sectionHeader(c, "Recommended Match", 16);
        recSection.addView(head);
        FrameLayout recWrap = new FrameLayout(c);
        recWrap.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        recWrap.addView(recommendedCard(c, main));
        ImageView close = new ImageView(c);
        close.setImageResource(R.drawable.ic_close);
        close.setColorFilter(0xFF8A8780);
        FrameLayout.LayoutParams cp = new FrameLayout.LayoutParams(
                Ui.dp(c, 11), Ui.dp(c, 11), Gravity.TOP | Gravity.END);
        cp.setMargins(0, Ui.dp(c, 0.5f), Ui.dp(c, 2.1f), 0);
        close.setLayoutParams(cp);
        close.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { recSection.setVisibility(View.GONE); }
        });
        recWrap.addView(close);
        recSection.addView(recWrap);
        body.addView(recSection);

        // ---- Ratings ----
        body.addView(ratingsSection(c, main, true));

        // ---- Friends ----
        body.addView(friendsSection(c, main, 17.5f));

        // ---- Game History ----
        LinearLayout ghHeader = sectionHeader(c, "Game History", 17);
        View ghSpacer = ghHeader.getChildAt(1);
        ghHeader.removeView(ghSpacer);
        TextView ghCount = Ui.text(c, String.valueOf(main.state.gamesPlayed), 7,
                Ui.WHITE, true);
        ghCount.setGravity(Gravity.CENTER);
        ghCount.setBackground(Ui.round(0xFF57544F, 6.5f, c));
        ghCount.setMinWidth(Ui.dp(c, 13));
        ghCount.setPadding(Ui.dp(c, 4), 0, Ui.dp(c, 4), 0);
        LinearLayout.LayoutParams gp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, Ui.dp(c, 13));
        gp.setMargins(Ui.dp(c, 5), 0, 0, 0);
        ghCount.setLayoutParams(gp);
        ghHeader.addView(ghCount);
        ghHeader.addView(ghSpacer);
        ghHeader.addView(Ui.text(c, "Accuracy", 8, 0xFFC5C2BD, false));
        body.addView(ghHeader);
        body.addView(historyCard(c, main));
        TextView viewAll = Ui.text(c, "View All Games", 8, 0xFF726C65, false);
        viewAll.setGravity(Gravity.CENTER);
        viewAll.setPadding(0, Ui.dp(c, 9), 0, 0);
        body.addView(viewAll);
    }

    // ---------------------------------------------------------------- unplayed states (071929 / 072058)

    private static void buildUnplayed(final android.content.Context c, final MainActivity main,
                                      LinearLayout body) {
        LinearLayout cards = Ui.linear(c, LinearLayout.HORIZONTAL);
        cards.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(c, 116));
        cp.setMargins(Ui.dp(c, 6), 0, Ui.dp(c, 9), 0);
        cards.setLayoutParams(cp);

        if (main.state.learnProgress == 0) {
            LinearLayout coach = Ui.linear(c, LinearLayout.VERTICAL);
            coach.setGravity(Gravity.CENTER);
            coach.setBackground(Ui.cardBg(c, 6));
            coach.setLayoutParams(new LinearLayout.LayoutParams(0, Ui.dp(c, 116), 1f));
            FrameLayout face = new FrameLayout(c);
            face.setBackground(Ui.round(0xFF2B2A27, 20, c));
            face.setClipToOutline(true);
            face.addView(Ui.glyph(c, "person", 24, 0xFF8A8780),
                    new FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT));
            coach.addView(face, new LinearLayout.LayoutParams(
                    Ui.dp(c, 42), Ui.dp(c, 42)));
            TextView pcName = Ui.text(c, "Play Coach", 10, Ui.WHITE, true);
            pcName.setPadding(0, Ui.dp(c, 6), 0, 0);
            coach.addView(pcName);
            coach.addView(Ui.text(c, "David", 8.5f, Ui.GREY, false));
            coach.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    NewGameScreen.startGame(main, "coach", 600000);
                }
            });
            cards.addView(coach);
            cards.addView(Ui.spacer(c, Ui.dp(c, 5), 1));

            FrameLayout learn = new FrameLayout(c);
            learn.setBackground(Ui.round(0xFF4E7634, 6, c));
            learn.setClipToOutline(true);
            learn.setLayoutParams(new LinearLayout.LayoutParams(0, Ui.dp(c, 116), 2.4f));
            ImageView lart = new ImageView(c);
            lart.setImageResource(R.drawable.learn_card);
            lart.setScaleType(ImageView.ScaleType.FIT_XY);
            learn.addView(lart, new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            learn.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) { main.navigate("learn"); }
            });
            cards.addView(learn);
        } else {
            cards.addView(personCard(c, "Daily Puzzle", "Solved by 117,549",
                    R.drawable.daily_icon, new View.OnClickListener() {
                        @Override public void onClick(View v) { main.navigate("puzzles"); }
                    }));
            cards.addView(Ui.spacer(c, Ui.dp(c, 5), 1));
            cards.addView(personCard(c, "Play Bots", "Marko", R.drawable.avatar_bot,
                    new View.OnClickListener() {
                        @Override public void onClick(View v) {
                            NewGameScreen.startGame(main, "bots", 600000);
                        }
                    }));
            cards.addView(Ui.spacer(c, Ui.dp(c, 5), 1));
            cards.addView(personCard(c, "Play Coach", "David", R.drawable.avatar_coach,
                    new View.OnClickListener() {
                        @Override public void onClick(View v) {
                            NewGameScreen.startGame(main, "coach", 600000);
                        }
                    }));
        }
        body.addView(cards);

        // Play Your First Game! banner (071929 style, native pieces)
        LinearLayout banner = Ui.linear(c, LinearLayout.VERTICAL);
        banner.setBackground(Ui.round(0xFF33362B, 6, c));
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(c, 116));
        bp.setMargins(Ui.dp(c, 6), Ui.dp(c, 8), Ui.dp(c, 9), 0);
        banner.setLayoutParams(bp);
        banner.setPadding(Ui.dp(c, 8), Ui.dp(c, 8), Ui.dp(c, 8), Ui.dp(c, 6));

        LinearLayout brow = Ui.linear(c, LinearLayout.HORIZONTAL);
        brow.setGravity(Gravity.CENTER_VERTICAL);
        TextView firstTitle = Ui.text(c, "Play Your First Game!", 11, Ui.WHITE, true);
        firstTitle.setSingleLine(true);
        brow.addView(firstTitle);
        brow.addView(Ui.spacerWeight(c, 1f));
        TextView startBtn = Ui.text(c, "Start Game", 9.5f, Ui.WHITE, true);
        startBtn.setSingleLine(true);
        startBtn.setGravity(Gravity.CENTER);
        startBtn.setBackground(Ui.round(Ui.GREEN, 5, c));
        startBtn.setPadding(Ui.dp(c, 8), Ui.dp(c, 4), Ui.dp(c, 8), Ui.dp(c, 4));
        brow.addView(startBtn);
        banner.addView(brow);
        banner.addView(Ui.spacerWeight(c, 1f));
        banner.addView(pieceRow(c, new String[]{"\u265F", "\u265F", "\u265F", "\u265F",
                "\u265F", "\u265F", "\u265F", "\u265F"}));
        banner.addView(pieceRow(c, new String[]{"\u265C", "\u265E", "\u265D", "\u265B",
                "\u265A", "\u265D", "\u265E", "\u265C"}));
        banner.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { main.navigate("newgame"); }
        });
        body.addView(banner);

        body.addView(friendsSection(c, main, 16f));
        body.addView(ratingsSection(c, main, false));

        LinearLayout ghHeader = sectionHeader(c, "Game History", 16);
        body.addView(ghHeader);
        body.addView(emptyHistoryCard(c, main));
    }

    // ---------------------------------------------------------------- top bar

    private static LinearLayout topBar(final android.content.Context c, final MainActivity main) {
        LinearLayout top = Ui.linear(c, LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);
        top.setPadding(Ui.dp(c, 10), Ui.dp(c, 6), Ui.dp(c, 12), Ui.dp(c, 6));

        FrameLayout avatar = new FrameLayout(c);
        avatar.setBackground(Ui.round(0xFFE7E5E3, 5, c));
        avatar.setClipToOutline(true);
        ImageView pawn = new ImageView(c);
        pawn.setImageResource(R.drawable.piece_wp);
        pawn.setColorFilter(0xFFB5B1AC);
        pawn.setScaleType(ImageView.ScaleType.FIT_CENTER);
        FrameLayout.LayoutParams pp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        pp.setMargins(Ui.dp(c, 2), Ui.dp(c, 2), Ui.dp(c, 2), Ui.dp(c, 2));
        avatar.addView(pawn, pp);
        avatar.setLayoutParams(new LinearLayout.LayoutParams(Ui.dp(c, 28), Ui.dp(c, 28)));
        avatar.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { main.navigate("more"); }
        });
        top.addView(avatar);

        ImageView flame = new ImageView(c);
        flame.setImageResource(R.drawable.icon_flame);
        flame.setScaleType(ImageView.ScaleType.FIT_CENTER);
        LinearLayout.LayoutParams fp = new LinearLayout.LayoutParams(
                Ui.dp(c, 22), Ui.dp(c, 24));
        fp.setMargins(0, 0, 0, 0);
        flame.setLayoutParams(fp);
        flame.setTranslationY(Ui.dp(c, 2f));
        top.addView(flame);
        TextView streak = Ui.text(c, String.valueOf(main.state.streak), 13, Ui.WHITE, true);
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        sp.setMargins(Ui.dp(c, 2), 0, 0, 0);
        streak.setLayoutParams(sp);
        top.addView(streak);

        top.addView(Ui.spacerWeight(c, 1f));

        int[] artIds = {R.drawable.icon_diamond, R.drawable.icon_people, R.drawable.icon_bell};
        int[] ws = {26, 25, 21};
        int[] hs = {24, 22, 21};
        View.OnClickListener[] clicks = {
                new View.OnClickListener() {
                    @Override public void onClick(View v) { main.navigate("premium"); }
                },
                new View.OnClickListener() {
                    @Override public void onClick(View v) { main.navigate("friends"); }
                },
                new View.OnClickListener() {
                    @Override public void onClick(View v) {
                        Toast.makeText(c, "No new notifications", Toast.LENGTH_SHORT).show();
                    }
                }
        };
        for (int i = 0; i < 3; i++) {
            ImageView iv = new ImageView(c);
            iv.setImageResource(artIds[i]);
            iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
            LinearLayout.LayoutParams ip = new LinearLayout.LayoutParams(
                    Ui.dp(c, ws[i]), Ui.dp(c, hs[i]));
            ip.setMargins(Ui.dp(c, 14), 0, 0, 0);
            iv.setLayoutParams(ip);
            iv.setOnClickListener(clicks[i]);
            top.addView(iv);
        }
        return top;
    }

    // ---------------------------------------------------------------- cards

    private static LinearLayout dailyPuzzleCard(final android.content.Context c,
                                                final MainActivity main, float weight) {
        LinearLayout card = Ui.linear(c, LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER);
        card.setBackground(Ui.cardBg(c, 6));
        card.setLayoutParams(new LinearLayout.LayoutParams(0, Ui.dp(c, 136), weight));
        ImageView icon = new ImageView(c);
        icon.setImageResource(R.drawable.daily_icon);
        icon.setScaleType(ImageView.ScaleType.FIT_CENTER);
        card.addView(icon, new LinearLayout.LayoutParams(Ui.dp(c, 40), Ui.dp(c, 38)));
        TextView t = Ui.text(c, "Daily Puzzle", 11, Ui.WHITE, true);
        t.setPadding(0, Ui.dp(c, 6), 0, 0);
        card.addView(t);
        card.addView(Ui.text(c, "Solved by 118,355", 9.5f, Ui.GREY, false));
        card.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { main.navigate("puzzles"); }
        });
        return card;
    }

    private static LinearLayout personCard(android.content.Context c, String title, String sub,
                                           int artRes, View.OnClickListener click) {
        LinearLayout card = Ui.linear(c, LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER);
        card.setBackground(Ui.cardBg(c, 6));
        card.setLayoutParams(new LinearLayout.LayoutParams(0, Ui.dp(c, 136), 1f));
        ImageView art = new ImageView(c);
        art.setImageResource(artRes);
        art.setScaleType(ImageView.ScaleType.FIT_CENTER);
        card.addView(art, new LinearLayout.LayoutParams(Ui.dp(c, 66), Ui.dp(c, 58)));
        TextView t = Ui.text(c, title, 11, Ui.WHITE, true);
        t.setPadding(0, Ui.dp(c, 5), 0, 0);
        card.addView(t);
        card.addView(Ui.text(c, sub, 9.5f, Ui.GREY, false));
        card.setOnClickListener(click);
        return card;
    }

    private static LinearLayout recommendedCard(final android.content.Context c,
                                                final MainActivity main) {
        LinearLayout card = Ui.linear(c, LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setBackground(Ui.cardBg(c, 6));
        card.setClipToOutline(true);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(c, 68));
        p.setMargins(Ui.dp(c, 6), Ui.dp(c, 6), Ui.dp(c, 9), 0);
        card.setLayoutParams(p);

        ImageView av = new ImageView(c);
        av.setImageResource(R.drawable.avatar_rec);
        av.setScaleType(ImageView.ScaleType.FIT_XY);
        card.addView(av, new LinearLayout.LayoutParams(Ui.dp(c, 68), Ui.dp(c, 68)));

        LinearLayout info = Ui.linear(c, LinearLayout.VERTICAL);
        info.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams ip = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        ip.setMargins(Ui.dp(c, 7.6f), 0, Ui.dp(c, 4), 0);
        info.setLayoutParams(ip);

        LinearLayout nameRow = Ui.linear(c, LinearLayout.HORIZONTAL);
        nameRow.setGravity(Gravity.CENTER_VERTICAL);
        nameRow.addView(Ui.text(c, "GustyBattery", 11.5f, Ui.WHITE, true));
        ImageView flag = new ImageView(c);
        flag.setImageDrawable(Ui.flagAf(c));
        flag.setScaleType(ImageView.ScaleType.FIT_CENTER);
        LinearLayout.LayoutParams flp = new LinearLayout.LayoutParams(
                Ui.dp(c, 13), Ui.dp(c, 10));
        flp.setMargins(Ui.dp(c, 3), 0, 0, 0);
        flag.setLayoutParams(flp);
        nameRow.addView(flag);
        info.addView(nameRow);

        LinearLayout daysRow = Ui.linear(c, LinearLayout.HORIZONTAL);
        daysRow.setGravity(Gravity.CENTER_VERTICAL);
        TextView clock = Ui.glyph(c, "settings", 10, 0xFFA5A19B);
        clock.setLayoutParams(new LinearLayout.LayoutParams(Ui.dp(c, 12), Ui.dp(c, 12)));
        daysRow.addView(clock);
        TextView days = Ui.text(c, "3 days", 9, 0xFFA5A19B, false);
        LinearLayout.LayoutParams dlp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dlp.setMargins(Ui.dp(c, 2), 0, 0, 0);
        days.setLayoutParams(dlp);
        daysRow.addView(days);
        LinearLayout.LayoutParams drp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        drp.setMargins(0, Ui.dp(c, 3), 0, 0);
        daysRow.setLayoutParams(drp);
        info.addView(daysRow);

        TextView skill = Ui.text(c, "SIMILAR SKILL", 8.8f, 0xFF7AAC4A, true);
        skill.setLetterSpacing(0.07f);
        skill.setGravity(Gravity.CENTER);
        skill.setBackground(Ui.round(0xFF333E2E, 2, c));
        skill.setPadding(Ui.dp(c, 2.5f), Ui.dp(c, 1.2f), Ui.dp(c, 2.5f), Ui.dp(c, 1.2f));
        LinearLayout.LayoutParams slp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        slp.setMargins(0, Ui.dp(c, 1.5f), 0, 0);
        skill.setLayoutParams(slp);
        info.addView(skill);
        card.addView(info);

        ImageView go = new ImageView(c);
        go.setImageResource(R.drawable.ic_challenge_board);
        go.setScaleType(ImageView.ScaleType.FIT_CENTER);
        go.setBackground(Ui.greenFill(c, 4));
        go.setPadding(Ui.dp(c, 6), Ui.dp(c, 6), Ui.dp(c, 6), Ui.dp(c, 6));
        LinearLayout.LayoutParams gop = new LinearLayout.LayoutParams(
                Ui.dp(c, 40), Ui.dp(c, 40));
        gop.setMargins(0, 0, Ui.dp(c, 10), 0);
        go.setLayoutParams(gop);
        go.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { startGusty(main); }
        });
        card.addView(go);
        card.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { startGusty(main); }
        });
        return card;
    }

    private static void startGusty(MainActivity main) {
        GameScreen.pendingPersona = new GameSession.Persona("GustyBattery", 1472, 3, 25,
                false, 600, 1400, 0.38, 0.52);
        GameScreen.pendingTime = 600000;
        GameScreen.pendingPassPlay = false;
        GameScreen.pendingUserBlack = true;
        main.navigate("game");
    }

    private static LinearLayout sectionHeader(android.content.Context c, String title, float topDp) {
        LinearLayout h = Ui.linear(c, LinearLayout.HORIZONTAL);
        h.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        p.setMargins(Ui.dp(c, 12), Ui.dp(c, topDp), Ui.dp(c, 12), 0);
        h.setLayoutParams(p);
        h.addView(Ui.text(c, title, 13.5f, Ui.WHITE, true));
        h.addView(Ui.spacerWeight(c, 1f));
        return h;
    }

    private static LinearLayout iconCard(final android.content.Context c, int artRes, String label,
                                         View.OnClickListener click) {
        LinearLayout card = Ui.linear(c, LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
        card.setBackground(Ui.cardBg(c, 6));
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, Ui.dp(c, 136), 1f);
        card.setLayoutParams(p);
        ImageView art = new ImageView(c);
        art.setImageResource(artRes);
        art.setScaleType(ImageView.ScaleType.FIT_CENTER);
        card.addView(art, new LinearLayout.LayoutParams(Ui.dp(c, 80), Ui.dp(c, 56)));
        TextView t = Ui.text(c, label, 11.5f, Ui.WHITE, true);
        t.setGravity(Gravity.CENTER);
        t.setPadding(0, Ui.dp(c, 7), 0, 0);
        card.addView(t);
        card.setOnClickListener(click);
        return card;
    }

    private static LinearLayout friendsSection(final android.content.Context c,
                                               final MainActivity main, float topDp) {
        LinearLayout section = Ui.linear(c, LinearLayout.VERTICAL);
        section.addView(sectionHeader(c, "Friends", topDp));
        LinearLayout fr = Ui.linear(c, LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams frp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(c, 136));
        frp.setMargins(-Ui.dp(c, 9), Ui.dp(c, 6), -Ui.dp(c, 9), 0);
        fr.setLayoutParams(frp);
        fr.addView(iconCard(c, R.drawable.fri_find, "Find Friends",
                new View.OnClickListener() {
                    @Override public void onClick(View v) { main.navigate("friends"); }
                }));
        fr.addView(gap(c, 8));
        fr.addView(iconCard(c, R.drawable.fri_challenge, "Challenge",
                new View.OnClickListener() {
                    @Override public void onClick(View v) { main.navigate("newgame"); }
                }));
        fr.addView(gap(c, 8));
        fr.addView(iconCard(c, R.drawable.fri_share, "Share Profile",
                new View.OnClickListener() {
                    @Override public void onClick(View v) {
                        Toast.makeText(c, "Profile link copied!", Toast.LENGTH_SHORT).show();
                    }
                }));
        section.addView(fr);
        return section;
    }

    private static View gap(android.content.Context c, float dp) {
        View v = new View(c);
        v.setLayoutParams(new LinearLayout.LayoutParams(Ui.dp(c, dp),
                ViewGroup.LayoutParams.MATCH_PARENT));
        return v;
    }

    private static LinearLayout ratingsSection(final android.content.Context c,
                                               final MainActivity main, boolean played) {
        LinearLayout section = Ui.linear(c, LinearLayout.VERTICAL);
        LinearLayout head = sectionHeader(c, "Ratings", played ? 17.5f : 16);
        if (played) {
            head.addView(Ui.text(c, "View All", 11.5f, 0xFFE1DEDA, false));
        }
        section.addView(head);
        if (played) {
            section.addView(ratingsCards(c, main));
        } else {
            section.addView(ratingsCard(c));
        }
        return section;
    }

    private static View ratingsCards(final android.content.Context c,
                                     final MainActivity main) {
        HorizontalScrollView hsv = new HorizontalScrollView(c);
        hsv.setHorizontalScrollBarEnabled(false);
        hsv.setOverScrollMode(View.OVER_SCROLL_NEVER);
        hsv.setClipToPadding(false);
        LinearLayout.LayoutParams hp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(c, 118));
        hp.setMargins(0, Ui.dp(c, 8), 0, 0);
        hsv.setLayoutParams(hp);

        LinearLayout row = Ui.linear(c, LinearLayout.HORIZONTAL);
        row.setPadding(Ui.dp(c, 12), 0, Ui.dp(c, 12), 0);
        hsv.addView(row, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));

        int[] art = {R.drawable.rate_bullet, R.drawable.rate_blitz,
                R.drawable.rate_daily, R.drawable.rate_allstats};
        String[] labels = {"Bullet", "Blitz", "Daily", "All Stats"};
        String[] values = {String.valueOf(main.state.bulletRating),
                String.valueOf(main.state.blitzRating),
                String.valueOf(main.state.dailyRating), "-"};
        for (int i = 0; i < 4; i++) {
            if (i > 0) row.addView(gap(c, 8));
            LinearLayout tile = Ui.linear(c, LinearLayout.VERTICAL);
            tile.setGravity(Gravity.CENTER_HORIZONTAL);
            tile.setBackground(Ui.cardBg(c, 7));
            tile.setLayoutParams(new LinearLayout.LayoutParams(Ui.dp(c, 112), Ui.dp(c, 118)));
            ImageView icon = new ImageView(c);
            icon.setImageResource(art[i]);
            icon.setScaleType(ImageView.ScaleType.FIT_CENTER);
            LinearLayout.LayoutParams ip = new LinearLayout.LayoutParams(
                    Ui.dp(c, 38), Ui.dp(c, 38));
            ip.setMargins(0, Ui.dp(c, 14), 0, 0);
            tile.addView(icon, ip);
            TextView l = Ui.text(c, labels[i], 13, Ui.WHITE, true);
            l.setGravity(Gravity.CENTER);
            l.setPadding(0, Ui.dp(c, 8), 0, 0);
            tile.addView(l);
            TextView value = Ui.text(c, values[i], 12.5f, 0xFFB5B1AB, false);
            value.setGravity(Gravity.CENTER);
            value.setPadding(0, Ui.dp(c, 6), 0, 0);
            tile.addView(value);
            row.addView(tile);
        }
        return hsv;
    }

    /** Single ratings container used by the unplayed screens. */
    private static LinearLayout ratingsCard(final android.content.Context c) {
        LinearLayout card = Ui.linear(c, LinearLayout.VERTICAL);
        card.setBackground(Ui.cardBg(c, 6));
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        p.setMargins(Ui.dp(c, 6), Ui.dp(c, 8), Ui.dp(c, 9), 0);
        card.setLayoutParams(p);
        card.setPadding(Ui.dp(c, 4), Ui.dp(c, 9), Ui.dp(c, 4), Ui.dp(c, 9));

        LinearLayout row = Ui.linear(c, LinearLayout.HORIZONTAL);
        card.addView(row, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        int[] art = {R.drawable.rate_bullet, R.drawable.rate_blitz,
                R.drawable.rate_rapid, R.drawable.rate_daily};
        String[] labels = {"Bullet", "Blitz", "Rapid", "Daily"};
        for (int i = 0; i < 4; i++) {
            LinearLayout tile = Ui.linear(c, LinearLayout.VERTICAL);
            tile.setGravity(Gravity.CENTER);
            tile.setLayoutParams(new LinearLayout.LayoutParams(0,
                    ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
            ImageView icon = new ImageView(c);
            icon.setImageResource(art[i]);
            icon.setScaleType(ImageView.ScaleType.FIT_CENTER);
            tile.addView(icon, new LinearLayout.LayoutParams(Ui.dp(c, 34), Ui.dp(c, 34)));
            TextView l = Ui.text(c, labels[i], 11.5f, Ui.WHITE, true);
            l.setGravity(Gravity.CENTER);
            l.setPadding(0, Ui.dp(c, 6), 0, 0);
            tile.addView(l);
            row.addView(tile);
        }
        TextView tip = Ui.text(c, "Play rated games to see your stats", 12.5f, Ui.WHITE, true);
        tip.setGravity(Gravity.CENTER);
        tip.setPadding(0, Ui.dp(c, 10), 0, Ui.dp(c, 3));
        card.addView(tip);
        TextView how = Ui.text(c, "How do ratings work?", 10, Ui.GREY, false);
        how.setGravity(Gravity.CENTER);
        how.setPadding(0, 0, 0, Ui.dp(c, 2));
        card.addView(how);
        return card;
    }

    private static LinearLayout emptyHistoryCard(final android.content.Context c,
                                                 final MainActivity main) {
        LinearLayout card = Ui.linear(c, LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER);
        card.setBackground(Ui.cardBg(c, 6));
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        p.setMargins(Ui.dp(c, 6), Ui.dp(c, 6), Ui.dp(c, 9), 0);
        card.setLayoutParams(p);
        card.setPadding(Ui.dp(c, 8), Ui.dp(c, 14), Ui.dp(c, 8), Ui.dp(c, 10));

        TextView folder = Ui.glyph(c, "folder_open", 36, 0xFF8A8780);
        folder.setLayoutParams(new LinearLayout.LayoutParams(Ui.dp(c, 48), Ui.dp(c, 50)));
        card.addView(folder);
        TextView t = Ui.text(c, "Your games will appear here", 14, Ui.WHITE, true);
        t.setGravity(Gravity.CENTER);
        t.setPadding(0, Ui.dp(c, 10), 0, Ui.dp(c, 10));
        card.addView(t);

        card.addView(historyRow(c, "public", "Play Online",
                new View.OnClickListener() {
                    @Override public void onClick(View v) { main.navigate("newgame"); }
                }));
        card.addView(historyRow(c, "smart_toy", "Play Bots",
                new View.OnClickListener() {
                    @Override public void onClick(View v) {
                        NewGameScreen.startGame(main, "bots", 600000);
                    }
                }));
        card.addView(historyRow(c, "psychology", "Play Coach",
                new View.OnClickListener() {
                    @Override public void onClick(View v) {
                        NewGameScreen.startGame(main, "coach", 600000);
                    }
                }));
        return card;
    }

    private static LinearLayout historyRow(final android.content.Context c, String glyphName,
                                           String label, View.OnClickListener click) {
        LinearLayout row = Ui.linear(c, LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER);
        row.setBackground(Ui.round(0xFF2E2C29, 6, c));
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(c, 48));
        p.setMargins(Ui.dp(c, 4), Ui.dp(c, 5), Ui.dp(c, 4), Ui.dp(c, 3));
        row.setLayoutParams(p);
        row.addView(Ui.glyph(c, glyphName, 19, 0xFFDAD7D2));
        TextView t = Ui.text(c, "   " + label, 14.5f, Ui.WHITE, true);
        row.addView(t);
        row.setOnClickListener(click);
        return row;
    }

    private static LinearLayout historyCard(final android.content.Context c,
                                            final MainActivity main) {
        LinearLayout card = Ui.linear(c, LinearLayout.HORIZONTAL);
        card.setBackground(Ui.cardBg(c, 6));
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(c, 64));
        p.setMargins(Ui.dp(c, 6), Ui.dp(c, 6), Ui.dp(c, 9), 0);
        card.setLayoutParams(p);
        card.setPadding(Ui.dp(c, 8), 0, Ui.dp(c, 9), 0);
        card.setGravity(Gravity.CENTER_VERTICAL);

        FrameLayout av = new FrameLayout(c);
        av.setBackground(Ui.round(0xFF4A4844, 16, c));
        av.setClipToOutline(true);
        av.addView(Ui.glyph(c, "person", 18, 0xFFE4E1DB),
                new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        card.addView(av, new LinearLayout.LayoutParams(Ui.dp(c, 42), Ui.dp(c, 42)));

        LinearLayout info = Ui.linear(c, LinearLayout.VERTICAL);
        LinearLayout.LayoutParams ip = new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        ip.setMargins(Ui.dp(c, 8), 0, 0, 0);
        info.setLayoutParams(ip);
        info.addView(Ui.text(c, main.state.lastOpponent.isEmpty() ? "codon201100" : main.state.lastOpponent,
                11.5f, Ui.WHITE, true));
        info.addView(Ui.text(c, (main.state.lastResult.isEmpty() ? "Finished" : main.state.lastResult)
                + "  •  10 min", 9, Ui.GREY, false));
        card.addView(info);

        card.addView(Ui.text(c, main.state.lastAccuracy.isEmpty() ? "—" : main.state.lastAccuracy,
                11.5f, 0xFF81B64C, true));
        return card;
    }

    private static LinearLayout pieceRow(android.content.Context c, String[] glyphs) {
        LinearLayout row = Ui.linear(c, LinearLayout.HORIZONTAL);
        for (String g : glyphs) {
            TextView t = Ui.text(c, g, 14, 0xFFEDEBE6, false);
            t.setGravity(Gravity.CENTER);
            row.addView(t, new LinearLayout.LayoutParams(0, Ui.dp(c, 17), 1f));
        }
        return row;
    }
}
