"""Bake the bundled DejaVu Sans into a high-resolution BMFont atlas (no runtime font dependency)."""
from pathlib import Path
from PIL import Image, ImageDraw, ImageFont
import math
UI=Path(__file__).resolve().parents[1]/'assets/ui'
SIZE=96;PAD=8;WIDTH=2048
font=ImageFont.truetype(str(UI/'DejaVuSans.ttf'),SIZE)
ascent,descent=font.getmetrics()
glyphs=[];x=y=PAD;row=0
for code in range(32,256):
    if 127<=code<160:continue
    char=chr(code);left,top,right,bottom=font.getbbox(char,anchor='ls')
    width=right-left;height=bottom-top
    if x+width+PAD>WIDTH:x=PAD;y+=row+PAD*2;row=0
    glyphs.append((code,x,y,width,height,left,ascent+top,round(font.getlength(char))))
    x+=width+PAD*2;row=max(row,height)
height=2**math.ceil(math.log2(y+row+PAD))
atlas=Image.new('RGBA',(WIDTH,height));d=ImageDraw.Draw(atlas)
for code,x,y,w,h,left,offset,advance in glyphs:
    d.text((x-left,y-(offset-ascent)),chr(code),font=font,fill='white',anchor='ls')
atlas.save(UI/'interface.png')
lines=[f'info face="DejaVu Sans" size={SIZE} bold=0 italic=0 charset="" unicode=1 stretchH=100 smooth=1 aa=1 padding=0,0,0,0 spacing=0,0',
       f'common lineHeight={ascent+descent} base={ascent} scaleW={WIDTH} scaleH={height} pages=1 packed=0',
       'page id=0 file="interface.png"',f'chars count={len(glyphs)}']
for code,x,y,w,h,left,offset,advance in glyphs:
    lines.append(f'char id={code} x={x} y={y} width={w} height={h} xoffset={left} yoffset={offset} xadvance={advance} page=0 chnl=15')
kernings=[]
for first,*_ in glyphs:
    for second,*_ in glyphs:
        amount=round(font.getlength(chr(first)+chr(second))-font.getlength(chr(first))-font.getlength(chr(second)))
        if amount:kernings.append(f'kerning first={first} second={second} amount={amount}')
lines.append(f'kernings count={len(kernings)}');lines.extend(kernings)
(UI/'interface.fnt').write_text('\n'.join(lines)+'\n')
print(f'Generated {WIDTH} x {height} atlas with {len(glyphs)} glyphs.')
