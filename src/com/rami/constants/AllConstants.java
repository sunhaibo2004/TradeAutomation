package com.rami.constants;

public class AllConstants {
    
    public static final String URL                              = "https://www.24option.com";
    public static final String USRNAME                          = "<USR>";
    public static final String PASSWORD                         = "<PASS>";
    
    public static final long   WEB_DRIVER_KILL_TIME             = 5 * 60000;
    public static final long   WEB_DRIVER_WAIT_FOR_LOAD         = 4000L;
    
    public static final long   CANDLE_DURATION                  = 5 * 60000L;
    public static final double CANDLE_BODY_MINIMUM              = 0.0008d;                   //x of minimum change, 'PIP'
    public static final double MACD_THRESHHOLD                  = 0d;
    public static final long   PERSISTANCE_INTERVAL             = 60 * 60000l;
    
    public static final double RISK_PERCENTAGE_OF_TOTAL_DEPOSIT = 0.25d;
    public static final double MINIMUM_PATTERN_HITRATIO         = 0.30d;
}