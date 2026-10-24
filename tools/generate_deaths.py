"""Eight pixel-grid death poses for templar, sentinel, guardian, archer and spearman."""
from pathlib import Path
from PIL import Image, ImageDraw
ROOT=Path(__file__).resolve().parents[1]/'assets/sprites'
characters=Image.open(ROOT/'characters.png').convert('RGBA')
sheet=Image.new('RGBA',(96*8,80*8))
bodies = [characters.crop((0,row*80,64,(row+1)*80)) for row in (0,6,7,8)]
bodies.append(Image.open(ROOT/'spearman.png').convert('RGBA').crop((0,0,64,80)))
for name in ('shield-bearer','brute','oil-thrower'):
    bodies.append(Image.open(ROOT/(name+'.png')).convert('RGBA').crop((0,0,64,80)))
for kind,base in enumerate(bodies):
    body=base.crop(base.getbbox())
    for frame,angle in enumerate((0,8,24,46,70,90,90,90)):
        im=Image.new('RGBA',(96,80));d=ImageDraw.Draw(im)
        # Knees buckle, the torso falls back, then settles on the ground.
        pose=body.rotate(angle,resample=Image.Resampling.NEAREST,expand=True)
        x=47-pose.width//2+min(frame,5)*2
        y=70-pose.height
        if frame==1:y+=3
        im.alpha_composite(pose,(x,y))
        if frame in (4,5,6):
            for j in range(5):
                dx=15+j*14;dy=70-(6-frame)*(j%3+1)
                d.rectangle((dx,dy,dx+2,dy+1),fill='#8e7b68')
        # Dropped equipment remains separate from the falling silhouette.
        if frame>=2:
            ex=12+frame*3
            if kind == 4:
                d.line((ex-12,68,ex+42,68),fill='#78503c',width=2)
                d.polygon(((ex+42,65),(ex+50,68),(ex+42,71)),fill='#a4b1b8')
            elif kind == 6:
                d.line((ex-8,68,ex+26,68),fill='#78503c',width=3)
                d.rectangle((ex+20,63,ex+32,72),fill='#9baeb5')
            elif kind == 7:
                d.ellipse((ex,64,ex+10,72),fill='#9b6145')
            else:
                d.line((ex,68,ex+19,68),fill='#a4b1b8',width=2)
                d.line((ex+4,65,ex+4,71),fill='#c7ab78')
        sheet.alpha_composite(im,(frame*96,kind*80))
sheet.save(ROOT/'deaths.png')
