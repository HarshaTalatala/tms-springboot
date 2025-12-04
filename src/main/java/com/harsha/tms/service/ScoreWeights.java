package com.harsha.tms.service;

/**
 * Constants for bid scoring weight calculation.
 */
public final class ScoreWeights {

    private ScoreWeights() {
        // Utility class
    }

    /**
     * Weight for price factor in bid scoring (70%).
     * Lower price gets higher score.
     */
    public static final double PRICE_WEIGHT = 0.7;

    /**
     * Weight for rating factor in bid scoring (30%).
     * Higher rating gets higher score.
     */
    public static final double RATING_WEIGHT = 0.3;

    /**
     * Maximum rating value for normalization (5 stars).
     */
    public static final double MAX_RATING = 5.0;
}
