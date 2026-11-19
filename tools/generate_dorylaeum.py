"""Reproduce Dorileia: dry plain, wagon ring, open camp and mounted commander."""
from pathlib import Path
import random
from PIL import Image, ImageDraw
from generate_nicaea import tent
from generate_constantinople import envoy_image
from level_map import write_map

DEST = Path(__file__).resolve().parents[1] / 'assets/levels/dorylaeum'
RNG = random.Random(10970701)


def save(im, name):
    path = DEST / name
    path.parent.mkdir(parents=True, exist_ok=True)
    im.resize((im.width * 2, im.height * 2), Image.Resampling.NEAREST).save(path)


def plain(region=0):
    im = Image.new('RGB', (480, 270))
    d = ImageDraw.Draw(im)
    for y in range(270):
        t = min(y / 220, 1)
        d.line((0, y, 479, y), fill=tuple(int(a*(1-t)+b*t) for a,b in zip((76,111,128),(226,186,121))))
    d.ellipse((361, 64, 387, 90), fill='#f5d994')
    d.polygon([(0,179),(61,143),(106,163),(180,121),(249,163),(309,138),(392,158),(455,125),(479,146),(479,269),(0,269)], fill='#a39473')
    d.polygon([(0,196),(92,180),(188,191),(287,175),(381,191),(479,173),(479,269),(0,269)], fill='#8b825b')
    d.polygon([(0,227),(105,211),(207,224),(322,209),(479,220),(479,269),(0,269)], fill='#ac905e')
    for _ in range(200):
        x,y=RNG.randrange(480), RNG.randrange(227,270)
        d.line((x,y,x+RNG.randrange(2,8),y-1),fill=RNG.choice(('#c7a473','#8a784f','#d1af79')))
    if region == 0:
        for x in (28,155,318,414):
            d.rectangle((x,213,x+40,221),fill='#73513a')
            d.line((x,212,x+40,212),fill='#d2b381',width=2)
            for wheel in (x+6,x+32): d.ellipse((wheel-4,219,wheel+4,227),fill='#493d32',outline='#bd9864')
    elif region == 1:
        for x,y in [(20,204),(116,218),(294,208),(403,213)]: tent(d,x,y,50,'#cdb381')
    elif region == 3:
        d.polygon([(0,245),(100,226),(168,239),(251,218),(355,242),(479,224),(479,269),(0,269)],fill='#796c50')
        d.polygon([(0,265),(106,241),(168,254),(249,233),(354,257),(479,239),(479,253),(354,269),(249,246),(168,266),(106,254),(0,269)],fill='#bca77c')
        for x,y in [(35,239),(174,243),(294,235),(421,240)]:
            d.polygon([(x,y),(x+8,y-14),(x+22,y-17),(x+31,y-5),(x+26,y+4)],fill='#665d4a')
            d.line((x+8,y-14,x+22,y-17,x+31,y-5),fill='#b5a078',width=2)
    else:
        for x in range(24,480,26):
            d.line((x,203,x,225),fill='#514736',width=2)
            d.polygon([(x,201),(x+15,205),(x,211)],fill='#8e4539')
        for x in (38,198,361): tent(d,x,235,65,'#dbc392')
    return im


def camp():
    exterior=Image.new('RGBA',(480,224)); d=ImageDraw.Draw(exterior)
    for x in range(0,480,48):
        d.rectangle((x,168,x+40,191),fill='#765139',outline='#c3a071',width=2)
        for wx in (x+7,x+34): d.ellipse((wx-6,185,wx+6,197),fill='#49382e',outline='#ad8a60',width=2)
    for x in (56,424):
        d.rectangle((x-23,94,x-18,223),fill='#815d40')
        d.rectangle((x+18,94,x+23,223),fill='#815d40')
        d.rectangle((x-23,94,x+23,101),fill='#bd9564')
        d.polygon([(x-18,104),(x+18,104),(x+15,146),(x,155),(x-15,146)],fill='#8e4539')
    save(exterior,'camp/exterior.png')
    im=plain(1);d=ImageDraw.Draw(im)
    for x in (6,130,322,421): tent(d,x,226,54,'#dfc293')
    for x in (18,100,351,441):
        d.rectangle((x,222,x+27,236),fill='#78563c',outline='#c6a174',width=2)
        for w in (x+4,x+23):d.ellipse((w-4,232,w+4,240),fill='#483b30',outline='#bb9567')
    d.rectangle((0,238,479,269),fill='#a98a59')
    for _ in range(140):
        x,y=RNG.randrange(480),RNG.randrange(241,270)
        d.line((x,y,x+3,y),fill=RNG.choice(('#c3a374','#967d50','#b99b6d')))
    save(im,'camp/interior.png')
    front=Image.new('RGBA',(480,270));d=ImageDraw.Draw(front)
    for x in (8,439):
        d.rectangle((x,258,x+32,269),fill='#6e5038',outline='#be9764')
        d.line((x,258,x+32,269),fill='#be9764')
    save(front,'camp/foreground.png')
    door=Image.new('RGBA',(32,128));d=ImageDraw.Draw(door)
    for x in range(0,32,4): d.rectangle((x,0,x+2,127),fill='#91623e')
    for y in (18,82,117): d.rectangle((0,y,31,y+4),fill='#554335')
    save(door,'camp/door.png')
    frame=Image.new('RGBA',(48,144));d=ImageDraw.Draw(frame)
    for x in (0,42): d.rectangle((x,0,x+5,143),fill='#b08a59')
    d.rectangle((0,0,47,6),fill='#c6a473')
    save(frame,'camp/door-frame.png')


def rider_art():
    """Ten hand-authored pixel poses: canter, bow, charge, fall and recovery."""
    sheet = Image.new('RGBA', (96 * 10, 64))
    ink, dark = '#17252d', '#263840'
    horse, light = '#47565b', '#72817f'
    gold, edge = '#bc9153', '#f0ce82'
    teal, shade = '#248485', '#18535e'
    for f in range(10):
        im = Image.new('RGBA', (96, 64)); d = ImageDraw.Draw(im)
        stride = {1: 7, 2: -6, 5: 10, 8: -9}.get(f, 0)
        dip = 2 if f in (2, 5, 8, 9) else 0
        rear = 5 if f == 6 else 0

        def poly(points, color, outline=ink):
            d.polygon(points, fill=color, outline=outline, width=1)

        def limb(points, color, width=4):
            d.line(points, fill=ink, width=width + 2)
            d.line(points, fill=color, width=width)

        # Tail strands and the far pair of legs, behind the horse's torso.
        poly([(21,35),(14,36),(10,42),(4,43),(9,47),(16,44),(23,40)], dark)
        d.line((17,38,12,43,7,45),fill=light)
        for hip,k in ((28,-1),(59,1)):
            knee=(hip+stride*k//2,48+dip)
            hoof=(hip+stride*k,59)
            limb([(hip,39+dip),knee,hoof],dark,3)
            d.rectangle((hoof[0]-2,59,hoof[0]+5,61),fill=ink)
        # Muscular chest, flank and arched neck, with a pale muzzle and facial blaze.
        poly([(19,33+dip),(25,28+dip),(43,27+dip),(59,29+dip-rear),(66,24-rear),
              (72,15-rear),(79,13-rear),(85,19-rear),(90,22-rear),(89,28-rear),
              (81,29-rear),(76,35),(72,43+dip),(59,46+dip),(31,46+dip),(21,42+dip)],horse)
        poly([(23,34+dip),(34,30+dip),(53,30+dip),(60,35+dip),(54,41+dip),
              (31,42+dip),(23,39+dip)],light,horse)
        poly([(63,33),(69,24-rear),(74,17-rear),(77,17-rear),(75,26),(70,37),(64,41)],light,horse)
        poly([(72,16-rear),(71,9-rear),(76,14-rear)],horse)
        poly([(78,15-rear),(80,9-rear),(82,17-rear)],horse)
        poly([(80,18-rear),(82,20-rear),(82,26-rear),(78,26-rear)],'#e6dbc0',horse)
        poly([(82,25-rear),(89,25-rear),(88,29-rear),(81,28-rear)],'#a7a79a')
        d.point((83,20-rear),fill='#ffe3a0')
        for j in range(5):
            poly([(71-j,19+j*3-rear),(69-j,22+j*3),(65-j,24+j*3),(67-j,19+j*3)],dark)
        d.line((78,18-rear,80,27-rear,86,29-rear),fill=gold,width=2)
        d.line((81,28-rear,57,34+dip),fill=edge)
        # Near legs with visible knee joints and bright horseshoes.
        for hip,k in ((29,1),(64,-1)):
            knee=(hip+stride*k//2,49+dip)
            hoof=(hip+stride*k,59)
            limb([(hip,40+dip),knee,hoof],horse,4)
            d.line((hip+1,42+dip,knee[0]+1,knee[1],hoof[0]+1,57),fill=light)
            d.rectangle((hoof[0]-2,58,hoof[0]+5,61),fill=ink)
            d.line((hoof[0]-1,60,hoof[0]+5,60),fill='#a7b5b2')
        # Embroidered saddle cloth, harness and plated rider's boot.
        poly([(35,30+dip),(58,30+dip),(59,45+dip),(49,49+dip),(35,45+dip)],shade)
        poly([(37,32+dip),(56,32+dip),(57,43+dip),(49,47+dip),(37,43+dip)],teal,gold)
        d.line((39,34+dip,54,34+dip,54,42+dip,48,45+dip,39,42+dip,39,34+dip),fill=edge)
        d.arc((43,35+dip,52,43+dip),70,290,fill=edge,width=2)
        d.rectangle((34,28+dip,57,31+dip),fill=ink)
        d.line((35,28+dip,56,28+dip),fill=gold)
        # Wind-swept cloak is separate from the horse silhouette.
        lean = 4 if f in (5,8) else -3 if f == 6 else 0
        rx, ry = 46+lean, 14+dip
        poly([(rx-3,ry+8),(rx-13,ry+10),(rx-23,ry+5),(rx-19,ry+16),
              (rx-28,ry+23),(rx-10,ry+20),(rx-2,ry+15)],shade)
        d.line((rx-4,ry+9,rx-14,ry+13,rx-20,ry+12),fill=teal,width=2)
        d.line((rx-12,ry+18,rx-23,ry+21),fill=gold)
        # Lamellar coat: individual scale rows and shoulder guards.
        poly([(rx-6,ry+7),(rx+7,ry+7),(rx+11,ry+15),(rx+6,ry+23),(rx-7,ry+22),(rx-10,ry+14)],dark)
        for row in range(ry+10,ry+22,3):
            for col in range(rx-5,rx+7,3):
                d.rectangle((col,row,col+1,row+1),fill=gold if (row+col)%2 else '#879390')
        poly([(rx-9,ry+8),(rx-3,ry+7),(rx-2,ry+12),(rx-8,ry+14)],gold)
        poly([(rx+5,ry+7),(rx+11,ry+10),(rx+10,ry+14),(rx+4,ry+12)],gold)
        limb([(rx+1,ry+22),(rx+6,ry+30),(rx+5,ry+36)],dark,4)
        d.line((rx+4,ry+25,rx+7,ry+31),fill=gold,width=2)
        d.rectangle((rx+2,ry+34,rx+11,ry+37),fill=ink)
        # Face, nasal helmet and feather crest rather than a featureless gold circle.
        poly([(rx-4,ry-1),(rx+5,ry-1),(rx+7,ry+5),(rx+3,ry+9),(rx-3,ry+6)],'#b78b64')
        poly([(rx-6,ry+1),(rx-5,ry-5),(rx,ry-10),(rx+5,ry-5),(rx+7,ry+1)],'#a9b8b5')
        d.line((rx,ry-8,rx,ry+1),fill=edge)
        d.line((rx-5,ry+1,rx+7,ry+1),fill=ink,width=2)
        d.line((rx+4,ry+1,rx+4,ry+6),fill='#d7ddcc')
        d.point((rx+1,ry+3),fill=ink)
        d.line((rx-4,ry+3,rx-3,ry+8,rx+2,ry+9),fill=dark,width=2)
        d.line((rx,ry-9,rx-6,ry-12,rx-11,ry-10),fill=teal,width=2)
        # Bow poses open visibly; the charge uses a long, lowered saber.
        if f in (3,4):
            grip=(67,32+dip)
            limb([(rx+7,ry+13),grip],gold,3)
            d.arc((61,17+dip,75,46+dip),270,90,fill=edge,width=2)
            pull=7 if f==3 else 0
            d.line((68,17+dip,61-pull,32+dip,68,46+dip),fill='#eee0b4')
            limb([(rx-5,ry+14),(61-pull,32+dip)],gold,3)
            if f==3:d.line((54-pull,32+dip,77,32+dip),fill='#f8e7ba')
        else:
            grip=(rx+14,ry+17) if f not in (5,8) else (66,30+dip)
            limb([(rx+7,ry+13),grip],gold,3)
            if f==6:tip=(73,7)
            elif f in (5,8):tip=(94,28+dip)
            elif f==9:tip=(70,46)
            else:tip=(72,25)
            d.line((*grip,*tip),fill=ink,width=4)
            d.line((*grip,*tip),fill='#d6e6df',width=2)
            d.line((grip[0]-2,grip[1]-2,grip[0]+3,grip[1]+3),fill=edge,width=2)
        if f == 7:
            fallen=Image.new('RGBA',(96,64)); fd=ImageDraw.Draw(fallen)
            fd.ellipse((11,47,83,61),fill=ink)
            fd.ellipse((16,46,70,59),fill=horse)
            fd.polygon([(42,43),(68,47),(73,56),(48,54),(31,59)],fill=teal)
            fd.ellipse((65,45,76,54),fill=gold)
            fd.line((30,54,18,60),fill=light,width=3)
            fd.line((72,57,89,59),fill='#d6e6df',width=2)
            im=fallen
        sheet.paste(im,(f*96,0))
    save(sheet,'boss/commander.png')



def map_file():
    tile=Image.new('RGB',(32,16),'#8e744c');d=ImageDraw.Draw(tile)
    d.rectangle((0,0,15,2),fill='#cfb17b')
    for _ in range(65):
        x,y=RNG.randrange(32),RNG.randrange(3,16)
        d.point((x,y),fill=RNG.choice(('#a48a5a','#b59a68','#756542')))
    save(tile,'maps/paving.png')
    solids=[(0,0,8960,64),(768,64,128,32),(896,64,256,64),(1152,64,128,32),
            (1856,64,256,32),(2432,64,96,32),(2528,64,256,64),(2784,64,96,32),
            (3264,64,192,32),(3584,128,224,32),(4032,64,96,32),(4128,64,256,64),(4384,64,96,32),
            (6944,64,160,32),
            (4544,64,96,32),(4640,64,224,64),(4864,64,96,32),
            (5248,64,256,32),(5632,128,224,32),(6176,64,96,32),(6272,64,192,64),(6464,64,96,32)]
    points=[('spawn',112,64),('checkpoint',7296,64),('sentinel',580,64),('archer',1024,128),
            ('spearman',1952,96),('archer',2640,128),('sentinel',3330,96),('archer',3696,160),
            ('spearman',4272,128),('archer',4736,128),('sentinel',5360,96),('spearman',6080,64),
            ('sentinel',7008,96),('mounted_commander',8248,64)]
    areas=[('bandage',x,y,24,28) for x,y in [(1300,64),(2900,64),(3820,64),(5504,64),(7160,64),(7960,64)]]
    areas += [('arena',7584,64,832,256),('gate',8416,64,32,256),('exit',8704,64,128,110)]
    write_map(DEST/'maps/dorylaeum.tmx',280,solids,points,areas)


if __name__ == '__main__':
    for i,name in enumerate(('march','plain','camp')):save(plain(i),'scenery/'+name+'.png')
    save(plain(3),'scenery/gully.png')
    camp()
    rider_art()
    save(envoy_image('#8e4539','#d2b582'),'envoys/bohemond.png')
    map_file()
