"""Generate the templar's original armor impact cue, without changing other assets."""
from pathlib import Path
import math
import random
import struct
import wave

rate = 22050
duration = .34
rng = random.Random(41)
samples = []
for i in range(round(rate * duration)):
    t = i / rate
    attack = min(1, t / .002)
    thud = math.sin(2 * math.pi * (135 * t - 105 * t * t)) * .58 * math.exp(-t * 22)
    plate = sum(math.sin(2 * math.pi * f * t) for f in (670, 1090, 1730)) * .10 * math.exp(-t * 20)
    rattle = rng.uniform(-1, 1) * .24 * math.exp(-t * 30)
    fade = min(1, (duration - t) / .025)
    sample = (thud + plate + rattle) * attack * fade
    samples.append(struct.pack('<h', round(max(-1, min(1, sample)) * 32767)))

output = Path(__file__).resolve().parents[1] / 'assets/audio/hurt.wav'
with wave.open(str(output), 'wb') as wav:
    wav.setnchannels(1)
    wav.setsampwidth(2)
    wav.setframerate(rate)
    wav.writeframes(b''.join(samples))
print(f'Generated {output}')
