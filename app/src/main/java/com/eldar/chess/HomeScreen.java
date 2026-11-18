package com.eldar.chess;

import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
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
        root.setBackgroundColor(Ui.BG);

        root.addView(topBar(c, main, played));

        ScrollView scroll = new ScrollView(c);
        scroll.setBackgroundColor(Ui.BG);
        scroll.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        LinearLayout body = Ui.linear(c, LinearLayout.VERTICAL);
        body.setPadding(Ui.dp(c, 6), Ui.dp(c, 4), Ui.dp(c, 6), Ui.dp(c, 8));
        scroll.addView(body, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        if (played) {
            // ---- D-state: Puzzles card + Daily Puzzle ----
            LinearLayout row = Ui.linear(c, LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(c, 178));
            rp.setMargins(Ui.dp(c, 2), Ui.dp(c, 2), Ui.dp(c, 2), 0);
            row.setLayoutParams(rp);

            ImageView puzzleCard = new ImageView(c);
            puzzleCard.setImageResource(R.drawable.puzzle_card);
            puzzleCard.setScaleType(ImageView.ScaleType.FIT_XY);
            puzzleCard.setBackground(Ui.round(0xFF000000, 8, c));
            puzzleCard.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) { main.navigate("puzzles"); }
            });
            row.addView(puzzleCard, new LinearLayout.LayoutParams(0,
                    Ui.dp(c, 176), 2.05f));

            Ui.spacer(c, Ui.dp(c, 4), 1);

            row.addView(dailyPuzzleCard(c, main, 1f));
            body.addView(row);

            body.addView(recommendedCard(c, main));
        } else {
            // ---- E / L state ----
            LinearLayout cards = Ui.linear(c, LinearLayout.HORIZONTAL);
            cards.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(c, 180));
            cp.setMargins(Ui.dp(c, 2), Ui.dp(c, 2), Ui.dp(c, 2), 0);
            cards.setLayoutParams(cp);

            if (main.state.learnProgress == 0) {
                // Play Coach card (narrow)
                LinearLayout coach = Ui.linear(c, LinearLayout.VERTICAL);
                coach.setGravity(Gravity.CENTER);
                coach.setBackground(Ui.round(Ui.CARD, 8, c));
                coach.setLayoutParams(new LinearLayout.LayoutParams(0, Ui.dp(c, 178), 1f));
                ImageView face = new ImageView(c);
                face.setImageResource(R.drawable.avatar_coach);
                face.setScaleType(ImageView.ScaleType.FIT_CENTER);
                face.setBackground(Ui.round(0xFF2B2A27, 6, c));
                coach.addView(face, new LinearLayout.LayoutParams(
                        Ui.dp(c, 62), Ui.dp(c, 62)));
                TextView pc = Ui.text(c, "Play Coach", 13, Ui.WHITE, true);
                pc.setPadding(0, Ui.dp(c, 6), 0, 0);
                coach.addView(pc);
                coach.addView(Ui.text(c, "David", 11, Ui.GREY, false));
                coach.setOnClickListener(new View.OnClickListener() {
                    @Override public void onClick(View v) {
                        NewGameScreen.startGame(main, "coach", 600000);
                    }
                });
                cards.addView(coach);

                Ui.spacer(c, Ui.dp(c, 4), 1);

                // Learn card (from screenshot, includes Start)
                ImageView learn = new ImageView(c);
                learn.setImageResource(R.drawable.learn_card);
                learn.setScaleType(ImageView.ScaleType.FIT_XY);
                learn.setLayoutParams(new LinearLayout.LayoutParams(0, Ui.dp(c, 178), 2.1f));
                learn.setOnClickListener(new View.OnClickListener() {
                    @Override public void onClick(View v) { main.navigate("learn"); }
                });
                cards.addView(learn);
            } else {
                cards.addView(dailyPuzzleCard(c, main, 1f));
                Ui.spacer(c, Ui.dp(c, 4), 1);
                cards.addView(personCard(c, R.drawable.avatar_bot, "Play Bots", "Marko",
                        new View.OnClickListener() {
                            @Override public void onClick(View v) {
                                NewGameScreen.startGame(main, "bots", 600000);
                            }
                        }));
                Ui.spacer(c, Ui.dp(c, 4), 1);
                cards.addView(personCard(c, R.drawable.avatar_coach2, "Play Coach", "David",
                        new View.OnClickListener() {
                            @Override public void onClick(View v) {
                                NewGameScreen.startGame(main, "coach", 600000);
                            }
                        }));
            }
            body.addView(cards);

            // Play Your First Game! banner - shown in BOTH pre-game states
            // (071929 new user and 072058 after the learn card moved on)
            ImageView banner = new ImageView(c);
            banner.setImageResource(R.drawable.first_game);
            banner.setScaleType(ImageView.ScaleType.FIT_CENTER);
            banner.setAdjustViewBounds(true);
            LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            bp.setMargins(Ui.dp(c, 2), Ui.dp(c, 8), Ui.dp(c, 148), 0);
            banner.setLayoutParams(bp);
            banner.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) { main.navigate("newgame"); }
            });
            body.addView(banner);
        }

        // section order follows the screenshots: played = Ratings before Friends
        // (072329), unplayed = Friends before Ratings (071929 / 072058)
        if (played) {
            body.addView(ratingsSection(c, main, true));
            body.addView(friendsSection(c, main));
        } else {
            body.addView(friendsSection(c, main));
            body.addView(ratingsSection(c, main, false));
        }

        // ---- Game History ----
        LinearLayout ghHeader = sectionHeader(c, "Game History", played ? "Accuracy" : null);
        if (played) {
            TextView ghCount = Ui.text(c, String.valueOf(main.state.gamesPlayed), 11,
                    Ui.WHITE, true);
            ghCount.setGravity(Gravity.CENTER);
            ghCount.setBackground(Ui.round(0xFF56534E, 9, c));
            LinearLayout.LayoutParams gp = new LinearLayout.LayoutParams(
                    Ui.dp(c, 18), Ui.dp(c, 18));
            gp.setMargins(Ui.dp(c, 6), 0, 0, 0);
            ghCount.setLayoutParams(gp);
            ghHeader.addView(ghCount, 1);
        }
        body.addView(ghHeader);
        if (played) {
            body.addView(historyCard(c, main));
            TextView viewAll = Ui.text(c, "View All Games", 13, 0xFF726C65, false);
            viewAll.setGravity(Gravity.CENTER);
            viewAll.setPadding(0, Ui.dp(c, 8), 0, 0);
            body.addView(viewAll);
        } else {
            body.addView(emptyHistoryCard(c, main));
        }

        // ---- Play button pinned ----
        TextView play = Ui.text(c, "Play", 17, Ui.WHITE, true);
        play.setGravity(Gravity.CENTER);
        play.setBackground(Ui.round(Ui.GREEN, 12, c));
        LinearLayout.LayoutParams pp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(c, 46));
        pp.setMargins(Ui.dp(c, 10), Ui.dp(c, 8), Ui.dp(c, 10), Ui.dp(c, 8));
        play.setLayoutParams(pp);
        play.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { main.navigate("newgame"); }
        });

        root.addView(scroll);
        root.addView(play);
        return root;
    }

    private static LinearLayout topBar(final android.content.Context c, final MainActivity main,
                                       boolean played) {
        LinearLayout top = Ui.linear(c, LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);
        top.setPadding(Ui.dp(c, 8), Ui.dp(c, 8), Ui.dp(c, 10), Ui.dp(c, 8));

        ImageView avatar = new ImageView(c);
        avatar.setImageResource(R.drawable.avatar_grey);
        avatar.setScaleType(ImageView.ScaleType.FIT_CENTER);
        avatar.setBackground(Ui.round(0xFF4A4844, 4, c));
        LinearLayout.LayoutParams ap = new LinearLayout.LayoutParams(Ui.dp(c, 30), Ui.dp(c, 30));
        avatar.setLayoutParams(ap);
        avatar.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { main.navigate("more"); }
        });
        top.addView(avatar);

        ImageView flame = new ImageView(c);
        flame.setImageResource(R.drawable.icon_flame);
        LinearLayout.LayoutParams fp = new LinearLayout.LayoutParams(Ui.dp(c, 18), Ui.dp(c, 20));
        fp.setMargins(Ui.dp(c, 8), 0, Ui.dp(c, 2), 0);
        flame.setLayoutParams(fp);
        top.addView(flame);
        TextView streak = Ui.text(c, String.valueOf(main.state.streak), 14, Ui.WHITE, true);
        top.addView(streak);

        Ui.spacerWeight(c, 1f);

        int[] icons = {R.drawable.icon_gem, R.drawable.icon_people, R.drawable.icon_bell};
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
            iv.setImageResource(icons[i]);
            LinearLayout.LayoutParams ip = new LinearLayout.LayoutParams(
                    Ui.dp(c, 22), Ui.dp(c, 22));
            ip.setMargins(Ui.dp(c, 10), 0, 0, 0);
            iv.setLayoutParams(ip);
            iv.setOnClickListener(clicks[i]);
            top.addView(iv);
        }
        return top;
    }

    private static LinearLayout dailyPuzzleCard(final android.content.Context c,
                                                final MainActivity main, float weight) {
        LinearLayout card = Ui.linear(c, LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER);
        card.setBackground(Ui.round(Ui.CARD, 8, c));
        card.setLayoutParams(new LinearLayout.LayoutParams(0, Ui.dp(c, 176), weight));
        ImageView icon = new ImageView(c);
        icon.setImageResource(R.drawable.icon_puzzle_green2);
        icon.setLayoutParams(new LinearLayout.LayoutParams(Ui.dp(c, 46), Ui.dp(c, 52)));
        card.addView(icon);
        TextView t = Ui.text(c, "Daily Puzzle", 12, Ui.WHITE, true);
        t.setPadding(0, Ui.dp(c, 8), 0, 0);
        card.addView(t);
        TextView s = Ui.text(c, "Solved by 118,355", 10, Ui.GREY, false);
        card.addView(s);
        card.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { main.navigate("puzzles"); }
        });
        return card;
    }

    private static LinearLayout personCard(android.content.Context c, int avatarRes,
                                           String title, String sub, View.OnClickListener click) {
        LinearLayout card = Ui.linear(c, LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER);
        card.setBackground(Ui.round(Ui.CARD, 8, c));
        card.setLayoutParams(new LinearLayout.LayoutParams(0, Ui.dp(c, 176), 1f));
        ImageView face = new ImageView(c);
        face.setImageResource(avatarRes);
        face.setScaleType(ImageView.ScaleType.FIT_CENTER);
        face.setBackground(Ui.round(0xFF2B2A27, 6, c));
        card.addView(face, new LinearLayout.LayoutParams(Ui.dp(c, 62), Ui.dp(c, 62)));
        TextView t = Ui.text(c, title, 13, Ui.WHITE, true);
        t.setPadding(0, Ui.dp(c, 6), 0, 0);
        card.addView(t);
        card.addView(Ui.text(c, sub, 11, Ui.GREY, false));
        card.setOnClickListener(click);
        return card;
    }

    private static LinearLayout recommendedCard(final android.content.Context c,
                                                final MainActivity main) {
        LinearLayout card = Ui.linear(c, LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setBackground(Ui.round(Ui.CARD, 8, c));
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(c, 72));
        p.setMargins(Ui.dp(c, 2), Ui.dp(c, 8), Ui.dp(c, 2), 0);
        card.setLayoutParams(p);
        card.setPadding(Ui.dp(c, 6), 0, Ui.dp(c, 8), 0);

        ImageView av = new ImageView(c);
        av.setImageResource(R.drawable.avatar_recommended);
        av.setLayoutParams(new LinearLayout.LayoutParams(Ui.dp(c, 56), Ui.dp(c, 58)));
        card.addView(av);

        LinearLayout info = Ui.linear(c, LinearLayout.VERTICAL);
        LinearLayout.LayoutParams ip = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        ip.setMargins(Ui.dp(c, 10), 0, 0, 0);
        info.setLayoutParams(ip);
        TextView name = Ui.text(c, "GustyBattery", 14, Ui.WHITE, true);
        info.addView(name);
        TextView days = Ui.text(c, "⊕  3 days", 11, Ui.GREY, false);
        info.addView(days);
        TextView skill = Ui.text(c, "SIMILAR SKILL", 10, 0xFF81B64C, true);
        skill.setPadding(0, Ui.dp(c, 3), 0, 0);
        info.addView(skill);
        card.addView(info);

        ImageView go = new ImageView(c);
        go.setImageResource(R.drawable.ic_board_glyph);
        go.setColorFilter(0xFF2E2C29);
        go.setBackground(Ui.round(Ui.GREEN, 6, c));
        go.setPadding(Ui.dp(c, 6), Ui.dp(c, 6), Ui.dp(c, 6), Ui.dp(c, 6));
        go.setLayoutParams(new LinearLayout.LayoutParams(Ui.dp(c, 36), Ui.dp(c, 36)));
        go.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                GameScreen.pendingPersona = new GameSession.Persona("GustyBattery", 1472, 3, 25,
                        false, 600, 1400, 0.38, 0.52);
                GameScreen.pendingTime = 600000;
                GameScreen.pendingPassPlay = false;
                GameScreen.pendingUserBlack = true;
                main.navigate("game");
            }
        });
        card.addView(go);
        card.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                GameScreen.pendingPersona = new GameSession.Persona("GustyBattery", 1472, 3, 25,
                        false, 600, 1400, 0.38, 0.52);
                GameScreen.pendingTime = 600000;
                GameScreen.pendingPassPlay = false;
                GameScreen.pendingUserBlack = true;
                main.navigate("game");
            }
        });

        final ImageView close = new ImageView(c);
        close.setImageResource(R.drawable.ic_close);
        close.setColorFilter(0xFF726C65);
        LinearLayout.LayoutParams xp = new LinearLayout.LayoutParams(Ui.dp(c, 16), Ui.dp(c, 16));
        xp.setMargins(Ui.dp(c, 6), 0, 0, Ui.dp(c, 46));
        close.setLayoutParams(xp);
        close.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                card.setVisibility(View.GONE);
            }
        });
        card.addView(close);
        return card;
    }

    private static LinearLayout sectionHeader(android.content.Context c, String title, String right) {
        LinearLayout h = Ui.linear(c, LinearLayout.HORIZONTAL);
        h.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        p.setMargins(Ui.dp(c, 6), Ui.dp(c, 12), Ui.dp(c, 6), Ui.dp(c, 2));
        h.setLayoutParams(p);
        h.setPadding(Ui.dp(c, 2), 0, Ui.dp(c, 4), 0);
        h.addView(Ui.text(c, title, 15, Ui.WHITE, true));
        Ui.spacerWeight(c, 1f);
        if (right != null) {
            h.addView(Ui.text(c, right, 13, Ui.GREY, false));
        }
        return h;
    }

    private static LinearLayout iconCard(android.content.Context c, int iconRes, String label,
                                         View.OnClickListener click) {
        LinearLayout card = Ui.linear(c, LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER);
        card.setBackground(Ui.round(Ui.CARD, 8, c));
        card.setLayoutParams(new LinearLayout.LayoutParams(0, Ui.dp(c, 116), 1f));
        ImageView iv = new ImageView(c);
        iv.setImageResource(iconRes);
        iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
        card.addView(iv, new LinearLayout.LayoutParams(Ui.dp(c, 58), Ui.dp(c, 60)));
        TextView t = Ui.text(c, label, 12, Ui.WHITE, true);
        t.setPadding(0, Ui.dp(c, 6), 0, 0);
        card.addView(t);
        card.setOnClickListener(click);
        return card;
    }

    private static LinearLayout friendsSection(final android.content.Context c,
                                               final MainActivity main) {
        LinearLayout section = Ui.linear(c, LinearLayout.VERTICAL);
        section.addView(sectionHeader(c, "Friends", null));
        LinearLayout fr = Ui.linear(c, LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams frp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(c, 118));
        frp.setMargins(Ui.dp(c, 2), Ui.dp(c, 2), Ui.dp(c, 2), 0);
        fr.setLayoutParams(frp);
        fr.addView(iconCard(c, R.drawable.icon_find_friends, "Find Friends",
                new View.OnClickListener() {
                    @Override public void onClick(View v) { main.navigate("friends"); }
                }));
        Ui.spacer(c, Ui.dp(c, 4), 1);
        fr.addView(iconCard(c, R.drawable.icon_challenge, "Challenge", new View.OnClickListener() {
            @Override public void onClick(View v) { main.navigate("newgame"); }
        }));
        Ui.spacer(c, Ui.dp(c, 4), 1);
        fr.addView(iconCard(c, R.drawable.icon_share, "Share Profile", new View.OnClickListener() {
            @Override public void onClick(View v) {
                Toast.makeText(c, "Profile link copied!", Toast.LENGTH_SHORT).show();
            }
        }));
        section.addView(fr);
        return section;
    }

    private static LinearLayout ratingsSection(final android.content.Context c,
                                               final MainActivity main, boolean played) {
        LinearLayout section = Ui.linear(c, LinearLayout.VERTICAL);
        section.addView(sectionHeader(c, "Ratings", played ? "View All" : null));
        section.addView(ratingsCard(c, main, played));
        return section;
    }

    private static LinearLayout ratingsCard(final android.content.Context c, final MainActivity main,
                                            boolean played) {
        LinearLayout card = Ui.linear(c, LinearLayout.VERTICAL);
        card.setBackground(Ui.round(Ui.CARD, 8, c));
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        p.setMargins(Ui.dp(c, 2), Ui.dp(c, 2), Ui.dp(c, 2), 0);
        card.setLayoutParams(p);
        card.setPadding(Ui.dp(c, 6), Ui.dp(c, 10), Ui.dp(c, 6), Ui.dp(c, 10));

        LinearLayout row = Ui.linear(c, LinearLayout.HORIZONTAL);
        card.addView(row, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        int[] icons = played
                ? new int[]{R.drawable.icon_bullet, R.drawable.icon_blitz, R.drawable.icon_daily,
                R.drawable.icon_allstats}
                : new int[]{R.drawable.icon_bullet, R.drawable.icon_blitz, R.drawable.icon_rapid,
                R.drawable.icon_daily};
        String[] labels = played
                ? new String[]{"Bullet", "Blitz", "Daily", "All Stats"}
                : new String[]{"Bullet", "Blitz", "Rapid", "Daily"};

        for (int i = 0; i < 4; i++) {
            LinearLayout tile = Ui.linear(c, LinearLayout.VERTICAL);
            tile.setGravity(Gravity.CENTER);
            tile.setLayoutParams(new LinearLayout.LayoutParams(0,
                    ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
            ImageView iv = new ImageView(c);
            iv.setImageResource(icons[i]);
            iv.setLayoutParams(new LinearLayout.LayoutParams(Ui.dp(c, 30), Ui.dp(c, 32)));
            tile.addView(iv);
            TextView l = Ui.text(c, labels[i], 12, Ui.WHITE, false);
            l.setPadding(0, Ui.dp(c, 4), 0, Ui.dp(c, 1));
            tile.addView(l);
            // the dash under each label only appears in the played state (072329)
            if (played) {
                tile.addView(Ui.text(c, "-", 12, Ui.WHITE, true));
            }
            row.addView(tile);
        }

        if (!played) {
            TextView tip = Ui.text(c, "Play rated games to see your stats", 14, Ui.WHITE, true);
            tip.setGravity(Gravity.CENTER);
            tip.setPadding(0, Ui.dp(c, 12), 0, Ui.dp(c, 4));
            card.addView(tip);
            TextView how = Ui.text(c, "How do ratings work?", 12, Ui.GREY, false);
            how.setGravity(Gravity.CENTER);
            how.setPadding(0, 0, 0, Ui.dp(c, 4));
            card.addView(how);
        }
        return card;
    }

    private static LinearLayout emptyHistoryCard(final android.content.Context c,
                                                 final MainActivity main) {
        LinearLayout card = Ui.linear(c, LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER);
        card.setBackground(Ui.round(Ui.CARD, 8, c));
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        p.setMargins(Ui.dp(c, 2), Ui.dp(c, 2), Ui.dp(c, 2), 0);
        card.setLayoutParams(p);
        card.setPadding(Ui.dp(c, 8), Ui.dp(c, 14), Ui.dp(c, 8), Ui.dp(c, 10));

        ImageView folder = new ImageView(c);
        folder.setImageResource(R.drawable.icon_folder);
        folder.setLayoutParams(new LinearLayout.LayoutParams(Ui.dp(c, 44), Ui.dp(c, 46)));
        card.addView(folder);
        TextView t = Ui.text(c, "Your games will appear here", 14, Ui.WHITE, true);
        t.setGravity(Gravity.CENTER);
        t.setPadding(0, Ui.dp(c, 10), 0, Ui.dp(c, 10));
        card.addView(t);

        card.addView(historyRow(c, R.drawable.icon_play_online, "Play Online",
                new View.OnClickListener() {
                    @Override public void onClick(View v) { main.navigate("newgame"); }
                }));
        card.addView(historyRow(c, R.drawable.icon_play_bots, "Play Bots",
                new View.OnClickListener() {
                    @Override public void onClick(View v) {
                        NewGameScreen.startGame(main, "bots", 600000);
                    }
                }));
        card.addView(historyRow(c, R.drawable.icon_play_coach, "Play Coach",
                new View.OnClickListener() {
                    @Override public void onClick(View v) {
                        NewGameScreen.startGame(main, "coach", 600000);
                    }
                }));
        return card;
    }

    private static TextView historyRow(final android.content.Context c, int iconRes, String label,
                                       View.OnClickListener click) {
        TextView row = new TextView(c);
        row.setText("   " + label);
        row.setTextSize(13);
        row.setTextColor(Ui.WHITE);
        row.setTypeface(null, android.graphics.Typeface.BOLD);
        row.setBackground(Ui.round(0xFF2E2C29, 6, c));
        row.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(c, 38));
        p.setMargins(Ui.dp(c, 4), Ui.dp(c, 4), Ui.dp(c, 4), Ui.dp(c, 2));
        row.setLayoutParams(p);
        // icon via compound drawable left
        android.graphics.drawable.Drawable d = c.getResources().getDrawable(iconRes);
        if (d != null) {
            d.setBounds(0, 0, Ui.dp(c, 20), Ui.dp(c, 20));
            row.setCompoundDrawablePadding(Ui.dp(c, 8));
            row.setCompoundDrawables(d, null, null, null);
        }
        row.setOnClickListener(click);
        return row;
    }

    private static LinearLayout historyCard(final android.content.Context c,
                                            final MainActivity main) {
        LinearLayout card = Ui.linear(c, LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER);
        card.setBackground(Ui.round(Ui.CARD, 8, c));
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(c, 64));
        p.setMargins(Ui.dp(c, 2), Ui.dp(c, 2), Ui.dp(c, 2), 0);
        card.setLayoutParams(p);
        card.setPadding(Ui.dp(c, 10), 0, Ui.dp(c, 10), 0);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);

        ImageView av = new ImageView(c);
        av.setImageResource(R.drawable.avatar_grey);
        av.setBackground(Ui.round(0xFF4A4844, 4, c));
        card.addView(av, new LinearLayout.LayoutParams(Ui.dp(c, 36), Ui.dp(c, 36)));

        LinearLayout info = Ui.linear(c, LinearLayout.VERTICAL);
        info.setLayoutParams(new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        ((LinearLayout.LayoutParams) info.getLayoutParams()).setMargins(Ui.dp(c, 10), 0, 0, 0);
        info.addView(Ui.text(c, main.state.lastOpponent.isEmpty() ? "codon201100" : main.state.lastOpponent,
                13, Ui.WHITE, true));
        info.addView(Ui.text(c, (main.state.lastResult.isEmpty() ? "Finished" : main.state.lastResult)
                + "  •  10 min", 11, Ui.GREY, false));
        card.addView(info);

        card.addView(Ui.text(c, main.state.lastAccuracy.isEmpty() ? "—" : main.state.lastAccuracy,
                13, 0xFF81B64C, true));
        return card;
    }
}
