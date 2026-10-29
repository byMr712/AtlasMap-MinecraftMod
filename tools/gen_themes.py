# Generates UI theme textures, light tool icons, tintable (grey) marker icons and the flat icon catalog.
# Run: python tools/gen_themes.py
import json
import os
import re

import numpy as np
from PIL import Image

ROOT = os.path.join(os.path.dirname(__file__), "..")
ASSETS = os.path.join(ROOT, "src", "client", "resources", "assets", "mapminecraft")
GUI = os.path.join(ASSETS, "textures", "gui")
rng = np.random.default_rng(26)


# ------------------------------------------------------------------ seamless noise helpers

def blur(a, r):
    """Box blur with wrap-around, so the texture stays tileable."""
    out = a.copy()
    for axis in (0, 1):
        acc = np.zeros_like(out)
        for d in range(-r, r + 1):
            acc += np.roll(out, d, axis=axis)
        out = acc / (2 * r + 1)
    return out


def noise(size, r):
    n = blur(rng.random((size, size)), r)
    n -= n.min()
    return n / max(1e-6, n.max())


def save(arr, name, scale=2):
    img = Image.fromarray(np.clip(arr, 0, 255).astype(np.uint8), "RGB")
    if scale > 1:
        img = img.resize((img.width * scale, img.height * scale), Image.NEAREST)
    img.save(os.path.join(GUI, "themes", name + ".png"))


def colorize(t, c0, c1):
    c0 = np.array(c0, float)
    c1 = np.array(c1, float)
    return c0 + (c1 - c0) * t[..., None]


def hexrgb(h):
    return ((h >> 16) & 255, (h >> 8) & 255, h & 255)


S = 128  # generated at 128 and upscaled 2x -> chunky 256x256 pixel-art tiles


def stone(base, dark, light, speck=0.06):
    t = 0.55 * noise(S, 1) + 0.45 * noise(S, 4)
    img = colorize(t, dark, light)
    m = rng.random((S, S)) < speck
    img[m] = img[m] * 0.82
    m = rng.random((S, S)) < speck / 2
    img[m] = np.minimum(255, img[m] * 1.1)
    return img


def deepslate(dark, light):
    t = 0.5 * noise(S, 1)
    rows = blur(rng.random((S, 1)).repeat(S, 1), 0)
    streak = (np.sin(np.arange(S)[:, None] / S * np.pi * 16 + rows * 2) + 1) / 2
    t = t + 0.5 * blur(streak, 1)
    t = (t - t.min()) / (t.max() - t.min())
    return colorize(t, dark, light)


def planks(dark, light):
    img = np.zeros((S, S, 3))
    board = 16
    grain = noise(S, 0) * 0.35 + blur(rng.random((S, S)), 2) * 0.0
    for b in range(S // board):
        tone = rng.uniform(0.35, 0.75)
        y0 = b * board
        stripe = np.linspace(0, np.pi * rng.uniform(4, 9), S)
        line = (np.sin(stripe + rng.uniform(0, 6)) + 1) / 2
        for y in range(y0, y0 + board):
            t = tone + 0.25 * (line - 0.5) + grain[y] * 0.5 - 0.15
            img[y] = colorize(np.clip(t, 0, 1), dark, light)
        img[y0] = colorize(np.full(S, 0.0), dark, light) * 0.8
        # nail-free joint every board at a random offset
        jx = rng.integers(0, S)
        img[y0:y0 + board, jx] *= 0.8
    return img


def bricks(mortar, dark, light):
    img = np.zeros((S, S, 3))
    t = noise(S, 1)
    bh, bw = 8, 16
    for y in range(S):
        row = y // bh
        off = (bw // 2) if row % 2 else 0
        for x in range(S):
            if y % bh == 0 or (x + off) % bw == 0:
                img[y, x] = mortar
            else:
                img[y, x] = colorize(np.array(t[y, x] * 0.8 + 0.2 * ((x + off) // bw % 3) / 2), dark, light)
    return img


def flat(c, jitter=1.5):
    img = np.empty((S, S, 3))
    img[:] = c
    img += (rng.random((S, S, 1)) - 0.5) * jitter
    return img


def gen_textures():
    os.makedirs(os.path.join(GUI, "themes"), exist_ok=True)
    save(stone(None, (150, 150, 150), (190, 190, 188)), "stone")
    save(stone(None, (128, 128, 128), (165, 165, 163)), "stone_dark")
    save(deepslate((46, 46, 52), (72, 72, 80)), "deepslate")
    save(deepslate((62, 62, 70), (90, 90, 100)), "deepslate_dark")
    save(flat((244, 245, 247)), "modern_light")
    save(flat((226, 230, 235)), "modern_light_dark")
    save(flat((32, 35, 41)), "modern_dark")
    save(flat((46, 51, 60)), "modern_dark_dark")
    save(planks((168, 128, 78), (214, 178, 120)), "oak")
    save(planks((140, 104, 60), (186, 148, 96)), "oak_dark")
    save(bricks((34, 14, 18), (70, 28, 32), (104, 44, 46)), "nether")
    save(bricks((44, 18, 22), (92, 36, 40), (128, 56, 56)), "nether_dark")
    save(stone(None, (214, 212, 158), (236, 234, 186), 0.08), "end")
    save(stone(None, (196, 192, 136), (220, 216, 162), 0.08), "end_dark")


# ------------------------------------------------------------------ light tool icons for dark themes

def gen_light_tools():
    src = os.path.join(GUI, "tools")
    dst = os.path.join(GUI, "tools_light")
    os.makedirs(dst, exist_ok=True)
    for f in os.listdir(src):
        if not f.endswith(".png"):
            continue
        a = np.array(Image.open(os.path.join(src, f)).convert("RGBA")).astype(float)
        lum = a[..., 0] * 0.299 + a[..., 1] * 0.587 + a[..., 2] * 0.114
        sat = a[..., :3].max(-1) - a[..., :3].min(-1)
        ink = (lum < 110) & (sat < 70) & (a[..., 3] > 0)
        # dark ink becomes pale ink; brightness order is inverted so details survive
        v = 235 - lum[ink] * 0.6
        a[ink, 0] = v
        a[ink, 1] = v * 0.97
        a[ink, 2] = v * 0.9
        Image.fromarray(a.astype(np.uint8), "RGBA").save(os.path.join(dst, f))


# ------------------------------------------------------------------ flat icon catalog + tint textures

DYES = {
    "white": 0xF0F0F0, "light_gray": 0x9D9D97, "gray": 0x5A6064, "black": 0x2A2A30, "brown": 0x835432,
    "red": 0xB02E26, "orange": 0xF9801D, "yellow": 0xFED83D, "lime": 0x80C71F, "green": 0x5E7C16,
    "cyan": 0x169C9C, "light_blue": 0x3AB3DA, "blue": 0x3C44AA, "purple": 0x8932B8, "magenta": 0xC74EBD,
    "pink": 0xF38BAA,
}
GEMS = {"sapphire": "blue", "topaz": "yellow", "onyx": "black", "gem_amethyst": "purple"}
RENAME = {
    "banner_white": ("Знамя", "Banner"), "candle_white": ("Свеча", "Candle"),
    "shape_circle_white": ("Круг", "Circle"), "shape_square_white": ("Квадрат", "Square"),
    "shape_triangle_white": ("Треугольник", "Triangle"), "shape_diamond_white": ("Ромб", "Diamond shape"),
    "shape_hexagon_white": ("Шестиугольник", "Hexagon"), "shape_star_white": ("Звёздочка", "Star shape"),
    "shape_plus_white": ("Плюс", "Plus"), "shape_ring_white": ("Кольцо", "Ring"),
    "ruby": ("Самоцвет", "Gem"),
}
MAX_NUMBER = 20


def alias_of(icon, all_ids):
    """Returns (base, colour) when the icon is just a coloured copy of another icon."""
    if icon in GEMS:
        return "ruby", DYES[GEMS[icon]]
    m = re.match(r"^(arrow_[nsew]{1,2})_blue$", icon)
    if m:
        return m.group(1), DYES["blue"]
    for dye in sorted(DYES, key=len, reverse=True):
        if icon.endswith("_" + dye):
            base = icon[: -len(dye) - 1]
            if base.startswith("shape_") or base in ("banner", "candle"):
                if dye == "white":
                    return None
                return base + "_white", DYES[dye]
            if base == "bed":
                return "bed", DYES[dye]
            if base in all_ids:
                return base, DYES[dye]
    return None


def tint_texture(src, dst):
    a = np.array(Image.open(src).convert("RGBA")).astype(float)
    lum = a[..., 0] * 0.299 + a[..., 1] * 0.587 + a[..., 2] * 0.114
    op = a[..., 3] > 0
    ref = np.percentile(lum[op], 85) if op.any() else 255
    g = np.clip(lum * (235.0 / max(ref, 1)), 0, 255)
    out = np.stack([g, g, g, a[..., 3]], -1)
    Image.fromarray(out.astype(np.uint8), "RGBA").save(dst)


def gen_icons():
    path = os.path.join(ASSETS, "icons.json")
    with open(path, encoding="utf-8") as f:
        data = json.load(f)
    if "groups" not in data:
        print("icons.json is already flat, skipping")
        return
    entries = [i for g in data["groups"] for i in g["icons"]]
    ids = {i["id"] for i in entries}
    keep, hidden, aliases = [], [], {}
    for e in entries:
        a = alias_of(e["id"], ids)
        if a:
            aliases[e["id"]] = {"icon": a[0], "color": "#%06X" % a[1]}
            continue
        if e["id"].startswith("num_") and int(e["id"][4:]) > MAX_NUMBER:
            hidden.append(e)
            continue
        if e["id"] in RENAME:
            e = {"id": e["id"], "ru": RENAME[e["id"]][0], "en": RENAME[e["id"]][1]}
        keep.append(e)
    markers = os.path.join(GUI, "markers")
    tint = os.path.join(GUI, "markers_tint")
    os.makedirs(tint, exist_ok=True)
    for old in aliases:
        p = os.path.join(markers, old + ".png")
        if os.path.exists(p):
            os.remove(p)
    for e in keep + hidden:
        tint_texture(os.path.join(markers, e["id"] + ".png"), os.path.join(tint, e["id"] + ".png"))
    with open(path, "w", encoding="utf-8") as f:
        json.dump({"icons": keep, "hidden": hidden, "aliases": aliases}, f, ensure_ascii=False, indent=1)
    print("icons: %d shown, %d hidden, %d colour copies removed" % (len(keep), len(hidden), len(aliases)))


if __name__ == "__main__":
    gen_textures()
    gen_light_tools()
    gen_icons()
