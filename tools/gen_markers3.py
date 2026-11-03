# Icon families that bring AtlasMapExtended to 500 marker icons, plus the icon catalog (icons.json).
# Called from gen_textures.py: build(MARKERS, PAL, grid_fn, save_fn, root_dir)
import json
import math
import os

from PIL import Image, ImageDraw

import gen_markers2 as m2
from gen_lang import ICONS as BASE_NAMES

TOTAL = 500

# Minecraft dye colours, muted for parchment: (dark, mid, light)
DYES = {
    "white": ((180, 176, 166), (230, 228, 220), (250, 248, 240)),
    "light_gray": ((120, 120, 112), (160, 160, 152), (196, 196, 188)),
    "gray": ((64, 64, 60), (96, 96, 92), (130, 130, 124)),
    "black": ((20, 18, 20), (40, 38, 40), (74, 72, 76)),
    "brown": ((86, 56, 30), (122, 82, 48), (156, 112, 72)),
    "red": ((110, 30, 24), (170, 48, 40), (206, 90, 70)),
    "orange": ((160, 80, 24), (220, 120, 40), (240, 160, 80)),
    "yellow": ((170, 140, 30), (230, 196, 50), (246, 224, 110)),
    "lime": ((86, 136, 30), (128, 190, 50), (170, 220, 90)),
    "green": ((52, 76, 24), (80, 110, 40), (116, 150, 64)),
    "cyan": ((22, 96, 104), (36, 140, 150), (80, 184, 190)),
    "light_blue": ((56, 112, 160), (90, 160, 214), (140, 196, 236)),
    "blue": ((32, 44, 110), (52, 70, 160), (90, 110, 200)),
    "purple": ((80, 34, 120), (120, 56, 170), (160, 100, 210)),
    "magenta": ((136, 44, 128), (190, 70, 180), (220, 120, 210)),
    "pink": ((176, 96, 124), (230, 140, 170), (246, 184, 204)),
}
DYE_RU = {"white": "белый", "light_gray": "светло-серый", "gray": "серый", "black": "чёрный", "brown": "коричневый",
          "red": "красный", "orange": "оранжевый", "yellow": "жёлтый", "lime": "лаймовый", "green": "зелёный",
          "cyan": "бирюзовый", "light_blue": "голубой", "blue": "синий", "purple": "фиолетовый",
          "magenta": "пурпурный", "pink": "розовый"}
DYE_EN = {k: k.replace("_", " ") for k in DYES}


def ru_adj(dye, gender):
    """Russian adjective agreement: m / f / n."""
    w = DYE_RU[dye]
    if gender == "m":
        return w
    if w.endswith("ий"):
        return w[:-2] + ("яя" if gender == "f" else "ее")
    return w[:-2] + ("ая" if gender == "f" else "ое")


def names(dye, ru_noun, gender, en_noun):
    a = ru_adj(dye, gender)
    return a[0].upper() + a[1:] + " " + ru_noun, DYE_EN[dye].capitalize() + " " + en_noun


def dye_pal(pal, dye):
    d, m, l = DYES[dye]
    p = dict(pal)
    p["1"], p["2"], p["3"] = d, m, l
    return p


# ------------------------------------------------------------------ glyphs
DIGITS = {
    "0": [".##.", "#..#", "#..#", "#..#", "#..#", "#..#", ".##."],
    "1": ["..#.", ".##.", "..#.", "..#.", "..#.", "..#.", ".###"],
    "2": [".##.", "#..#", "...#", "..#.", ".#..", "#...", "####"],
    "3": ["###.", "...#", "...#", ".##.", "...#", "...#", "###."],
    "4": ["#..#", "#..#", "#..#", "####", "...#", "...#", "...#"],
    "5": ["####", "#...", "###.", "...#", "...#", "#..#", ".##."],
    "6": [".##.", "#...", "#...", "###.", "#..#", "#..#", ".##."],
    "7": ["####", "...#", "..#.", "..#.", ".#..", ".#..", ".#.."],
    "8": [".##.", "#..#", "#..#", ".##.", "#..#", "#..#", ".##."],
    "9": [".##.", "#..#", "#..#", ".###", "...#", "...#", ".##."],
}
NARROW = {
    "0": "###/#.#/#.#/#.#/#.#/#.#/###", "1": ".#./##./.#./.#./.#./.#./###", "2": "###/..#/..#/###/#../#../###",
    "3": "###/..#/..#/###/..#/..#/###", "4": "#.#/#.#/#.#/###/..#/..#/..#", "5": "###/#../#../###/..#/..#/###",
    "6": "###/#../#../###/#.#/#.#/###", "7": "###/..#/..#/.#./.#./.#./.#.", "8": "###/#.#/#.#/###/#.#/#.#/###",
    "9": "###/#.#/#.#/###/..#/..#/###",
}
SMALL = {
    "0": ["###", "#.#", "#.#", "#.#", "###"], "1": [".#.", "##.", ".#.", ".#.", "###"],
}
LETTERS = {
    "A": ".###./#...#/#...#/#####/#...#/#...#/#...#", "B": "####./#...#/#...#/####./#...#/#...#/####.",
    "C": ".###./#...#/#..../#..../#..../#...#/.###.", "D": "####./#...#/#...#/#...#/#...#/#...#/####.",
    "E": "#####/#..../#..../####./#..../#..../#####", "F": "#####/#..../#..../####./#..../#..../#....",
    "G": ".###./#...#/#..../#.###/#...#/#...#/.###.", "H": "#...#/#...#/#...#/#####/#...#/#...#/#...#",
    "I": ".###./..#../..#../..#../..#../..#../.###.", "J": "..###/...#./...#./...#./#..#./#..#./.##..",
    "K": "#...#/#..#./#.#../##.../#.#../#..#./#...#", "L": "#..../#..../#..../#..../#..../#..../#####",
    "M": "#...#/##.##/#.#.#/#.#.#/#...#/#...#/#...#", "N": "#...#/##..#/#.#.#/#..##/#...#/#...#/#...#",
    "O": ".###./#...#/#...#/#...#/#...#/#...#/.###.", "P": "####./#...#/#...#/####./#..../#..../#....",
    "Q": ".###./#...#/#...#/#...#/#.#.#/#..#./.##.#", "R": "####./#...#/#...#/####./#.#../#..#./#...#",
    "S": ".###./#...#/#..../.###./....#/#...#/.###.", "T": "#####/..#../..#../..#../..#../..#../..#..",
    "U": "#...#/#...#/#...#/#...#/#...#/#...#/.###.", "V": "#...#/#...#/#...#/#...#/#...#/.#.#./..#..",
    "W": "#...#/#...#/#...#/#.#.#/#.#.#/##.##/#...#", "X": "#...#/#...#/.#.#./..#../.#.#./#...#/#...#",
    "Y": "#...#/#...#/.#.#./..#../..#../..#../..#..", "Z": "#####/....#/...#./..#../.#.../#..../#####",
}
CYRILLIC = {
    "Б": "#####/#..../#..../####./#...#/#...#/####.", "Г": "#####/#..../#..../#..../#..../#..../#....",
    "Д": "..##./.#.#./.#.#./.#.#./.#.#./#####/#...#", "Ж": "#.#.#/#.#.#/.###./..#../.###./#.#.#/#.#.#",
    "З": ".###./#...#/....#/..##./....#/#...#/.###.", "И": "#...#/#...#/#..##/#.#.#/##..#/#...#/#...#",
    "Й": ".#.#./#...#/#..##/#.#.#/##..#/#...#/#...#", "Л": "..###/.#..#/.#..#/.#..#/.#..#/.#..#/#...#",
    "П": "#####/#...#/#...#/#...#/#...#/#...#/#...#", "У": "#...#/#...#/#...#/.####/....#/#...#/.###.",
    "Ф": "..#../.###./#.#.#/#.#.#/#.#.#/.###./..#..", "Ц": "#..#./#..#./#..#./#..#./#..#./#####/....#",
    "Ч": "#...#/#...#/#...#/.####/....#/....#/....#", "Ш": "#.#.#/#.#.#/#.#.#/#.#.#/#.#.#/#.#.#/#####",
    "Щ": "#.#.#/#.#.#/#.#.#/#.#.#/#.#.#/#####/....#", "Ъ": "##.../.#.../.#.../.###./.#..#/.#..#/.###.",
    "Ы": "#...#/#...#/#...#/###.#/#.#.#/#.#.#/###.#", "Ь": "#..../#..../#..../####./#...#/#...#/####.",
    "Э": ".###./#...#/....#/..###/....#/#...#/.###.", "Ю": "#..#./#.#.#/#.#.#/###.#/#.#.#/#.#.#/#..#.",
    "Я": ".####/#...#/#...#/.####/..#.#/.#..#/#...#", "Ё": ".#.#./...../#####/#..../####./#..../#####",
}
CYR_ID = {"Б": "be", "Г": "ge", "Д": "de", "Ж": "zhe", "З": "ze", "И": "i", "Й": "ishort", "Л": "el", "П": "pe",
          "У": "u", "Ф": "ef", "Ц": "tse", "Ч": "che", "Ш": "sha", "Щ": "shcha", "Ъ": "hard", "Ы": "yeru",
          "Ь": "soft", "Э": "e", "Ю": "yu", "Я": "ya", "Ё": "yo"}

INK = (58, 38, 22, 255)


def badge(glyphs, gap=1, square=False, fill=(240, 228, 196), rim=(184, 140, 44)):
    """A round (or square) parchment medallion with ink glyphs in the middle."""
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for y in range(16):
        for x in range(16):
            dx, dy = x + 0.5 - 8, y + 0.5 - 8
            dist = max(abs(dx), abs(dy)) if square else math.hypot(dx, dy)
            limit = 7.0 if square else 7.6
            if dist <= limit:
                if dist > limit - 0.9:
                    c = INK
                elif dist > limit - 1.9:
                    c = rim + (255,)
                else:
                    c = fill + (255,)
                img.putpixel((x, y), c)
    widths = [len(g[0]) for g in glyphs]
    total = sum(widths) + gap * (len(glyphs) - 1)
    h = len(glyphs[0])
    x0 = (16 - total) // 2
    y0 = (16 - h) // 2
    for g in glyphs:
        for yy, row in enumerate(g):
            for xx, ch in enumerate(row):
                if ch == "#":
                    img.putpixel((x0 + xx, y0 + yy), INK)
        x0 += len(g[0]) + gap
    return img


def shape_mask(kind):
    m = [[False] * 16 for _ in range(16)]
    if kind in ("star", "triangle", "hexagon"):
        im = Image.new("L", (64, 64), 0)
        d = ImageDraw.Draw(im)
        c = 32
        if kind == "star":
            pts = []
            for i in range(10):
                a = -math.pi / 2 + i * math.pi / 5
                r = 27 if i % 2 == 0 else 11.5
                pts.append((c + math.cos(a) * r, c + 2 + math.sin(a) * r))
        elif kind == "triangle":
            pts = [(c, 7), (c + 25, 54), (c - 25, 54)]
        else:
            pts = [(c + math.cos(math.pi / 6 + i * math.pi / 3) * 26, c + math.sin(math.pi / 6 + i * math.pi / 3) * 26)
                   for i in range(6)]
        d.polygon(pts, fill=255)
        im = im.resize((16, 16), Image.BOX)
        for y in range(16):
            for x in range(16):
                m[y][x] = im.getpixel((x, y)) >= 128
        return m
    for y in range(16):
        for x in range(16):
            dx, dy = x + 0.5 - 8, y + 0.5 - 8
            r = math.hypot(dx, dy)
            if kind == "circle":
                v = r <= 6.3
            elif kind == "ring":
                v = 3.3 <= r <= 6.3
            elif kind == "square":
                v = max(abs(dx), abs(dy)) <= 5.5
            elif kind == "diamond":
                v = abs(dx) + abs(dy) <= 6.6
            elif kind == "plus":
                v = (abs(dx) <= 2 and abs(dy) <= 6) or (abs(dy) <= 2 and abs(dx) <= 6)
            else:
                v = False
            m[y][x] = v
    return m


def shape_img(kind, dye):
    d, mid, l = DYES[dye]
    m = shape_mask(kind)
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    inside = lambda x, y: 0 <= x < 16 and 0 <= y < 16 and m[y][x]
    for y in range(16):
        for x in range(16):
            if m[y][x]:
                if not inside(x - 1, y - 1) or not inside(x, y - 1) and not inside(x - 1, y):
                    c = l
                elif not inside(x + 1, y + 1):
                    c = d
                else:
                    c = mid
                img.putpixel((x, y), c + (255,))
            elif inside(x + 1, y) or inside(x - 1, y) or inside(x, y + 1) or inside(x, y - 1):
                img.putpixel((x, y), INK)
    return img


ARROW_UP = ["", "", "2", "222", "22222", "2222222", "222222222", "222", "222", "222", "222", "222", "222"]
ARROW_DIAG = [">.........22222", ">..........2222", ">.........22222", ">........222.22", ">.......222...2",
              ">......222", ">.....222", ">....222", ">...222", ">..222", ">..22"]

BANNER = ["", "lllllllllllll", "322222221", "322222221", "322333221", "323222321", "322333221", "322222221",
          "322222221", "322222221", "322222221", "3222.2221", "322...221"]
CANDLE = ["", "", "o", "oYo", "k", "33222", "32221", "32221", "32221", "32221", "32221", "32221", "33221"]


def recolor(rows, mapping):
    return ["".join(mapping.get(c, c) for c in r) for r in rows]


def build(MARKERS, PAL, grid_fn, save_fn, root, base_groups):
    """Creates the extra icons and writes icons.json. Returns the total number of icons."""
    groups = {k: list(v) for k, v in base_groups.items()}
    groups.update({"decor": [], "shapes": [], "arrows": [], "numbers": [], "letters": []})
    nm = {k: v for k, v in BASE_NAMES.items()}

    def add(group, iid, img, ru, en):
        save_fn(img, "markers", iid)
        groups[group].append(iid)
        nm[iid] = (ru, en)

    def add_grid(group, iid, rows, pal, ru, en, auto_outline=True):
        img = m2.render(rows, pal) if auto_outline else render_plain(rows, pal)
        add(group, iid, img, ru, en)

    def render_plain(rows, pal):
        img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
        for y, row in enumerate(rows):
            for x, ch in enumerate(row.ljust(16, ".")):
                if ch != "." and pal.get(ch) is not None:
                    img.putpixel((x, y), pal[ch] + (255,))
        return img

    # coloured pins and flags (the hand drawn ones exist in red + a few colours already)
    for dye in ("light_gray", "gray", "brown", "orange", "lime", "cyan", "light_blue", "magenta", "pink"):
        add_grid("basic", "pin_" + dye, recolor(MARKERS["pin"], {"r": "2", "R": "3", "d": "1"}), dye_pal(PAL, dye),
                 *names(dye, "булавка", "f", "pin"), auto_outline=False)
    for dye in ("black", "light_gray", "gray", "brown", "orange", "lime", "cyan", "light_blue", "magenta", "pink"):
        add_grid("basic", "flag_" + dye, recolor(MARKERS["flag"], {"r": "2", "R": "3"}), dye_pal(PAL, dye),
                 *names(dye, "флаг", "m", "flag"), auto_outline=False)
    add_grid("basic", "heart_blue", recolor(MARKERS["heart"], {"r": "2", "R": "3", "d": "1"}), dye_pal(PAL, "blue"),
             "Синее сердце", "Blue heart", auto_outline=False)
    for dye in ("red", "blue", "green", "purple"):
        add_grid("basic", "star_" + dye, recolor(MARKERS["star"], {"y": "2", "Y": "3"}), dye_pal(PAL, dye),
                 *names(dye, "звезда", "f", "star"), auto_outline=False)
    # buildings
    for dye in ("blue", "green", "purple", "brown", "black"):
        add_grid("build", "house_" + dye, recolor(MARKERS["house"], {"r": "2", "R": "3"}), dye_pal(PAL, dye),
                 *names(dye, "дом", "m", "house"), auto_outline=False)
    for dye in ("blue", "green", "yellow", "white", "purple"):
        add_grid("build", "tent_" + dye, recolor(m2.NEW["tent"], {"r": "2"}), dye_pal(PAL, dye),
                 *names(dye, "палатка", "f", "tent"))
    # gems
    for iid, dye, ru, en in (("ruby", "red", "Рубин", "Ruby"), ("sapphire", "blue", "Сапфир", "Sapphire"),
                             ("topaz", "yellow", "Топаз", "Topaz"), ("onyx", "black", "Оникс", "Onyx"),
                             ("gem_amethyst", "purple", "Аметистовый камень", "Amethyst gem")):
        add_grid("resources", iid, recolor(MARKERS["diamond"], {"C": "3", "c": "2"}), dye_pal(PAL, dye), ru, en,
                 auto_outline=False)
    # potions and shields
    for dye in ("red", "orange", "yellow", "lime", "cyan", "blue", "pink"):
        add_grid("items", "potion_" + dye, recolor(m2.NEW["potion"], {"V": "2"}), dye_pal(PAL, dye),
                 *names(dye, "зелье", "n", "potion"))
    for dye in ("red", "blue", "green", "yellow", "purple", "black", "white", "orange"):
        add_grid("items", "shield_" + dye, recolor(m2.NEW["shield"], {"l": "2", "r": "h" if dye != "white" else "n"}),
                 dye_pal(PAL, dye), *names(dye, "щит", "m", "shield"))
    # decor: beds, banners, candles in all 16 dyes
    for dye in DYES:
        if dye != "red":
            add_grid("decor", "bed_" + dye, recolor(MARKERS["bed"], {"r": "2", "R": "3"}), dye_pal(PAL, dye),
                     *names(dye, "кровать", "f", "bed"), auto_outline=False)
    for dye in DYES:
        add_grid("decor", "banner_" + dye, BANNER, dye_pal(PAL, dye), *names(dye, "баннер", "m", "banner"))
    for dye in DYES:
        add_grid("decor", "candle_" + dye, CANDLE, dye_pal(PAL, dye), *names(dye, "свеча", "f", "candle"))
    # shapes
    shapes = [("circle", "круг", "m", "circle"), ("square", "квадрат", "m", "square"),
              ("triangle", "треугольник", "m", "triangle"), ("diamond", "ромб", "m", "rhombus"),
              ("hexagon", "шестиугольник", "m", "hexagon"), ("star", "звёздочка", "f", "star shape"),
              ("plus", "крест", "m", "plus"), ("ring", "кольцо", "n", "ring")]
    for kind, ru, gender, en in shapes:
        for dye in DYES:
            add("shapes", "shape_" + kind + "_" + dye, shape_img(kind, dye), *names(dye, ru, gender, en))
    # arrows: 8 directions, two colours
    dirs = [("n", "вверх", "up"), ("ne", "вверх-вправо", "up-right"), ("e", "вправо", "right"),
            ("se", "вниз-вправо", "down-right"), ("s", "вниз", "down"), ("sw", "вниз-влево", "down-left"),
            ("w", "влево", "left"), ("nw", "вверх-влево", "up-left")]
    for dye, ru_c, en_c in (("red", "Красная", "Red"), ("blue", "Синяя", "Blue")):
        up = m2.render(ARROW_UP, dye_pal(PAL, dye))
        diag = m2.render(ARROW_DIAG, dye_pal(PAL, dye))
        for i, (d, ru, en) in enumerate(dirs):
            base = up if i % 2 == 0 else diag
            img = base.rotate(-90 * (i // 2), expand=False)
            add("arrows", "arrow_" + d + ("" if dye == "red" else "_blue"), img, ru_c + " стрелка " + ru,
                en_c + " arrow " + en)
    # numbers 1..100
    for n in range(1, 101):
        s = str(n)
        if n == 100:
            gl = [SMALL["1"], SMALL["0"], SMALL["0"]]
        elif n >= 10:
            gl = [NARROW[c].split("/") for c in s]
        else:
            gl = [DIGITS[c] for c in s]
        add("numbers", "num_" + s, badge(gl), "Номер " + s, "Number " + s)
    # letters: latin + cyrillic letters that look different from latin ones
    for ch, g in LETTERS.items():
        add("letters", "letter_" + ch.lower(), badge([g.split("/")], square=True), "Буква " + ch, "Letter " + ch)
    for ch, g in CYRILLIC.items():
        add("letters", "letter_cyr_" + CYR_ID[ch], badge([g.split("/")], square=True), "Буква " + ch, "Letter " + ch)

    group_names = {"basic": ("Основные", "Basic"), "build": ("Постройки", "Buildings"), "nature": ("Природа", "Nature"),
                   "resources": ("Ресурсы", "Resources"), "mobs": ("Мобы и опасность", "Mobs and danger"),
                   "items": ("Транспорт и предметы", "Travel and items"), "decor": ("Декор", "Decor"),
                   "shapes": ("Фигуры", "Shapes"), "arrows": ("Стрелки", "Arrows"), "numbers": ("Номера", "Numbers"),
                   "letters": ("Буквы", "Letters")}
    catalog = {"groups": []}
    total = 0
    seen = set()
    for gid, icons in groups.items():
        entries = []
        for iid in icons:
            assert iid not in seen, iid
            seen.add(iid)
            ru, en = nm[iid]
            entries.append({"id": iid, "ru": ru, "en": en})
        total += len(entries)
        catalog["groups"].append({"id": gid, "ru": group_names[gid][0], "en": group_names[gid][1], "icons": entries})
    with open(os.path.join(root, "icons.json"), "w", encoding="utf-8") as f:
        json.dump(catalog, f, ensure_ascii=False, indent=1)
    return total


BASE_GROUPS = {
    "basic": ["pin", "pin_blue", "pin_green", "pin_yellow", "pin_purple", "pin_white", "pin_black",
              "flag", "flag_blue", "flag_green", "flag_yellow", "flag_purple", "flag_white",
              "star", "heart", "question", "exclamation", "warning", "cross", "crown", "trophy", "key", "note",
              "hourglass"],
    "build": ["house", "castle", "tower", "church", "temple", "windmill", "lighthouse", "market",
              "tent", "igloo", "well", "bridge", "ruins", "bell", "bed", "campfire", "portal", "end"],
    "nature": ["tree", "pine", "palm", "cactus", "flower", "mushroom", "apple", "farm",
               "mountain", "cave", "volcano", "wave", "snowflake", "sun", "moon", "lightning"],
    "resources": ["mine", "diamond", "emerald", "gold", "iron", "copper", "coal", "redstone",
                  "lapis", "amethyst", "log", "fish", "chest", "coin"],
    "mobs": ["skull", "wither", "creeper", "zombie", "spider", "slime", "ghast", "enderman",
             "fire", "tnt", "sword", "cow", "pig", "sheep", "chicken", "horse", "wolf", "cat", "bee"],
    "items": ["anchor", "boat", "minecart", "elytra", "axe", "bow", "shield", "potion",
              "anvil", "crafting", "furnace", "book", "cauldron", "compass", "bucket", "cake"],
}
