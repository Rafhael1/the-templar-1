"""Original modal game score: synthesized wordless choir, lute, bells and drums.

Run with tools/requirements.txt installed. No samples or existing melodies are used.
All note tails and room reflections wrap around the loop boundary.
"""
from pathlib import Path
import wave

import numpy as np

RATE = 22050
OUTPUT = Path(__file__).resolve().parents[1] / 'assets/audio'
TAU = 2 * np.pi
# Shared D-dorian motif, with a different pace and orchestration in each scene.
PHRASES = (
    ((62, 2), (65, 1), (67, 1), (69, 3), (67, 1), (65, 2), (64, 2)),
    ((62, 3), (64, 1), (65, 2), (69, 2), (67, 2), (62, 2)),
    ((69, 2), (71, 1), (72, 1), (74, 2), (72, 2), (69, 2), (67, 2)),
    ((65, 2), (67, 1), (69, 1), (67, 2), (64, 2), (62, 4)),
)


def frequency(midi):
    return 440 * 2 ** ((midi - 69) / 12)


def envelope(t, duration, attack, release):
    return np.minimum(1, t / attack) * np.minimum(1, (duration - t) / release)


def choir(midi, duration, seed):
    """Additive male vowel with formants, independent singers and gentle vibrato."""
    t = np.arange(round(duration * RATE)) / RATE
    signal = np.zeros(len(t))
    rng = np.random.default_rng(seed)
    fundamental = frequency(midi)
    for singer in range(3):
        f = fundamental * 2 ** ((singer - 1) * 5 / 1200)
        vibrato_rate = 4.4 + singer * .23
        phase = TAU * f * t + .003 * f / vibrato_rate * np.sin(TAU * vibrato_rate * t + singer)
        onset = rng.uniform(0, TAU)
        for harmonic in range(1, min(38, int(6500 / f))):
            hz = harmonic * f
            formants = (.65 * np.exp(-.5 * ((hz - 650) / 150) ** 2)
                        + .40 * np.exp(-.5 * ((hz - 1100) / 210) ** 2)
                        + .16 * np.exp(-.5 * ((hz - 2450) / 300) ** 2))
            amplitude = (.16 + formants) / harmonic ** .85
            signal += amplitude * np.sin(harmonic * phase + onset) / 3
    return signal * envelope(t, duration, .22, .32)


def lute(midi, duration):
    t = np.arange(round(duration * RATE)) / RATE
    phase = TAU * frequency(midi) * t
    signal = sum(np.sin(h * phase) * np.exp(-t * (2.4 + h * .75)) / h
                 for h in range(1, 8))
    return signal * envelope(t, duration, .008, .10)


def bell(midi, duration):
    t = np.arange(round(duration * RATE)) / RATE
    signal = sum(gain * np.sin(TAU * frequency(midi) * ratio * t) * np.exp(-t * decay)
                 for ratio, gain, decay in ((1, 1, .8), (2.01, .38, 1.3), (2.76, .2, 2.0), (4.07, .1, 3)))
    return signal * envelope(t, duration, .006, .3)


def drum(accent, seed):
    duration = .65 if accent else .3
    t = np.arange(round(duration * RATE)) / RATE
    rng = np.random.default_rng(seed)
    # Falling pitch and a short skin transient, rather than a modern snare/kick.
    skin = np.sin(TAU * ((66 if accent else 115) * t + 3 * (1 - np.exp(-t * 30))))
    noise = rng.uniform(-1, 1, len(t))
    noise = np.convolve(noise, np.ones(7) / 7, mode='same')
    return (skin * np.exp(-t * (10 if accent else 20))
            + .3 * noise * np.exp(-t * 45)) * envelope(t, duration, .003, .08)


def add(buffer, start, signal, volume, pan=0):
    offset = round(start * RATE) % len(buffer)
    stereo = signal[:, None] * np.array([np.sqrt((1 - pan) / 2), np.sqrt((1 + pan) / 2)]) * volume
    end = min(len(stereo), len(buffer) - offset)
    buffer[offset:offset + end] += stereo[:end]
    if end < len(stereo):
        buffer[:len(stereo) - end] += stereo[end:]


def render(name, bpm, beats, kind):
    beat = 60 / bpm
    duration = beats * beat
    mix = np.zeros((round(duration * RATE), 2), dtype=np.float32)
    roots = (38, 41, 36, 43, 38, 45, 43, 38)
    # Warm sustained fifths leave the chant exposed; no dense cinematic chords.
    for section, root in enumerate(roots):
        start = section * beats / 8 * beat
        length = beats / 8 * beat
        for pitch, gain, pan in ((root, .11, -.25), (root + 7, .055, .25)):
            add(mix, start, choir(pitch, length + .2, section + pitch), gain, pan)

    phrase_span = beats / 8
    for phrase_index in range(8):
        phrase = PHRASES[phrase_index % 4]
        cursor = phrase_index * phrase_span
        # Exploration phrases breathe: the final quarter is intentionally empty.
        scale = phrase_span / 12 * (.74 if kind == 'exploration' else .94)
        for note_index, (pitch, length) in enumerate(phrase):
            note_duration = length * scale * beat
            add(mix, cursor * beat, choir(pitch - 12, note_duration, phrase_index * 10 + note_index),
                .25 if kind == 'boss' else .22, -.12)
            if kind == 'boss' and phrase_index % 2 == 1:
                add(mix, cursor * beat, choir(pitch, note_duration, 700 + note_index), .075, .18)
            cursor += length * scale

    for bar in range(beats // 4):
        root = roots[min(7, bar * 4 * 8 // beats)] + 12
        if kind == 'menu':
            pattern = ((0, root + 12), (1.5, root + 19), (3, root + 14))
        elif kind == 'exploration':
            pattern = ((0, root + 12), (2.5, root + 19)) if bar % 2 == 0 else ()
        else:
            pattern = tuple((step * .5, root + interval) for step, interval in
                            enumerate((0, 7, 12, 7, 0, 7, 14, 7)))
        for position, pitch in pattern:
            add(mix, (bar * 4 + position) * beat, lute(pitch, 1.7),
                .075 if kind == 'boss' else .055, .38 if bar % 2 else -.38)
        if kind == 'boss':
            for position, accent in ((0, True), (1.5, False), (2, True), (3, False), (3.5, False)):
                add(mix, (bar * 4 + position) * beat, drum(accent, bar * 8 + int(position * 2)),
                    .22 if accent else .08, -.15 if accent else .3)
        elif kind == 'menu' and bar % 2 == 0:
            add(mix, bar * 4 * beat, drum(True, bar), .055)
        if bar % (8 if kind == 'exploration' else 4) == 0:
            add(mix, bar * 4 * beat, bell(74 if kind != 'boss' else 62, 5), .065, .2)

    write_loop(name, mix, .78 if kind == 'boss' else .66)


def render_victory():
    """A rising D-major hymn with bells and a gentle plagal cadence."""
    beat = 60 / 88
    mix = np.zeros((round(32 * beat * RATE), 2), dtype=np.float32)
    roots = (50, 55, 57, 50, 59, 55, 55, 50)
    melody = (
        ((62, 1), (66, 1), (69, 2)),
        ((71, 2), (69, 1), (67, 1)),
        ((69, 1), (73, 1), (76, 2)),
        ((74, 4),),
        ((74, 1), (73, 1), (71, 2)),
        ((71, 1), (69, 1), (67, 2)),
        ((67, 2), (71, 2)),
        ((66, 2), (62, 2)),
    )
    for bar, (root, phrase) in enumerate(zip(roots, melody)):
        start = bar * 4 * beat
        third = 3 if root == 59 else 4
        for interval, gain, pan in ((0, .12, -.3), (third, .07, .2), (7, .08, .3)):
            add(mix, start, choir(root + interval, 4 * beat + .2, bar * 10 + interval), gain, pan)
        cursor = start
        for index, (pitch, length) in enumerate(phrase):
            add(mix, cursor, choir(pitch, length * beat, 400 + bar * 10 + index), .22, -.1)
            cursor += length * beat
        for step, interval in enumerate((12, 12 + third, 19, 12 + third)):
            add(mix, start + step * beat, lute(root + interval, 1.7), .07, .35)
        add(mix, start, bell(root + 24, 4.5), .09, -.2)
        add(mix, start, drum(True, 900 + bar), .06)
    write_loop('victory-chant.wav', mix, .70)


def write_loop(name, mix, peak):
    # Circular room reflections preserve tails across the loop, without a silent gap.
    dry = mix.copy()
    for delay, gain in ((.071, .10), (.137, .09), (.233, .17), (.379, .13), (.557, .09), (.811, .055), (1.127, .035)):
        mix += np.roll(dry[:, ::-1], round(delay * RATE), axis=0) * gain
    mix -= mix.mean(axis=0)
    mix *= peak / np.max(np.abs(mix))
    pcm = np.round(mix * 32767).astype('<i2')
    OUTPUT.mkdir(parents=True, exist_ok=True)
    with wave.open(str(OUTPUT / name), 'wb') as output:
        output.setparams((2, 2, RATE, 0, 'NONE', 'not compressed'))
        output.writeframes(pcm.tobytes())
    rms = np.sqrt(np.mean(mix ** 2))
    seam = np.max(np.abs(mix[0] - mix[-1]))
    print(f'{name}: {len(mix) / RATE:.2f}s, stereo {RATE}Hz, peak {np.max(np.abs(mix)):.3f}, RMS {rms:.3f}, seam {seam:.4f}')


if __name__ == '__main__':
    render('menu-chant.wav', 72, 96, 'menu')
    render('ambience.wav', 64, 128, 'exploration')
    render('boss-chant.wav', 112, 192, 'boss')
    render_victory()
