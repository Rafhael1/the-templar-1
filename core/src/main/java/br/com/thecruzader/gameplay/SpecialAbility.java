package br.com.thecruzader.gameplay;

/**
 * Boss rewards. Each entry defines an implemented ability, with localized presentation keys.
 */
public enum SpecialAbility {
    GOLDEN_STRIKE(3, 2.5, "special.goldenStrike");

    private final int charges;
    private final double damageMultiplier;
    private final String textKey;

    SpecialAbility(int charges, double damageMultiplier, String textKey) {
        this.charges = charges;
        this.damageMultiplier = damageMultiplier;
        this.textKey = textKey;
    }

    public int charges() {
        return charges;
    }

    public double damageMultiplier() {
        return damageMultiplier;
    }

    public String textKey() {
        return textKey;
    }
}
