"""Deterministic pixel art for shield bearer, mace brute and oil thrower."""
from pathlib import Path
import math
from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[1] / 'assets/sprites'
for kind, cloth, shade in (('shield-bearer','#365c69','#263e4b'),
                           ('brute','#754951','#41404e'),
                           ('oil-thrower','#b38a49','#594333')):
    sheet = Image.new('RGBA',(512,80))
    for frame in range(8):
        im = Image.new('RGBA',(64,80)); d = ImageDraw.Draw(im)
        ox,oy = 14,12 + int(math.sin(frame*math.pi/2))
        step = round(math.sin(frame*math.pi/4)*5)
        def rect(box,color):
            a,b,c,e=box; d.rectangle((a+ox,b+oy,c+ox,e+oy),fill=color)
        for x,s in ((4,step),(20,-step)):
            rect((x,39,x+8,54+s),'#aaa38e')
            rect((x-2,52+s,x+10,58+s),'#403530')
        rect((3,16,29,40),shade)
        rect((7,17,26,40),cloth)
        d.polygon([(ox+6,oy+36),(ox+27,oy+36),(ox+30,oy+47),
                   (ox+19,oy+47),(ox+17,oy+41),(ox+14,oy+47),(ox+3,oy+47)],fill=cloth)
        rect((8,18,10,32),'#c6b590');rect((6,33,28,36),'#78503c');rect((18,33,22,36),'#c6b590')
        rect((8,3,26,18),'#46525c');rect((14,6,25,15),'#be9270')
        rect((21,9,24,10),'#292d32');rect((16,14,24,17),'#493d35')
        if kind=='oil-thrower':
            d.polygon([(ox+5,oy+7),(ox+9,oy-4),(ox+17,oy-7),(ox+27,oy),
                       (ox+29,oy+18),(ox+25,oy+20),(ox+25,oy+5),(ox+13,oy+4),(ox+12,oy+20),(ox+5,oy+19)],fill=shade)
            rect((-3,21,5,38),'#78503c');rect((-4,20,6,23),'#c6b590')
            rect((11,20,27,23),'#78503c');rect((23,23,26,32),'#78503c')
        else:
            # Chain coif, layered armor and a distinct silhouette for each infantry role.
            if kind=='brute':
                # Heavy boots, broad torso, leather apron and overlapping iron plates.
                for x,step_y in ((2,step),(21,-step)):
                    rect((x,39,x+11,53+step_y),'#41404e')
                    rect((x-1,52+step_y,x+12,58+step_y),'#292d32')
                    rect((x,43,x+11,46),'#78503c')
                    rect((x,52+step_y,x+11,53+step_y),'#9baeb5')
                d.polygon([(ox-3,oy+19),(ox+9,oy+13),(ox+28,oy+14),
                           (ox+36,oy+23),(ox+31,oy+41),(ox+2,oy+41)],fill='#292d32')
                rect((4,20,29,35),'#754951')
                for row in range(3):
                    for col in range(3):
                        x,y=5+col*8,21+row*5
                        rect((x,y,x+6,y+3),'#46525c')
                        rect((x,y,x+6,y),'#9baeb5')
                # Rounded pauldrons with a bright upper edge and riveted straps.
                for x in (-5,27):
                    d.polygon([(ox+x,oy+19),(ox+x+3,oy+15),(ox+x+11,oy+16),
                               (ox+x+14,oy+22),(ox+x+12,oy+28),(ox+x,oy+27)],fill='#46525c')
                    rect((x+2,17,x+9,19),'#9baeb5')
                    rect((x+1,25,x+11,27),'#292d32')
                    for rivet in (x+3,x+9):rect((rivet,23,rivet+1,24),'#c6b590')
                rect((2,34,31,38),'#403530');rect((15,34,21,38),'#c6b590')
                rect((17,35,19,37),'#292d32')
                for x in (4,14,24):
                    rect((x,39,x+6,47),'#594333');rect((x+1,40,x+5,45),'#78503c')
                    rect((x+2,42,x+3,43),'#c6b590')
                # Low iron cap, nose guard and a visible dark beard instead of a conical helmet.
                rect((6,2,29,16),'#46525c')
                d.polygon([(ox+5,oy+3),(ox+8,oy-3),(ox+23,oy-5),
                           (ox+29,oy),(ox+29,oy+5)],fill='#46525c')
                rect((9,-2,24,0),'#9baeb5');rect((6,3,29,5),'#78503c')
                rect((13,6,28,14),'#be9270');rect((22,8,26,9),'#292d32')
                rect((19,5,21,13),'#9baeb5');rect((14,14,27,19),'#403530')
                rect((16,18,24,21),'#403530');rect((23,14,26,14),'#d2bc8c')
                rect((30,27,36,32),'#78503c');rect((31,27,35,28),'#c6b590')
            else:
                # Mail sleeves frame the teal surcoat; layered brass trim stays readable at 1x.
                rect((1,18,8,34),'#46525c');rect((26,18,33,34),'#46525c')
                for y in range(21,33,3):
                    for x in (2,6,27,31):rect((x,y,x+1,y+1),'#9baeb5')
                rect((8,17,25,33),'#365c69')
                rect((9,18,11,31),'#c6b590');rect((23,18,25,31),'#263e4b')
                rect((12,20,13,29),'#719393')
                rect((4,36,29,39),'#263e4b')
                for x in (6,15,24):
                    rect((x,40,x+4,46),'#46525c');rect((x,40,x+4,41),'#9baeb5')
                rect((5,47,13,48),'#c6b590');rect((20,47,28,48),'#c6b590')
                rect((5,17,10,21),'#9baeb5');rect((27,17,33,21),'#9baeb5')
                rect((6,17,10,18),'#d3d7cf');rect((28,17,33,18),'#d3d7cf')
                # Tall wrapped helmet, cheek guards and a narrow eye slit.
                d.polygon([(ox+6,oy+5),(ox+9,oy-4),(ox+17,oy-8),
                           (ox+25,oy-4),(ox+28,oy+5)],fill='#46525c')
                d.polygon([(ox+10,oy+1),(ox+17,oy-6),(ox+24,oy+1)],fill='#9baeb5')
                rect((16,-5,18,4),'#d3d7cf')
                rect((5,2,28,5),'#c6b590');rect((6,6,28,7),'#e0d4b6')
                rect((7,8,12,19),'#46525c');rect((11,9,26,17),'#be9270')
                rect((18,9,26,10),'#292d32');rect((17,7,19,15),'#9baeb5')
                rect((7,18,14,23),'#46525c');rect((23,17,27,21),'#46525c')
                rect((8,19,10,20),'#9baeb5');rect((24,18,26,19),'#9baeb5')
        rect((28,27,34,32),'#be9270')
        sheet.alpha_composite(im,(frame*64,0))
    sheet.save(ROOT/(kind+'.png'))
shield=Image.new('RGBA',(32,44));d=ImageDraw.Draw(shield)
# Riveted bronze rim, inset wood panels and a hammered boss; no templar insignia.
d.polygon([(2,2),(10,0),(22,0),(30,2),(30,28),(25,36),(16,43),(7,36),(1,28)],fill='#292d32')
d.polygon([(3,3),(11,1),(21,1),(29,3),(28,28),(23,35),(16,41),(9,35),(3,28)],fill='#c6b590')
d.polygon([(5,5),(12,3),(20,3),(27,5),(26,27),(22,33),(16,38),(10,33),(5,27)],fill='#263e4b')
for x in (7,12,17,22):
    d.line((x,6,x,28 if x in (7,22) else 34),fill='#365c69',width=3)
    d.line((x,7,x,27 if x in (7,22) else 32),fill='#486f78')
d.line((6,9,16,5,26,9),fill='#e0d4b6',width=1)
d.line((8,29,16,35,24,29),fill='#c6b590',width=2)
d.polygon([(16,10),(24,20),(16,30),(8,20)],fill='#78503c')
d.polygon([(16,12),(22,20),(16,28),(10,20)],fill='#c6b590')
d.ellipse((10,14,22,26),fill='#46525c');d.ellipse((12,16,21,24),fill='#9baeb5')
d.rectangle((13,16,16,18),fill='#d3d7cf');d.rectangle((17,22,20,24),fill='#46525c')
for x,y in ((4,6),(27,6),(3,18),(28,18),(6,29),(25,29),(15,38)):
    d.rectangle((x,y,x+1,y+1),fill='#e0d4b6')
shield.save(ROOT/'infantry-shield.png')
mace=Image.new('RGBA',(76,24));d=ImageDraw.Draw(mace)
# Leather-bound grip, thick ash haft and a compact flanged iron head.
d.rectangle((0,9,55,14),fill='#403530');d.rectangle((2,10,53,13),fill='#8e7356')
d.line((20,10,52,10),fill='#c6b590')
for x in range(2,20,4):d.line((x,9,x+2,14),fill='#594333',width=2)
d.rectangle((0,8,2,15),fill='#c6b590');d.rectangle((48,7,53,16),fill='#c6b590')
d.polygon([(54,4),(58,2),(69,2),(74,7),(75,15),(70,21),(58,21),(54,18)],fill='#292d32')
d.rectangle((56,5,72,18),fill='#46525c');d.rectangle((60,4,67,19),fill='#9baeb5')
d.rectangle((62,5,64,18),fill='#d3d7cf');d.rectangle((68,6,72,17),fill='#667580')
for box in ((55,1,60,5),(66,0,70,5),(55,18,60,22),(66,18,70,23),(72,8,75,14)):
    d.rectangle(box,fill='#9baeb5')
d.rectangle((56,1,59,2),fill='#d3d7cf');d.rectangle((67,0,69,1),fill='#d3d7cf')
mace.save(ROOT/'mace.png')
pot=Image.new('RGBA',(16,20));d=ImageDraw.Draw(pot)
d.rectangle((6,3,9,6),fill='#c6b590');d.ellipse((2,6,13,18),fill='#594333')
d.rectangle((4,8,11,16),fill='#9b6145');d.rectangle((5,9,6,14),fill='#d2bc8c')
d.rectangle((7,0,8,2),fill='#f6d59b');d.rectangle((8,2,10,4),fill='#d08a51')
pot.save(ROOT/'oil-pot.png')
