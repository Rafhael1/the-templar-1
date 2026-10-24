"""Generate only the spearman's pixel art body and separately animated spear."""
from pathlib import Path
import math
from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[1] / 'assets/sprites'
sheet = Image.new('RGBA', (512, 80))
for frame in range(8):
    body = Image.new('RGBA', (64, 80))
    draw = ImageDraw.Draw(body)
    ox, oy = 14, 12 + int(math.sin(frame * math.pi / 2))
    step = round(math.sin(frame * math.pi / 4) * 6)

    def rect(box, color):
        x1, y1, x2, y2 = box
        draw.rectangle((x1 + ox, y1 + oy, x2 + ox, y2 + oy), fill=color)

    # Rust tunic and ivory headwrap distinguish the lighter, shieldless silhouette.
    rect((4,39,13,54+step), '#aaa38e'); rect((20,39,28,54-step), '#aaa38e')
    rect((2,52+step,14,58+step), '#403530'); rect((20,52-step,32,58-step), '#403530')
    rect((3,16,29,36), '#594333')
    rect((8,17,25,36), '#9b6145')
    draw.polygon([(ox+8,oy+34),(ox+25,oy+34),(ox+29,oy+47),
                  (ox+18,oy+47),(ox+16,oy+40),(ox+14,oy+47),(ox+4,oy+47)], fill='#9b6145')
    rect((8,18,10,32), '#d2bc8c'); rect((23,18,25,32), '#664333')
    rect((5,44,13,47), '#d2bc8c'); rect((19,44,28,47), '#d2bc8c')
    rect((6,32,27,35), '#594333'); rect((18,32,21,35), '#c7ab78')
    rect((9,3,25,17), '#be9270'); rect((21,8,24,9), '#292d32')
    rect((15,14,24,17), '#493d35')
    draw.polygon([(ox+5,oy+5),(ox+7,oy-3),(ox+14,oy-6),(ox+23,oy-4),
                  (ox+27,oy+4)], fill='#d2bc8c')
    rect((6,2,27,5), '#eee0bb'); rect((8,-2,24,0), '#eee0bb')
    rect((5,6,9,20), '#d2bc8c'); rect((5,18,7,26), '#eee0bb')
    # Two hands held level with the shaft; no shield or heavy shoulder plates.
    rect((1,19,7,27), '#9b6145'); rect((27,19,32,26), '#9b6145')
    rect((2,27,17,30), '#be9270'); rect((28,26,37,29), '#be9270')
    sheet.alpha_composite(body, (frame * 64, 0))
sheet.save(ROOT / 'spearman.png')

spear = Image.new('RGBA', (144, 12))
draw = ImageDraw.Draw(spear)
draw.rectangle((0,5,125,7), fill='#78503c')
draw.line((0,5,125,5), fill='#c7ab78')
draw.rectangle((116,4,124,8), fill='#d2bc8c')
draw.polygon([(124,0),(143,6),(124,11),(128,6)], fill='#9baeb5')
draw.line((125,1,143,6), fill='#e0e4d9')
spear.save(ROOT / 'spear.png')
