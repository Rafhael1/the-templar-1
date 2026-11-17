"""Build the Niceia TMX map and its pixel art with Pillow."""
from pathlib import Path
import random

from PIL import Image, ImageDraw

from generate_constantinople import arch, banner, bricks, envoy_image
from level_map import write_map

DEST = Path(__file__).resolve().parents[1] / 'assets/levels/nicaea'
RANDOM = random.Random(6191097)
STONE = ('#515749', '#928b69', '#857e64', '#a39a74', '#79765f')
WOOD = ('#392a29', '#6b4937', '#7d543d', '#654633')


def save(image, name):
    path = DEST / name
    path.parent.mkdir(parents=True, exist_ok=True)
    image.resize((image.width * 2, image.height * 2), Image.Resampling.NEAREST).save(path)


def landscape(sky, horizon):
    image = Image.new('RGB', (480, 270))
    d = ImageDraw.Draw(image)
    for y in range(270):
        t = min(y / 185, 1)
        d.line((0, y, 479, y), fill=tuple(round(a * (1 - t) + b * t) for a, b in zip(sky, horizon)))
    d.ellipse((352, 100, 380, 128), fill='#edcc8b')
    for _ in range(50):
        x, y = RANDOM.randrange(480), RANDOM.randrange(38, 144)
        d.line((x, y, x + RANDOM.randrange(6, 36), y), fill=tuple((a + b * 2) // 3
               for a, b in zip(sky, horizon)))
    d.polygon([(0, 157), (43, 137), (106, 156), (177, 125), (234, 151), (280, 140),
               (318, 153), (410, 129), (480, 155), (480, 269), (0, 269)], fill='#738681')
    d.polygon([(0, 171), (102, 163), (153, 176), (222, 164), (296, 173),
               (373, 159), (420, 169), (480, 163), (480, 269), (0, 269)], fill='#576e66')
    return image, d


def walls(d, top, bottom, towers):
    bricks(d, (0, top, 479, bottom), ('#62675a', '#a39c7f', '#938d76', '#9b9478'), 10, 5)
    for x in range(0, 480, 13):
        d.rectangle((x, top - 6, x + 7, top), fill='#a39c7f')
    for x, width in towers:
        bricks(d, (x, top - 15, x + width, bottom), STONE)
        for crenel in range(x, x + width, 12):
            d.rectangle((crenel, top - 22, min(x + width, crenel + 7), top - 15), fill='#a39a74')
        arch(d, x + width // 2 - 3, top - 9, 6, 16, '#464b49')
        d.line((x, bottom - 4, x + width, bottom - 4), fill='#b09d74')


def tent(d, x, y, width, canvas):
    height = width * 2 // 3
    d.polygon([(x - 8, y), (x + width // 2, y - height - 4), (x + width + 9, y)], fill='#565848')
    d.polygon([(x, y), (x + width // 2, y - height), (x + width, y)], fill=canvas)
    d.polygon([(x + width // 3, y), (x + width // 2, y - height + 4),
               (x + width * 2 // 3, y)], fill='#4d4439')
    d.line((x + width // 2, y - height, x + width * 4 // 5, y), fill='#978464')
    d.line((x - 11, y + 3, x + width // 6, y - height // 2), fill='#a48d65')
    d.line((x + width + 10, y + 3, x + width * 5 // 6, y - height // 2), fill='#a48d65')


def scenery():
    camp, d = landscape((65, 100, 104), (202, 185, 128))
    walls(d, 174, 204, [(22, 27), (153, 37), (303, 31), (433, 38)])
    d.polygon([(0, 220), (124, 208), (210, 219), (311, 202), (480, 215), (480, 269), (0, 269)],
              fill='#6b7455')
    for x, y, w, color in [(30, 230, 54, '#b89b67'), (122, 222, 40, '#af936a'),
                            (254, 232, 65, '#c4ac79'), (397, 226, 51, '#a6916d')]:
        tent(d, x, y, w, color)
    for _ in range(120):
        x, y = RANDOM.randrange(480), RANDOM.randrange(235, 268)
        d.line((x, y, x + 4, y - 1), fill=RANDOM.choice(('#88825b', '#565f45', '#a08e66')))
    save(camp, 'scenery/camp.png')

    lake, d = landscape((54, 90, 115), (183, 194, 153))
    walls(d, 164, 189, [(8, 29), (116, 31), (313, 34), (462, 17)])
    d.rectangle((0, 189, 479, 269), fill='#3e7478')
    for _ in range(240):
        x, y = RANDOM.randrange(480), RANDOM.randrange(190, 269)
        d.line((x, y, x + RANDOM.randrange(3, 28), y),
               fill=RANDOM.choice(('#518489', '#68979a', '#476e76', '#7b9d8f')))
    for x, y, w in [(72, 226, 56), (230, 211, 38), (387, 233, 78)]:
        d.polygon([(x, y), (x + w, y - 2), (x + w - 11, y + 7), (x + 10, y + 6)], fill='#473b3b')
        d.line((x + w // 2, y, x + w // 2, y - w // 2), width=2, fill='#58423c')
        d.polygon([(x + w // 2, y - w // 2), (x + w // 2, y - 3), (x + 7, y - 6)], fill='#c5b17d')
        d.line((x + 8, y + 1, x + w - 6, y), fill='#a27b58')
        for oar in range(x + 15, x + w - 7, 9):
            d.line((oar, y + 3, oar - 8, y + 12), fill='#938a64')
    d.polygon([(0, 255), (54, 249), (110, 256), (198, 251), (250, 257),
               (361, 248), (479, 252), (479, 269), (0, 269)], fill='#647455')
    save(lake, 'scenery/lake.png')

    works, d = landscape((76, 101, 91), (214, 181, 119))
    walls(d, 170, 208, [(17, 30), (125, 37), (291, 34), (431, 30)])
    d.polygon([(0, 223), (117, 215), (225, 219), (327, 211), (479, 218),
               (479, 269), (0, 269)], fill='#74664b')
    for x, y in [(87, 226), (365, 236)]:
        d.rectangle((x, y - 40, x + 48, y), fill='#514839')
        for post in (x, x + 12, x + 38, x + 48):
            d.rectangle((post, y - 45, post + 3, y), fill='#947350')
        d.line((x, y, x + 48, y - 38), fill='#866345', width=3)
        d.line((x, y - 38, x + 48, y), fill='#866345', width=3)
        d.polygon([(x - 8, y - 42), (x + 24, y - 60), (x + 55, y - 42)], fill='#58483e')
        d.line((x - 6, y - 43, x + 24, y - 58), fill='#b08758', width=2)
    for _ in range(105):
        x, y = RANDOM.randrange(480), RANDOM.randrange(224, 269)
        d.line((x, y, x + 7, y), fill=RANDOM.choice(('#8d7959', '#665941', '#9e845b')))
    save(works, 'scenery/earthworks.png')

    ridge, d = landscape((75, 87, 91), (199, 154, 105))
    d.polygon([(0, 174), (110, 154), (185, 168), (268, 149), (355, 166), (479, 147),
               (479, 269), (0, 269)], fill='#5d6352')
    for x in range(0, 480, 9):
        y = 209 - x // 32 + RANDOM.randrange(5)
        d.polygon([(x, 247), (x, y), (x + 3, y - 6), (x + 7, y), (x + 7, 247)], fill='#605144')
        d.line((x + 1, y, x + 1, 247), fill='#827257')
    for x, y, w in [(46, 217, 53), (171, 215, 59), (316, 206, 65)]:
        tent(d, x, y, w, '#a38d61')
    d.rectangle((0, 241, 479, 269), fill='#645b46')
    for _ in range(85):
        x, y = RANDOM.randrange(480), RANDOM.randrange(242, 269)
        d.line((x, y, x + 7, y), fill=RANDOM.choice(('#797052', '#534d40')))
    save(ridge, 'scenery/ridge.png')


def redoubt():
    exterior = Image.new('RGBA', (480, 224))
    d = ImageDraw.Draw(exterior)
    bricks(d, (0, 169, 479, 223), STONE)
    for x in range(0, 480, 8):
        top = 153 - RANDOM.randrange(8)
        d.polygon([(x, 183), (x, top), (x + 3, top - 8), (x + 7, top), (x + 7, 183)], fill='#72553f')
        d.line((x + 1, top, x + 1, 182), fill='#9f7651')
    for center in (56, 424):
        bricks(d, (center - 44, 117, center + 44, 223), STONE)
        d.polygon([(center - 48, 118), (center - 36, 98), (center + 36, 98), (center + 48, 118)],
                  fill='#765044')
        d.line((center - 47, 117, center + 47, 117), fill='#b88c59', width=2)
        arch(d, center - 22, 123, 44, 100, '#70604a')
        arch(d, center - 17, 128, 34, 95, '#3d3d33')
    for x in (149, 224, 299, 370):
        banner(d, x, 158, '#83523d')
    save(exterior, 'redoubt/exterior.png')

    interior = Image.new('RGB', (480, 270))
    d = ImageDraw.Draw(interior)
    bricks(d, (0, 0, 479, 237), STONE, 18, 8)
    for center in (76, 240, 404):
        arch(d, center - 67, 62, 134, 172, '#75654b')
        arch(d, center - 59, 70, 118, 163, '#453f39')
        for x in (center - 22, center + 14):
            arch(d, x, 110, 8, 54, '#a3976d')
        banner(d, center - 11, 151, '#986048')
    for x in (0, 150, 317, 467):
        d.rectangle((x, 34, x + 12, 237), fill='#664d38')
        d.line((x + 2, 37, x + 2, 237), fill='#a77a52', width=2)
        for y in (54, 171, 228):
            d.rectangle((x - 2, y, x + 15, y + 6), fill='#4e4942')
    d.rectangle((0, 0, 479, 40), fill='#504033')
    for x in range(0, 480, 48):
        d.line((x, 0, x, 40), fill='#7a573f', width=3)
    bricks(d, (0, 238, 479, 269), WOOD, 34, 7)
    d.line((0, 238, 479, 238), fill='#b48755', width=2)
    save(interior, 'redoubt/interior.png')

    foreground = Image.new('RGBA', (480, 270))
    d = ImageDraw.Draw(foreground)
    d.rectangle((0, 257, 479, 269), fill='#3b3129')
    d.line((0, 257, 479, 257), fill='#9e7450', width=2)
    for x in range(8, 480, 24):
        d.line((x, 260, x + 4, 268), fill='#694c3a')
    save(foreground, 'redoubt/foreground.png')

    door = Image.new('RGBA', (32, 128))
    d = ImageDraw.Draw(door)
    arch(d, 0, 0, 31, 127, '#523c2d')
    for x in (3, 9, 14, 17, 23, 29):
        d.line((x, 13 if x in (3, 29) else 5, x, 127), fill='#876143')
    for y in (24, 57, 91, 117):
        d.rectangle((1, y, 30, y + 3), fill='#433f3d')
        for x in (3, 12, 19, 28):
            d.point((x, y + 1), fill='#a99773')
    save(door, 'redoubt/door.png')

    frame = Image.new('RGBA', (48, 144))
    d = ImageDraw.Draw(frame)
    arch(d, 0, 0, 47, 143, '#6d5e48')
    arch(d, 3, 3, 41, 140, '#a18a61')
    arch(d, 7, 8, 33, 143, (0, 0, 0, 0))
    save(frame, 'redoubt/door-frame.png')


def duelist_art():
    """Nine right-facing poses. Feet meet the floor at pixel 44."""
    sheet = Image.new('RGBA', (72 * 9, 48))
    ink, leather = '#262232', '#4a3943'
    silk, shade = '#a14142', '#69313e'
    bronze, gold = '#bc784f', '#f9cb83'
    steel = '#f3ead6'
    stances = [
        ((27, 29), (15, 38), (44, 25), (55, 12)),
        ((24, 25), (12, 31), (44, 27), (57, 20)),
        ((26, 29), (13, 37), (44, 24), (56, 13)),
        ((27, 24), (14, 34), (32, 17), (19, 6)),
        ((31, 27), (18, 38), (50, 23), (68, 25)),
        ((33, 19), (48, 9), (39, 27), (24, 8)),
        ((46, 27), (67, 8), (50, 23), (69, 34)),
        ((29, 34), (20, 45), (43, 33), (55, 44)),
        ((25, 32), (11, 39), (45, 32), (61, 36)),
    ]

    for frame, (rear_grip, rear_tip, lead_grip, lead_tip) in enumerate(stances):
        pose = Image.new('RGBA', (72, 48))
        d = ImageDraw.Draw(pose)
        lean = (0, 1, 1, -3, 4, -2, 4, -2, 0)[frame]
        dip = (0, 2, 0, 1, 3, 3, 5, 8, 0)[frame]
        stride = (0, 5, -5, -3, 6, 2, 7, 2, 0)[frame]
        cx = 35 + lean

        def arm(shoulder, grip, tip):
            d.line((shoulder, grip), fill=ink, width=5)
            d.line((shoulder, grip), fill=bronze, width=3)
            d.line((grip, tip), fill=ink, width=5)
            d.line((grip, tip), fill=steel, width=3)
            x, y = grip
            d.ellipse((x - 2, y - 2, x + 2, y + 2), fill=gold)

        if frame == 4:
            d.arc((34, 8, 71, 40), 315, 70, fill='#9fdace', width=2)
        if frame == 6:
            d.arc((36, 1, 71, 46), 280, 102, fill='#f3b474', width=2)
            d.line((51, 27, 69, 9), fill='#e6c39c')
        for x, offset in [(32, -stride), (39, stride)]:
            d.line((cx, 30 + dip, x + offset, 42), fill=ink, width=6)
            d.line((x + offset, 39, x + offset + 4, 43), fill=bronze, width=3)
            d.rectangle((x + offset - 2, 43, x + offset + 5, 44), fill=ink)
        d.polygon([(cx - 5, 15 + dip), (cx - 14, 19 + dip), (cx - 23, 17 + dip),
                   (cx - 29, 24 + dip), (cx - 17, 23 + dip), (cx - 8, 27 + dip)],
                  fill=shade)
        d.line((cx - 27, 23 + dip, cx - 17, 21 + dip, cx - 7, 19 + dip), fill=silk, width=2)
        arm((cx - 3, 21 + dip), rear_grip, rear_tip)
        d.polygon([(cx - 6, 15 + dip), (cx + 6, 16 + dip), (cx + 9, 22 + dip),
                   (cx + 4, 34 + dip), (cx - 7, 33 + dip), (cx - 9, 23 + dip)], fill=ink)
        d.polygon([(cx - 5, 17 + dip), (cx + 5, 17 + dip), (cx + 6, 25 + dip),
                   (cx + 2, 31 + dip), (cx - 5, 30 + dip)], fill=leather)
        for y in range(20 + dip, 30 + dip, 3):
            d.line((cx - 5, y, cx + 4, y - 1), fill=bronze)
        d.line((cx - 7, 31 + dip, cx + 3, 32 + dip), fill=gold, width=2)
        d.polygon([(cx - 6, 9 + dip), (cx - 2, 5 + dip), (cx + 3, 4 + dip),
                   (cx + 8, 10 + dip), (cx + 7, 17 + dip), (cx + 1, 19 + dip),
                   (cx - 6, 15 + dip)], fill=ink)
        d.polygon([(cx - 5, 9 + dip), (cx + 2, 5 + dip), (cx + 7, 9 + dip),
                   (cx + 7, 12 + dip), (cx - 6, 13 + dip)], fill=silk)
        d.line((cx, 12 + dip, cx + 7, 11 + dip), fill=gold, width=2)
        d.rectangle((cx + 3, 14 + dip, cx + 7, 15 + dip), fill=steel)
        arm((cx + 3, 22 + dip), lead_grip, lead_tip)
        if frame == 8:
            fallen = Image.new('RGBA', (72, 48))
            fallen.paste(pose.rotate(90, expand=True).resize((50, 33), Image.Resampling.NEAREST), (11, 17))
            pose = fallen
        sheet.paste(pose, (frame * 72, 0))
    save(sheet, 'boss/duelist.png')


def map_file():
    paving = Image.new('RGB', (32, 16))
    d = ImageDraw.Draw(paving)
    bricks(d, (0, 0, 31, 15), STONE, 8, 4)
    d.line((0, 0, 15, 0), fill='#bab089')
    d.line((0, 1, 15, 1), fill='#8f875d')
    save(paving, 'maps/paving.png')

    solids = [
        (0, 0, 10240, 64),
        # Low earthworks outside the besiegers' camp.
        (768, 64, 128, 32), (896, 64, 320, 64), (1216, 64, 128, 32),
        (1536, 64, 256, 32),
        # Landing, causeway and an optional raised supply platform.
        (2368, 64, 128, 32), (2496, 64, 320, 64), (2816, 64, 128, 32),
        (3264, 128, 224, 32), (3552, 64, 288, 32),
        # Earthworks, raised timber deck and a tiered approach to the sanctuary.
        (4320, 64, 96, 32), (4416, 64, 96, 64), (4512, 64, 288, 96),
        (4800, 64, 96, 64), (4896, 64, 96, 32),
        (5312, 64, 160, 32), (5504, 128, 288, 32), (5824, 64, 160, 32),
        (6208, 64, 96, 32), (6304, 64, 288, 64), (6592, 64, 96, 32),
        # Incline defended by the relief force.
        (7360, 64, 160, 32), (7680, 64, 96, 32), (7776, 64, 96, 64),
        (7872, 64, 320, 96), (8192, 64, 96, 64), (8288, 64, 96, 32),
        (8608, 64, 256, 32),
    ]
    points = [
        ('spawn', 112, 64), ('checkpoint', 6848, 64),
        ('sentinel', 632, 64), ('archer', 1088, 128), ('spearman', 1650, 96),
        ('sentinel', 2700, 128), ('archer', 3664, 96),
        ('spearman', 4660, 160), ('archer', 5680, 160), ('sentinel', 6448, 128),
        ('sentinel', 7456, 96), ('spearman', 8020, 160), ('archer', 8752, 96),
        ('relief_duelist', 9568, 64),
    ]
    areas = [('bandage', x, y, 24, 28) for x, y in
             [(920, 128), (2900, 96), (4916, 96),
              (6640, 96), (8072, 160), (8912, 64)]]
    areas.extend([('arena', 9184, 64, 704, 256), ('gate', 9888, 64, 32, 256),
                  ('exit', 10048, 64, 128, 110)])
    write_map(DEST / 'maps/nicaea.tmx', 320, solids, points, areas)


if __name__ == '__main__':
    scenery()
    redoubt()
    duelist_art()
    save(envoy_image('#855437', '#c8ac76'), 'envoys/marshal.png')
    save(envoy_image('#416b68', '#d4b178'), 'envoys/boutoumites.png')
    map_file()
