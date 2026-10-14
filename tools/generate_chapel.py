"""Original modular Gothic chapel pixel art. Writes only assets/levels/exiles-road/chapel/*.png."""
from pathlib import Path
from PIL import Image, ImageDraw
import math
import random

ROOT = Path(__file__).resolve().parents[1] / 'assets'
DEST = ROOT / 'levels/exiles-road/chapel'
DEST.mkdir(parents=True, exist_ok=True)
STONE = '#807167'
SHADE = '#655864'
DARK = '#35333f'
DEEP = '#202735'
GOLD = '#c7ab78'
LIGHT = '#e5dbc0'
BLUE = '#365c69'
RED = '#743844'
rng = random.Random(1099)


def cross(d, x, y, size):
    d.rectangle((x - 2, y, x + 2, y + size), fill='#54444b')
    d.rectangle((x - size // 3, y + size // 3, x + size // 3, y + size // 3 + 4), fill='#54444b')
    d.line((x, y + 1, x, y + size - 1), fill=GOLD, width=2)
    d.line((x - size // 3 + 1, y + size // 3 + 1, x + size // 3 - 1, y + size // 3 + 1), fill=GOLD, width=2)
    for px, py in ((x, y), (x - size // 3, y + size // 3 + 1), (x + size // 3, y + size // 3 + 1)):
        d.rectangle((px - 1, py - 1, px + 1, py + 1), fill='#e5d6a7')


def pointed(d, box, color):
    x, y, w, h = box
    d.polygon([(x, y + h), (x, y + w // 2), (x + w // 4, y + 5), (x + w // 2, y),
               (x + w * 3 // 4, y + 5), (x + w, y + w // 2), (x + w, y + h)], fill=color)


def masonry(d, box, colors, cell=24, height=10):
    x0, y0, x1, y1 = box
    d.rectangle(box, fill=colors[0])
    for row, y in enumerate(range(y0, y1, height)):
        for x in range(x0 - (cell // 2 if row % 2 else 0), x1, cell):
            left, right = max(x0, x), min(x1, x + cell - 2)
            d.rectangle((left, y, right, min(y1, y + height - 2)), fill=rng.choice(colors))
            if right - left > 5:
                d.line((left + 2, y + 1, min(right, left + 8), y + 1), fill=colors[-1])


def stained_window(image, x, y, width, height):
    d = ImageDraw.Draw(image)
    pointed(d, (x - 4, y - 4, width + 8, height + 8), '#272934')
    pointed(d, (x - 2, y - 2, width + 4, height + 4), '#a18a70')
    pointed(d, (x, y, width, height), GOLD)
    mask = Image.new('L', image.size)
    pointed(ImageDraw.Draw(mask), (x + 2, y + 3, width - 4, height - 5), 255)
    glass = Image.new('RGBA', image.size)
    g = ImageDraw.Draw(glass)
    colors = ['#365c69', '#456d80', '#743844', '#9e6b58', '#c7ab78', '#a7b59e']
    for yy in range(y, y + height + 1, 7):
        for xx in range(x, x + width + 1, 6):
            g.rectangle((xx, yy, xx + 5, yy + 6), fill=colors[((xx - x) // 6 + (yy - y) // 7) % len(colors)])
            g.line((xx, yy, xx + 5, yy + 6), fill='#242c36')
    glass.putalpha(mask)
    image.alpha_composite(glass)
    d = ImageDraw.Draw(image)
    d.line((x + width // 2, y + 4, x + width // 2, y + height - 1), fill='#e5d6a7', width=2)
    for yy in (y + height // 3, y + height * 2 // 3):
        d.line((x + 1, yy, x + width - 1, yy), fill=GOLD, width=2)
    d.rectangle((x - 4, y + height, x + width + 4, y + height + 3), fill=STONE)
    d.line((x - 3, y + height, x + width + 3, y + height), fill=GOLD)


def rose(d, x, y, radius):
    d.ellipse((x - radius - 4, y - radius - 4, x + radius + 4, y + radius + 4), fill='#272934')
    d.ellipse((x - radius - 2, y - radius - 2, x + radius + 2, y + radius + 2), fill=STONE)
    d.ellipse((x - radius, y - radius, x + radius, y + radius), fill=GOLD)
    d.ellipse((x - radius + 3, y - radius + 3, x + radius - 3, y + radius - 3), fill=DEEP)
    for i in range(12):
        a, b = i * math.tau / 12, (i + 1) * math.tau / 12
        outer = radius - 5
        triangle = [(x, y), (round(x + math.cos(a) * outer), round(y + math.sin(a) * outer)),
                    (round(x + math.cos(b) * outer), round(y + math.sin(b) * outer))]
        d.polygon(triangle, fill=[BLUE, RED, '#a18a70'][i % 3])
        d.line(triangle[:2], fill=GOLD)
        px, py = round(x + math.cos(a) * radius * .64), round(y + math.sin(a) * radius * .64)
        d.ellipse((px - 3, py - 3, px + 3, py + 3), outline='#e5d6a7')
    d.ellipse((x - 8, y - 8, x + 8, y + 8), fill=GOLD)
    d.ellipse((x - 5, y - 5, x + 5, y + 5), fill=RED)
    cross(d, x, y - 4, 8)


def save(image, name):
    image.resize((image.width * 2, image.height * 2), Image.Resampling.NEAREST).save(DEST / name)


# Exterior modules join at native X=128 and X=368, leaving the two portals unscaled.
exterior = Image.new('RGBA', (480, 224))
d = ImageDraw.Draw(exterior)
d.rectangle((24, 88, 462, 222), fill='#4d454e')
masonry(d, (27, 92, 459, 222), ['#655864', '#807167', '#76695e', '#8e7b68'])
# Long slate roof with copper ridge, repetitive hand-laid tiles and a carved eave.
d.rectangle((108, 67, 440, 96), fill='#3e3847')
for y in range(70, 94, 5):
    d.line((106, y, 442, y), fill='#594b50')
    for x in range(110 + y % 3, 439, 12): d.line((x, y, x - 2, y + 4), fill='#807167')
d.line((110, 67, 439, 67), fill=GOLD, width=2)
d.rectangle((103, 94, 445, 99), fill='#b4a382')
for x in range(110, 440, 8): d.rectangle((x, 99, x + 3, 103), fill=SHADE)
# Buttresses, projecting bases and recessed side windows.
for x in (112, 192, 272, 352, 446):
    d.polygon([(x - 6, 220), (x - 4, 113), (x, 101), (x + 5, 112), (x + 8, 220)], fill=SHADE)
    d.line((x - 2, 114, x - 4, 219), fill='#b4a382', width=2)
    d.rectangle((x - 8, 216, x + 10, 222), fill=STONE)
for x in (142, 222, 302, 382): stained_window(exterior, x, 114, 26, 55)
d = ImageDraw.Draw(exterior)
# Bell tower, lancet bell chamber and steep gable.
masonry(d, (29, 41, 88, 219), ['#655864', '#807167', '#76695e', '#a18a70'], cell=17)
d.polygon([(19, 44), (58, 15), (97, 44)], fill='#3e3847')
d.line((20, 43, 57, 17, 96, 43), fill=GOLD, width=2)
d.rectangle((24, 43, 93, 47), fill='#b4a382')
cross(d, 58, 0, 16)
for x in (37, 66):
    pointed(d, (x, 52, 14, 35), DEEP)
    d.line((x + 1, 65, x + 1, 86), fill='#a18a70')
    d.rectangle((x + 5, 65, x + 9, 78), fill='#8e7356')
    d.polygon([(x + 4, 73), (x + 10, 73), (x + 12, 79), (x + 2, 79)], fill=GOLD)
    d.point((x + 7, 81), fill='#e5d6a7')
rose(d, 58, 108, 16)
# Entry and far portal, with carved jambs and open black doorway.
for cx in (56, 424):
    pointed(d, (cx - 22, 132, 44, 90), '#b4a382')
    pointed(d, (cx - 19, 135, 38, 87), SHADE)
    pointed(d, (cx - 16, 139, 32, 83), '#171f2a')
    for y in range(164, 217, 12):
        d.line((cx - 22, y, cx - 18, y), fill='#4d454e')
        d.line((cx + 18, y, cx + 22, y), fill='#4d454e')
    d.rectangle((cx - 26, 219, cx + 26, 223), fill='#a18a70')
    d.line((cx - 23, 219, cx + 23, 219), fill=GOLD)
# Ivy, a small carved relief, weathering and rose bushes around the approach.
for x in (30, 87, 452):
    for y in range(159, 219, 5):
        xx = x + (y // 5) % 3 - 1
        d.line((xx, y, xx, y + 5), fill='#3c4d38')
        d.rectangle((xx - 2, y + 1, xx - 1, y + 3), fill='#647650')
        d.point((xx + 2, y + 4), fill='#7f8663')
for x in (18, 104, 470):
    d.rectangle((x - 6, 212, x + 6, 222), fill='#3c4d38')
    for yy in (213, 217):
        d.rectangle((x - 3, yy, x - 1, yy + 2), fill='#973f4e')
        d.point((x + 2, yy + 1), fill='#d08a8c')
for x in range(124, 408, 47):
    d.line((x, 188, x + 4, 192, x + 4, 197), fill='#4d454e')
save(exterior, 'exterior.png')

# Interior: the opaque floor begins at native Y=238, exactly world Y=64.
interior = Image.new('RGBA', (480, 270), '#202735')
d = ImageDraw.Draw(interior)
masonry(d, (0, 0, 479, 237), ['#292d38', '#35333f', '#3e3847', '#544b52'], cell=25, height=11)
# Recessed apse and multiple ribbed bays.
pointed(d, (175, 31, 130, 205), '#272934')
pointed(d, (183, 39, 114, 193), '#35333f')
for cx in (82, 166, 314, 398):
    d.line([(cx - 40, 177), (cx - 39, 75), (cx - 29, 41), (cx, 17), (cx + 29, 41), (cx + 39, 75), (cx + 40, 177)], fill='#655864', width=5)
    d.line([(cx - 38, 177), (cx - 37, 75), (cx - 27, 43), (cx, 21)], fill='#a18a70', width=2)
    d.line([(cx + 37, 177), (cx + 37, 75), (cx + 27, 43), (cx, 21)], fill='#807167', width=2)
for x in (38, 122, 202, 278, 358, 442):
    d.rectangle((x - 5, 45, x + 5, 220), fill=SHADE)
    d.rectangle((x - 2, 52, x, 216), fill='#b4a382')
    d.rectangle((x - 8, 43, x + 8, 49), fill='#a18a70')
    d.rectangle((x - 8, 216, x + 9, 223), fill=STONE)
    for yy in (82, 130, 180): d.line((x - 5, yy, x + 5, yy), fill='#4d454e')
for x in (68, 152, 300, 384): stained_window(interior, x, 61, 28, 92)
d = ImageDraw.Draw(interior)
rose(d, 240, 104, 29)
# Carved walnut choir stalls sit against the back wall, leaving the combat floor open.
for x in (100, 322):
    d.rectangle((x - 4, 181, x + 63, 222), fill='#342b32')
    for i in range(4):
        px = x + i * 15
        pointed(d, (px, 178, 13, 36), '#594333')
        pointed(d, (px + 2, 182, 9, 25), '#8e7356')
        d.line((px + 6, 187, px + 6, 204), fill='#655346')
        d.rectangle((px, 211, px + 13, 216), fill=RED)
        d.rectangle((px - 1, 216, px + 14, 222), fill='#655346')
        d.point((px + 6, 183), fill=GOLD)
# Steps, carved altar, embroidered cloth, tabernacle and Marian icon.
for y, x0, x1 in ((231, 191, 290), (227, 200, 281), (223, 208, 273)):
    d.rectangle((x0, y, x1, y + 4), fill=SHADE)
    d.line((x0, y, x1, y), fill=GOLD)
d.rectangle((215, 203, 266, 223), fill='#4d454e')
d.rectangle((217, 206, 264, 221), fill=STONE)
for x in (221, 255):
    d.rectangle((x, 208, x + 5, 219), fill='#655864')
    d.line((x + 1, 209, x + 1, 218), fill='#a18a70')
d.rectangle((210, 200, 271, 205), fill=GOLD)
d.rectangle((211, 201, 270, 204), fill=LIGHT)
d.rectangle((227, 203, 254, 219), fill='#ddd8c1')
d.line((228, 217, 253, 217), fill=GOLD)
for x in range(229, 252, 4): d.point((x, 218), fill='#8e7356')
cross(d, 240, 207, 9)
d.rectangle((231, 187, 250, 199), fill='#8e7356')
d.rectangle((233, 188, 248, 198), fill=GOLD)
pointed(d, (235, 190, 11, 8), RED)
cross(d, 241, 191, 6)
# Our Lady from the game's own sanctuary, mounted in a framed devotional panel.
marian = Image.open(ROOT / 'sprites/sanctuary.png').crop((38, 40, 75, 95))
interior.alpha_composite(marian, (222, 135))
d = ImageDraw.Draw(interior)
d.line((221, 137, 221, 188), fill=GOLD)
d.line((259, 137, 259, 188), fill=GOLD)
# Candlesticks, prayer books and an open lectern.
for x in (217, 264):
    d.rectangle((x - 2, 197, x + 2, 199), fill=GOLD)
    d.line((x, 190, x, 197), fill=GOLD)
    d.rectangle((x - 1, 180, x + 1, 190), fill=LIGHT)
d.rectangle((289, 210, 309, 214), fill='#655346')
d.line((299, 213, 299, 229), fill='#8e7356', width=3)
d.polygon([(287, 208), (298, 205), (311, 208), (310, 212), (298, 210), (288, 212)], fill=LIGHT)
d.line((298, 206, 298, 210), fill='#8e7356')
for x, y in ((291, 208), (302, 208)): d.line((x, y, x + 5, y + 1), fill=GOLD)
for x in (187, 313):
    d.rectangle((x, 218, x + 9, 221), fill=RED)
    d.line((x + 1, 219, x + 8, 219), fill=GOLD)
# Wall fixtures, suspended lamps and a censer with narrow metal chains.
for x in (92, 178, 302, 388):
    d.rectangle((x - 3, 183, x + 3, 189), fill='#594333')
    d.line((x, 188, x, 190), fill=GOLD)
    d.rectangle((x - 2, 188, x + 2, 191), fill=GOLD)
    d.rectangle((x - 1, 182, x + 1, 188), fill=LIGHT)
for x in (140, 340):
    d.line((x, 0, x, 49), fill='#8e7356')
    d.polygon([(x - 7, 50), (x, 44), (x + 7, 50), (x + 5, 63), (x - 5, 63)], fill='#594333')
    d.rectangle((x - 3, 51, x + 3, 61), fill='#d08a51')
    d.line((x, 50, x, 62), fill=GOLD)
    d.line((x - 5, 63, x + 5, 63), fill=GOLD)
# Refracted stained-glass light is dithered on the rear floor, behind fighters.
for i, x in enumerate((68, 152, 300, 384)):
    for y in range(159, 235):
        spread = (y - 159) // 7
        for xx in range(x + 9 - spread, x + 18 + spread):
            if (xx + y) % 5 == 0: d.point((xx, y), fill=['#456d80', '#655864', '#8e7356', '#365c69'][i])
# Inlaid stone pavement and a central processional carpet.
for y in range(238, 270, 8):
    for x in range(-16 if (y // 8) % 2 else 0, 480, 32):
        d.rectangle((x, y, x + 30, min(269, y + 6)), fill=['#655864', '#807167', '#76695e'][(x // 32 + y // 8) % 3])
        d.line((x + 1, y, x + 29, y), fill='#a18a70')
        d.point((x + 9, y + 4), fill='#4d454e')
d.rectangle((217, 238, 263, 269), fill=RED)
d.line((219, 238, 219, 269), fill=GOLD)
d.line((261, 238, 261, 269), fill=GOLD)
for y in range(241, 270, 9):
    d.polygon([(240, y), (244, y + 3), (240, y + 6), (236, y + 3)], outline='#c7ab78')
# Store as RGB so the entire indoor view is opaque.
save(interior.convert('RGB'), 'interior.png')

# Foreground vault and corner pillars provide depth without covering the combat lane.
foreground = Image.new('RGBA', (480, 270))
d = ImageDraw.Draw(foreground)
for x in (0, 467):
    d.rectangle((x, 0, x + 12, 238), fill='#272934')
    d.line((x + 9, 25, x + 9, 235), fill='#544b52', width=2)
    d.rectangle((x, 231, x + 12, 238), fill=SHADE)
for side in (0, 1):
    points = [(0, 0), (240, 0), (168, 11), (93, 26), (31, 57), (0, 101)]
    if side: points = [(480 - x, y) for x, y in points]
    d.polygon(points, fill='#272934')
    ridge = [(0, 98), (31, 55), (93, 25), (168, 10), (240, 0)]
    if side: ridge = [(480 - x, y) for x, y in ridge]
    d.line(ridge, fill='#655864', width=3)
save(foreground, 'foreground.png')

# Two sliding oak panels, with iron scrollwork and hand-forged studs.
door = Image.new('RGBA', (32, 128), '#342b32')
d = ImageDraw.Draw(door)
for x in range(1, 32, 4):
    d.rectangle((x, 0, x + 2, 127), fill=['#594333', '#655346', '#8e7356'][(x // 4) % 3])
    for y in range(4, 127, 17): d.line((x + 1, y, x + 1, y + 6), fill='#4d454e')
for y in (20, 62, 105):
    d.rectangle((0, y, 31, y + 3), fill='#46525c')
    d.line((1, y, 30, y), fill='#a29b88')
    for x in (3, 10, 22, 29): d.point((x, y + 1), fill=GOLD)
    d.arc((2, y - 7, 13, y + 10), 180, 350, fill='#a29b88')
    d.arc((18, y - 7, 29, y + 10), 190, 360, fill='#a29b88')
d.line((15, 0, 15, 127), fill='#202735', width=2)
for x in (12, 20):
    d.ellipse((x - 2, 71, x + 2, 76), outline=GOLD)
    d.point((x, 70), fill='#e5d6a7')
save(door, 'door.png')
frame = Image.new('RGBA', (48, 144))
d = ImageDraw.Draw(frame)
pointed(d, (0, 0, 47, 143), '#35333f')
pointed(d, (2, 2, 43, 141), STONE)
pointed(d, (5, 5, 37, 138), GOLD)
pointed(d, (7, 8, 33, 135), '#655864')
pointed(d, (8, 11, 31, 133), '#171f2a')
for y in range(33, 143, 12):
    d.line((2, y, 6, y), fill='#35333f')
    d.line((41, y, 45, y), fill='#35333f')
    d.point((3, y + 2), fill='#b4a382')
cross(d, 23, 7, 14)
save(frame, 'door-frame.png')
print('Generated detailed chapel exterior modules, opaque interior, foreground vault and oak doors.')
