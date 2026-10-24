"""Original phase poses and layered, deterministic enemy attack foley (no map changes)."""
from pathlib import Path
from PIL import Image, ImageDraw
import array
import math
import random
import wave

ROOT = Path(__file__).resolve().parents[1] / 'assets'
atlas = Image.new('RGBA', (1536, 560))
characters = Image.open(ROOT/'sprites/characters.png')
sources = [Image.open(ROOT/f'sprites/{name}.png').crop((0, 0, 64, 80))
           for name in ('brute', 'spearman', 'oil-thrower')]
sources.extend((characters.crop((0, 480, 64, 560)),
                Image.open(ROOT/'sprites/shield-bearer.png').crop((0, 0, 64, 80)),
                characters.crop((0, 640, 64, 720)), characters.crop((0, 560, 64, 640))))
# Eight poses per phase interpolate planted stances, shoulders and hands.
def key(values, frame):
    keys = ((0,1,2), (2,3,4), (4,5,6,7))[frame//8]
    t = (frame%8)/7 * (len(keys)-1)
    i = min(len(keys)-2, int(t))
    return round(values[keys[i]] + (values[keys[i+1]]-values[keys[i]])*(t-i))
for row, source in enumerate(sources):
    for frame in range(24):
        pose = Image.new('RGBA', (64,80))
        lean = key((-1,-3,-4,4,5,3,1,0), frame)
        lift = key((0,-1,-2,1,2,2,1,0), frame)
        # Separate upper body and planted legs; attacking enemies no longer march in place.
        pose.alpha_composite(source.crop((0,50,64,80)), (0,50))
        pose.alpha_composite(source.crop((0,0,64,50)), (lean,lift))
        d = ImageDraw.Draw(pose)
        # Cloth folds, armor shading and bent elbows follow the weapon's choreography.
        if row == 0:
            hand_y = key((36,24,16,26,42,43,39,36), frame)
            d.line((43+lean,33+lift,46,hand_y,49,hand_y),fill='#292d32',width=8)
            d.line((44+lean,32+lift,47,hand_y),fill='#667580',width=5)
            d.rectangle((45,hand_y-2,50,hand_y+2),fill='#be9270')
            d.line((21+lean,45+lift,34+lean,47+lift),fill='#c6b590')
        elif row == 1:
            reach = key((-2,-5,-7,6,8,4,1,0), frame)
            for shoulder in (22,41):
                d.line((shoulder+lean,35+lift,shoulder+reach,42),fill='#594333',width=7)
                d.line((shoulder+lean,34+lift,shoulder+reach,41),fill='#be9270',width=4)
            d.line((25+lean,46+lift,35+lean,47+lift),fill='#d2bc8c')
        elif row == 2:
            hand_x=key((46,48,46,55,53,47,44,44), frame)
            hand_y=key((40,25,16,23,37,44,44,42), frame)
            d.line((42+lean,34+lift,hand_x,hand_y),fill='#594333',width=8)
            d.line((43+lean,33+lift,hand_x,hand_y),fill='#be9270',width=4)
            d.line((22+lean,35+lift,26+lean,43+lift),fill='#c6b590',width=2)
        elif row == 5:
            # Bow arm reaches forward; the drawing hand pulls back towards the cheek.
            draw_back = key((0,-3,-7,-2,1,1,0,0), frame)
            d.line((41+lean,34+lift,47+lean,39+lift), fill='#3c4d38', width=7)
            d.line((42+lean,33+lift,48+lean,38+lift), fill='#be9270', width=4)
            d.line((24+lean,34+lift,29+draw_back+lean,24+lift), fill='#3c4d38', width=7)
            d.line((25+lean,33+lift,30+draw_back+lean,23+lift), fill='#be9270', width=4)
        else:
            # Planted feet, coiled shoulder, then an extended sword arm and relaxed return.
            hand_y = key((42,29,21,36,45,46,44,42), frame)
            hand_x = key((44,43,42,49,50,48,45,44), frame)
            d.line((42+lean,34+lift,hand_x+lean,hand_y+lift), fill='#46525c', width=8)
            d.line((43+lean,33+lift,hand_x+lean,hand_y+lift), fill='#9baeb5', width=5)
            d.rectangle((hand_x+lean-2,hand_y+lift-2,hand_x+lean+2,hand_y+lift+2), fill='#be9270')
            d.line((23+lean,35+lift,25+lean,44+lift), fill='#46525c', width=6)
        atlas.alpha_composite(pose,(frame*64,row*80))
atlas.save(ROOT/'sprites/enemy-attacks.png')

RATE = 22050
cues = {'mace-lift': .38, 'mace-swing': .26, 'mace-impact': .65,
        'spear-draw': .26, 'spear-thrust': .21, 'oil-prime': .36,
        'oil-throw': .30, 'pot-shatter': .42, 'fire-ignite': 3.0}
for index,(name,duration) in enumerate(cues.items()):
    rng = random.Random(1900+index)
    values=[]; low=0.0; last=0.0
    for i in range(round(RATE*duration)):
        t=i/RATE; p=t/duration; noise=rng.uniform(-1,1)
        low += .08*(noise-low)
        high=noise-last;last=noise
        if name == 'mace-lift':
            value=.15*low*math.sin(math.pi*p)**2 + .08*math.sin(math.tau*(110*t-35*t*t))*math.sin(math.pi*p)
        elif name == 'mace-swing':
            envelope=math.sin(math.pi*p)**2
            value=envelope*(.65*low+.045*noise)*(.3+.7*p)
        elif name == 'mace-impact':
            value=.28*math.sin(math.tau*(68*t-30*t*t))*math.exp(-t*10)
            value+=.21*noise*math.exp(-t*35)+.10*math.sin(math.tau*213*t)*math.exp(-t*14)
            value+=.04*high*math.exp(-t*7)*(math.sin(t*113)**12)
        elif name == 'spear-draw':
            value=.12*noise*math.sin(math.pi*p)**2 + .06*math.sin(math.tau*310*t)*math.exp(-t*20)
        elif name == 'spear-thrust':
            value=(.24*noise+.3*low)*math.sin(math.pi*p)**3
            value+=.07*math.sin(math.tau*900*t)*math.exp(-t*35)
        elif name == 'oil-prime':
            value=.12*noise*math.sin(math.pi*p)**2 + .04*math.sin(math.tau*(340*t+600*t*t))*math.exp(-t*8)
        elif name == 'oil-throw':
            value=.45*low*math.sin(math.pi*p)**2 + .08*math.sin(math.tau*(220*t-200*t*t))*math.exp(-t*10)
        elif name == 'pot-shatter':
            value=.16*high*math.exp(-t*18)
            for start,freq in ((0,1200),(.045,1900),(.09,2450),(.15,1550)):
                if t>=start:value+=.06*math.sin(math.tau*freq*(t-start))*math.exp(-(t-start)*42)
        else:
            value=.55*low*math.exp(-t*4)+.16*noise*math.exp(-t*18)
            value+=(.045*high*math.sin(t*97)**18 + .10*low) * min(1, (duration-t)/.45)
        # Short fades prevent clicks while keeping each cue's natural relative loudness.
        value*=min(1,t/.004,(duration-t)/.025)
        values.append(max(-.95,min(.95,value)))
    samples=array.array('h',(round(v*32767) for v in values))
    with wave.open(str(ROOT/f'audio/{name}.wav'),'wb') as f:
        f.setnchannels(1);f.setsampwidth(2);f.setframerate(RATE);f.writeframes(samples.tobytes())
print('Generated 168 phase poses and nine original weapon/fire cues.')
