"""Original deterministic placeholder art and audio. Run with tools/requirements.txt installed.
Runtime consumes committed PNG/TMX/WAV files; Python is not a game dependency.
"""
from pathlib import Path
from PIL import Image, ImageDraw
import math
import random
import struct
import wave
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / 'assets'
for name in ('sprites', 'maps', 'audio', 'ui'):
    (ASSETS / name).mkdir(parents=True, exist_ok=True)
rng = random.Random(1099)

def knight(frame, state, kind=0):
    im = Image.new('RGBA', (64, 80)); d = ImageDraw.Draw(im)
    bob = int(math.sin(frame * math.pi / 2)) if state in ('idle', 'run') else 0
    ox, oy = 14, 12 + bob
    if kind == 2: oy -= 7
    plate = '#a4b1b8' if kind == 0 else ('#7e777d' if kind == 1 else '#77728d')
    shadow = '#4d5865' if kind == 0 else '#41404e'
    cloth = '#ddd8c1' if kind == 0 else '#655e5f'
    red = '#973f4e' if kind == 0 else '#754951'
    def rect(box, color):
        x1,y1,x2,y2 = box; d.rectangle((x1+ox,y1+oy,x2+ox,y2+oy), fill=color)
    step = round(math.sin(frame*math.pi/4)*6) if state == 'run' else 0
    if state == 'air': step = -4
    if state == 'dodge': oy += 9
    # Cape, chainmail, articulated boots, tabard, steel great helm.
    d.polygon([(ox+8,oy+17),(ox+25,oy+17),(ox+28,oy+48),(ox-8-frame%3,oy+43)],fill=red)
    rect((4,40,12,55+step),shadow); rect((20,40,28,55-step),shadow)
    rect((2,52+step,13,58+step),'#282c37'); rect((20,52-step,32,58-step),'#282c37')
    rect((5,44,12,48),plate); rect((20,44,27,48),plate)
    rect((3,16,28,41),shadow)
    for y in range(18,39,3):
        for x in range(4,29,4): rect((x,y,x+1,y+1),'#909ca1')
    rect((7,18,25,43),cloth); rect((7,19,10,43),'#9faaa4')
    rect((11,40,25,43),'#bbbca9'); rect((6,33,27,36),'#655346');rect((17,33,21,36),'#c7ab78')
    rect((16,19,20,32),red); rect((12,23,24,27),red)
    rect((6,2,25,17),shadow);rect((8,0,25,13),plate);rect((8,0,25,2),'#d0d7cc')
    rect((8,8,25,10),'#242936');rect((17,3,19,17),'#c4c9bc')
    for x in (11,14,22): rect((x,13,x,14),'#343c47')
    rect((0,17,7,24),plate);rect((26,17,33,24),plate)
    rect((1,24,6,31),shadow);rect((28,24,32,31),shadow)
    if kind == 2:
        rect((5,-4,27,-1),'#c4a16c')
        for x in (5,14,24): rect((x,-9,x+3,-3),'#c4a16c')
    # Weapons and player shield are drawn by WorldRenderer to follow combat direction.
    if state == 'hurt':
        tint = Image.new('RGBA', im.size, (200,60,65,90)); tint.putalpha(im.getchannel('A').point(lambda a: int(a*.35)))
        im = Image.alpha_composite(im,tint)
    return im

def muslim_soldier(frame, commander=False, archer=False):
    """Fictional medieval Muslim soldier with wrapped helmet, tunic and round shield."""
    im = Image.new('RGBA', (64, 80)); d = ImageDraw.Draw(im)
    ox, oy = 14, 12 + int(math.sin(frame * math.pi / 2))
    step = round(math.sin(frame * math.pi / 4) * 6)
    cloth = '#365c69' if commander else '#647650'
    shade = '#263e4b' if commander else '#3c4d38'
    trim = '#d2ae68' if commander else '#c6b590'
    steel, mail = '#9baeb5', '#46525c'
    def rect(box, color):
        x1,y1,x2,y2 = box; d.rectangle((x1+ox,y1+oy,x2+ox,y2+oy), fill=color)
    if commander:
        d.polygon([(ox+5,oy+15),(ox+27,oy+15),(ox+30,oy+48),
                   (ox-7-frame%3,oy+44)], fill=shade)
    # Loose trousers and boots retain the existing walking cadence.
    rect((4,39,13,54+step), '#aaa38e'); rect((20,39,28,54-step), '#aaa38e')
    rect((2,52+step,14,58+step), '#403530'); rect((20,52-step,32,58-step), '#403530')
    rect((3,15,29,37), mail)
    for y in range(17,36,3):
        for x in range(4,29,4): rect((x,y,x+1,y+1), steel)
    # Split tunic, sash and contrasting hem.
    rect((8,17,25,36), cloth)
    d.polygon([(ox+8,oy+34),(ox+25,oy+34),(ox+29,oy+47),
               (ox+18,oy+47),(ox+16,oy+40),(ox+14,oy+47),(ox+4,oy+47)], fill=cloth)
    rect((8,18,10,32), trim); rect((23,18,25,32), shade)
    rect((5,44,13,47), trim); rect((19,44,28,47), trim)
    rect((6,32,27,35), '#78503c'); rect((18,32,21,35), trim)
    # Open face, mail coif and cloth-wrapped conical helmet.
    rect((7,4,25,19), mail); rect((13,6,25,15), '#be9270')
    rect((21,9,24,10), '#292d32'); rect((15,14,24,17), '#493d35')
    d.polygon([(ox+7,oy+5),(ox+11,oy-3),(ox+17,oy-7),(ox+23,oy-3),
               (ox+27,oy+5)], fill=steel)
    rect((16,-5,18,3), '#d3d7cf')
    rect((5,2,27,5), trim); rect((7,6,26,7), '#e0d4b6')
    rect((5,7,9,21), trim); rect((5,17,7,24), '#e0d4b6')
    rect((27,18,32,26), steel); rect((28,27,32,32), '#be9270')
    if archer:
        # Cloth hood and leather quiver distinguish the ranged silhouette.
        d.polygon([(ox+6,oy+7),(ox+8,oy-3),(ox+16,oy-6),(ox+25,oy-1),
                   (ox+27,oy+10),(ox+23,oy+18),(ox+8,oy+18)], fill=shade)
        rect((14,5,24,13), '#be9270'); rect((21,8,24,9), '#292d32')
        rect((-2,19,2,36), '#594333')
        for x in (-2,0,2): rect((x,12,x,22), trim)
    elif commander:
        # Plated commander helmet, broad brass shoulders and a dark red mantle.
        rect((6,-1,26,13), mail); rect((8,0,25,11), steel)
        rect((8,0,25,2), trim); rect((8,7,25,9), '#292d32')
        rect((16,2,18,15), trim)
        rect((-1,17,8,23), trim); rect((26,17,34,23), trim)
        rect((9,18,12,30), trim); rect((22,18,25,30), trim)
        rect((12,34,22,37), '#973f4e')
    else:
        # Round brass-bound shield shares the tunic colors and armor highlights.
        d.ellipse((ox-5,oy+22,ox+14,oy+43), fill=mail)
        d.ellipse((ox-4,oy+23,ox+13,oy+42), fill=trim)
        d.ellipse((ox-2,oy+25,ox+11,oy+40), fill=shade)
        d.ellipse((ox-1,oy+25,ox+10,oy+38), fill=cloth)
        d.arc((ox-4,oy+23,ox+13,oy+42), 180, 285, fill='#e0d4b6')
        d.arc((ox-3,oy+24,ox+12,oy+41), 10, 100, fill='#78503c')
        for x,y in ((4,24),(11,31),(4,40),(-3,31)):
            rect((x,y,x,y), '#e0d4b6')
        d.ellipse((ox+1,oy+28,ox+8,oy+37), fill=mail)
        d.ellipse((ox+2,oy+29,ox+7,oy+36), fill=steel)
        rect((3,30,5,31), '#d3d7cf')
        rect((5,34,6,35), mail)
    return im

sheet=Image.new('RGBA',(64*8,80*9))
for row,state in enumerate(('idle','run','air','attack','dodge','hurt','sentinel','guardian','archer')):
    for frame in range(8):
        sprite = muslim_soldier(frame, commander=row==7, archer=row==8) if row>=6 else knight(frame,state)
        sheet.paste(sprite,(frame*64,row*80))
sheet.save(ASSETS/'sprites/characters.png')

# Sword sprites use the characters' one-pixel grid and existing armor/trim palette.
# Compact straight templar blade and curved enemy scimitar; guardian scales with its body.
weapons = Image.new('RGBA', (40 * 2, 16))
for kind in range(2):
    im = Image.new('RGBA', (40, 16)); d = ImageDraw.Draw(im)
    outline = '#4d5865' if kind == 0 else '#46525c'
    steel = '#a4b1b8' if kind == 0 else '#9baeb5'
    highlight = '#d0d7cc' if kind == 0 else '#d3d7cf'
    gold = '#c7ab78' if kind == 0 else '#c6b590'
    d.rectangle((0,6,2,9), fill=outline)
    d.rectangle((1,7,2,8), fill=gold)
    d.rectangle((3,7,7,8), fill='#655346' if kind == 0 else '#594333')
    if kind == 0:
        d.rectangle((7,2,9,13), fill=outline)
        d.rectangle((8,3,8,12), fill=gold)
        d.rectangle((10,6,29,9), fill=outline)
        d.rectangle((10,6,28,7), fill=steel)
        d.line((11,6,27,6), fill=highlight)
        d.rectangle((29,7,31,8), fill=steel)
        d.point((32,7), fill=highlight)
    else:
        # Swept brass quillons and a broad, upswept single-edged blade.
        d.line([(6,3),(8,5),(8,10),(10,12)], fill=outline, width=3)
        d.line([(6,3),(8,5),(8,10),(10,12)], fill=gold)
        d.polygon([(10,6),(19,6),(24,5),(28,3),(32,0),(34,0),
                   (32,5),(29,9),(25,11),(20,12),(14,11),(10,9)], fill=outline)
        d.polygon([(10,7),(19,7),(24,6),(29,4),(32,2),(31,5),
                   (28,8),(24,10),(20,11),(14,10),(10,8)], fill=steel)
        d.line([(12,9),(20,10),(24,9),(28,7),(31,4),(32,2)], fill=highlight)
        d.point((5,7), fill=gold)
    weapons.paste(im, (kind * 40, 0))
weapons.save(ASSETS/'sprites/swords.png')

# Kite shield: tabard ivory/red, helmet steel and belt brass; second frame is impact.
shields = Image.new('RGBA', (24 * 2, 36))
for flash in range(2):
    im = Image.new('RGBA', (24, 36)); d = ImageDraw.Draw(im)
    outline = '#4d5865'
    rim = '#d0d7cc' if flash else '#a4b1b8'
    d.polygon([(3,3),(20,3),(22,5),(22,20),(20,25),(17,29),
               (12,33),(7,29),(4,25),(2,20),(2,5)], fill=outline)
    d.polygon([(4,4),(19,4),(21,6),(21,20),(19,24),(16,28),
               (12,31),(8,28),(5,24),(3,20),(3,6)], fill=rim)
    d.polygon([(5,6),(18,6),(19,7),(19,20),(17,24),(12,28),
               (7,24),(5,20)], fill='#ddd8c1')
    d.polygon([(5,7),(7,7),(7,20),(9,24),(12,28),(7,24),(5,20)], fill='#9faaa4')
    d.line([(18,7),(18,20),(16,23),(12,27)], fill='#bbbca9')
    d.line([(4,6),(4,19),(6,24)], fill='#d0d7cc')
    d.rectangle((11,9,13,23), fill='#973f4e')
    d.rectangle((8,13,16,16), fill='#973f4e')
    d.line((13,10,13,12), fill='#754951')
    for x,y in ((5,5),(18,5),(4,18),(20,18),(12,30)):
        d.point((x,y), fill='#c7ab78')
    shields.paste(im, (flash * 24, 0))
shields.save(ASSETS/'sprites/shields.png')

# Eucharistic host: round ivory wafer with a small embossed central cross.
bread = Image.new('RGBA', (32,32)); d = ImageDraw.Draw(bread)
d.ellipse((2,2,29,29), fill='#bbbca9')
d.ellipse((3,3,28,28), fill='#ddd8c1')
d.ellipse((4,4,27,27), fill='#e5dbc0')
d.arc((4,4,27,27), 185, 290, fill='#eef1e4')
d.arc((4,4,27,27), 10, 95, fill='#c7ab78')
d.rectangle((15,10,16,21), fill='#a99775')
d.rectangle((11,14,20,15), fill='#a99775')
d.line((17,11,17,21), fill='#eef1e4')
d.line((12,16,20,16), fill='#eef1e4')
bread.save(ASSETS/'sprites/bread-of-life.png')

# Ranged equipment and the guardian's larger shield follow the same pixel palette.
bow=Image.new('RGBA',(20,36));d=ImageDraw.Draw(bow)
d.line([(5,2),(10,5),(14,11),(16,18),(14,25),(10,31),(5,34)],fill='#46525c',width=3)
d.line([(5,2),(10,5),(14,11),(16,18),(14,25),(10,31),(5,34)],fill='#c6b590')
d.line((5,3,5,33),fill='#ddd8c1');d.rectangle((13,15,16,20),fill='#594333')
bow.save(ASSETS/'sprites/bow.png')
arrow=Image.new('RGBA',(16,6));d=ImageDraw.Draw(arrow)
d.line((1,3,12,3),fill='#c6b590');d.line((1,1,4,3),fill='#ddd8c1');d.line((1,5,4,3),fill='#ddd8c1')
d.polygon([(11,1),(15,3),(11,5)],fill='#9baeb5');d.point((14,3),fill='#d3d7cf')
arrow.save(ASSETS/'sprites/arrow.png')
shield=Image.new('RGBA',(32,40));d=ImageDraw.Draw(shield)
d.ellipse((2,3,29,36),fill='#46525c');d.ellipse((3,4,28,35),fill='#d2ae68')
d.ellipse((5,6,26,33),fill='#263e4b');d.ellipse((7,8,24,31),fill='#365c69')
d.arc((3,4,28,35),180,285,fill='#e0d4b6')
for x,y in ((15,5),(27,18),(15,34),(4,18)):d.rectangle((x,y,x+1,y+1),fill='#e0d4b6')
d.ellipse((10,13,21,26),fill='#46525c');d.ellipse((12,15,19,24),fill='#9baeb5')
d.rectangle((13,16,15,18),fill='#d3d7cf')
shield.save(ASSETS/'sprites/guardian-shield.png')

# HUD icons and nine-slice UI frame use the game's existing palette.
hearts=Image.new('RGBA',(32,16))
for empty in range(2):
    im=Image.new('RGBA',(16,16));d=ImageDraw.Draw(im)
    shape=[(2,3),(5,3),(7,5),(9,3),(12,3),(14,5),(14,8),(8,14),
           (6,14),(0,8),(0,5)]
    d.polygon(shape,fill='#282c37')
    d.polygon([(2,4),(5,4),(7,6),(9,4),(12,4),(13,5),(13,8),
               (7,13),(1,8),(1,5)],fill='#4d5865' if empty else '#973f4e')
    if not empty:
        d.line((3,5,5,5),fill='#d08a8c');d.point((2,6),fill='#d08a8c')
        d.line((8,11,12,7),fill='#754951')
    hearts.paste(im,(empty*16,0))
hearts.save(ASSETS/'sprites/hearts.png')
frame=Image.new('RGBA',(16,16),'#101722');d=ImageDraw.Draw(frame)
d.rectangle((0,0,15,15),outline='#4d5865');d.rectangle((1,1,14,14),outline='#655346')
for x,y in ((1,1),(12,1),(1,12),(12,12)):d.rectangle((x,y,x+2,y+2),fill='#c7ab78')
frame.save(ASSETS/'sprites/ui-frame.png')

# A compact tileset usable directly from Tiled.
tiles=Image.new('RGBA',(128,32)); d=ImageDraw.Draw(tiles)
for tile in range(4):
    x=tile*32; base=['#5d535a','#3f3945','#777069','#403a42'][tile]
    d.rectangle((x,0,x+31,31),fill=base)
    for yy in (0,16,31): d.line((x,yy,x+31,yy),fill='#292b35')
    for xx,yy in ((15,0),(6,16),(28,16)): d.line((x+xx,yy,x+xx,yy+15),fill='#292b35')
    if tile in (0,2):
        d.rectangle((x,0,x+31,3),fill='#b4a382');d.line((x,5,x+31,5),fill='#827563')
    for _ in range(18):
        xx=x+rng.randrange(32); yy=rng.randrange(7,30)
        d.point((xx,yy),fill='#71646a')
tiles.save(ASSETS/'maps/stone.png')

# Painted pixel layers at half resolution, scaled with nearest-neighbor.
bg=Image.new('RGB',(480,270)); d=ImageDraw.Draw(bg)
for y in range(270):
    t=y/270; d.line((0,y,480,y),fill=tuple(int(a+(b-a)*t) for a,b in zip((12,19,30),(111,79, 70))))
d.ellipse((356,28,403,75),fill='#b8aa8c'); d.ellipse((360,30,399,69),fill='#d4c49d')
for i in range(45):
    x,y=rng.randrange(480),rng.randrange(140);d.point((x,y),fill='#727681')
for x,y in ((-40,175),(70,140),(230,155),(360,125)):
    d.polygon([(x-80,240),(x+70,y),(x+215,240)],fill='#363341')
for i in range(14):
    x=i*40-15; y=145+rng.randrange(35)
    d.rectangle((x,y,x+30,242),fill='#393641')
    d.rectangle((x+4,y-5,x+8,y),fill='#393641');d.rectangle((x+23,y-5,x+27,y),fill='#393641')
    for wx in (8,20): d.rectangle((x+wx,y+12,x+wx+3,y+24),fill='#1f2734')
bg.resize((960,540),Image.Resampling.NEAREST).save(ASSETS/'sprites/sky.png')
ruins=Image.new('RGBA',(480,270));d=ImageDraw.Draw(ruins)
for x in (40,195,360):
    d.rectangle((x,105,x+14,240),fill='#594b50');d.rectangle((x+3,114,x+5,237),fill='#7e6860')
    d.rectangle((x-4,101,x+18,109),fill='#8e7b68');d.rectangle((x-5,233,x+19,240),fill='#8e7b68')
    d.line((x+13,120,x+50,120),fill='#a78e67',width=2)
    d.polygon([(x+20,123),(x+47,123),(x+47,175),(x+33,167),(x+20,178)],fill='#743844')
    d.rectangle((x+31,131,x+35,157),fill='#b99569');d.rectangle((x+25,138,x+41,142),fill='#b99569')
ruins.resize((960,540),Image.Resampling.NEAREST).save(ASSETS/'sprites/ruins.png')

# Orthogonal map. Pixel coordinates in the XML use Tiled's top-left origin.
W,H,T=280,17,32
map_el=ET.Element('map',version='1.10',tiledversion='1.11.2',orientation='orthogonal',renderorder='right-down',width=str(W),height=str(H),tilewidth='32',tileheight='32',infinite='0',nextlayerid='4',nextobjectid='30')
ts=ET.SubElement(map_el,'tileset',firstgid='1',name='stone',tilewidth='32',tileheight='32',tilecount='4',columns='4')
ET.SubElement(ts,'image',source='stone.png',width='128',height='32')
layer=ET.SubElement(map_el,'layer',id='1',name='terrain',width=str(W),height=str(H)); data=ET.SubElement(layer,'data',encoding='csv')
grid=[[0]*W for _ in range(H)]
for x in range(W): grid[15][x]=1;grid[16][x]=2
platforms=[(448,160,160),(672,224,160),(896,160,128),(1152,160,192),
           (1632,192,160),(1920,160,160),(2816,160,128),(3040,224,192),
           (3392,160,160),(3616,224,192),(3872,160,160),(4288,160,192),
           (4576,224,160),(4832,160,160),(5184,192,192),
           (5952,160,192),(6208,224,160),(6464,288,160),(6720,224,192),
           (7072,160,160),(7360,224,192),(6912,288,192),(7520,256,192)]
stairs=[(2208,96,64),(2272,128,64),(2336,160,96),(2432,128,64),(2496,96,64),
        (7488,96,64),(7552,128,64),(7616,160,96),(7712,128,64),(7776,96,64)]
for x,top,width in platforms:
    row=H-top//T
    for col in range(x//T,(x+width)//T): grid[row][col]=3
# Wide stair treads lead up to a landing, then descend into the guardian arena.
for x,top,width in stairs:
    for y in range(64,top,32):
        for col in range(x//T,(x+width)//T):
            grid[H-1-y//T][col]=3 if y==top-32 else 2
# Top of tile row is H*32-row*32; requested top matches this.
data.text='\n'+',\n'.join(','.join(map(str,row)) for row in grid)+'\n'
coll=ET.SubElement(map_el,'objectgroup',id='2',name='collisions',visible='0')
i=1
def rectangle(parent,name,x,y,w,h,typ=None):
    global i
    obj=ET.SubElement(parent,'object',id=str(i),name=name,x=str(x),y=str(H*T-y-h),width=str(w),height=str(h),**({'type':typ} if typ else {}));i+=1;return obj
rectangle(coll,'ground',0,0,W*T,64)
for n,(x,top,width) in enumerate(platforms): rectangle(coll,f'platform-{n}',x,top-32,width,32)
for n,(x,top,width) in enumerate(stairs): rectangle(coll,f'stair-{n}',x,64,width,top-64)
entities=ET.SubElement(map_el,'objectgroup',id='3',name='entities',visible='0')
def point(name,x,y,typ):
    global i
    obj=ET.SubElement(entities,'object',id=str(i),name=name,x=str(x),y=str(H*T-y),**{'type':typ});i+=1;ET.SubElement(obj,'point')
point('templar',96,64,'spawn');point('sanctuary',4480,64,'checkpoint')
for n,x in enumerate((620,1080,1856,2160,3328,4032,4960,5728,6560,7296,7904)): point(f'sentinel-{n}',x,64,'sentinel')
for n,(x,y) in enumerate(((1440,64),(3680,224),(5280,64),(6240,224),(7136,64))): point(f'archer-{n}',x,y,'archer')
point('gate-guardian',8576,64,'guardian')
for n,(x,y) in enumerate(((742,232),(1632,72),(2944,72),(3776,232),(4320,72),
                            (5536,72),(6320,232),(6912,72),(7872,72),(8080,72))):
    rectangle(entities,f'bread-of-life-{n}',x,y,28,28,'relic')
rectangle(entities,'boss-arena',8032,64,736,256,'arena')
rectangle(entities,'locked-gate',8768,64,40,176,'gate');rectangle(entities,'pilgrims-exit',8832,64,96,100,'exit')
map_el.set('nextobjectid',str(i))
ET.indent(map_el);ET.ElementTree(map_el).write(ASSETS/'maps/exiles-road.tmx',encoding='UTF-8',xml_declaration=True)

# Synthesized original sound cues and a quiet looping drone.
RATE=22050
def sound(name,duration,fn):
    with wave.open(str(ASSETS/'audio'/name),'wb') as f:
        f.setnchannels(1);f.setsampwidth(2);f.setframerate(RATE)
        samples=[]
        for i in range(int(duration*RATE)):
            t=i/RATE; value=max(-1,min(1,fn(t,duration)))
            samples.append(struct.pack('<h',int(value*32767)))
        f.writeframes(b''.join(samples))
sound('sword.wav',.20,lambda t,d:(rng.uniform(-1,1)*.20+math.sin(2*math.pi*(950*t-1500*t*t))*.08)*(1-t/d))
sound('hit.wav',.17,lambda t,d:(rng.uniform(-1,1)*.35+math.sin(2*math.pi*100*t)*.25)*math.exp(-t*22))
sound('jump.wav',.15,lambda t,d:math.sin(2*math.pi*(190*t+500*t*t))*.12*(1-t/d))
sound('hurt.wav',.25,lambda t,d:math.sin(2*math.pi*(160*t-180*t*t))*.22*math.exp(-t*10))
sound('chime.wav',1.1,lambda t,d:sum(math.sin(2*math.pi*f*t) for f in (440,660,880))*.09*math.exp(-t*3))
import runpy
runpy.run_path(str(ROOT / 'tools/generate_atmosphere.py'))
runpy.run_path(str(ROOT / 'tools/generate_deaths.py'))
runpy.run_path(str(ROOT / 'tools/generate_font.py'))
runpy.run_path(str(ROOT / 'tools/generate_icon.py'))
print('Generated sprites, layered backgrounds, Tiled map and six original audio files.')
