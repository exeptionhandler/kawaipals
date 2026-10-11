package com.kawaipals.core;

/**
 * PalType – master registry of every collectable Pal in KawaiPals.
 *
 * <p>Each constant encodes:
 * <ul>
 *   <li>{@code id}           – lowercase identifier used for NBT, item names, textures, etc.</li>
 *   <li>{@code rarity}       – {@link PalRarity} tier</li>
 *   <li>{@code displayName}  – human-readable English name</li>
 *   <li>{@code powerDescription} – short English description of the passive power granted</li>
 * </ul>
 *
 * <p>The actual power logic (applying status effects, attributes, etc.) lives in
 * {@link com.kawaipals.entity.PalEntity#applyOwnerBuff}.
 */
public enum PalType {

    // ══════════════════════════════════════════════════════════════════════════
    // COMMON PALS
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Gotita – a small water-drop spirit that quickens your step.
     * Grants: Speed I (permanent while summoned).
     */
    GOTITA(
        "gotita",
        PalRarity.COMMON,
        "Gotita",
        "Grants Speed I while summoned."
    ),

    /**
     * Brillin – a glowing firefly that lights up the dark.
     * Grants: Night Vision (permanent while summoned).
     */
    BRILLIN(
        "brillin",
        PalRarity.COMMON,
        "Brillin",
        "Grants Night Vision while summoned."
    ),

    /**
     * Carbonito – a tiny coal-sprite that fuels your energy.
     * Grants: Haste I (permanent while summoned).
     */
    CARBONITO(
        "carbonito",
        PalRarity.COMMON,
        "Carbonito",
        "Grants Haste I while summoned."
    ),

    // ══════════════════════════════════════════════════════════════════════════
    // RARE PALS
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Zephyra – a feathered wind-sprite that softens your landings.
     * Grants: Feather Falling aura (Slow Falling) while summoned.
     */
    ZEPHYRA(
        "zephyra",
        PalRarity.RARE,
        "Zephyra",
        "Grants Slow Falling while summoned."
    ),

    /**
     * Fenixito – a miniature phoenix wreathed in soft flame.
     * Grants: Fire Resistance (permanent while summoned).
     */
    FENIXITO(
        "fenixito",
        PalRarity.RARE,
        "Fenixito",
        "Grants Fire Resistance while summoned."
    ),

    /**
     * Musgosin – a mossy sprite that slowly mends your wounds.
     * Grants: Regeneration I (permanent while summoned).
     */
    MUSGOSIN(
        "musgosin",
        PalRarity.RARE,
        "Musgosin",
        "Grants Regeneration I while summoned."
    ),

    // ══════════════════════════════════════════════════════════════════════════
    // EPIC PALS
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Tempesin – a storm spirit that fortifies your body.
     * Grants: +4 max hearts (attribute bonus) while summoned.
     */
    TEMPESIN(
        "tempesin",
        PalRarity.EPIC,
        "Tempesin",
        "Adds +4 max hearts while summoned."
    ),

    /**
     * Luminarix – a blazing astral tiger that empowers your strikes.
     * Grants: Strength II + Absorption (2 extra hearts) while summoned.
     */
    LUMINARIX(
        "luminarix",
        PalRarity.EPIC,
        "Luminarix",
        "Grants Strength II and Absorption while summoned."
    ),

    /**
     * Guardian Astral – an ancient guardian that shields you with magic.
     * Grants: a magic shield that absorbs the next hit; 30-second cooldown.
     */
    GUARDIAN_ASTRAL(
        "guardian_astral",
        PalRarity.EPIC,
        "Guardian Astral",
        "Activates a magic shield absorbing the next hit (30s cooldown)."
    ),

    // ══════════════════════════════════════════════════════════════════════════
    // LEGENDARY PALS
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Pegasito – a radiant winged horse.
     * Grants: mountable (right-click entity to ride) + creative-flight while riding.
     */
    PEGASITO(
        "pegasito",
        PalRarity.LEGENDARY,
        "Pegasito",
        "Rideable – grants flight while mounted."
    ),

    /**
     * Celestia – a stellar deer of boundless life.
     * Grants: +8 max hearts + Regeneration II while summoned.
     */
    CELESTIA(
        "celestia",
        PalRarity.LEGENDARY,
        "Celestia",
        "Adds +8 max hearts and Regeneration II while summoned."
    ),

    /**
     * Xolotl Rey – the divine dog of resurrection.
     * Grants: auto-resurrect the player once per real day (resets at midnight).
     */
    XOLOTL_REY(
        "xolotl_rey",
        PalRarity.LEGENDARY,
        "Xolotl Rey",
        "Resurrects you automatically once per day upon death."
    );

    // ── Fields ───────────────────────────────────────────────────────────────

    /** Lowercase string identifier – used for NBT keys, texture paths, lang keys, etc. */
    public final String id;

    /** Rarity tier of this Pal. */
    public final PalRarity rarity;

    /** Display name shown in tooltips and the Compendium. */
    public final String displayName;

    /** Short description of the power granted while the Pal is summoned. */
    public final String powerDescription;

    // ── Constructor ──────────────────────────────────────────────────────────
    PalType(String id, PalRarity rarity, String displayName, String powerDescription) {
        this.id               = id;
        this.rarity           = rarity;
        this.displayName      = displayName;
        this.powerDescription = powerDescription;
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    /**
     * Returns the lang key for this Pal's display name, e.g.
     * {@code "kawaipals.pal.gotita.name"}.
     */
    public String getNameLangKey() {
        return "kawaipals.pal." + id + ".name";
    }

    /**
     * Returns the lang key for this Pal's power description, e.g.
     * {@code "kawaipals.pal.gotita.power"}.
     */
    public String getPowerLangKey() {
        return "kawaipals.pal." + id + ".power";
    }

    /**
     * Returns the resource path to this Pal's entity texture, e.g.
     * {@code "kawaipals:textures/entity/gotita.png"}.
     */
    public String getTexturePath() {
        return "kawaipals:textures/entity/" + id + ".png";
    }

    /**
     * Look up a {@link PalType} by its {@link #id} string.
     *
     * @param id the lowercase id string stored in NBT
     * @return the matching PalType, or {@code null} if not found
     */
    public static PalType fromId(String id) {
        for (PalType type : values()) {
            if (type.id.equals(id)) return type;
        }
        return null;
    }
}
