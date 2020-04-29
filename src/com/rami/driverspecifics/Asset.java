package com.rami.driverspecifics;

import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.rami.common.finance.CandleTriAnalyzer;
import com.rami.common.finance.Candles;
import com.rami.common.finance.DecisionType;
import com.rami.common.interfaces.IAssetOps;
import com.rami.common.interfaces.ICommonOps;
import com.rami.common.interfaces.IPersist;
import com.rami.common.interfaces.IWorkers;
import com.rami.constants.AllConstants;
import com.rami.persistance.XPersistor;

public final class Asset {
    private final String              name;
    private final Map<String, Long[]> patternWinLoose = Collections.synchronizedMap(new HashMap<String, Long[]>());
    private double                    ema26           = 0d;
    private double                    ema12           = 0d;
    private double                    ema9            = 0d;
    private transient double          maxVariance     = 0d;
    private transient ICommonOps      iCommonOps      = null;
    private transient IAssetOps       iAssetOps       = null;
    private transient IWorkers        iWorkers        = null;
    
    public String getName() {
        return name;
    }
    
    public Asset(String name, ICommonOps icop, IAssetOps iassetop, IWorkers iWorkers) {
        super();
        this.name = name;
        this.iCommonOps = icop;
        this.iAssetOps = iassetop;
        this.iWorkers = iWorkers;
        try {
            IPersist persistanceHelper = new XPersistor();
            Asset previous = persistanceHelper.getPersistedObj(name);
            this.ema12 = previous.ema12;
            this.ema26 = previous.ema26;
            this.ema9 = previous.ema9;
            this.patternWinLoose.putAll(previous.patternWinLoose);
        } catch (FileNotFoundException fnfe) {
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /*
     * FUNCTIONS TO COMPUTE AN ASSET'S MACD
     */
    private synchronized void recomputeEma26(long minutesPassed, double ema26) {
        this.ema26 = calculateEMA(ema26, minutesPassed, this.ema26);
    }
    
    private synchronized void recomputeEma12(long minutesPassed, double ema12) {
        this.ema12 = calculateEMA(ema12, minutesPassed, this.ema12);
    }
    
    private synchronized void recomputeEma9(long minutesPassed) {
        this.ema9 = calculateEMA(getMACD(), minutesPassed, this.ema9);
    }
    
    private double calculateEMA(double priceNow, long numberOfMinutes, double priceThen) {
        double k = 2.0 / (numberOfMinutes + 1);
        return priceNow * k + priceThen * (1 - k);
    }
    
    public synchronized double getMACD() {
        return ema12 - ema26;
    }
    
    public void startMACDComputationThreads() {
        final Asset asset = this;
        Callable<Boolean> macd9_12_26Watcher = new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                try {
                    long minutesPassed = 0l;
                    while (true) {
                        Thread.sleep(1 * 60000);
                        minutesPassed++;
                        if (0 == minutesPassed % 9 && 0 != asset.ema26) {
                            asset.recomputeEma9(minutesPassed);
                        }
                        if (0 == minutesPassed % 12) {
                            asset.recomputeEma12(minutesPassed, asset.iAssetOps.getPointInTimeValue(asset.getName()));
                        }
                        if (0 == minutesPassed % 26) {
                            asset.recomputeEma26(minutesPassed, asset.iAssetOps.getPointInTimeValue(asset.getName()));
                        }
                        double varianceNow = asset.getMACD() - asset.ema9;
                        
                        if ((varianceNow > 0 && asset.maxVariance > 0) || (varianceNow < 0 && asset.maxVariance < 0)) {
                            if (Math.abs(varianceNow) > Math.abs(asset.maxVariance)) {
                                asset.maxVariance = varianceNow;
                            }
                        } else {
                            asset.maxVariance = varianceNow;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;
            }
        };
        this.iWorkers.submit(macd9_12_26Watcher, 0);
    }
    
    // //////////////////////////////////////////////////////////////////////////////////
    /*
     * FUNCTIONS TO COMPUTE AN ASSETS CANDLE PATTERN
     */
    public double getCandlePatternUpOrDownRatio(String pattern) {
        if (pattern == null || pattern.equalsIgnoreCase("UNKNOWN")) {
            return 0d;
        }
        
        if (patternWinLoose.containsKey(pattern)) {
            Long[] upDown = patternWinLoose.get(pattern);
            long wentUp = upDown[0];
            long wentDown = upDown[1];
            if (wentUp > wentDown) {
                return (wentUp + 1) / (wentUp + wentDown + 1);
            } else if (wentUp < wentDown) {
                return -(wentDown + 1) / (wentUp + wentDown + 1);
            } else {
                return 0d;
            }
        }
        return 0d;
    }
    
    private double[] getCandle(long candleDurationinMs) throws Exception {
        
        double open = 0d, high = 0d, low = Double.MAX_VALUE, close = 0d;
        long now = System.currentTimeMillis();
        long toStop = now + candleDurationinMs;
        
        while (System.currentTimeMillis() < toStop) {
            Double dblVal = iAssetOps.getPointInTimeValue(this.getName());
            open = open == 0 ? dblVal : open;
            high = high >= dblVal ? high : dblVal;
            close = dblVal == 0 ? close : dblVal;
            low = low > close ? close : low;
            Thread.sleep(100);
        }
        System.out.printf("%-30s %-20s %-6.3f %-6.3f %-6.3f %-6.3f\n", new Date(), this.getName(), open, high, low,
                close);
        return new double[] { open, high, low, close };
    }
    
    // //////////////////////////////////////////////////////////////////////////////////
    /*
     * USE MACD AND CANDLE PATTERN TO MAKE THE INVESTMENT
     */
    public synchronized void startTrackingAssetThread() throws Exception {
        final Candles<double[]> candleList = new Candles<double[]>();
        final Asset asset = this;
        Callable<Boolean> candleSeeker = null;
        Callable<Boolean> candleMaker = null;
        Future<?> candleMakerTask = null;
        Future<?> candleSeekerTask = null;
        
        try {
            // Producer
            candleMaker = new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    try {
                        while (true) {
                            double[] candleNow = getCandle(AllConstants.CANDLE_DURATION);
                            candleList.add(candleNow);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return true;
                }
            };
            candleMakerTask = iWorkers.submit(candleMaker, 0);
            
            // Consumer
            candleSeeker = new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    try {
                        while (true) {
                            List<double[]> candleSetToBeAnalyzed = candleList.getTriad();
                            
                            double[] candle_1 = candleSetToBeAnalyzed.remove(0);
                            double[] candle_2 = candleSetToBeAnalyzed.remove(0);
                            double[] candle_3 = candleSetToBeAnalyzed.remove(0);
                            final String pattern = CandleTriAnalyzer.getPatternOfTriCandles(new double[][] { candle_1,
                                    candle_2, candle_3 });
                            if (null == pattern || pattern.isEmpty() || pattern.equalsIgnoreCase("UNKNOWN")) {
                                continue;
                            }
                            System.out.println("PATTERN : " + pattern + " found @ " + asset.getName());
                            double callOrPutProbability = asset.getCandlePatternUpOrDownRatio(pattern);
                            DecisionType toDo = DecisionType.WAIT;
                            if (Math.abs(callOrPutProbability) >= AllConstants.MINIMUM_PATTERN_HITRATIO) {
                                toDo = callOrPutProbability > 0 ? DecisionType.CALL : DecisionType.PUT;
                            }
                            double varianceFromMACD = getMACD() - asset.ema9;
                            switch (toDo) {
                            case WAIT:
                                break;
                            case PUT:
                                if (varianceFromMACD < asset.maxVariance) {
                                    asset.iAssetOps.put(asset.getName(), -callOrPutProbability);
                                }
                                break;
                            case CALL:
                                if (varianceFromMACD > asset.maxVariance) {
                                    asset.iAssetOps.call(asset.getName(), callOrPutProbability);
                                }
                                break;
                            }
                            
                            final double valueBefore = candle_3[3];
                            final DecisionType decisionTaken = toDo;
                            Callable<Double> patternWatcher = new Callable<Double>() {
                                @Override
                                public Double call() throws Exception {
                                    if (!asset.patternWinLoose.containsKey(pattern)) {
                                        asset.patternWinLoose.put(pattern, new Long[] { 0l, 0l });
                                    }
                                    Long[] upOrDown = asset.patternWinLoose.get(pattern);
                                    Thread.sleep(asset.iAssetOps.getNextPositionTime(asset.getName())
                                            - System.currentTimeMillis());
                                    double valueNow = asset.iAssetOps.getPointInTimeValue(asset.name);
                                    System.out.printf("Pattern validation for [%s], with MACD-9[%f] in progress...\n",
                                            asset.getName(), asset.getMACD() - asset.ema9);
                                    
                                    if (valueNow != 0) {
                                        if (valueBefore < valueNow) {
                                            if (decisionTaken == DecisionType.CALL) {
                                                System.out.printf("Won !!  [%s]\n", asset.getName());
                                            } else if (decisionTaken == DecisionType.PUT) {
                                                System.out.printf("Lost    [%s]\n", asset.getName());
                                            }
                                            asset.patternWinLoose.put(pattern,
                                                    new Long[] { ++upOrDown[0], upOrDown[1] });
                                        } else if (valueBefore > valueNow) {
                                            if (decisionTaken == DecisionType.PUT) {
                                                System.out.printf("Won !!  [%s]\n", asset.getName());
                                            } else if (decisionTaken == DecisionType.CALL) {
                                                System.out.printf("Lost    [%s]\n", asset.getName());
                                            }
                                            asset.patternWinLoose.put(pattern,
                                                    new Long[] { upOrDown[0], ++upOrDown[1] });
                                        }
                                    }
                                    System.out.printf("Pattern validation for [%s] complete.\n", asset.getName());
                                    return valueNow;
                                }
                            };
                            iWorkers.submit(Executors.privilegedCallable(patternWatcher), 1);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return true;
                }
            };
            
            candleSeekerTask = iWorkers.submit(candleSeeker, 0);
        } finally {
        }
    }
}