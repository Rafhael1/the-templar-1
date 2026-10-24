"""Generate original road scenery and an architectural prop atlas, without changing other assets."""
from pathlib import Path
from PIL import Image, ImageDraw
import math
import random

ROOT = Path(__file__).resolve().parents[1] / 'assets'
SCENERY = ROOT / 'levels/exiles-road/scenery'
DARK = '#292a35'
STONE = '#655b62'
EDGE = '#94816c'
LIGHT = '#b29a79'


def masonry(d, box, fill, seed):
    x, y, right, bottom = box
    d.rectangle(box, fill=fill)
    rng = random.Random(seed)
    for row in range(y + 2, bottom, 8):
        d.line((x, row, right, row), fill='#403d49')
        for col in range(x + (8 if row % 16 else 0), right, 16):
            d.line((col, row, col, min(row + 7, bottom)), fill='#403d49')
            if rng.random() < .5:
                d.line((col + 2, row + 2, min(col + 10, right), row + 2), fill='#746773')


def arch(d, x, y, w, h, fill):
    d.polygon([(x,y+h),(x,y+w//2),(x+w//4,y+w//5),(x+w//2,y),
               (x+3*w//4,y+w//5),(x+w,y+w//2),(x+w,y+h)], fill=fill)


def candle(d, x, y):
    d.rectangle((x,y-8,x+2,y), fill='#c5b79a')
    d.line((x+1,y-12,x+1,y-9), fill='#f1c88c')
    d.point((x+1,y-13), fill='#b57545')


# Props share one atlas and one pixel density. Transparent openings show the rear scenery.
atlas = Image.new('RGBA', (96*6,128))
for kind in range(6):
    im = Image.new('RGBA',(96,128)); d = ImageDraw.Draw(im)
    if kind == 0:  # Broken processional arch, 192 x 256 world pixels.
        for x, h in ((8,108),(72,96)):
            masonry(d,(x,128-h,x+15,127),STONE,kind+x)
            d.rectangle((x-4,121,x+19,127),fill=EDGE)
            d.rectangle((x-3,128-h,x+18,133-h),fill=LIGHT)
            d.line((x+3,135-h,x+3,118),fill=EDGE,width=2)
        for x,y in ((21,20),(29,12),(37,7),(45,4),(53,8),(61,14)):
            d.polygon([(x,y),(x+7,y-2),(x+12,y+9),(x+3,y+12)],fill=STONE)
            d.line((x,y,x+7,y-2),fill=LIGHT)
        d.rectangle((44,0,49,14),fill=EDGE)
        d.rectangle((40,4,53,7),fill=LIGHT)
        for x,y in ((75,48),(78,53),(74,60),(79,68),(78,76),(82,80)):
            d.rectangle((x,y,x+4,y+3),fill='#4b5c51')
        d.line((14,65,18,70,14,76,16,82),fill=DARK)
    elif kind == 1:  # Votive shrine with a carved, hooded figure.
        masonry(d,(27,55,68,127),STONE,1)
        arch(d,23,40,50,78,EDGE); arch(d,28,47,40,69,DARK)
        d.ellipse((43,58,53,69),fill='#b49a72')
        d.ellipse((45,60,51,66),fill='#574b4c')
        d.polygon([(43,68),(53,68),(61,105),(35,105)],fill='#7c8580')
        d.line((43,72,40,99),fill='#b1b1a0',width=2)
        d.line((51,72,55,100),fill='#4d575b',width=3)
        d.rectangle((44,78,51,81),fill=LIGHT)
        d.rectangle((21,108,74,113),fill=LIGHT)
        d.rectangle((19,121,76,127),fill=EDGE)
        d.rectangle((46,27,49,43),fill=LIGHT); d.rectangle((41,32,54,35),fill=LIGHT)
        for x in (26,32,62,68): candle(d,x,107)
        for x in (24,60):
            d.rectangle((x,117,x+10,119),fill='#3c5148')
            for xx in range(x,x+10,4): d.rectangle((xx,114,xx+2,116),fill='#8d5360')
    elif kind == 2:  # Wayside graves and a small ossuary.
        masonry(d,(22,95,46,126),STONE,2)
        d.rectangle((28,76,33,110),fill=EDGE);d.rectangle((19,84,42,89),fill=EDGE)
        d.line((29,77,29,106),fill=LIGHT)
        arch(d,54,103,20,23,STONE)
        d.rectangle((59,111,69,121),fill=DARK)
        d.rectangle((21,124,79,127),fill='#4a4a46')
        for x in (58,64):
            d.rectangle((x,115,x+4,119),fill='#b3a58a')
            d.point((x+1,116),fill=DARK);d.point((x+3,116),fill=DARK)
        candle(d,47,126)
    elif kind == 3:  # Gnarled tree with narrow, pixel-stepped branches.
        for points,w in [([(44,127),(49,96),(43,72),(47,50),(43,20)],8),
                         ([(46,82),(27,68),(19,48),(8,43)],5),
                         ([(47,65),(66,51),(72,31),(85,23)],5),
                         ([(44,46),(30,34),(28,17)],3),
                         ([(23,57),(32,48),(33,36)],3),
                         ([(65,51),(79,53),(91,44)],3)]:
            d.line(points,fill='#414047',width=w)
            d.line([(x-1,y) for x,y in points],fill='#74665e',width=max(1,w//3))
        for x in (31,41,51,61):d.line((47,114,x,127),fill='#645750',width=3)
        d.line((68,48,68,75),fill='#77716a')
        d.rectangle((64,73,72,82),fill='#544b52');d.rectangle((66,75,70,79),fill='#b2905e')
    elif kind == 4:  # Abandoned pilgrims' cart, cracked wheels and torn canopy.
        for x in (23,69):
            d.ellipse((x-12,101,x+12,125),fill='#302d36',outline=EDGE,width=2)
            d.line((x-8,105,x+8,121),fill='#8c7058',width=2)
            d.line((x+8,105,x-8,121),fill='#8c7058',width=2)
            d.ellipse((x-3,110,x+3,116),fill='#a38866')
        masonry(d,(10,89,81,106),'#635047',4)
        d.line((15,90,17,61,32,49,61,49,77,62,79,90),fill='#8e7356',width=2)
        d.polygon([(17,61),(32,49),(61,49),(77,62),(70,82),(59,75),(56,85),(43,78),(30,83),(22,78)],fill='#76535b')
        for x in range(28,69,12):d.line((x,55,x-3,77),fill='#9b7773')
        d.line((78,99,94,110),fill='#94785a',width=3)
        d.rectangle((33,88,52,98),fill='#a18a70');d.rectangle((38,84,46,88),fill='#c0ac84')
    else:  # Chains, weathered pennant, and wall brackets.
        d.rectangle((36,0,39,14),fill=EDGE)
        d.line((37,12,63,21),fill='#938773',width=2)
        for y in range(22,64,5):d.rectangle((62,y,64,y+3),outline='#a0947c')
        d.polygon([(33,20),(56,20),(56,94),(46,106),(44,96),(33,101)],fill='#613743')
        d.line((35,22,35,97),fill='#a08463')
        d.rectangle((44,36,46,73),fill='#b39a73');d.rectangle((38,46,52,49),fill='#b39a73')
        d.rectangle((58,64,69,71),fill=EDGE)
        d.line((60,72,67,72),fill='#e2ba7c')
    atlas.alpha_composite(im,(kind*96,0))
atlas.resize((1152,256),Image.Resampling.NEAREST).save(ROOT/'sprites/environment.png')

# Distant structures are deliberately darker than the playable stone and actors.
for kind in range(4):
    im=Image.new('RGBA',(480,270));d=ImageDraw.Draw(im)
    rng=random.Random(1095+kind)
    for x in range(-30,510,55):
        y=151+rng.randrange(40)
        d.polygon([(x-20,243),(x+35,y),(x+95,243)],fill='#30313f')
    if kind == 0:
        # Monastery silhouette behind the roadside bivouac.
        masonry(d,(138,122,319,234),'#49434e',11)
        for x in (140,293):
            masonry(d,(x,72,x+24,234),'#514854',x)
            d.polygon([(x-4,72),(x+12,49),(x+29,72)],fill='#363440')
            arch(d,x+8,91,9,23,'#242833')
            d.line((x+12,42,x+12,55),fill='#8c7b69');d.line((x+8,46,x+16,46),fill='#8c7b69')
        for x in range(173,290,29): arch(d,x,148,17,51,'#262a35')
        for x in (4,69,342,414):
            d.polygon([(x-12,238),(x+29,204),(x+67,238)],fill='#71605b')
            d.polygon([(x+29,204),(x+37,238),(x+67,238)],fill='#4d444e')
            d.rectangle((x+22,222,x+31,238),fill=DARK)
            for xx in range(x, x+60,16):d.line((x+29,205,xx,237),fill='#87725f')
    elif kind == 1:
        # Broken aqueduct and cemetery beneath a high bell tower.
        for x in range(-32,510,64):
            masonry(d,(x,150,x+63,238),'#514952',x)
            arch(d,x+9,161,46,79,'#2e303d')
            d.rectangle((x,147,x+63,153),fill='#807066')
        masonry(d,(310,58,344,153),'#56505a',12)
        arch(d,319,70,16,35,'#232733')
        d.ellipse((323,87,331,98),fill='#87745e')
        d.polygon([(303,58),(327,29),(350,58)],fill='#3f3b49')
        for x in range(10,480,39):
            y=226+rng.randrange(9);d.rectangle((x,y-12,x+3,y+11),fill='#736563');d.rectangle((x-4,y-8,x+7,y-5),fill='#736563')
    elif kind == 2:
        # Roofless cloister: pointed arcades, tracery, buttresses and rose window.
        masonry(d,(22,111,459,239),'#524953',17)
        for x in range(38,458,68):
            arch(d,x,130,48,110,'#282c38')
            arch(d,x+5,136,38,104,'#32333f')
            d.rectangle((x-9,121,x-3,237),fill='#8b756b')
            d.rectangle((x-13,235,x+2,240),fill='#9c8470')
        masonry(d,(190,48,285,128),'#584e59',22)
        d.polygon([(185,49),(236,17),(291,49)],fill='#665763')
        d.ellipse((215,60,260,105),fill='#242a36',outline='#8c7769',width=3)
        for angle in range(0,360,45):
            d.line((238,83,238+int(20*math.cos(math.radians(angle))),83+int(20*math.sin(math.radians(angle)))),fill='#76666a',width=2)
        d.ellipse((232,77,244,89),fill='#a88d68')
        for x in (28,166,352,451):
            d.rectangle((x,104,x+9,135),fill='#665763');d.line((x+4,95,x+4,108),fill='#8f7b69')
    else:
        # The approach to a fortified sanctuary, with spires at varied heights.
        masonry(d,(0,177,479,239),'#514b55',31)
        for x in range(0,480,24):d.rectangle((x,169,x+12,179),fill='#6d5e65')
        for x,h in ((24,108),(131,146),(312,124),(421,163)):
            masonry(d,(x,240-h,x+35,239),'#63545c',x)
            d.polygon([(x-4,240-h),(x+17,210-h),(x+39,240-h)],fill='#393845')
            arch(d,x+11,253-h,12,34,'#252a36')
            d.rectangle((x-3,233,x+38,239),fill='#887367')
        for x in (216,258):
            d.line((x,126,x,237),fill='#8a7966',width=2)
            d.polygon([(x+2,130),(x+22,130),(x+22,177),(x+13,187),(x+2,181)],fill='#553847')
    # Stone chips and worn lower courses keep silhouettes from reading as flat blocks.
    for _ in range(320):
        x=rng.randrange(480);y=rng.randrange(200,240)
        if im.getpixel((x,y))[3]:d.point((x,y),fill='#877568')
    im.resize((960,540),Image.Resampling.NEAREST).save(SCENERY/f'region-{kind}.png')
print('Generated six environment props and four detailed road backgrounds.')
