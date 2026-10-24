"""Generate only the authored first-level TMX, without touching art or audio."""
from pathlib import Path
import argparse
import os
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / 'assets'
parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument('--map-output', type=Path, required=True)
args = parser.parse_args()

# Orthogonal map. Pixel coordinates in the XML use Tiled's top-left origin.
W,H,T=280,17,32
map_el=ET.Element('map',version='1.10',tiledversion='1.11.2',orientation='orthogonal',renderorder='right-down',width=str(W),height=str(H),tilewidth='32',tileheight='32',infinite='0',nextlayerid='5',nextobjectid='30')
ts=ET.SubElement(map_el,'tileset',firstgid='1',name='stone',tilewidth='32',tileheight='32',tilecount='4',columns='4')
ET.SubElement(ts,'image',source='stone.png',width='128',height='32')
layer=ET.SubElement(map_el,'layer',id='1',name='terrain',width=str(W),height=str(H)); data=ET.SubElement(layer,'data',encoding='csv')
grid=[[0]*W for _ in range(H)]
for x in range(W): grid[15][x]=1;grid[16][x]=2
# Camp ledges, a broken aqueduct, ruined galleries, then the chapel ascent.
# Tuples are (left, surface height, width), in world pixels.
platforms=[(448,160,160),(672,224,160),(896,160,128),(1152,160,288),
           (1792,320,768),  # Aqueduct deck; the crypt runs below it.
           (2816,160,160),(3040,256,256),(3392,160,128),(3680,224,160),
           (4832,160,288),(5184,192,224),
           (4768,384,832),  # Vaulted gallery roof, also an upper route.
           (7520,256,192)]
# Short, broad steps can be climbed in both directions with the normal jump.
stairs=[(3584,96,96),(3968,96,96),
        (5632,96,128),(5888,128,160),(6048,160,96),
        (6144,192,64),(6368,256,96),(6624,320,128),
        (6880,384,224),(7104,320,32),(7136,256,96),(7232,192,96),
        (7328,96,96),(7424,160,96),(7712,160,64),(7776,96,64)]
# Open entrances on either side expose the lower passage instead of sealing it
# behind solid staircases. These stepping stones provide access to the deck.
stairs += [(1536,96,96),(2208,96,64),(2272,160,160),(2432,128,128)]
platforms += [(1632,192,96),(1728,256,64),
              (2560,256,96),(2656,160,96),
              (4544,160,96),(4640,256,96),(4736,320,32),(5600,288,96)]
# The ridge is solid rock: no pits underneath its footholds that can trap a fall.
stairs += [(6208,224,160),(6464,288,160),(6752,352,128)]
for x,top,width in platforms:
    row=H-top//T
    for col in range(x//T,(x+width)//T): grid[row][col]=3
# Wide stair treads lead up to a landing, then descend into the guardian arena.
for x,top,width in stairs:
    for y in range(64,top,32):
        for col in range(x//T,(x+width)//T):
            grid[H-1-y//T][col]=3 if y==top-32 else 2
# Top of tile row is H*32-row*32; requested top matches this.
data.text='\n'+',\n'.join(','.join(map(str,row)) for row in grid)+'\n'
coll=ET.SubElement(map_el,'objectgroup',id='2',name='collisions',visible='0')
i=1
def rectangle(parent,name,x,y,w,h,typ=None):
    global i
    obj=ET.SubElement(parent,'object',id=str(i),name=name,x=str(x),y=str(H*T-y-h),width=str(w),height=str(h),**({'type':typ} if typ else {}));i+=1;return obj
rectangle(coll,'ground',0,0,W*T,64)
for n,(x,top,width) in enumerate(platforms): rectangle(coll,f'platform-{n}',x,top-32,width,32)
for n,(x,top,width) in enumerate(stairs): rectangle(coll,f'stair-{n}',x,64,width,top-64)
entities=ET.SubElement(map_el,'objectgroup',id='3',name='entities',visible='0')
def point(name,x,y,typ):
    global i
    obj=ET.SubElement(entities,'object',id=str(i),name=name,x=str(x),y=str(H*T-y),**{'type':typ});i+=1;ET.SubElement(obj,'point')
def floor_at(x, width=30):
    surfaces = [(left, top, span) for left, top, span in platforms + stairs
                if left <= x and left + span >= x + width]
    return max([64] + [top for _, top, _ in surfaces])

point('templar',96,64,'spawn');point('sanctuary',4480,64,'checkpoint')
variants = {'sentinel-1': 'shield_bearer', 'sentinel-4': 'brute', 'archer-1': 'oil_thrower', 'sentinel-7': 'shield_bearer', 'sentinel-10': 'brute', 'archer-4': 'oil_thrower'}
for n,(x,y) in enumerate(((520, 160), (980, 160), (1856, 64), (2048, 64), (2900, 160), (3470, 160), (4120, 64), (4910, 160), (5824, 64), (7056, 384), (7600, 256))): point(f'sentinel-{n}',x, floor_at(x) if x >= 6000 else y,variants.get(f'sentinel-{n}', 'sentinel'))
for n,(x,y) in enumerate(((1250, 160), (3740, 224), (5020, 160), (7260, 192), (7888, 64))): point(f'archer-{n}',x, floor_at(x) if x >= 6000 else y,variants.get(f'archer-{n}', 'archer'))
point('gate-guardian',8576,64,'guardian')
for n,(x,y) in enumerate(((742,232),(1888,72),(3096,264),(3776,232),(4320,72),
                            (5536,72),(5520,392),(6320,232),(6976,72),(7872,72),(8080,72),
                            (3896,72),(6688,328))):
    rectangle(entities,f'bandage-{n}',x,max(y, floor_at(x,28)+8) if x >= 6000 else y,28,28,'bandage')
rectangle(entities,'boss-arena',8032,64,736,256,'arena')
rectangle(entities,'locked-gate',8768,64,40,176,'gate');rectangle(entities,'pilgrims-exit',8832,64,96,100,'exit')
for n,(x,y) in enumerate(((4640,64),(5980,128))): point(f'spearman-{n}',x,y,'spearman')
# Alternate-route guards and isolated ground encounters fill the long empty stretches.
# Leave the sanctuary approach and the chapel threshold as deliberate breathing spaces.
for name,x,y,kind in (
        ('camp-road-patrol',820,64,'sentinel'),
        ('aqueduct-watch',2150,320,'sentinel'),
        ('aqueduct-descent',2670,160,'sentinel'),
        ('aqueduct-rearguard',2464,320,'shield_bearer'),
        ('cloister-roof-watch',5370,384,'archer'),
        ('cloister-descent',5700,96,'sentinel'),
        ('ridge-lancer',6824,352,'spearman'),
        ('chapel-road-patrol',7392,96,'sentinel')):
    point(name,x,y,kind)
rooms=ET.SubElement(map_el,'objectgroup',id='4',name='passages',visible='0')
rectangle(rooms,'penitents-ridge',5984,64,1920,448,'mountain')
rectangle(rooms,'crypt',1792,64,768,224,'cave')
rectangle(rooms,'pilgrims-gallery',4768,64,832,288,'gallery')
map_el.set('nextobjectid',str(i))
if args.map_output is not None:
    ts.find('image').set('source', Path(os.path.relpath(ASSETS / 'maps/stone.png', args.map_output.resolve().parent)).as_posix())
    ET.indent(map_el)
    ET.ElementTree(map_el).write(args.map_output, encoding='UTF-8', xml_declaration=True)

