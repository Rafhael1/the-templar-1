"""Original pixel scenery and opening illustrations."""
from pathlib import Path
from PIL import Image, ImageDraw
ROOT = Path(__file__).resolve().parents[1] / 'assets'
SCENERY = ROOT / 'levels/exiles-road/scenery'
SCENERY.mkdir(parents=True, exist_ok=True)
# One source of truth for road backgrounds and the architectural prop atlas.
import runpy
runpy.run_path(str(Path(__file__).with_name('generate_environment.py')))
# Council chamber: proportioned figure, one blessing hand and a pastoral staff.
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
for x in (177,280):
    d.polygon([(x,31),(x+22,31),(x+22,100),(x+11,110),(x,100)],fill='#743844')
    d.line((x,32,x+22,32),fill='#c7ab78',width=2)
    d.rectangle((x+10,43,x+13,85),fill='#c7ab78')
    d.rectangle((x+3,57,x+19,60),fill='#c7ab78')
d.rectangle((0,171,479,199),fill='#544b52')
for y in (177,189):d.line((0,y,479,y),fill='#393641')
for x in range(0,480,48):d.line((x,178,x,187),fill='#393641')
for x,y,w in ((196,165,90),(205,156,72),(214,148,54)):
    d.rectangle((x,y,x+w,y+8),fill='#807167');d.line((x,y,x+w,y),fill='#c7ab78')
pope=Image.new('RGBA',(480,200));d=ImageDraw.Draw(pope)
# Robe, stole, sleeves: a calm asymmetrical stance, rather than raised stick arms.
d.polygon([(227,94),(251,94),(257,147),(220,147)],fill='#4d5865')
d.polygon([(228,95),(250,95),(254,145),(223,145)],fill='#ddd8c1')
d.polygon([(228,99),(231,135),(225,145),(223,145)],fill='#9faaa4')
d.polygon([(247,99),(252,144),(245,144),(242,111)],fill='#bbbca9')
d.rectangle((230,96,233,140),fill='#973f4e');d.rectangle((245,96,248,140),fill='#973f4e')
d.rectangle((232,137,246,143),fill='#c7ab78')
d.polygon([(228,97),(214,90),(210,82),(206,83),(209,96),(225,109)],fill='#9faaa4')
d.polygon([(226,98),(214,93),(209,85),(208,86),(212,97),(226,104)],fill='#ddd8c1')
d.rectangle((206,78,210,85),fill='#be9270');d.line((207,76,207,81),fill='#e0b08a');d.line((210,77,210,82),fill='#e0b08a')
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
figure=pope.crop(pope.getbbox())
figure=figure.resize((round(figure.width*.85),round(figure.height*.85)),Image.Resampling.NEAREST)
im.alpha_composite(figure,(239-figure.width//2,147-figure.height))
# Knights are derived from the existing character atlas, with consistent proportions.
knight=Image.open(ROOT/'sprites/characters.png').crop((0,0,64,80))
for x,y in ((53,101),(98,103),(145,105),(273,105),(320,103),(366,101)):
    im.alpha_composite(knight,(x,y))
d=ImageDraw.Draw(im)
for x in (190,285):
    d.rectangle((x,131,x+4,158),fill='#655346');d.rectangle((x-4,156,x+8,159),fill='#c7ab78')
    d.rectangle((x,122,x+3,131),fill='#ddd8c1');d.polygon([(x+1,115),(x-1,121),(x+3,121)],fill='#f6d59b')
im.resize((960,400),Image.Resampling.NEAREST).save(ROOT/'sprites/council.png')
march=Image.open(ROOT/'sprites/sky.png').convert('RGBA').resize((480,270),Image.Resampling.NEAREST)
march.alpha_composite(Image.open(SCENERY/'region-3.png').resize((480,270),Image.Resampling.NEAREST))
knight=Image.open(ROOT/'sprites/characters.png').crop((0,0,64,80)).resize((32,40),Image.Resampling.NEAREST)
for x in range(38,400,48):march.alpha_composite(knight,(x,204))
march.crop((0,50,480,250)).resize((960,400),Image.Resampling.NEAREST).save(ROOT/'sprites/march.png')
print('Generated four regions and opening illustrations.')
