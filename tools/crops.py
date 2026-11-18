"""Crop all art assets directly from the user's screenshots - final calibrated coords."""
from PIL import Image, ImageDraw, ImageFont
import os

SRC = r"C:\Users\User\OneDrive\Pictures\Screenshots"
OUT = r"C:\Users\User\WorkBuddy AI\2026-11-18-07-24-05\ChessApp\assets_crops"
os.makedirs(OUT, exist_ok=True)

CROPS = {
    # login
    "login_hero":   ("Screenshot 2026-11-18 071700.png", 0, 60, 536, 585),
    "flag_de":      ("Screenshot 2026-11-18 071700.png", 74, 708, 102, 742),
    # home new user (071929)
    "avatar_coach": ("Screenshot 2026-11-18 071929.png", 46, 136, 109, 185),
    "learn_card":   ("Screenshot 2026-11-18 071929.png", 155, 85, 531, 265),
    "first_game":   ("Screenshot 2026-11-18 071929.png", 5, 284, 388, 467),
    "icon_find_friends": ("Screenshot 2026-11-18 071929.png", 72, 556, 137, 630),
    "icon_challenge":    ("Screenshot 2026-11-18 071929.png", 262, 556, 333, 632),
    "icon_share":        ("Screenshot 2026-11-18 071929.png", 450, 558, 522, 628),
    "icon_bullet":  ("Screenshot 2026-11-18 071929.png", 76, 766, 114, 806),
    "icon_blitz":   ("Screenshot 2026-11-18 071929.png", 196, 766, 230, 806),
    "icon_rapid":   ("Screenshot 2026-11-18 071929.png", 314, 766, 348, 806),
    "icon_daily":   ("Screenshot 2026-11-18 071929.png", 430, 764, 466, 806),
    # home with puzzles (072329)
    "puzzle_card":  ("Screenshot 2026-11-18 072329.png", 10, 92, 376, 270),
    "icon_puzzle_badge": ("Screenshot 2026-11-18 072329.png", 26, 106, 54, 130),
    "icon_daily2":  ("Screenshot 2026-11-18 072329.png", 440, 140, 492, 182),
    "icon_gem":     ("Screenshot 2026-11-18 072329.png", 416, 26, 440, 50),
    "icon_people":  ("Screenshot 2026-11-18 072329.png", 452, 26, 482, 50),
    "icon_bell":    ("Screenshot 2026-11-18 072329.png", 490, 25, 517, 52),
    "icon_flame":   ("Screenshot 2026-11-18 072329.png", 41, 28, 64, 54),
    "avatar_recommended": ("Screenshot 2026-11-18 072329.png", 11, 315, 86, 390),
    "icon_allstats":("Screenshot 2026-11-18 072329.png", 432, 468, 468, 504),
    # home2 (072058)
    "avatar_bot":   ("Screenshot 2026-11-18 072058.png", 232, 136, 298, 192),
    "avatar_coach2":("Screenshot 2026-11-18 072058.png", 418, 136, 488, 192),
    "icon_puzzle_green2": ("Screenshot 2026-11-18 072058.png", 44, 139, 100, 194),
    # watch (072021)
    "icon_watch_title": ("Screenshot 2026-11-18 072021.png", 220, 23, 257, 52),
    "logo_titled":  ("Screenshot 2026-11-18 072021.png", 66, 140, 150, 204),
    "logo_total":   ("Screenshot 2026-11-18 072021.png", 66, 206, 150, 272),
    "logo_club":    ("Screenshot 2026-11-18 072021.png", 66, 276, 150, 340),
    "logo_speed":   ("Screenshot 2026-11-18 072021.png", 64, 342, 150, 406),
    "news_photo":   ("Screenshot 2026-11-18 072021.png", 52, 467, 492, 718),
    # learn (072013)
    "icon_learn_title": ("Screenshot 2026-11-18 072013.png", 216, 26, 250, 54),
    "avatar_coach_bubble": ("Screenshot 2026-11-18 072013.png", 33, 83, 94, 152),
    "piece3d_pawn":   ("Screenshot 2026-11-18 072013.png", 320, 190, 365, 272),
    "piece3d_queen":  ("Screenshot 2026-11-18 072013.png", 161, 300, 220, 403),
    "piece3d_rook":   ("Screenshot 2026-11-18 072013.png", 318, 438, 368, 522),
    "piece3d_bishop": ("Screenshot 2026-11-18 072013.png", 163, 568, 218, 658),
    "piece3d_knight": ("Screenshot 2026-11-18 072013.png", 318, 703, 372, 778),
    # new game (072117)
    "icon_hand_pawn": ("Screenshot 2026-11-18 072117.png", 193, 23, 227, 57),
    "icon_stopwatch": ("Screenshot 2026-11-18 072117.png", 208, 90, 234, 117),
    "icon_medal":     ("Screenshot 2026-11-18 072117.png", 203, 216, 229, 242),
    "icon_playfriend":("Screenshot 2026-11-18 072117.png", 203, 270, 229, 297),
    "icon_playbots":  ("Screenshot 2026-11-18 072117.png", 216, 320, 242, 347),
    "icon_playcoach": ("Screenshot 2026-11-18 072117.png", 211, 368, 239, 397),
    # new game expanded (072125)
    "icon_custom":  ("Screenshot 2026-11-18 072125.png", 193, 453, 220, 480),
    "icon_inperson":("Screenshot 2026-11-18 072125.png", 196, 503, 222, 530),
    # friends (072242)
    "icon_fb":      ("Screenshot 2026-11-18 072242.png", 313, 96, 342, 127),
    "icon_search_contacts": ("Screenshot 2026-11-18 072242.png", 60, 98, 94, 127),
    "icon_invite":  ("Screenshot 2026-11-18 072242.png", 73, 156, 107, 187),
    "icon_link":    ("Screenshot 2026-11-18 072242.png", 301, 156, 335, 187),
    "icon_search_bar": ("Screenshot 2026-11-18 072242.png", 33, 216, 57, 240),
    # game (072146)
    "avatar_grey":  ("Screenshot 2026-11-18 072146.png", 22, 139, 62, 181),
    # premium icons
    "icon_diamond": ("Screenshot 2026-11-18 072258.png", 293, 178, 347, 237),
    "icon_crown":   ("Screenshot 2026-11-18 072307.png", 294, 180, 346, 225),
    "icon_star":    ("Screenshot 2026-11-18 072315.png", 293, 176, 347, 234),
    "icon_heart":   ("Screenshot 2026-11-18 072320.png", 296, 176, 352, 230),
    # game history (071939)
    "icon_folder":  ("Screenshot 2026-11-18 071939.png", 168, 614, 222, 666),
    "icon_play_online": ("Screenshot 2026-11-18 071939.png", 146, 708, 174, 734),
    "icon_play_bots":   ("Screenshot 2026-11-18 071939.png", 146, 756, 174, 782),
    "icon_play_coach":  ("Screenshot 2026-11-18 071939.png", 146, 801, 174, 827),
}

files = {}
for name, (f, x1, y1, x2, y2) in CROPS.items():
    if f not in files:
        files[f] = Image.open(os.path.join(SRC, f)).convert("RGB")
    im = files[f]
    # coordinates are already in the file's own pixel space (read off grids)
    crop = im.crop((x1, y1, x2, y2))
    crop.save(os.path.join(OUT, name + ".png"))

# upscale small crops 3x for crispness when Android scales them
for name in list(CROPS.keys()):
    p = os.path.join(OUT, name + ".png")
    im = Image.open(p)
    if im.width < 140:
        im.resize((im.width * 3, im.height * 3), Image.LANCZOS).save(p)

# verification sheets
from collections import defaultdict
groups = defaultdict(list)
for name, (f, *_r) in CROPS.items():
    groups[f].append(name)

try:
    font = ImageFont.truetype("arialbd.ttf", 14)
except Exception:
    font = ImageFont.load_default()

for f, names in groups.items():
    imgs = [Image.open(os.path.join(OUT, n + ".png")) for n in names]
    pad = 10
    row_h = max(i.height for i in imgs) + 36
    total_w = sum(i.width for i in imgs) + pad * (len(imgs) + 1)
    sheet = Image.new("RGB", (total_w, row_h), (15, 15, 15))
    d = ImageDraw.Draw(sheet)
    x = pad
    for n, i in zip(names, imgs):
        sheet.paste(i, (x, 26))
        d.text((x, 4), n, fill=(255, 220, 80), font=font)
        x += i.width + pad
    sheet.save(os.path.join(OUT, "_sheet_" + f.split(" ")[-1].replace(".png", "") + ".png"))
print("done", len(CROPS), "crops")
