# Extra marker icons for MapMinecraft (fill-only grids, the ink outline is added automatically).
# Imported by gen_textures.py.
from PIL import Image

EXTRA_PAL = {
    'h': (240, 236, 226), 'n': (52, 48, 46), 'i': (214, 214, 206), 'z': (96, 150, 86),
    'P': (236, 156, 160), 'q': (200, 110, 120), 'a': (150, 104, 30), 'j': (196, 108, 64),
    'J': (232, 150, 108), 'u': (78, 52, 30), 'f': (224, 72, 30),
}

INK = (58, 38, 22)
NO_OUTLINE = set("kK")


def place(rows):
    """Rows are centred around column 7.5 unless prefixed with '>' (then used as-is)."""
    out = []
    for r in rows:
        if r.startswith(">"):
            s = r[1:]
        elif r == "":
            s = ""
        else:
            start = 8 - (len(r) + 1) // 2
            s = "." * start + r
        assert len(s) <= 16, (r, len(s))
        out.append(s.ljust(16, "."))
    while len(out) < 16:
        out.append("." * 16)
    assert len(out) == 16, len(out)
    return out


def render(rows, pal):
    rows = place(rows)
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    filled = [[False] * 16 for _ in range(16)]
    for y, row in enumerate(rows):
        for x, ch in enumerate(row):
            if ch != ".":
                img.putpixel((x, y), pal[ch] + (255,))
                filled[y][x] = ch not in NO_OUTLINE
    for y in range(16):
        for x in range(16):
            if rows[y][x] != ".":
                continue
            for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                nx, ny = x + dx, y + dy
                if 0 <= nx < 16 and 0 <= ny < 16 and filled[ny][nx]:
                    img.putpixel((x, y), INK + (255,))
                    break
    return img


def ingot(light, mid, dark):
    return ["", "", "", "", "",
            light * 2 + mid * 5,
            mid + light + mid * 7,
            mid * 11,
            mid * 11,
            dark * 13]


def ore(c1, c2, c3):
    return ["", "", "",
            c1 * 3,
            c1 + c2 + c1 * 3,
            c1 * 2 + c2 + c1 * 3 + c3,
            c1 + c2 + c1 * 5 + c3 + c1,
            c1 * 5 + c2 + c1 * 4 + c3,
            c1 * 11,
            c1 * 3 + c2 + c1 * 4 + c3,
            c1 * 2 + c3 + c1 * 4,
            c1 * 5]


NEW = {
    # --- buildings
    "tower": ["", "S.S.S.S", "SSSSSSS", "SSSkSSS", "SSSkSSS", "SSSSSSS", "SsSSSsS", "SSSSSSS", "SSSkSSS", "SSSkSSS",
              "SSSSSSSSS", "SSSbbbSSS", "SSSbbbSSS", "SSSbbbSSS"],
    "windmill": ["", "ee.......ee", "ee.....ee", "ee...ee", "eebee", "bbb", "ebbbe", "eebbbee", "ee.bbb.ee",
                 "ee.bbbbb.ee", "bbbbb", "bbbbbbb", "bbbkbbb", "bbbkbbb", "bbbbbbb"],
    "church": ["y", "yyy", "y", "RRR", "RRRRR", "ttt", "tWt", "ttt", "RRRRRRRRRRR", "RRRRRRRRRRRRR",
               "ttttttttttt", "tWttbbbttWt", "tWttbbbttWt", "ttttbbbtttt", "ttttbbbtttt"],
    "tent": ["", "", "b", "r", "rrr", "rrrrr", "rrrkrrr", "rrrkkkrrr", "rrrkkkkkrrr", "rrrkkkkkkkrrr",
             "rrrkkkkkkkrrr", "GGGGGGGGGGGGG"],
    "well": ["", "", "bbbbbbbbb", "bbbbbbbbbbb", "b.......b", "b...k...b", "b..sss..b", "b.......b",
             "SSSSSSSSSSS", "SsSSsSSsSSS", "SWWWWWWWWWS", "SSsSSSsSSsS", "SSSSSSSSSSS"],
    "bridge": ["", "", "", "", "bbbbbbbbbbbbb", "SSSSSSSSSSSSS", "SsSSsSSsSSsSS", "SSSSSSSSSSSSS", "SSS.......SSS",
               "SS.........SS", "S...........S", "SWWWWWWWWWWWS", "WWWWWWWWWWWWW"],
    "lighthouse": ["", "r", "rrr", "Y.YYYYY.Y", "YYYYY", "rrrrrrr", "eee", "rrr", "eeeee", "rrrrr", "eeeee",
                   "rrrrrrr", "eeeeeee", "SSSSSSSSS", "WWWWWWWWWWWWW"],
    "igloo": ["", "", "", "", "hhh", "hhhhhhh", "hhWhhhWhh", "hhhhhhhhhhh", "hWhhhkhhhWh", "hhhhhkkkhhhhh",
              "hWhhhkkkhhhWh", "hhhhhkkkhhhhh", "WWWWWWWWWWWWW"],
    "market": ["", "", "", "rhrhrhrhrhr", "rhrhrhrhrhr", "rrrrrrrrrrr", "b.........b", "b..o.R.y..b", "b.........b",
               "bbbbbbbbbbb", "blllllllllb", "blllllllllb", "bbbbbbbbbbb"],
    "ruins": ["", "", "", "", ">..S.......S", ">..SS......S", ">..SS...S..SS", ">.SSS..SS..SS", ">.SsS..SS.SSS",
              ">.SSS.SSS.SsS", ">.SSSsSSSSSSS", ">.SSSSSSSSSSSS", ">.GGGGGGGGGGGGG"],
    "bell": ["", "b", "yyy", "yYyyy", "yYyyy", "yyYyyyy", "yyYyyyy", "yyyyyyy", "yyyyyyyyy", "yyyyyyyyyyy",
             "aaaaaaaaaaa", "y"],
    # --- nature
    "pine": ["", "g", "ggg", "gGggg", "ggg", "gGggg", "gGggggg", "ggggg", "gGggggg", "gGggggggg", "ggggggg",
             "gGggggggg", "gGggggggggg", "bbb", "bbb"],
    "palm": ["", ">.....GG..GG", ">..GGGGggGGGG", ">.GG..GbbG..GG", ">.G....bb....G", ">.......bb", ">.......bb",
             ">........bb", ">........bb", ">.......bb", ">.......bb", ">......bbb", ">..ttttttttttt"],
    "cactus": ["", "", ">......ggg", ">......gGg", ">..g...gGg", ">.ggg..gGg..g", ">.gGg..gGg.ggg",
               ">.gGgggggg.gGg", ">..ggggggGgggGg", ">......gGgggg", ">......gGg", ">......gGg", ">......gGg",
               ">....ttttttt"],
    "flower": ["", "", "RRR", "RRRRR", "RRYRR", "RRRRR", "RRR", "g", ">.......gGG", ">......GGg", ">.......g",
               ">.......g", "GGgGG"],
    "mushroom": ["", "", "", "rrrrr", "rrhrrrr", "rrrrrhrrr", "rhrrrrrhrrr", "rrrrrrrrrrr", "eeeee", "eee", "eee",
                 "eee", "eeeee"],
    "volcano": ["", "o..f..o", "f.f", "fff", "ufffu", "uuufuuu", "uuuufuuuu", "uuuuuufuuuu", "uuuuuuufuuuuu",
                "uuuuuuuuuuuuu"],
    "snowflake": ["", "", "W", "W.W.W", "WWW", "W..W..W", "WWWWWWWWW", "W..W..W", "WWW", "W.W.W", "W"],
    "sun": ["", "", "Y", "Y..Y..Y", "yyy", "yYYYy", "yYYYYYy", "YYyYYYYYyYY", "yYYYYYy", "yYYYy", "yyy",
            "Y..Y..Y", "Y"],
    "moon": ["", ">.....yyyy", ">...yyyY", ">..yyYY.....Y", ">..yYY", ">.yyYY", ">.yyYY", ">.yyYY", ">.yyYYY",
             ">..yyYYYY...yY", ">...yyYYYYYYY", ">.....yyyYY"],
    "wave": ["", "", "", ">..WWW....WWW", ">.WWwWW..WWwWW", ">.Ww.wWWWWw.wW", "", ">..WWW....WWW",
             ">.WWwWW..WWwWW", ">.Ww.wWWWWw.wW", "", ">..WWW....WWW", ">.WWwWW..WWwWW"],
    "apple": ["", ">.......bG", ">.......bGG", "rrr.rrr", "rrRrrrrrr", "rRRrrrrrrrr", "rRrrrrrrrrr", "rrrrrrrrrrr",
              "rrrrrrrrrrr", "rrrrrrrrr", "rrrrrrr", "rr.rr"],
    # --- resources
    "iron": ingot("h", "i", "S"),
    "gold": ingot("Y", "y", "a"),
    "copper": ingot("J", "j", "u"),
    "coal": ore("n", "S", "n"),
    "redstone": ore("r", "R", "d"),
    "lapis": ore("w", "W", "x"),
    "amethyst": ["", "", ">.......V", ">......vV", ">..V...vVV", ">..vV..vVV.V", ">..vVV.vVVvV", ">..vVVvvVVvV",
                 ">...vVvvVvVV", ">..SSSSSSSSSS", ">..SsSSSsSSSs"],
    "log": ["", "", "", "bbbbb", "bbtttbb", "btttttttb", "bttlllttb", "btltttltb", "bttlllttb", "btttttttb",
            "bbtttbb", "bbbbb"],
    "fish": ["", "", "", "", ">.....www......w", ">...wwwwwww...ww", ">..wwhwwwwww.www", ">.wwwwwwwwwwwww",
             ">..wwWWWWWww.www", ">...wwwwwww...ww", ">.....www......w"],
    "bee": ["", "", "", ">.....hh..hh", ">.....hhhhhh", ">..yykyykyy", ">.yyykyykyyy", ">.kyykyykyyy",
            ">.yyykyykyyy", ">..yykyykyy", ">...k...k"],
    # --- mobs and danger
    "creeper": ["", "", "GGgGGGGGGgGG", "GgGGGGGgGGGG", "GGnnGGGGnnGG", "GGnnGGgGnnGG", "GGGGnnnnGGGG",
                "GGGnnnnnnGGG", "GGGnnnnnnGGG", "GGGnnGGnnGGG", "GgGGGGGGGgGG", "GGGgGGGGGGGG"],
    "zombie": ["", "", "gggggggggggg", "gggggggggggg", "zzzzzzzzzzzz", "zzzzzzzzzzzz", "zKKzzzzzzKKz",
               "zzzzzggzzzzz", "zzzzzggzzzzz", "zzzKKKKKKzzz", "zzzzzzzzzzzz", "zzzzzzzzzzzz"],
    "spider": ["", "", "", ">..k........k", ">...k......k", ">.k..knnnnk..k", ">..kknnnnnnkk", ">....nrnnrn",
               ">..kknnnnnnkk", ">.k..knnnnk..k", ">...k......k", ">..k........k"],
    "slime": ["", "", "", "GGGGGGGGGG", "GMMMMMMMMG", "GMggMMggMG", "GMggMMggMG", "GMMMMMMMMG", "GMMMMggMMG",
              "GMMMMMMMMG", "GGGGGGGGGG"],
    "ghast": ["", "", "hhhhhhhhhh", "hhhhhhhhhh", "hKKhhhhKKh", "hhhhhhhhhh", "hhhKKKKhhh", "hhhhhhhhhh",
              "hhhhhhhhhh", "h.h.hh.h.h", "h...h..h.h", "....h....."],
    "enderman": ["", "", "", "nnnnnnnnnn", "nnnnnnnnnn", "nnnnnnnnnn", "VvVnnnnVvV", "nnnnnnnnnn", "nnnnnnnnnn",
                 "nnnn", "nnnn", "nnnn"],
    "fire": ["", ">.......o", ">......oo", ">...o..oYo", ">..oo.oYYo..o", ">..oYooYYYo.oo", ">.oYYoYYeYYooYo",
             ">.oYYYYYeeYYYYo", ">.oYYYeeeeeYYYo", ">..oYYeeeeeYYo", ">...ooYYYYYoo", ">.....ooooo"],
    "warning": ["", "", "y", "yyy", "yykyy", "yyykyyy", "yyykyyy", "yyyykyyyy", "yyyykyyyy", "yyyyyyyyyyy",
                "yyyyykyyyyy", "yyyyyyyyyyyyy"],
    "tnt": ["", "", "", "b", "rrrrrrrrrr", "rRrrRrrRrr", "hhhhhhhhhh", "hnnhnhnhnh", "hhhhhhhhhh", "rRrrRrrRrr",
            "rrrrrrrrrr"],
    # --- animals
    "cow": ["", "", "", "e........e", "enhhnnnnne", "nhhnnnhhnn", "nhKhnnhKhn", "nhhhnnhhhn", "nnPPPPPPnn",
            "nPkPPPPkPn", "nPPPPPPPPn", ".nPPPPPPn."],
    "pig": ["", "", "", "PPPPPPPPPP", "PPPPPPPPPP", "PKhPPPPhKP", "PPPPPPPPPP", "PPqqqqqqPP", "PPqKqqKqPP",
            "PPqqqqqqPP", "PPPPPPPPPP"],
    "sheep": ["", "", "", "hhhhhhhhhh", "hhhhhhhhhh", "hhtttttthh", "htKttttKth", "htttttttth", "httPPPPtth",
              "hhtttttthh", "hhhhhhhhhh"],
    "chicken": ["", "", ">......hhhh", ">.....hhhhhh", ">.....hKhhhh", ">...yyhhhhhh", ">...yyhhhhhh", ">.....rhhhhh",
                ">.....rhhhhhhhh", ">......hhhhhhhh", ">......hhhhhhh", ">.......hhhhh", ">........y..y",
                ">........y..y"],
    "horse": ["", "", ">........u.u", ">.......bbbbu", ">......bbKbbbu", ">.....bbbbbbbuu", ">....bbbbbbbbuu",
              ">...bbbbbbbbbbu", ">..lbbbbb.bbbbu", ">..llbbb..bbbbu", ">...ll....bbbbu", ">.........bbbbu"],
    "wolf": ["", "", "", "SS......SS", "SSS....SSS", "SSSSSSSSSS", "SSKSSSSKSS", "SSSSSSSSSS", "SShhhhhhSS",
             "SShhnnhhSS", "hhhhhh", "hhhh"],
    "cat": ["", "", "", "o........o", "oo......oo", "oooooooooo", "ooMKooMKoo", "oooooooooo", "ooooPPoooo",
            "hoooooooho", "oooooooooo"],
    # --- travel and items
    "minecart": ["", "", "", "", "", "SSSSSSSSSSS", "SsssssssssS", "SsssssssssS", "SSSSSSSSSSS", "nn.....nn",
                 "nn.....nn", "bbbbbbbbbbbbb", "l.l.l.l.l.l.l"],
    "elytra": ["", "", ">..ss......ss", ">..sSs....sSs", ">.sSSs....sSSs", ">.sSSSs..sSSSs", ">.sSSSs..sSSSs",
               ">..sSSs..sSSs", ">..sSSs..sSSs", ">...sSs..sSs", ">...sS....Ss", ">....s....s"],
    "axe": ["", ">.......bb", ">...SSSSbb", ">..SiSSSbb", ">..SiSSSbb", ">...SSSSbb", ">.......bb", ">.......bb",
            ">.......bb", ">.......bb", ">.......bb", ">.......bb", ">.......bb", ">.......bb"],
    "bow": ["", ">......bb", ">......k.b", ">......k..b", ">......k...b", ">......k...b", ">......k....b",
            ">.SSSSSSSSSSSsi", ">......k....b", ">......k...b", ">......k...b", ">......k..b", ">......k.b",
            ">......bb"],
    "shield": ["", "lllllllllll", "lllllrlllll", "lllllrlllll", "lrrrrrrrrrl", "lllllrlllll", "lllllrlllll",
               "lllllrlllll", "lllllrlll", "lllrlll", "lllll", "lll"],
    "potion": ["", "bbb", "WWW", "WhW", "WWhWW", "WhVVVVW", "WVVVVVVVW", "WVVhVVVVW", "WVVVVVVVW", "WVVVVVVVW",
               "WVVVVVW", "WWWWW"],
    "anvil": ["", "", "", "", "", "sssssssssssss", "nnnnnnnnnnn", "nnnnnnn", "nnnnn", "nnnnn", "nnnnnnnnn",
              "nnnnnnnnnnn"],
    "crafting": ["", "", "tttttttttttt", "tbtbtbtbtbtt", "tttttttttttt", "bbbbbbbbbbbb", "bSSbbbbbblbb",
                 "bSSbbbbbblbb", "bbSbbbbbllbb", "bbbbbbbbbbbb", "bbbbbbbbbbbb"],
    "furnace": ["", "", "SSSSSSSSSSSS", "SsSSSSSSSSsS", "SSnnnnnnnnSS", "SSSSSSSSSSSS", "SSnnnnnnnnSS",
                "SSnoYYoYonSS", "SSnoooooonSS", "SSSSSSSSSSSS", "SsSSSSSSSSsS", "SSSSSSSSSSSS"],
    "cauldron": ["", "", "", "", "nnnnnnnnnnn", "nwwwwwwwwwn", "nnnnnnnnnnn", "nnnnnnnnnnn", "nnnnnnnnnnn",
                 "nnnnnnnnn", "nn.....nn"],
    "compass": ["", "", "", "yyyyy", "yhhrhhy", "yhhhrhhhy", "yhhhrhhhy", "yhhhyhhhy", "yhhhnhhhy", "yhhhnhhhy",
                "yhhnhhy", "yyyyy"],
    "key": ["", "", "", "", "", ">..yyy", ">.yy.yy", ">.y...yyyyyyyy", ">.yy.yy...y.y", ">..yyy....y.y"],
    "crown": ["", "", "", "", "y...y...y", "yy.yyy.yy", "yyyyyyyyy", "yRyyCyyRy", "yyyyyyyyy", "aaaaaaaaa"],
    "trophy": ["", "", "yyyyyyyyyyy", "y.yYyyyyy.y", "y.yYyyyyy.y", ".yyyyyyyyy.", "yyyyyyy", "yyyyy", "yyy", "y",
               "y", "yyy", "bbbbbbb", "bbbbbbb"],
    "coin": ["", "", "", "aaaaa", "ayyyyya", "ayYYYYYya", "ayYYaYYya", "ayYaaaYya", "ayYYaYYya", "ayYYYYYya",
             "ayyyyya", "aaaaa"],
    "note": ["", "", ">......vvvvvvv", ">......vvvvvvv", ">......v.....v", ">......v.....v", ">......v.....v",
             ">......v.....v", ">......v.....v", ">....vvv...vvv", ">...vvvv..vvvv", ">...vvvv..vvvv",
             ">....vv....vv"],
    "hourglass": ["", "", "bbbbbbbbb", "WtttttttW", "WtttttW", "WtttW", "WtW", "WtW", "WWtWW", "WWWtWWW",
                  "WWWtttWWW", "WtttttttW", "bbbbbbbbb"],
    "lightning": ["", ">........YYY", ">.......YYY", ">......YYY", ">.....YYY", ">....YYYYYYY", ">.......YYY",
                  ">......YYY", ">.....YYY", ">....YYY", ">...YY", ">..Y"],
    "bucket": ["", "", "", "sssssss", "s.....s", "iiiiiiiiiii", "iwwwwwwwwwi", "iiiiiiiiiii", "iiiiiiiii",
               "iiiiiiiii", "iiiiiii"],
    "cake": ["", "", "", "", "hhhhhhhhhhh", "hrhhhrhhhrh", "hhhhhhhhhhh", "lhlhllhlhll", "lllllllllll",
             "ttttttttttt", "lllllllllll"],
}

# recolours of the hand drawn pin and flag
PIN_COLORS = {
    "pin_blue": ("w", "W", "x"), "pin_green": ("g", "G", "m"), "pin_yellow": ("y", "Y", "a"),
    "pin_purple": ("v", "V", "x"), "pin_white": ("i", "h", "s"), "pin_black": ("n", "s", "K"),
}
FLAG_COLORS = {
    "flag_blue": ("w", "W"), "flag_green": ("g", "G"), "flag_yellow": ("y", "Y"), "flag_purple": ("v", "V"),
    "flag_white": ("i", "h"),
}


def recolor(rows, mapping):
    return ["".join(mapping.get(c, c) for c in r) for r in rows]
