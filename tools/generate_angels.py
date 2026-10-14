"""Shared six-frame winged angel sprite; writes only assets/sprites/angels.png."""
from pathlib import Path
from PIL import Image, ImageDraw
import math
ROOT = Path(__file__).resolve().parents[1] / 'assets/sprites'
sheet = Image.new('RGBA', (96 * 6, 112))
for frame in range(6):
    image = Image.new('RGBA', (48, 56))
    d = ImageDraw.Draw(image)
    spread = [19, 23, 24, 20, 15, 16][frame]
    lift = [0, -3, -5, -1, 4, 3][frame]
    # Individually stepped feathers retain crisp outlines as the wings beat.
    for direction in [-1, 1]:
        root = 24 + direction * 5
        tip = 24 + direction * spread
        d.polygon([(root, 20), (tip, 9 + lift), (tip - direction * 2, 26 + lift),
                   (root, 37), (24, 29)], fill='#8e9290')
        d.polygon([(root, 21), (tip - direction, 11 + lift), (tip - direction * 3, 25 + lift),
                   (root, 35), (24, 28)], fill='#d1d5cc')
        for feather in range(7):
            start_x = root + direction * (2 + feather * 2)
            end_x = 24 + direction * (spread - feather // 2)
            top = 14 + lift + feather * 2
            bottom = 23 + lift + feather * 2
            d.line((start_x, top, end_x, bottom), fill='#eee5ce', width=2)
            d.point((end_x - direction, bottom - 1), fill='#fff3da')
    # A luminous halo, white tunic, gold sash and a blessing hand.
    d.ellipse((17, 4, 31, 9), outline='#c7ab78', width=2)
    d.line((20, 4, 28, 4), fill='#fff3da')
    d.rectangle((19, 12, 29, 22), fill='#c7ab78')
    d.rectangle((20, 13, 28, 22), fill='#d8bea0')
    d.rectangle((19, 11, 29, 14), fill='#e5dbc0')
    d.rectangle((19, 13, 20, 20), fill='#e5dbc0')
    d.point((22, 17), fill='#655346')
    d.point((26, 17), fill='#655346')
    d.line((23, 20, 25, 20), fill='#aa8979')
    d.polygon([(20, 22), (28, 22), (34, 49), (24, 53), (14, 49)], fill='#8e9290')
    d.polygon([(21, 23), (27, 23), (31, 48), (24, 51), (17, 48)], fill='#eee5ce')
    d.line((23, 26, 22, 48), fill='#c7c9bf', width=2)
    d.line((27, 29, 29, 47), fill='#fff3da')
    d.line((20, 24, 27, 40), fill='#c7ab78', width=3)
    d.line((21, 24, 28, 40), fill='#e5d6a7')
    d.line((27, 25, 35, 28, 40, 23), fill='#8e9290', width=5)
    d.line((27, 24, 35, 27, 40, 22), fill='#eee5ce', width=3)
    d.rectangle((39, 19, 42, 23), fill='#d8bea0')
    d.line((40, 18, 40, 22), fill='#e5dbc0')
    d.line((20, 25, 16, 32, 18, 35), fill='#eee5ce', width=3)
    d.rectangle((17, 34, 20, 37), fill='#d8bea0')
    sheet.alpha_composite(image.resize((96, 112), Image.Resampling.NEAREST), (frame * 96, 0))
sheet.save(ROOT / 'angels.png')
print('Generated six pixel art wingbeat frames.')
