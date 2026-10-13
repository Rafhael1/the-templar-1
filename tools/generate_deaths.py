"""Eight pixel-grid death poses for templar, sentinel, guardian and archer."""
from pathlib import Path
from PIL import Image, ImageDraw
ROOT=Path(__file__).resolve().parents[1]/'assets/sprites'
characters=Image.open(ROOT/'characters.png').convert('RGBA')
sheet=Image.new('RGBA',(96*8,80*4))
for kind,row in enumerate((0,6,7,8)):
    base=characters.crop((0,row*80,64,(row+1)*80))
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
            d.line((ex,68,ex+19,68),fill='#a4b1b8',width=2)
            d.line((ex+4,65,ex+4,71),fill='#c7ab78')
        sheet.alpha_composite(im,(frame*96,kind*80))
sheet.save(ROOT/'deaths.png')
