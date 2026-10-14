"""Original Marian roadside shrine in the game's pixel palette; writes only sanctuary.png."""
from pathlib import Path
from PIL import Image, ImageDraw

DESTINATION = Path(__file__).resolve().parents[1] / 'assets/sprites/sanctuary.png'


def generate():
    image = Image.new('RGBA', (96, 128))
    d = ImageDraw.Draw(image)
    outline, stone, shadow = '#4d454e', '#807167', '#655864'
    gold, gold_light, gold_shadow = '#c7ab78', '#e5d6a7', '#8e7356'
    # Three worn stone steps and an altar with an ivory cloth.
    d.rectangle((2, 121, 93, 127), fill=outline)
    d.rectangle((3, 121, 92, 125), fill=shadow)
    d.line((4, 121, 91, 121), fill='#a18a70')
    d.rectangle((9, 113, 88, 121), fill=stone)
    d.line((10, 113, 87, 113), fill=gold)
    d.rectangle((17, 102, 80, 113), fill=shadow)
    d.rectangle((20, 104, 77, 111), fill=stone)
    d.line((24, 109, 31, 109), fill=shadow)
    d.line((65, 104, 65, 110), fill=shadow)
    d.rectangle((11, 97, 86, 103), fill=outline)
    d.rectangle((12, 97, 85, 100), fill=gold)
    d.rectangle((17, 99, 80, 102), fill='#e5dbc0')
    d.rectangle((41, 101, 55, 111), fill='#ddd8c1')
    d.line((42, 109, 54, 109), fill=gold_shadow)
    d.rectangle((47, 103, 49, 107), fill=gold)
    # A pointed masonry niche; its tall part sits to the right of the checkpoint anchor.
    d.polygon([(28, 97), (28, 48), (35, 38), (54, 26), (58, 26), (77, 38), (84, 48), (84, 97)], fill=outline)
    d.polygon([(31, 97), (31, 49), (38, 40), (55, 29), (57, 29), (74, 40), (81, 49), (81, 97)], fill=stone)
    d.line([(32, 96), (32, 49), (39, 41), (55, 30)], fill='#b4a382', width=2)
    d.line([(79, 96), (79, 49), (73, 42), (58, 31)], fill=shadow, width=2)
    for y in (58, 71, 84):
        d.line((31, y, 36, y), fill=outline)
        d.line((76, y + 3, 81, y + 3), fill=outline)
    d.polygon([(36, 96), (36, 51), (42, 43), (56, 34), (70, 43), (76, 51), (76, 96)], fill=gold_shadow)
    d.polygon([(38, 94), (38, 52), (44, 45), (56, 37), (68, 45), (74, 52), (74, 94)], fill='#202735')
    d.line((39, 56, 39, 91), fill=gold)
    d.line((73, 56, 73, 91), fill=gold)
    d.line((40, 94, 72, 94), fill=gold)
    # Our Lady: gold halo, ivory veil, blue mantle, folded hands and a peaceful face.
    d.ellipse((45, 41, 67, 64), fill=gold_shadow)
    d.ellipse((46, 42, 66, 63), fill=gold)
    d.arc((47, 43, 65, 62), 185, 320, fill=gold_light)
    d.polygon([(52, 46), (61, 46), (66, 52), (68, 64), (73, 89), (69, 93), (43, 93), (40, 88), (44, 63), (47, 52)], fill='#263e4b')
    d.polygon([(52, 48), (60, 48), (64, 54), (65, 66), (70, 90), (60, 91), (55, 76), (46, 90), (43, 88), (47, 63), (49, 54)], fill='#365c69')
    d.line([(48, 62), (46, 74), (44, 87)], fill='#66858a', width=2)
    d.line([(65, 66), (67, 79), (69, 88)], fill='#1f3541', width=2)
    d.polygon([(52, 48), (60, 48), (63, 53), (63, 65), (67, 73), (62, 72), (59, 64), (52, 65), (49, 73), (46, 74), (49, 62), (49, 53)], fill='#ddd8c1')
    d.line([(51, 51), (53, 49), (59, 49), (61, 52)], fill='#f2ead4')
    d.rectangle((52, 54, 60, 63), fill='#be9270')
    d.line((52, 54, 59, 54), fill='#e0b08a')
    d.line((53, 58, 54, 58), fill='#78503c')
    d.line((58, 58, 59, 58), fill='#78503c')
    d.point((56, 60), fill='#e0b08a')
    d.line((55, 62, 57, 62), fill='#78503c')
    d.polygon([(53, 66), (59, 66), (62, 90), (49, 90)], fill='#e5dbc0')
    d.line((51, 88, 60, 88), fill=gold)
    d.polygon([(49, 72), (51, 71), (56, 75), (56, 80), (52, 78)], fill='#66858a')
    d.polygon([(62, 71), (64, 74), (58, 80), (56, 79), (57, 75)], fill='#66858a')
    d.line((55, 73, 56, 80), fill='#e0b08a', width=2)
    d.line((57, 73, 57, 80), fill='#be9270')
    for x, y in ((42, 54), (70, 56), (42, 80), (70, 82)):
        d.point((x, y), fill=gold)
    # A carved gilded cross with trefoil tips, a highlighted spine and a central jewel.
    d.rectangle((53, 5, 59, 26), fill=outline)
    d.rectangle((45, 11, 67, 17), fill=outline)
    for x, y in ((56, 4), (45, 14), (67, 14), (56, 25)):
        d.rectangle((x - 2, y - 2, x + 2, y + 2), fill=gold_shadow)
        d.point((x, y - 3), fill=gold_light)
        d.point((x - 3, y), fill=gold)
        d.point((x + 3, y), fill=gold)
    d.rectangle((54, 6, 58, 25), fill=gold)
    d.rectangle((45, 12, 67, 16), fill=gold)
    d.line((55, 6, 55, 24), fill=gold_light)
    d.line((46, 12, 66, 12), fill=gold_light)
    d.line((58, 17, 58, 24), fill=gold_shadow)
    d.rectangle((54, 12, 58, 16), fill=gold_shadow)
    d.rectangle((55, 13, 57, 15), fill='#973f4e')
    d.point((55, 13), fill='#d08a8c')
    # Wax candles and flowers on the low sides of the altar. Flames are drawn in-game on activation.
    for x in (22, 84):
        d.rectangle((x - 4, 107, x + 4, 109), fill=gold_shadow)
        d.line((x - 3, 107, x + 3, 107), fill=gold)
        d.rectangle((x - 1, 89, x + 2, 106), fill='#bbbca9')
        d.rectangle((x - 1, 89, x, 105), fill='#e5dbc0')
        d.point((x, 88), fill=outline)
    for x, y in ((14, 91), (19, 87), (10, 87)):
        d.line((x, y + 1, 17, 96), fill='#647650')
        d.rectangle((x - 1, y - 1, x + 1, y + 1), fill='#ddd8c1')
        d.point((x, y), fill=gold)
    image.save(DESTINATION)


if __name__ == '__main__':
    generate()
    print(f'Generated {DESTINATION}')
