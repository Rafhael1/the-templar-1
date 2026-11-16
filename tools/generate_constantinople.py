"""Build the Constantinople map and its pixel art. Run with Pillow from tools/requirements.txt."""
from pathlib import Path
import random
import xml.etree.ElementTree as ET

from PIL import Image, ImageDraw

DEST = Path(__file__).resolve().parents[1] / 'assets/levels/constantinople'
rng = random.Random(1097)
GOLD = '#bd9764'
PURPLE = '#62394f'


def save(image, name, scale=2):
    path = DEST / name
    path.parent.mkdir(parents=True, exist_ok=True)
    image.resize((image.width * scale, image.height * scale), Image.Resampling.NEAREST).save(path)


def bricks(draw, box, palette, width=12, height=6):
    left, top, right, bottom = box
    draw.rectangle(box, fill=palette[0])
    for row, y in enumerate(range(top, bottom, height)):
        for x in range(left - row % 2 * (width // 2), right, width):
            draw.rectangle((max(left, x), y, min(right, x + width - 2), min(bottom, y + height - 2)),
                           fill=rng.choice(palette[1:]))


def arch(draw, x, y, w, h, color):
    r = w // 2
    draw.pieslice((x, y, x + w, y + w), 180, 360, fill=color)
    draw.rectangle((x, y + r, x + w, y + h), fill=color)


def banner(draw, x, y, color):
    draw.rectangle((x - 3, y, x + 24, y + 2), fill=GOLD)
    draw.polygon([(x, y + 3), (x + 21, y + 3), (x + 21, y + 45), (x + 10, y + 53), (x, y + 45)],
                 fill=color)
    draw.line((x + 2, y + 5, x + 2, y + 43), fill=GOLD)
    draw.line((x + 19, y + 5, x + 19, y + 43), fill=GOLD)
    draw.polygon([(x + 10, y + 13), (x + 15, y + 24), (x + 10, y + 35), (x + 5, y + 24)], fill=GOLD)
    draw.rectangle((x + 9, y + 21, x + 11, y + 27), fill=color)


def dome(draw, center, base, radius, wall, roof, light):
    draw.rectangle((center - radius, base - 12, center + radius, base + 36), fill=wall)
    draw.pieslice((center - radius - 3, base - radius, center + radius + 3, base + radius),
                  180, 360, fill=roof)
    for offset in (-radius // 2, 0, radius // 2):
        draw.line((center + offset // 3, base - radius + 4, center + offset, base - 2), fill=light)
    draw.rectangle((center - radius - 3, base - 2, center + radius + 3, base + 2), fill=light)
    for x in range(center - radius + 4, center + radius - 3, 8):
        arch(draw, x, base + 6, 3, 8, roof)
    draw.line((center, base - radius - 7, center, base - radius), fill=light)
    draw.line((center - 2, base - radius - 5, center + 2, base - radius - 5), fill=light)


def sky():
    image = Image.new('RGB', (480, 270))
    draw = ImageDraw.Draw(image)
    for y in range(270):
        t = min(y, 198) / 198
        color = tuple(round(a + (b - a) * t) for a, b in zip((45, 59, 88), (201, 153, 120)))
        draw.line((0, y, 479, y), fill=color)
    for y in (32, 46, 78, 96, 111, 126):
        for x in range(0, 480, 96):
            left = x + rng.randrange(24)
            draw.rectangle((left, y, left + rng.randrange(12, 62), y + 1), fill='#ae8d8b')
    return image


def scenery():
    walls = sky()
    d = ImageDraw.Draw(walls)
    for x, y, size in [(57, 158, 28), (168, 132, 44), (290, 148, 32), (408, 135, 37)]:
        dome(d, x, y, size, '#9c827f', '#685d72', '#8f7a84')
    bricks(d, (0, 183, 479, 261), ('#675865', '#85717a', '#8c757c', '#7e6a75'))
    for x in range(0, 480, 16):
        d.rectangle((x, 175, x + 8, 182), fill='#8c757c')
    for x in (18, 136, 257, 377):
        bricks(d, (x, 151, x + 37, 262), ('#62535f', '#9c8283', '#947c81', '#86717a'))
        for turret in range(x - 2, x + 38, 13):
            d.rectangle((turret, 145, turret + 8, 152), fill='#a08986')
        arch(d, x + 16, 169, 5, 20, '#514959')
    save(walls, 'scenery/walls.png')

    city = sky()
    d = ImageDraw.Draw(city)
    for x, y, size in [(57, 153, 26), (239, 110, 51), (315, 151, 28), (442, 143, 24)]:
        dome(d, x, y, size, '#a78b83', '#755c70', '#aa8382')
    for x in range(-20, 480, 41):
        y = 168 + rng.randrange(26)
        d.polygon([(x - 3, y), (x + 16, y - 12), (x + 40, y)], fill='#704d62')
        bricks(d, (max(0, x), y, min(479, x + 38), 250), ('#655665', '#9b7d7c', '#91767b', '#856b73'))
        if x > 0:
            arch(d, x + 10, y + 9, 8, 14, '#514859')
            arch(d, x + 23, y + 9, 8, 14, '#514859')
    d.rectangle((0, 229, 479, 269), fill='#77616d')
    save(city, 'scenery/city.png')

    harbor = sky()
    d = ImageDraw.Draw(harbor)
    d.ellipse((348, 104, 378, 134), fill='#dfab7c')
    d.polygon([(0, 165), (50, 156), (84, 161), (135, 150), (190, 162), (226, 149),
               (302, 162), (340, 155), (398, 164), (447, 151), (480, 165), (480, 196),
               (0, 196)], fill='#827887')
    d.rectangle((0, 178, 479, 269), fill='#466273')
    for _ in range(270):
        x = rng.randrange(480)
        y = rng.randrange(178, 269)
        d.line((x, y, min(479, x + rng.randrange(2, 14)), y),
               fill=rng.choice(('#5f7b84', '#728b90', '#3c566a', '#8d8e8b')))
    for x, y, scale in [(70, 212, 1.0), (203, 195, .65), (350, 225, 1.3)]:
        def ship(points):
            return [(x + round(px * scale), y + round(py * scale)) for px, py in points]
        d.polygon(ship([(-38, -2), (30, -2), (43, -8), (29, 8), (-25, 8)]), fill='#483c50')
        d.line(ship([(0, 0), (0, -60)]), fill='#544657', width=2)
        d.polygon(ship([(-2, -57), (1, -17), (-31, -12), (-22, -27)]), fill='#b69a8e')
        d.polygon(ship([(4, -48), (25, -16), (5, -19)]), fill='#876e7e')
        d.line(ship([(-35, 11), (39, 11)]), fill='#879a9a')
    save(harbor, 'scenery/harbor.png')

    market = sky()
    d = ImageDraw.Draw(market)
    for x, y, r in [(44, 144, 31), (184, 117, 40), (340, 136, 29), (447, 121, 34)]:
        dome(d, x, y, r, '#9d8588', '#6b5975', '#a08b92')
    arcade = ('#594757', '#947b7c', '#a0857e', '#8b7379')
    bricks(d, (0, 178, 479, 263), arcade, 13, 6)
    for x in range(4, 480, 36):
        arch(d, x, 186, 24, 65, '#544457')
        arch(d, x + 4, 191, 16, 54, '#665064')
    for x, color in [(12, '#895161'), (109, '#647786'), (206, '#986852'),
                     (303, '#7c4b63'), (400, '#647786')]:
        d.polygon([(x, 222), (x + 12, 204), (x + 60, 204), (x + 73, 222)], fill=color)
        for rib in range(4):
            d.line((x + 12 + rib * 14, 205, x + 3 + rib * 20, 222), fill='#b29184', width=2)
        d.rectangle((x, 222, x + 73, 225), fill='#553f51')
        d.line((x + 4, 225, x + 4, 265), fill='#584257', width=3)
        d.line((x + 68, 225, x + 68, 265), fill='#584257', width=3)
    save(market, 'scenery/market.png')


def customs_house():
    exterior = Image.new('RGBA', (480, 224))
    d = ImageDraw.Draw(exterior)
    for center in (56, 424):
        dome(d, center, 63, 37, '#a78883', '#755267', GOLD)
    stones = ('#615564', '#aa9188', '#9d837f', '#947881', '#b09a8d')
    bricks(d, (0, 111, 479, 223), stones)
    for y in (106, 109, 132):
        d.line((0, y, 479, y), fill=GOLD, width=2)
    for center in (56, 424):
        bricks(d, (center - 41, 87, center + 41, 223), stones, 16, 8)
        arch(d, center - 25, 95, 50, 128, '#664f62')
        arch(d, center - 21, 99, 42, 124, GOLD)
        arch(d, center - 18, 105, 36, 118, '#302d41')
        banner(d, center - 47, 115, PURPLE)
    for x in range(140, 369, 44):
        arch(d, x, 146, 20, 45, '#544556')
        arch(d, x + 3, 151, 14, 37, '#776379')
        d.line((x + 10, 153, x + 10, 187), fill=GOLD)
    save(exterior, 'customs/exterior.png')

    room = Image.new('RGB', (480, 270))
    d = ImageDraw.Draw(room)
    bricks(d, (0, 0, 479, 269), ('#494253', '#8f7b7c', '#8a737c', '#826979'))
    for center in (64, 240, 416):
        arch(d, center - 70, 20, 140, 179, GOLD)
        arch(d, center - 65, 24, 130, 175, '#604e68')
        arch(d, center - 53, 37, 106, 140, '#725b77')
        if center == 240:
            for x in (203, 232, 261):
                arch(d, x, 82, 16, 65, '#d1a6a2')
                d.line((x + 8, 90, x + 8, 145), fill='#5b4965', width=2)
            d.line((185, 151, 295, 151), fill=GOLD, width=2)
        else:
            banner(d, center - 10, 90, PURPLE)
    for x in (0, 139, 327, 466):
        d.rectangle((x, 37, x + 13, 236), fill='#b99b8c')
        d.rectangle((x + 9, 37, x + 13, 236), fill='#795e70')
        for y in (38, 43, 222, 231):
            d.rectangle((x - 3, y, x + 16, y + 3), fill=GOLD)
    d.rectangle((0, 206, 479, 235), fill='#816578')
    for x in range(8, 479, 24):
        d.polygon([(x, 210), (x + 10, 220), (x, 230), (x - 10, 220)], outline='#ad867d')
    bricks(d, (0, 238, 479, 269), ('#62525e', '#8e7982', '#9d8489', '#877282'), 32, 8)
    d.line((0, 238, 479, 238), fill='#c5a492', width=2)
    save(room, 'customs/interior.png')

    foreground = Image.new('RGBA', (480, 270))
    d = ImageDraw.Draw(foreground)
    d.rectangle((0, 256, 479, 269), fill='#433645')
    d.line((0, 256, 479, 256), fill=GOLD, width=2)
    for x in range(8, 480, 16):
        d.line((x, 259, x + 5, 267), fill='#796176', width=2)
    save(foreground, 'customs/foreground.png')

    door = Image.new('RGBA', (32, 128))
    d = ImageDraw.Draw(door)
    arch(d, 0, 0, 31, 127, '#3d313d')
    for x in (2, 8, 14, 17, 23, 29):
        d.line((x, 12 if x in (2, 29) else 5, x, 127), fill='#6f4949', width=2)
    for y in (25, 57, 91, 119):
        d.line((1, y, 30, y), fill=GOLD, width=2)
    d.line((15, 3, 15, 127), fill='#352939', width=2)
    save(door, 'customs/door.png')

    frame = Image.new('RGBA', (48, 144))
    d = ImageDraw.Draw(frame)
    arch(d, 0, 0, 47, 143, '#5f4c60')
    arch(d, 3, 3, 41, 140, GOLD)
    arch(d, 7, 8, 33, 143, (0, 0, 0, 0))
    save(frame, 'customs/door-frame.png')


def paving():
    image = Image.new('RGB', (32, 16))
    d = ImageDraw.Draw(image)
    stones = ('#504451', '#7e7079', '#756673', '#8a7980', '#816d75')
    bricks(d, (0, 0, 31, 15), stones, 8, 4)
    d.line((0, 0, 15, 0), fill='#d0b1a1')
    d.line((0, 1, 15, 1), fill='#9a8186')
    save(image, 'maps/paving.png')


def envoy(name, cloak, trim, crown=False):
    image = Image.new('RGBA', (24, 36))
    d = ImageDraw.Draw(image)
    d.ellipse((3, 31, 21, 35), fill='#362e3d80')
    d.polygon([(9, 12), (15, 11), (18, 16), (21, 32), (6, 32), (4, 28), (7, 16)], fill='#292435')
    d.polygon([(9, 13), (15, 12), (17, 16), (19, 30), (7, 31), (6, 27)], fill=cloak)
    d.line((14, 17, 15, 29), fill=trim, width=2)
    d.line((8, 28, 18, 28), fill=trim)
    d.rectangle((8, 31, 12, 33), fill='#3e3038')
    d.rectangle((14, 31, 18, 33), fill='#3e3038')
    d.polygon([(8, 6), (14, 5), (16, 7), (15, 12), (10, 14), (7, 12), (6, 10), (8, 9)], fill='#c2997f')
    d.polygon([(7, 12), (11, 12), (14, 10), (14, 13), (11, 15), (8, 14)], fill='#544049')
    d.point((9, 9), fill='#342832')
    if crown:
        d.polygon([(7, 7), (7, 3), (10, 5), (12, 2), (14, 4), (16, 3), (16, 7)], fill=GOLD)
        d.point((12, 5), fill='#ac5364')
    else:
        d.polygon([(7, 7), (9, 4), (14, 4), (17, 7), (15, 8)], fill=trim)
    d.line((11, 18, 7, 21, 5, 19), fill=trim, width=2)
    d.line((3, 17, 6, 22), fill='#dec9a2', width=2)
    save(image, 'envoys/' + name + '.png')


def captain_art():
    """Ten poses with shared anchors, facing right."""
    sheet = Image.new('RGBA', (40 * 10, 48))
    ink, shade = '#252237', '#42334d'
    brass, light = '#b77843', '#f1cb73'
    for frame in range(10):
        art = Image.new('RGBA', (40, 48))
        d = ImageDraw.Draw(art)
        stride = {1: -4, 2: 4}.get(frame, 0)
        lean = {3: -2, 4: 3, 5: -2, 6: 1, 7: 2, 8: -2}.get(frame, 0)

        def polygon(points, fill):
            d.polygon([(x + lean, y) for x, y in points], fill=fill)

        def line(points, fill, width=1):
            d.line([(x + lean, y) for x, y in points], fill=fill, width=width)

        polygon([(17, 15), (25, 20), (21, 34), (18, 43), (11, 39), (5, 42),
                 (9, 32), (6, 36), (6, 27), (11, 19)], ink)
        polygon([(16, 16), (22, 21), (18, 32), (16, 40), (11, 36), (7, 39),
                 (10, 31), (8, 32), (8, 27), (12, 20)], '#693852')
        line([(13, 23), (10, 30), (8, 36)], '#a55b6f')
        for boot, offset in [(16, stride), (23, -stride)]:
            polygon([(boot - 3, 30), (boot + 3, 30), (boot + offset + 3, 43),
                     (boot + offset + 5, 44), (boot + offset + 5, 45), (boot + offset - 3, 45)], ink)
            line([(boot, 35), (boot + offset, 42)], '#8b5544', 2)
            line([(boot + offset - 1, 43), (boot + offset + 2, 43)], brass)

        polygon([(14, 17), (25, 16), (27, 22), (23, 33), (14, 34), (11, 26)], ink)
        for row in range(4):
            y = 20 + row * 3
            line([(14, y), (23 - row // 2, y - 1)], '#92604e', 2)
            for x in range(15 + row % 2, 24, 3):
                line([(x, y - 1), (x, y - 1)], brass)
        line([(14, 32), (23, 31)], '#d39a58', 2)
        line([(16, 33), (12, 39)], '#685476', 2)
        polygon([(10, 19), (12, 15), (17, 16), (21, 20), (17, 23), (10, 22)], shade)
        line([(11, 20), (14, 17), (19, 20)], brass, 2)
        polygon([(16, 16), (14, 11), (16, 6), (21, 4), (25, 7), (27, 12), (27, 16),
                 (22, 19)], ink)
        polygon([(15, 10), (18, 6), (23, 6), (25, 9), (26, 11), (20, 10)], brass)
        line([(18, 7), (23, 7)], light)
        polygon([(20, 12), (26, 12), (26, 15), (23, 17), (20, 15)], '#a95e40')
        line([(22, 12), (25, 12)], '#fff1ae')
        polygon([(18, 7), (14, 7), (12, 5), (10, 6), (8, 2), (14, 2), (18, 4), (21, 5)], '#a34f63')
        line([(9, 2), (14, 3), (18, 5)], '#d77983')

        if frame in (6, 7):
            grip = (30 - lean, 25)
            elbow = (25, 24)
            line([(21, 22), elbow, grip], ink, 5)
            line([(21, 22), elbow, grip], brass, 3)
            line([(28, 13), (32, 16), (34, 21), (34, 29), (31, 34), (27, 36)], ink, 3)
            line([(28, 13), (32, 16), (34, 21), (34, 29), (31, 34), (27, 36)], light)
            draw = (17, 25) if frame == 6 else (26, 25)
            line([(28, 13), draw, (27, 36)], '#ead9a8')
            line([(19, 27), draw], brass, 3)
            if frame == 6:
                line([(18, 25), (36, 25)], '#ffd27e')
                polygon([(37, 25), (34, 23), (34, 27)], '#fff1ae')
        else:
            grip = {3: (15, 16), 4: (25, 25), 5: (32, 23), 8: (16, 31)}.get(frame, (24, 29))
            end = {3: (6, 6), 4: (35, 30), 5: (37, 18), 8: (10, 42)}.get(frame, (30, 42))
            line([(22, 22), grip], ink, 5)
            line([(22, 22), grip], brass, 3)
            line([grip, end], ink, 4)
            line([grip, end], '#f2c986', 2)
            line([(grip[0] - 2, grip[1] + 1), (grip[0] + 2, grip[1] - 1)], light, 2)
        if frame == 9:
            art = art.rotate(90, expand=True, resample=Image.Resampling.NEAREST)
            collapsed = Image.new('RGBA', (40, 48))
            collapsed.paste(art.resize((40, 34), Image.Resampling.NEAREST), (0, 14))
            art = collapsed
        sheet.paste(art, (frame * 40, 0))
    save(sheet, 'boss/captain.png')


def map_file():
    columns, rows, tile = 280, 17, 32
    height = rows * tile
    solids = [
        (0, 0, columns * tile, 64),
        # Outer walls: a broad rampart and a second tower terrace.
        (704, 64, 96, 32), (800, 64, 96, 64), (896, 64, 416, 96),
        (1312, 64, 96, 64), (1408, 64, 64, 32), (1664, 64, 256, 32),
        # Imperial quarter: split colonnades with a raised gallery.
        (2720, 64, 96, 32), (2816, 64, 320, 64), (3136, 64, 64, 32),
        (3328, 128, 192, 32), (3552, 64, 96, 32), (3648, 64, 384, 64),
        (4032, 64, 96, 32),
        # Market: low awnings, steep shop roofs and storage terraces.
        (5088, 128, 224, 32), (5376, 64, 96, 32), (5472, 64, 96, 64),
        (5568, 64, 352, 96), (5920, 64, 64, 64), (5984, 64, 64, 32),
        (6176, 64, 192, 32),
        # Waterfront: raised loading pier and warehouse steps.
        (6624, 64, 96, 32), (6720, 64, 256, 64), (6976, 64, 64, 32),
        (7456, 64, 96, 32), (7552, 64, 160, 64), (7712, 64, 64, 32),
    ]
    grid = [[0] * columns for _ in range(rows)]
    for x, y, width, depth in solids:
        for column in range(x // tile, (x + width) // tile):
            for row in range(y // tile, (y + depth) // tile):
                grid[rows - row - 1][column] = 2
    for row in range(1, rows):
        for column in range(columns):
            if grid[row][column] and not grid[row - 1][column]:
                grid[row][column] = 1

    tmx = ET.Element('map', {'version': '1.10', 'tiledversion': '1.11.2', 'orientation': 'orthogonal',
                            'renderorder': 'right-down', 'width': str(columns), 'height': str(rows),
                            'tilewidth': '32', 'tileheight': '32', 'infinite': '0', 'nextlayerid': '4'})
    tiles = ET.SubElement(tmx, 'tileset', firstgid='1', name='paving', tilewidth='32', tileheight='32',
                          tilecount='2', columns='2')
    ET.SubElement(tiles, 'image', source='paving.png', width='64', height='32')
    layer = ET.SubElement(tmx, 'layer', id='1', name='terrain', width=str(columns), height=str(rows))
    ET.SubElement(layer, 'data', encoding='csv').text = '\n' + ',\n'.join(
        ','.join(map(str, row)) for row in grid) + '\n'
    collisions = ET.SubElement(tmx, 'objectgroup', id='2', name='collisions')
    entities = ET.SubElement(tmx, 'objectgroup', id='3', name='entities')
    object_id = 1

    def place(group, kind, x, y, w=0, h=0):
        nonlocal object_id
        node = ET.SubElement(group, 'object', id=str(object_id), x=str(x), y=str(height - y - h))
        object_id += 1
        if kind:
            node.set('type', kind)
        if w:
            node.set('width', str(w))
            node.set('height', str(h))
        else:
            ET.SubElement(node, 'point')

    for solid in solids:
        place(collisions, None, *solid)
    for kind, x, y in [
        ('spawn', 112, 64), ('checkpoint', 4864, 64),
        ('sentinel', 528, 64), ('archer', 1224, 160), ('spearman', 1808, 96),
        ('sentinel', 2930, 128), ('sentinel', 3750, 128), ('archer', 3980, 128),
        ('sentinel', 5264, 160), ('spearman', 5720, 160), ('archer', 6260, 96),
        ('sentinel', 6840, 128), ('archer', 7640, 128),
        ('harbor_captain', 8304, 64),
    ]:
        place(entities, kind, x, y)
    for kind, x, y, w, h in [
        ('bandage', x, y, 24, 28) for x, y in [
            (848, 128), (1496, 64), (2180, 64), (3180, 96), (3392, 160), (4180, 64),
            (5170, 160), (5870, 160), (6432, 64), (6910, 128), (7792, 64), (8120, 64),
        ]
    ] + [
        ('arena', 7936, 64, 704, 256), ('gate', 8640, 64, 32, 256), ('exit', 8816, 64, 112, 110),
    ]:
        place(entities, kind, x, y, w, h)
    tmx.set('nextobjectid', str(object_id))
    ET.indent(tmx, space='  ')
    ET.ElementTree(tmx).write(DEST / 'maps/constantinople.tmx', encoding='UTF-8', xml_declaration=True)


if __name__ == '__main__':
    scenery()
    customs_house()
    envoy('alexios', '#723d62', '#d7ad71', crown=True)
    envoy('delegate', '#943f4f', '#bb935d')
    envoy('steward', '#416e76', '#b1a18e')
    paving()
    captain_art()
    map_file()
