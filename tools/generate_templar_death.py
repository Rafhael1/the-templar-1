"""Generates only the shared templar-death sheet and original wordless requiem."""
from pathlib import Path
from PIL import Image, ImageDraw
import array
import math
import random
import wave

ROOT = Path(__file__).resolve().parents[1] / 'assets'
SHEET = Image.new('RGBA', (128 * 12, 112))
PLATE, DARK, PAPER, RED, GOLD = '#a4b1b8', '#4d5865', '#ddd8c1', '#973f4e', '#c7ab78'
base = Image.open(ROOT / 'sprites/characters.png').convert('RGBA').crop((0, 0, 64, 80))
for frame in range(12):
    im = Image.new('RGBA', (128, 112))
    d = ImageDraw.Draw(im)
    # An upright planted sword becomes a cross throughout the final prayer.
    sword_x = 81
    d.line((sword_x, 72, sword_x, 101), fill=PLATE, width=2)
    d.line((sword_x + 1, 73, sword_x + 1, 100), fill=PAPER)
    d.rectangle((sword_x - 6, 71, sword_x + 6, 73), fill=GOLD)
    d.rectangle((sword_x - 1, 63, sword_x + 1, 70), fill='#655346')
    d.rectangle((sword_x - 2, 61, sword_x + 2, 63), fill=GOLD)
    if frame == 0:
        im.alpha_composite(base, (32, 30))
    elif frame < 9:
        drop = 3 if frame == 1 else 0
        lean = max(0, frame - 6) * 3
        head_y = (46 if frame == 1 else 52) + drop + lean
        # Bending legs and resting knee, separate from the praying torso.
        d.rectangle((50, 83, 60, 95), fill=DARK)
        d.rectangle((46, 93, 61, 102), fill='#282c37')
        d.rectangle((63, 86, 74, 96), fill=PLATE)
        d.rectangle((70, 94, 80, 101), fill=DARK)
        d.rectangle((75, 99, 86, 103), fill='#282c37')
        d.polygon([(52, head_y + 17), (68 + lean, head_y + 17), (72, 94), (41, 96)], fill='#754951')
        d.polygon([(54, head_y + 17), (62 + lean, head_y + 17), (56, 91), (42, 93)], fill=RED)
        d.rectangle((54 + lean, head_y + 17, 69 + lean, 86), fill=DARK)
        d.rectangle((55 + lean, head_y + 17, 68 + lean, 83), fill=PAPER)
        d.rectangle((60 + lean, head_y + 20, 63 + lean, 80), fill=RED)
        d.rectangle((56 + lean, head_y + 25, 67 + lean, head_y + 28), fill=RED)
        d.rectangle((54 + lean, head_y, 67 + lean, head_y + 16), fill=DARK)
        d.rectangle((55 + lean, head_y, 66 + lean, head_y + 12), fill=PLATE)
        d.rectangle((57 + lean, head_y + 2, 65 + lean, head_y + 4), fill='#d1d5cc')
        d.rectangle((56 + lean, head_y + 7, 66 + lean, head_y + 9), fill='#26313d')
        d.line((62 + lean, head_y + 5, 62 + lean, head_y + 14), fill=GOLD)
        # Forehead, breast, left shoulder, right shoulder, then hands folded.
        hand = [(78, 68), (63, head_y + 3), (64, head_y + 29),
                (56, head_y + 23), (70, head_y + 23), (78, 69), (78, 69), (78, 72)][frame - 1]
        d.line((69 + lean, head_y + 20, hand[0], hand[1]), fill=DARK, width=7)
        d.line((70 + lean, head_y + 19, hand[0], hand[1]), fill=PLATE, width=4)
        d.rectangle((hand[0] - 2, hand[1] - 2, hand[0] + 2, hand[1] + 2), fill=PAPER)
        if frame >= 6:
            d.line((55 + lean, head_y + 22, 77, 70 + lean // 2), fill=PLATE, width=4)
    else:
        # The knight bows to the earth; the final silhouette and red tabard remain recognizable.
        body = base.crop(base.getbbox()).rotate(75 if frame == 9 else 90,
                resample=Image.Resampling.NEAREST, expand=True)
        im.alpha_composite(body, (58 - body.width // 2, 103 - body.height))
        if frame == 9:
            for j in range(6):
                d.rectangle((29 + j * 11, 102 - j % 3, 30 + j * 11, 102 - j % 3), fill='#8e7b68')
    # His discarded shield keeps its red Christian cross.
    d.polygon([(25, 83), (42, 85), (41, 98), (33, 104), (25, 97)], fill=DARK)
    d.polygon([(27, 85), (40, 87), (39, 97), (33, 101), (27, 96)], fill=PAPER)
    d.line((33, 87, 33, 98), fill=RED, width=3)
    d.line((29, 91, 37, 91), fill=RED, width=3)
    SHEET.alpha_composite(im, (frame * 128, 0))
SHEET.save(ROOT / 'sprites/templar-death.png')

# Original D-minor requiem with a G-minor -> D-minor plagal (Amen) cadence.
RATE, DURATION = 22050, 6.4
samples = [0.0] * int(RATE * DURATION)
rng = random.Random(1099)

def add(start, duration, tone):
    for i in range(int(duration * RATE)):
        n = round(start * RATE) + i
        if n >= len(samples): break
        samples[n] += tone(i / RATE, duration)

def note(frequency, volume):
    def tone(t, duration):
        envelope = min(1, t / .28) * min(1, (duration - t) / .65)
        vibrato = .004 * math.sin(math.tau * 4.3 * t)
        phase = math.tau * frequency * (t + vibrato * t * .15)
        organ = math.sin(phase) + .26 * math.sin(2 * phase) + .11 * math.sin(3 * phase)
        choir = math.sin(phase * 1.002 + .4) * .26 + math.sin(phase * .998) * .26
        return volume * envelope * (organ + choir)
    return tone

# Opening bell, brief armour impact and four slow chord voices.
add(0, 5.8, lambda t, d: sum(math.sin(math.tau * 293.66 * partial * t) * amplitude * math.exp(-t * decay)
    for partial, amplitude, decay in [(1, .15, .8), (2.01, .055, 1.1), (2.76, .025, 1.6), (4.07, .016, 2.3)]))
for start, notes in [(0.25, (146.83, 220, 293.66, 349.23)), (1.8, (130.81, 196, 261.63, 329.63)),
                      (3.0, (98, 196, 233.08, 293.66)), (4.1, (146.83, 220, 293.66, 349.23))]:
    for frequency in notes: add(start, 2.25, note(frequency, .039))
for start, volume in [(0, .08), (1.0, .025), (2.75, .045)]:
    add(start, .15, lambda t, d, v=volume: (rng.uniform(-1, 1) + .35 * math.sin(math.tau * 850 * t)) * v * math.exp(-t * 40))
# Reverberation is generated into the cue, independent of platform audio effects.
dry = samples.copy()
for delay, gain in [(.241, .18), (.389, .11), (.557, .07)]:
    shift = round(delay * RATE)
    for i in range(shift, len(samples)): samples[i] += dry[i - shift] * gain
peak = max(abs(v) for v in samples)
pcm = array.array('h', (round(v / peak * .72 * 32767 * min(1, (DURATION - i / RATE) / .45))
                        for i, v in enumerate(samples)))
import sys
if sys.byteorder != 'little': pcm.byteswap()
with wave.open(str(ROOT / 'audio/templar-requiem.wav'), 'wb') as output:
    output.setparams((1, 2, RATE, 0, 'NONE', 'not compressed'))
    output.writeframes(pcm.tobytes())
print('Generated 12 templar death poses and 6.4-second original requiem.')
