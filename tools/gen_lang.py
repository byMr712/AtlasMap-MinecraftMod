# Generates en_us.json and ru_ru.json. Run: python tools/gen_lang.py
import json
import os
import re

ROOT = os.path.join(os.path.dirname(__file__), "..")
OUT = os.path.join(ROOT, "src", "client", "resources", "assets", "mapminecraft", "lang")

# key: (ru, en)
K = {
    "key.category.mapminecraft.main": ("Карта (MapMinecraft)", "Map (MapMinecraft)"),
    "key.mapminecraft.open_map": ("Открыть карту", "Open map"),
    "key.mapminecraft.marker_list": ("Список меток", "Marker list"),
    "key.mapminecraft.new_marker": ("Метка на месте игрока", "Marker at player position"),
    "key.mapminecraft.toggle_minimap": ("Вкл/выкл миникарту", "Toggle minimap"),
    "key.mapminecraft.minimap_zoom_in": ("Миникарта: приблизить", "Minimap: zoom in"),
    "key.mapminecraft.minimap_zoom_out": ("Миникарта: отдалить", "Minimap: zoom out"),
    "key.mapminecraft.ruler": ("Линейка (на карте)", "Ruler (in map)"),
    "key.mapminecraft.editor": ("Редактор рисунков (на карте)", "Drawing editor (in map)"),
    "key.mapminecraft.toggle_drawings": ("Показать/скрыть рисунки (на карте)", "Show/hide drawings (in map)"),
    "map.title": ("Карта мира", "World map"),
    "field.name": ("Название", "Name"),
    "field.name_hint": ("Введите название…", "Enter a name…"),
    "field.desc": ("Описание", "Description"),
    "field.desc_hint": ("Заметки об этом месте…", "Notes about this place…"),
    "field.coords": ("Координаты", "Coordinates"),
    "field.shots": ("Скриншоты (%s)", "Screenshots (%s)"),
    "list.search": ("Поиск", "Search"),
    "list.search_hint": ("Поиск меток…", "Search markers…"),
    "list.title": ("Метки · %s", "Markers · %s"),
    "list.sort_tip": ("ЛКМ — следующая сортировка, ПКМ — предыдущая", "LMB — next sort order, RMB — previous"),
    "list.hide_all": ("Скрыть все метки", "Hide all markers"),
    "list.show_all": ("Показать все метки", "Show all markers"),
    "list.empty": ("Меток пока нет. Нажмите ПКМ по карте, чтобы поставить метку.", "No markers yet. Right-click the map to place one."),
    "list.nothing_found": ("Ничего не найдено.", "Nothing found."),
    "marker.unnamed": ("Без названия", "Unnamed"),
    "marker.show": ("Показать", "Show"),
    "marker.hide": ("Скрыть", "Hide"),
    "marker.no_desc": ("— описания нет —", "— no description —"),
    "marker.no_shots": ("Скриншотов нет", "No screenshots"),
    "marker.default_name": ("Метка %s", "Marker %s"),
    "unit.blocks": ("бл.", "blocks"),
    "unit.blocks_short": ("бл", "b"),
    "ruler.total": ("Длина: %s бл. · точек: %s", "Length: %s blocks · points: %s"),
    "ruler.hint": ("ЛКМ — точка, ПКМ — убрать последнюю, Backspace — сброс", "LMB — add point, RMB — remove last, Backspace — reset"),
    "ruler.direct": ("Напрямую: %s (ΔX %s, ΔZ %s)", "Direct: %s (ΔX %s, ΔZ %s)"),
    "tool.list": ("Список меток [%s]", "Marker list [%s]"),
    "tool.ruler": ("Линейка [%s]", "Ruler [%s]"),
    "tool.editor": ("Редактор — рисовать на карте [%s]", "Editor — draw on the map [%s]"),
    "tool.hide_drawings": ("Скрыть рисунки [%s]", "Hide drawings [%s]"),
    "tool.show_drawings": ("Показать рисунки [%s]", "Show drawings [%s]"),
    "tool.hide_markers": ("Скрыть метки на карте", "Hide markers on the map"),
    "tool.show_markers": ("Показать метки на карте", "Show markers on the map"),
    "tool.center": ("К игроку [C]", "Center on player [C]"),
    "tool.zoom_in": ("Приблизить [+ / колесо]", "Zoom in [+ / wheel]"),
    "tool.zoom_out": ("Отдалить [- / колесо]", "Zoom out [- / wheel]"),
    "tool.dims": ("Измерения", "Dimensions"),
    "tool.settings": ("Настройки миникарты", "Minimap settings"),
    "tool.close": ("Закрыть карту [%s / Esc]", "Close map [%s / Esc]"),
    "draw.pen": ("Перо — свободное рисование", "Pen — freehand"),
    "draw.line": ("Прямая линия", "Straight line"),
    "draw.rect": ("Прямоугольник", "Rectangle"),
    "draw.circle": ("Круг", "Circle"),
    "draw.eraser": ("Ластик", "Eraser"),
    "draw.undo": ("Отменить [Ctrl+Z]", "Undo [Ctrl+Z]"),
    "draw.clear": ("Стереть все рисунки этого измерения", "Erase all drawings in this dimension"),
    "confirm.clear_drawings": ("Стереть все рисунки в этом измерении?", "Erase all drawings in this dimension?"),
    "confirm.delete_marker": ("Удалить метку «%s» вместе со скриншотами?", "Delete marker “%s” together with its screenshots?"),
    "sort.name": ("По названию", "By name"),
    "sort.distance": ("По расстоянию", "By distance"),
    "sort.date": ("Сначала новые", "Newest first"),
    "sort.icon": ("По иконке", "By icon"),
    "button.edit": ("Изменить", "Edit"),
    "button.delete": ("Удалить", "Delete"),
    "button.goto": ("На карте", "Show"),
    "button.save": ("Сохранить", "Save"),
    "button.cancel": ("Отмена", "Cancel"),
    "button.done": ("Готово", "Done"),
    "button.yes": ("Да", "Yes"),
    "button.no": ("Нет", "No"),
    "button.back": ("Назад", "Back"),
    "edit.new": ("Новая метка", "New marker"),
    "edit.title": ("Правка метки", "Edit marker"),
    "edit.icon": ("Иконка: %s", "Icon: %s"),
    "edit.here": ("Взять координаты игрока", "Use player position"),
    "edit.choose_icon": ("Выберите иконку", "Choose an icon"),
    "shots.add_tip": ("Добавить скриншоты", "Add screenshots"),
    "shots.remove": ("Убрать скриншот", "Remove screenshot"),
    "shots.title": ("Добавить скриншоты", "Add screenshots"),
    "shots.hint": ("Нажмите на снимок, чтобы прикрепить его к метке. Файлы также можно перетащить прямо в окно игры.",
                   "Click a screenshot to attach it to the marker. You can also drag files into the game window."),
    "shots.empty": ("В папке screenshots пока пусто — сделайте снимок клавишей F2 или выберите файл.",
                    "The screenshots folder is empty — press F2 in game or pick a file."),
    "shots.from_file": ("Из файла…", "From file…"),
    "shots.failed": ("Не удалось загрузить изображение", "Failed to load the image"),
    "shots.loading": ("Загрузка…", "Loading…"),
    "shots.counter": ("Скриншот %s из %s  ·  ←/→", "Screenshot %s of %s  ·  ←/→"),
    "shots.dialog_title": ("Выберите изображения", "Choose images"),
    "settings.title": ("Миникарта", "Minimap"),
    "settings.minimap": ("Миникарта: %s", "Minimap: %s"),
    "settings.size": ("Размер: %s", "Size: %s"),
    "settings.zoom": ("Масштаб: %s", "Zoom: %s"),
    "settings.corner": ("Угол: %s", "Corner: %s"),
    "settings.coords": ("Координаты: %s", "Coordinates: %s"),
    "settings.mm_markers": ("Метки: %s", "Markers: %s"),
    "corner.tr": ("справа сверху", "top right"),
    "corner.tl": ("слева сверху", "top left"),
    "corner.br": ("справа снизу", "bottom right"),
    "corner.bl": ("слева снизу", "bottom left"),
    "on": ("вкл", "on"),
    "off": ("выкл", "off"),
    "dims.title": ("Измерения", "Dimensions"),
    "dim.overworld": ("Верхний мир", "Overworld"),
    "dim.nether": ("Незер", "Nether"),
    "dim.end": ("Энд", "The End"),
    "atlas.button": ("Атлас карт", "Map atlas"),
    "atlas.title": ("Атлас карт", "Map atlas"),
    "atlas.subtitle": ("Сохранённые карты, метки и скриншоты — отдельно для каждого сервера и мира",
                       "Saved maps, markers and screenshots — separately for every server and world"),
    "atlas.servers": ("Серверы", "Servers"),
    "atlas.singleplayer": ("Одиночные миры", "Singleplayer worlds"),
    "atlas.confirm_delete": ("Удалить карту «%s» со всеми метками, рисунками и скриншотами?",
                             "Delete the map “%s” with all markers, drawings and screenshots?"),
    "atlas.no_servers": ("Карт серверов пока нет. Зайдите на сервер — карта начнёт рисоваться сама.",
                         "No server maps yet. Join a server and the map will draw itself."),
    "atlas.no_worlds": ("Карт одиночных миров пока нет.", "No singleplayer maps yet."),
    "atlas.info": ("Меток: %s · %s", "Markers: %s · %s"),
    "button.configure": ("Настроить", "Configure"),
    "button.configure_tip": ("Изменить название, иконку, описание и скриншоты", "Edit name, icon, description and screenshots"),
    "shots.draw": ("Рисовать на скриншоте", "Draw on the screenshot"),
    "shots.draw_stop": ("Закончить рисование", "Stop drawing"),
    "shots.draw_hint": ("Рисование: %s из %s  ·  Esc — готово", "Drawing: %s of %s  ·  Esc — done"),
    "shots.clear_drawings": ("Стереть рисунки на этом скриншоте", "Erase drawings on this screenshot"),
    "icons.search_hint": ("Поиск иконки: дом, синий, 12, стрелка…", "Search icons: house, blue, 12, arrow…"),
    "icons.found": ("Найдено: %s", "Found: %s"),
    "icons.group.basic": ("Основные", "Basic"),
    "icons.group.build": ("Постройки", "Buildings"),
    "icons.group.nature": ("Природа", "Nature"),
    "icons.group.resources": ("Ресурсы", "Resources"),
    "icons.group.mobs": ("Мобы и опасность", "Mobs and danger"),
    "icons.group.items": ("Транспорт и предметы", "Travel and items"),
}

ICONS = {
    "pin": ("Булавка", "Pin"), "house": ("Дом", "House"), "castle": ("Замок", "Castle"), "temple": ("Храм", "Temple"),
    "cave": ("Пещера", "Cave"), "mine": ("Шахта", "Mine"), "portal": ("Портал", "Portal"), "end": ("Око Края", "Ender eye"),
    "skull": ("Опасность", "Danger"), "sword": ("Битва", "Battle"), "star": ("Звезда", "Star"), "flag": ("Флаг", "Flag"),
    "chest": ("Сундук", "Chest"), "diamond": ("Алмазы", "Diamonds"), "emerald": ("Торговля", "Trading"),
    "cross": ("Клад", "Treasure"), "tree": ("Лес", "Forest"), "mountain": ("Гора", "Mountain"), "farm": ("Ферма", "Farm"),
    "anchor": ("Порт", "Harbor"), "boat": ("Лодка", "Boat"), "bed": ("Кровать", "Bed"), "campfire": ("Лагерь", "Camp"),
    "heart": ("Любимое место", "Favorite"), "question": ("Неизвестно", "Unknown"), "exclamation": ("Важно", "Important"),
    "pin_blue": ("Синяя булавка", "Blue pin"), "pin_green": ("Зелёная булавка", "Green pin"),
    "pin_yellow": ("Жёлтая булавка", "Yellow pin"), "pin_purple": ("Фиолетовая булавка", "Purple pin"),
    "pin_white": ("Белая булавка", "White pin"), "pin_black": ("Чёрная булавка", "Black pin"),
    "flag_blue": ("Синий флаг", "Blue flag"), "flag_green": ("Зелёный флаг", "Green flag"),
    "flag_yellow": ("Жёлтый флаг", "Yellow flag"), "flag_purple": ("Фиолетовый флаг", "Purple flag"),
    "flag_white": ("Белый флаг", "White flag"),
    "warning": ("Внимание", "Warning"), "crown": ("Корона", "Crown"), "trophy": ("Кубок", "Trophy"), "key": ("Ключ", "Key"),
    "note": ("Музыка", "Music"), "hourglass": ("Песочные часы", "Hourglass"),
    "tower": ("Башня", "Tower"), "church": ("Церковь", "Church"), "windmill": ("Мельница", "Windmill"),
    "lighthouse": ("Маяк", "Lighthouse"), "market": ("Рынок", "Market"), "tent": ("Палатка", "Tent"), "igloo": ("Иглу", "Igloo"),
    "well": ("Колодец", "Well"), "bridge": ("Мост", "Bridge"), "ruins": ("Руины", "Ruins"), "bell": ("Деревня", "Village"),
    "pine": ("Ель", "Spruce"), "palm": ("Пальма", "Palm"), "cactus": ("Кактус", "Cactus"), "flower": ("Цветы", "Flowers"),
    "mushroom": ("Грибы", "Mushrooms"), "apple": ("Яблоко", "Apple"), "volcano": ("Вулкан", "Volcano"), "wave": ("Вода", "Water"),
    "snowflake": ("Снег", "Snow"), "sun": ("Солнце", "Sun"), "moon": ("Луна", "Moon"), "lightning": ("Молния", "Lightning"),
    "gold": ("Золото", "Gold"), "iron": ("Железо", "Iron"), "copper": ("Медь", "Copper"), "coal": ("Уголь", "Coal"),
    "redstone": ("Редстоун", "Redstone"), "lapis": ("Лазурит", "Lapis"), "amethyst": ("Аметист", "Amethyst"),
    "log": ("Древесина", "Wood"), "fish": ("Рыбалка", "Fishing"), "coin": ("Монеты", "Coins"),
    "wither": ("Иссушитель", "Wither"), "creeper": ("Крипер", "Creeper"), "zombie": ("Зомби", "Zombie"), "spider": ("Паук", "Spider"),
    "slime": ("Слизень", "Slime"), "ghast": ("Гаст", "Ghast"), "enderman": ("Эндермен", "Enderman"), "fire": ("Огонь", "Fire"),
    "tnt": ("Динамит", "TNT"), "cow": ("Корова", "Cow"), "pig": ("Свинья", "Pig"), "sheep": ("Овца", "Sheep"),
    "chicken": ("Курица", "Chicken"), "horse": ("Лошадь", "Horse"), "wolf": ("Волк", "Wolf"), "cat": ("Кошка", "Cat"),
    "bee": ("Пчёлы", "Bees"), "minecart": ("Вагонетка", "Minecart"), "elytra": ("Элитры", "Elytra"), "axe": ("Топор", "Axe"),
    "bow": ("Лук", "Bow"), "shield": ("Щит", "Shield"), "potion": ("Зелья", "Potions"), "anvil": ("Наковальня", "Anvil"),
    "crafting": ("Верстак", "Crafting table"), "furnace": ("Печь", "Furnace"), "book": ("Чары", "Enchanting"),
    "cauldron": ("Котёл", "Cauldron"), "compass": ("Компас", "Compass"), "bucket": ("Ведро", "Bucket"), "cake": ("Торт", "Cake"),
}


def full(k):
    return k if k.startswith("key.") else "mapminecraft." + k


def main():
    for k, v in ICONS.items():
        K["icon." + k] = v
    os.makedirs(OUT, exist_ok=True)
    ru = {full(k): v[0] for k, v in K.items()}
    en = {full(k): v[1] for k, v in K.items()}
    with open(os.path.join(OUT, "ru_ru.json"), "w", encoding="utf-8") as f:
        json.dump(ru, f, ensure_ascii=False, indent=2)
    with open(os.path.join(OUT, "en_us.json"), "w", encoding="utf-8") as f:
        json.dump(en, f, ensure_ascii=False, indent=2)
    # sanity check: every key used in code must exist
    used = set()
    for dp, _, fs in os.walk(os.path.join(ROOT, "src", "client", "java")):
        for fn in fs:
            if fn.endswith(".java"):
                src = open(os.path.join(dp, fn), encoding="utf-8").read()
                used.update(re.findall(r'T\.(?:tr|s)\("([a-z_.]+)"', src))
    missing = [k for k in sorted(used) if "mapminecraft." + k not in en]
    print(len(K), "keys; missing:", missing)


if __name__ == "__main__":
    main()
