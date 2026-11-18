from PIL import Image, ImageDraw, ImageFilter
import os

GEN = r"C:\Users\User\WorkBuddy AI\2026-11-18-07-24-05\ChessApp\tools\gen\Recreate_this_exact_chess_app__2026-11-18T03-39-39.png"
OUT = r"C:\Users\User\WorkBuddy AI\2026-11-18-07-24-05\ChessApp\app\src\main\res"

img = Image.open(GEN).convert("RGB")
w, h = img.size

# Crop into the inner rounded square (remove outer dark margin)
crop = img.crop((70, 70, w - 70, h - 70))
w, h = crop.size

# Paint over the AI watermark (bottom-right) with surrounding color
patch_x0, patch_y0 = int(w * 0.72), int(h * 0.88)
sample = crop.crop((int(w*0.55), int(h*0.88), int(w*0.70), int(h*0.99)))
# use median-ish average color
px = list(sample.getdata())
avg = tuple(sum(c[i] for c in px) // len(px) for i in range(3))
patch = Image.new("RGB", (w - patch_x0, h - patch_y0), avg)
mask = Image.new("L", patch.size, 0)
ImageDraw.Draw(mask).rounded_rectangle([0, 0, patch.size[0], patch.size[1]], radius=40, fill=255)
mask = mask.filter(ImageFilter.GaussianBlur(6))
crop.paste(patch, (patch_x0, patch_y0), mask)

master = crop.resize((512, 512), Image.LANCZOS)

def rounded(im, size, radius_pct):
    s = im.resize((size, size), Image.LANCZOS)
    r = int(size * radius_pct)
    m = Image.new("L", (size, size), 0)
    d = ImageDraw.Draw(m)
    d.rounded_rectangle([0, 0, size, size], radius=r, fill=255)
    out = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    out.paste(s, (0, 0), m)
    return out

mipmap = os.path.join(OUT, "mipmap")
os.makedirs(mipmap, exist_ok=True)
rounded(master, 512, 0.18).save(os.path.join(mipmap, "ic_launcher.png"))
print("icon written")
