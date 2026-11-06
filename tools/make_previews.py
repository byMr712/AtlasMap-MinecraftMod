# Builds YouTube thumbnails (1280x720) from the in-game screenshots in thumbnails/.
# Only overlays a chat "prompt" bubble and a little text; the screenshot itself is untouched.
import os

from PIL import Image, ImageDraw, ImageEnhance, ImageFilter, ImageFont

DIR = os.path.join(os.path.dirname(__file__), "..", "thumbnails")
F = os.path.join(os.environ["WINDIR"], "Fonts") + os.sep
W, H = 1280, 720


def base(name, crop=None):
    im = Image.open(os.path.join(DIR, name)).convert("RGB")
    if crop:
        im = im.crop(crop)
    im = im.resize((W, H), Image.LANCZOS)
    im = ImageEnhance.Contrast(im).enhance(1.07)
    im = ImageEnhance.Color(im).enhance(1.12)
    # gentle vignette
    v = Image.new("L", (W, H), 0)
    d = ImageDraw.Draw(v)
    for i in range(40):
        d.rectangle([i * 2, i * 2, W - i * 2, H - i * 2], outline=int(110 * (1 - i / 40)))
    v = v.filter(ImageFilter.GaussianBlur(20))
    im = Image.composite(Image.new("RGB", (W, H), (25, 14, 6)), im, v)
    return im.convert("RGBA")


def shadow_box(im, box, radius, fill, outline=None, width=0, blur=14, offset=(0, 8), alpha=110):
    sh = Image.new("RGBA", im.size, (0, 0, 0, 0))
    sd = ImageDraw.Draw(sh)
    x0, y0, x1, y1 = box
    sd.rounded_rectangle([x0 + offset[0], y0 + offset[1], x1 + offset[0], y1 + offset[1]], radius, fill=(0, 0, 0, alpha))
    sh = sh.filter(ImageFilter.GaussianBlur(blur))
    im.alpha_composite(sh)
    d = ImageDraw.Draw(im)
    d.rounded_rectangle(box, radius, fill=fill, outline=outline, width=width)


def sparkle(d, cx, cy, r, color):
    """Four pointed 'AI' sparkle, drawn as a polygon (no brand logo)."""
    k = 0.28
    pts = [(cx, cy - r), (cx + r * k, cy - r * k), (cx + r, cy), (cx + r * k, cy + r * k),
           (cx, cy + r), (cx - r * k, cy + r * k), (cx - r, cy), (cx - r * k, cy - r * k)]
    d.polygon(pts, fill=color)


def prompt_bubble(im, x, y, text, size=40, max_w=None):
    font = ImageFont.truetype(F + "seguisb.ttf", size)
    d = ImageDraw.Draw(im)
    tw = d.textlength(text, font=font)
    h = int(size * 2.1)
    w = int(tw + h * 2.3)
    if max_w:
        w = min(w, max_w)
    shadow_box(im, (x, y, x + w, y + h), h // 2, fill=(255, 255, 255, 246), outline=(210, 210, 216), width=2)
    d = ImageDraw.Draw(im)
    # sparkle on the left
    cx = x + h // 2 + 4
    cy = y + h // 2
    sparkle(d, cx, cy, h * 0.30, (124, 92, 255))
    sparkle(d, cx + h * 0.27, cy - h * 0.24, h * 0.12, (255, 150, 60))
    d.text((x + h, cy), text, font=font, fill=(32, 32, 40), anchor="lm")
    # send button
    bx = x + w - h // 2 - 4
    d.ellipse([bx - h * 0.32, cy - h * 0.32, bx + h * 0.32, cy + h * 0.32], fill=(32, 32, 40))
    a = h * 0.14
    d.polygon([(bx, cy - a * 1.3), (bx + a, cy), (bx + a * 0.35, cy), (bx + a * 0.35, cy + a * 1.2),
               (bx - a * 0.35, cy + a * 1.2), (bx - a * 0.35, cy), (bx - a, cy)], fill=(255, 255, 255))
    return w, h


def reply_bubble(im, x, y, text, size=36):
    font = ImageFont.truetype(F + "seguisb.ttf", size)
    d = ImageDraw.Draw(im)
    tw = d.textlength(text, font=font)
    h = int(size * 1.9)
    w = int(tw + h * 1.6)
    shadow_box(im, (x, y, x + w, y + h), 18, fill=(124, 92, 255, 250))
    d = ImageDraw.Draw(im)
    sparkle(d, x + h * 0.48, y + h / 2, h * 0.24, (255, 255, 255))
    d.text((x + h * 0.9, y + h / 2), text, font=font, fill=(255, 255, 255), anchor="lm")


def headline(im, text, x, y, size=104, fill=(255, 246, 220), anchor="la"):
    d = ImageDraw.Draw(im)
    font = ImageFont.truetype(F + "impact.ttf", size)
    d.text((x, y), text, font=font, fill=fill, stroke_width=max(6, size // 13), stroke_fill=(36, 20, 8), anchor=anchor)


def save(im, name):
    im.convert("RGB").save(os.path.join(DIR, name), quality=95)


# A: the prompt only - the picture tells the story
im = base("clean_low.png")
prompt_bubble(im, 36, 34, "Сделай мне мод на карту для Майнкрафта")
save(im, "preview_A_prompt.png")

# B: prompt + the AI answer
im = base("clean_low.png")
prompt_bubble(im, 36, 34, "Сделай мне мод на карту для Майнкрафта")
reply_bubble(im, 36, 140, "Готово!")
save(im, "preview_B_prompt_answer.png")

# C: short headline + small prompt
im = base("clean_low.png")
headline(im, "МОД ОТ НЕЙРОСЕТИ", 40, 20, size=112)
prompt_bubble(im, 40, 150, "Сделай карту для Майнкрафта", size=32)
save(im, "preview_C_headline.png")

print("ok")
