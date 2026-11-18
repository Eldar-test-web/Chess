"""Overlay a labeled coordinate grid on screenshots for precise crop calibration."""
from PIL import Image, ImageDraw, ImageFont
import os

SRC = r"C:\Users\User\OneDrive\Pictures\Screenshots"
OUT = r"C:\Users\User\WorkBuddy AI\2026-11-18-07-24-05\ChessApp\diagnostics"
os.makedirs(OUT, exist_ok=True)

FILES = [
    "Screenshot 2026-11-18 071929.png",
    "Screenshot 2026-11-18 071939.png",
    "Screenshot 2026-11-18 072021.png",
    "Screenshot 2026-11-18 072058.png",
    "Screenshot 2026-11-18 072329.png",
    "Screenshot 2026-11-18 072242.png",
    "Screenshot 2026-11-18 072117.png",
    "Screenshot 2026-11-18 072125.png",
    "Screenshot 2026-11-18 072146.png",
    "Screenshot 2026-11-18 072258.png",
    "Screenshot 2026-11-18 072307.png",
    "Screenshot 2026-11-18 072315.png",
    "Screenshot 2026-11-18 072320.png",
    "Screenshot 2026-11-18 072013.png",
]

try:
    font = ImageFont.truetype("arial.ttf", 13)
    font_big = ImageFont.truetype("arialbd.ttf", 15)
except Exception:
    font = ImageFont.load_default()
    font_big = font

for f in FILES:
    im = Image.open(os.path.join(SRC, f)).convert("RGB")
    d = ImageDraw.Draw(im)
    W, H = im.size
    step = 50
    for x in range(0, W, step):
        c = (255, 60, 60) if x % 100 == 0 else (90, 90, 90)
        d.line([(x, 0), (x, H)], fill=c, width=1)
        if x % 100 == 0:
            d.text((x + 2, 2), str(x), fill=(255, 220, 60), font=font)
            d.text((x + 2, H - 18), str(x), fill=(255, 220, 60), font=font)
    for y in range(0, H, step):
        c = (255, 60, 60) if y % 100 == 0 else (90, 90, 90)
        d.line([(0, y), (W, y)], fill=c, width=1)
        if y % 100 == 0:
            d.text((2, y + 2), str(y), fill=(255, 220, 60), font=font)
            d.text((W - 40, y + 2), str(y), fill=(255, 220, 60), font=font)
    im.save(os.path.join(OUT, "grid_" + f.split(" ")[-1].replace(".png", "") + ".png"))
print("grids done")
