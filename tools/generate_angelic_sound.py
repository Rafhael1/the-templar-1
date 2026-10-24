"""Generate the original choir and bell cue for the 4.6-second angel descent."""
import array
import math
from pathlib import Path
import sys
import wave

RATE = 22050
DURATION = 4.6
samples = [0.0] * round(RATE * DURATION)


def add_choir(frequency):
    for i in range(len(samples)):
        t = i / RATE
        envelope = min(1, t / .45) * min(1, (DURATION - t) / 1.3)
        # Slightly detuned voices with gentle vibrato and a soft vowel timbre.
        vibrato = .004 * math.sin(math.tau * 4.5 * t)
        for detune in (.998, 1.002):
            phase = math.tau * frequency * detune * t + vibrato
            voice = math.sin(phase) + .22 * math.sin(2 * phase) + .09 * math.sin(3 * phase)
            samples[i] += .045 * envelope * voice


def add_bell(start, frequency):
    for i in range(round(start * RATE), len(samples)):
        t = i / RATE - start
        envelope = min(1, t / .012) * math.exp(-t * 2.1)
        samples[i] += .12 * envelope * (math.sin(math.tau * frequency * t)
                + .3 * math.sin(math.tau * frequency * 2.01 * t))


# Luminous D-major choir, with rising bells during the descent.
for pitch in (293.66, 369.99, 440.0, 587.33):
    add_choir(pitch)
for start, pitch in ((0, 880), (.35, 1108.73), (.7, 1320), (1.8, 1760)):
    add_bell(start, pitch)

dry = samples.copy()
for delay, gain in ((.19, .22), (.31, .16), (.47, .10), (.67, .06)):
    shift = round(delay * RATE)
    for i in range(shift, len(samples)):
        samples[i] += dry[i - shift] * gain

peak = max(abs(value) for value in samples)
pcm = array.array('h', (round(value / peak * .8 * 32767
        * min(1, (DURATION - i / RATE) / .55)) for i, value in enumerate(samples)))
if sys.byteorder != 'little':
    pcm.byteswap()
output_path = Path(__file__).resolve().parents[1] / 'assets/audio/angelic.wav'
with wave.open(str(output_path), 'wb') as output:
    output.setparams((1, 2, RATE, 0, 'NONE', 'not compressed'))
    output.writeframes(pcm.tobytes())
print('Generated 4.6-second original angelic choir and bells.')
