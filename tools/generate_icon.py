"""Reproduce the app emblem using the game's pixel grid and armor palette."""
from pathlib import Path
from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[1]
DEST = ROOT / 'assets' / 'branding'
DEST.mkdir(parents=True, exist_ok=True)

icon = Image.new('RGBA', (64, 64))
d = ImageDraw.Draw(icon)
ink = '#242936'
steel = '#a4b1b8'
light = '#d0d7cc'
shadow = '#4d5865'
gold = '#c7ab78'
ivory = '#ddd8c1'
red = '#973f4e'

# Stepped corners and inset frame, with room around the emblem at Dock size.
frame = [(9,2),(54,2),(61,9),(61,54),(54,61),(9,61),(2,54),(2,9)]
d.polygon(frame, fill=ink)
d.line(frame + [frame[0]], fill=gold, width=1)
d.polygon([(10,5),(53,5),(58,10),(58,53),(53,58),(10,58),(5,53),(5,10)], fill='#343c47')
d.rectangle((9,9,54,30), fill='#3d4650')
d.rectangle((9,31,54,54), fill='#303843')
for x,y in ((8,8),(55,8),(8,55),(55,55)):
    d.rectangle((x,y,x+1,y+1), fill=gold)

# Burgundy mantle gives the steel silhouette the same contrast as the player.
d.polygon([(21,26),(43,26),(51,47),(43,51),(21,51),(13,47)], fill='#643340')
d.polygon([(21,27),(26,29),(19,47),(14,46)], fill=red)
d.polygon([(38,29),(43,27),(50,46),(45,47)], fill=red)

# Great helm: flat crown, dark visor, brass nose guard and pixel rivets.
d.polygon([(22,10),(41,10),(46,15),(46,29),(41,35),(22,35),(17,29),(17,15)], fill=ink)
d.polygon([(23,12),(40,12),(44,16),(44,28),(40,32),(23,32),(19,28),(19,16)], fill=shadow)
d.rectangle((22,13,40,29), fill=steel)
d.rectangle((22,13,40,15), fill=light)
d.rectangle((39,16,42,28), fill='#7e9099')
d.rectangle((20,20,42,23), fill=ink)
d.rectangle((30,15,33,31), fill=gold)
d.line((30,15,30,31), fill='#e0d4b6')
for x in (24,27,36,39):
    d.point((x,27), fill=ink)
d.rectangle((24,30,38,31), fill=shadow)

# Heater shield: bronze rim, ivory face and compact crimson cross.
d.polygon([(16,32),(47,32),(47,44),(43,51),(32,58),(20,51),(16,44)], fill=ink)
d.polygon([(18,33),(45,33),(45,44),(41,50),(32,55),(22,50),(18,44)], fill=gold)
d.polygon([(20,35),(43,35),(43,43),(39,48),(32,52),(24,48),(20,43)], fill=ivory)
d.polygon([(40,35),(43,35),(43,43),(39,48),(32,52),(32,50),(38,46),(40,42)], fill='#bbbca9')
d.line((20,35,39,35), fill='#f0ecd7')
d.rectangle((29,36,34,48), fill=red)
d.rectangle((24,39,39,43), fill=red)
d.line((29,36,33,36), fill='#b65a61')
d.line((24,39,28,39), fill='#b65a61')
for x,y in ((19,34),(44,34),(19,43),(44,43),(32,54)):
    d.point((x,y), fill='#e0d4b6')

# Every size is sampled directly from the same grid, preserving sharp pixels.
for size in (16,32,64,128,256,512,1024):
    icon.resize((size,size), Image.Resampling.NEAREST).save(DEST / f'icon-{size}.png')
master = icon.resize((1024,1024), Image.Resampling.NEAREST)
master.save(DEST / 'o-templario.icns', append_images=[
    icon.resize((size,size), Image.Resampling.NEAREST)
    for size in (16,32,64,128,256,512)
])
master.save(DEST / 'o-templario.ico', sizes=[(s,s) for s in (16,32,64,128,256)],
            append_images=[icon.resize((s,s), Image.Resampling.NEAREST) for s in (16,32,64,128,256)])
