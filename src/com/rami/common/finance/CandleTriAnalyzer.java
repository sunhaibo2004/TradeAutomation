package com.rami.common.finance;

import com.rami.constants.AllConstants;

public class CandleTriAnalyzer {
    
    public static String getPatternOfTriCandles(double[][] quadCandles) {
        String pattern = "";
        double[] c1 = quadCandles[0];
        double[] c2 = quadCandles[1];
        double[] c3 = quadCandles[2];
        
        // O H L C
        // 0 1 2 3
        
        // SPECIAL PATTERNS
        if (Math.abs(c3[0] - c3[3]) >= c3[1] * AllConstants.CANDLE_BODY_MINIMUM
                && Math.abs(c2[0] - c2[3]) <= c2[1] * AllConstants.CANDLE_BODY_MINIMUM
                && Math.abs(c1[0] - c1[3]) >= c1[1] * AllConstants.CANDLE_BODY_MINIMUM) {
            if (c1[0] > c1[3] && c3[3] >= (c1[0] + c1[3]) / 2 && c2[1] < c1[3] && c2[1] < c3[0]) {
                return pattern += "MORNING STAR";
            } else if (c1[0] < c1[3] && c3[0] <= (c1[0] + c1[3]) / 2 && c2[2] > c1[1] && c2[2] < c3[1]) {
                return pattern += "EVENING STAR";
            }
        }
        
        // 3 CANDLE PATTERNS
        if (Math.abs(c3[0] - c3[3]) >= c3[1] * AllConstants.CANDLE_BODY_MINIMUM
                && Math.abs(c2[0] - c2[3]) >= c2[1] * AllConstants.CANDLE_BODY_MINIMUM
                && Math.abs(c1[0] - c1[3]) >= c1[1] * AllConstants.CANDLE_BODY_MINIMUM) {
            if (c2[3] < c3[0] && c3[0] < c3[3] && c3[3] < c2[3]) {
                return pattern += "BULLISH HARAMI";
            } else if (c2[0] < c3[3] && c3[3] < c3[0] && c3[0] < c2[3]) {
                return pattern += "BEARISH HARAMI";
            } else if (c3[3] < c1[0] && c1[0] < c3[0] && c3[0] < c1[3] && c1[3] < c2[0] && c2[0] < c2[3]) {
                return pattern += "BEAR KICK";
            } else if (c3[0] < c2[3] && c2[3] < c3[3] && c3[3] < c2[0] && c2[0] < c1[3] && c1[3] < c1[0]) {
                return pattern += "BULL KICK";
            } else if (c1[0] < c1[3] && c2[0] < c2[3] && c3[0] < c3[3]
                    && (c3[1] - c3[3] + c3[0] - c3[2]) >= 2 * (c3[3] - c3[0])) {
                return pattern += "SHOOTING STAR";
            } else if (c1[0] > c1[3] && c2[0] > c2[3] && c3[0] > c3[3]
                    && (c3[1] + c3[3] - c3[0] - c3[2]) >= 2 * (c3[0] - c3[3])) {
                return pattern += "INVERTED HAMMER";
            }
        }
        
        // 2 CANDLE PATTERNS
        if (Math.abs(c3[0] - c3[3]) >= c3[1] * AllConstants.CANDLE_BODY_MINIMUM
                && Math.abs(c2[0] - c2[3]) >= c2[1] * AllConstants.CANDLE_BODY_MINIMUM) {
            if (c3[0] < c2[3] && c2[3] < c2[0] && c2[0] < c3[3]) {
                return pattern += "BULLISH ENGULFING";
            } else if (c3[3] < c2[0] && c2[0] < c2[3] && c2[3] < c3[0]) {
                return pattern += "BEARISH ENGULFING";
            } else if (c3[0] < c2[3] && c2[3] < c3[3] && c3[3] < c2[0]) {
                return pattern += "PIERCING PATTERN";
            } else if (c2[0] < c3[3] && c3[3] < c2[3] && c2[3] < c3[0]) {
                return pattern += "DARK CLOUD";
            }
        }
        return "UNKNOWN";
    }
}
