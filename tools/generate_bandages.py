"""Generate the original 32 x 32 linen-bandage pickup; preserves every other asset."""
from pathlib import Path
from PIL import Image, ImageDraw

DESTINATION = Path(__file__).resolve().parents[1] / 'assets/sprites/bandages.png'


def generate():
    image = Image.new('RGBA', (32, 32))
    draw = ImageDraw.Draw(image)
    outline, shadow, linen, light = '#4d4546', '#a99d83', '#e5dbc0', '#f2ead4'
    # Loose folded strip under a tightly rolled bundle. All edges stay on the pixel grid.
    draw.polygon([(8, 18), (18, 18), (25, 25), (25, 28), (19, 28), (13, 24), (8, 24)], fill=outline)
    draw.polygon([(10, 19), (17, 19), (23, 25), (23, 26), (19, 26), (13, 22), (10, 22)], fill=linen)
    draw.line([(14, 21), (20, 25), (23, 25)], fill=shadow)
    draw.polygon([(4, 10), (8, 6), (20, 6), (27, 11), (27, 20), (23, 24), (10, 24), (4, 19)], fill=outline)
    draw.polygon([(5, 11), (9, 7), (19, 7), (25, 11), (25, 20), (22, 22), (10, 22), (5, 18)], fill=shadow)
    draw.polygon([(5, 11), (9, 8), (19, 8), (23, 11), (21, 16), (9, 16), (5, 14)], fill=light)
    draw.rectangle((6, 14, 19, 19), fill=linen)
    draw.line((7, 17, 18, 17), fill=shadow)
    draw.line((9, 9, 18, 9), fill=linen)
    # The visible end of the roll shows layers of cloth, rather than a medical emblem.
    draw.ellipse((17, 9, 27, 21), fill=outline)
    draw.ellipse((18, 10, 26, 20), fill=linen)
    draw.arc((19, 11, 25, 19), 30, 320, fill=shadow)
    draw.line([(22, 13), (23, 13), (24, 15), (23, 17), (21, 17), (20, 15)], fill=light)
    draw.rectangle((22, 14, 23, 16), fill=outline)
    # Brown tie across the folded linen keeps the bundle distinct at HUD size.
    draw.rectangle((11, 8, 13, 21), fill='#8e7356')
    draw.line((12, 9, 12, 20), fill='#c7ab78')
    draw.rectangle((10, 14, 14, 16), fill='#655346')
    draw.point((11, 14), fill='#c7ab78')
    image.save(DESTINATION)


if __name__ == '__main__':
    generate()
    print(f'Generated {DESTINATION}')
