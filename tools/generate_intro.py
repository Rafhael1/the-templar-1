"""Original layered pixel-art intro. Regenerates only assets/sprites/intro/*.png."""
from pathlib import Path
from PIL import Image, ImageDraw
import math

ROOT = Path(__file__).resolve().parents[1] / 'assets'
DEST = ROOT / 'sprites/intro'
DEST.mkdir(parents=True, exist_ok=True)

# Preserve the original chamber's architecture, separating everything that moves.
im=Image.new('RGBA',(480,200),'#202735');d=ImageDraw.Draw(im)
for y in range(12,169,14):
    for x in range(-24 if y%28 else 0,480,48):
        d.rectangle((x,y,x+46,y+12),fill='#35333f')
        d.line((x,y+12,x+46,y+12),fill='#282c37')
for x in (60,148,310,398):
    d.polygon([(x,93),(x,38),(x+12,23),(x+24,38),(x+24,93)],fill='#807167')
    d.polygon([(x+4,90),(x+4,40),(x+12,30),(x+20,40),(x+20,90)],fill='#263e4b')
    d.line((x+12,33,x+12,90),fill='#655346',width=2)
    d.line((x+4,60,x+20,60),fill='#655346',width=2)
for x in (24,120,336,432):
    d.rectangle((x,15,x+17,172),fill='#544b52')
    d.rectangle((x+3,24,x+5,168),fill='#807167')
    d.rectangle((x-5,13,x+22,23),fill='#8e7b68')
    d.rectangle((x-4,164,x+21,172),fill='#8e7b68')
d.rectangle((0,171,479,199),fill='#544b52')
for y in (177,189):d.line((0,y,479,y),fill='#393641')
for x in range(0,480,48):d.line((x,178,x,187),fill='#393641')
for x,y,w in ((196,165,90),(205,156,72),(214,148,54)):
    d.rectangle((x,y,x+w,y+8),fill='#807167');d.line((x,y,x+w,y),fill='#c7ab78')
im.save(DEST / 'chamber.png')


def pope_frame(frame):
    pope=Image.new('RGBA',(480,200));d=ImageDraw.Draw(pope)
    # Robe, stole, sleeves: a calm asymmetrical stance, rather than raised stick arms.
    d.polygon([(227,94),(251,94),(257,147),(220,147)],fill='#4d5865')
    d.polygon([(228,95),(250,95),(254,145),(223,145)],fill='#ddd8c1')
    d.polygon([(228,99),(231,135),(225,145),(223,145)],fill='#9faaa4')
    d.polygon([(247,99),(252,144),(245,144),(242,111)],fill='#bbbca9')
    d.rectangle((230,96,233,140),fill='#973f4e');d.rectangle((245,96,248,140),fill='#973f4e')
    d.rectangle((232,137,246,143),fill='#c7ab78')
    dx, dy = ((0, 4), (-1, 2), (-2, 0), (-3, -2), (-2, 0), (-1, 2))[frame]
    d.polygon([(228,97),(214+dx,90+dy),(210+dx,82+dy),(206+dx,83+dy),(209+dx,96+dy),(225,109)],fill='#9faaa4')
    d.polygon([(226,98),(214+dx,93+dy),(209+dx,85+dy),(208+dx,86+dy),(212+dx,97+dy),(226,104)],fill='#ddd8c1')
    d.rectangle((206+dx,78+dy,210+dx,85+dy),fill='#be9270')
    d.line((207+dx,76+dy,207+dx,81+dy),fill='#e0b08a')
    d.line((210+dx,77+dy,210+dx,82+dy),fill='#e0b08a')
    d.polygon([(249,98),(257,105),(260,113),(255,115),(246,107)],fill='#ddd8c1')
    d.rectangle((257,109,263,113),fill='#be9270')
    # Face and modest mitre share the characters' armor outline and skin palette.
    d.rectangle((231,77,247,94),fill='#78503c');d.rectangle((233,78,245,91),fill='#be9270')
    d.rectangle((233,79,236,80),fill='#ddd8c1');d.rectangle((242,79,245,80),fill='#ddd8c1')
    d.point((235,82),fill='#292d32');d.point((243,82),fill='#292d32')
    d.line((239,82,239,86),fill='#e0b08a');d.line((236,89,242,89),fill='#78503c')
    d.polygon([(232,88),(235,92),(239,95),(244,92),(247,88),(246,94),(239,98),(232,93)],fill='#bbbca9')
    d.polygon([(230,78),(231,63),(238,53),(248,63),(249,78)],fill='#4d5865')
    d.polygon([(232,76),(233,64),(238,57),(246,64),(247,76)],fill='#ddd8c1')
    d.rectangle((232,73,247,76),fill='#c7ab78');d.rectangle((238,62,240,71),fill='#c7ab78');d.rectangle((235,65,243,67),fill='#c7ab78')
    d.rectangle((261,85,263,146),fill='#c7ab78');d.arc((260,73,272,89),180,470,fill='#c7ab78',width=2)
    d.rectangle((263,82,267,84),fill='#c7ab78')
    if frame in (2, 4):
        d.rectangle((237, 88, 241, 90), fill='#78503c')
        d.line((238, 88, 240, 88), fill='#ddd8c1')
    if frame == 5:
        d.line((234, 82, 236, 82), fill='#78503c')
        d.line((242, 82, 244, 82), fill='#78503c')
    return pope.crop((200, 48, 280, 152)).resize((68, 88), Image.Resampling.NEAREST)


pope_atlas = Image.new('RGBA', (68 * 6, 88))
for frame in range(6):
    pope_atlas.alpha_composite(pope_frame(frame), (68 * frame, 0))
pope_atlas.save(DEST / 'pope.png')

# The cloth bends in horizontal pixel strips, carrying its embroidered cross with it.
banner_atlas = Image.new('RGBA', (32 * 8, 80))
for frame in range(8):
    banner = Image.new('RGBA', (32, 80))
    d = ImageDraw.Draw(banner)
    for y in range(2, 78):
        shift = round(math.sin(frame * math.pi / 4 - y / 19) * (y / 35))
        taper = max(0, y - 66)
        left, right = 4 + shift + taper, 26 + shift - taper
        if left <= right:
            d.line((left, y, right, y), fill='#743844')
            d.point((left, y), fill='#973f4e')
            d.point((right, y), fill='#54444b')
            if 14 <= y <= 54:
                d.line((14 + shift, y, 16 + shift, y), fill='#c7ab78')
            if 28 <= y <= 30:
                d.line((8 + shift, y, 23 + shift, y), fill='#c7ab78')
    d.rectangle((3, 0, 27, 2), fill='#c7ab78')
    banner_atlas.alpha_composite(banner, (frame * 32, 0))
banner_atlas.save(DEST / 'banner.png')

characters = Image.open(ROOT / 'sprites/characters.png')
standing = characters.crop((0, 0, 64, 80))
kneeling = Image.new('RGBA', (64 * 4, 80))
for frame in range(4):
    pose = Image.new('RGBA', (64, 80))
    if frame == 0:
        pose = standing.copy()
    else:
        drop = (0, 3, 6, 9)[frame]
        pose.alpha_composite(standing.crop((0, 0, 64, 54)), (0, drop))
        d = ImageDraw.Draw(pose)
        d.polygon([(18, 51 + drop), (29, 51 + drop), (29, 64), (38, 67), (38, 71), (19, 71)], fill='#4d5865')
        d.rectangle((20, 57 + drop // 2, 27, 65), fill='#a4b1b8')
        d.polygon([(33, 51 + drop), (42, 51 + drop), (45, 65), (47, 70), (37, 70), (31, 63)], fill='#4d5865')
        d.rectangle((37, 63, 44, 67), fill='#a4b1b8')
        d.rectangle((20, 68, 36, 72), fill='#282c37')
        d.rectangle((37, 68, 48, 72), fill='#282c37')
    kneeling.alpha_composite(pose, (frame * 64, 0))
kneeling.save(DEST / 'kneeling.png')

marchers = Image.new('RGBA', (32 * 8, 40))
for frame in range(8):
    pose = characters.crop((frame * 64, 80, (frame + 1) * 64, 160)).resize((32, 40), Image.Resampling.NEAREST)
    marchers.alpha_composite(pose, (frame * 32, 0))
marchers.save(DEST / 'marchers.png')

sky = Image.open(ROOT / 'sprites/sky.png').convert('RGBA').resize((480, 270), Image.Resampling.NEAREST)
sky.crop((0, 50, 480, 250)).save(DEST / 'march-sky.png')
wall = Image.open(ROOT / 'levels/exiles-road/scenery/region-3.png').resize((480, 270), Image.Resampling.NEAREST)
wall.crop((0, 50, 480, 250)).save(DEST / 'march-walls.png')
print('Generated layered chamber, blessing/kneeling/walking atlases and waving standards.')
