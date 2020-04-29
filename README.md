# TradeAutomation
Automated stock and commodity trading framework to track, analyze and place orders based on  MACD, candle stick charting and user configured risk-parameters

Abstract
There are large corporations which employ computer algorithms to make trading decisions to initiate transactions based on the data that is obtained electronically, much before human traders even begin processing the information they observe. Many such strategies such as Algorithmic Trading / High Frequency Trading have resulted in monumental changes of the market already, so much so as to be part of public debates of their efficacy and the risks they pose. This paper focuses on the technical aspects of a trading solution which operates completely automatically.
Problem statement
Almost all markets are open only on weekdays, and are active in a short period which tends to overlap with the working hours of people for whom trading is not the primary occupation. Trading is also an extremely stressful task. The psychological gains made by a winning trade involve the same risks as the psychological losses made by a losing trade. Human traders tend to be biased due to such experiences. This is the oft quoted selling point for automated trading. But the automated traders’ world is a mysterious and hidden world. Seldom do you get to hear about anyone doing it. The intention of this submission is to use the stage provided by Parasparam to bring together such individuals.
Our solution
There have been many attempts to ‘beat’ the market with a wide variety of solutions, such as incorporating different trading strategies, machine learning and data mining. This paper does not endorse any trading strategy that might be ‘right’, but aims only to show one such fully working solution as to how it can be implemented and the technical aspects of that solution.
Evidence the solution works
A complete working prototype of the project has been built at time of submission of the paper. The trading system does the following tasks (and thereby freeing the user):
1. Login to the brokerage account of the user using his/her credentials.
2. Track for price movements patterns (approaches used mentioned below briefly).
3. Based on the user-configured risks (the only thing the user has to do), places orders.
4. Tracks the order(s) placed.
 The prototype uses two strategies that are well-respected and employed by a multitude of traders:
1. Candlestick charting: Candlesticks reflect the impact of investors' emotions on security prices and are used by technical analysts to determine when to enter and exit trades.

2. MACD (Moving Average Convergence Divergence): Is a trend-following momentum indicator that shows the relationship between two moving averages of prices. The MACD is calculated by subtracting the 26-day exponential moving average (EMA) from the 12-day EMA. A nine-day EMA of the MACD, called the "signal line", is then plotted on top of the MACD, functioning as a trigger for buy and sell signals. 
Class diagram of prototype:

Description of components:
1. Asset: Objects of this class is specific to one asset. Tracks price range, does candle charting and analysis, computes MACD. It is also responsible for placing orders if both MACD analysis and the candle pattern analysis concur to an agreeable result.
2. XPersistor: Implements persistence logic for the components involved (in this case, to XML files).
3. SeleniumWebDriverHandler: This is the main class which is specific to the web driver used. It is responsible for actions like; login on behalf of the user to the broker site, getting the server time, getting a list of assets provided by the broker site and, placing orders for a commodity.
4. Candle: An object of this class holds one OHLC (Open-High-Low-Close) value set.
5. CandleTriAnalyzer: Analyses a set of candles for valid patterns.
6. AllConstants: Configurable params set here (candle stick duration, minimum price change to track, MACD threshold, durations, risks of total deposit in one transaction etc.)

Competitive approaches
As mentioned earlier, there have been many attempts at algorithmic trading since the late 1980s, but they are shrouded in secrecy and it is next to impossible to discuss them for the same reason.
Current status
A prototype of the solution has been developed and is ready to be demoed.
Next steps
The next ideal steps in improving the solution would be to:
1. Employ devices such as NLP (Neuro-linguistic programming) to gather data based on social feeds such as facebook and twitter, so as to (hopefully) use the analysis to execute trades when the market reacts.
2. Scale-up to handle multiple users.
References
http://www.investopedia.com
http://stockcharts.com
http://www.onlinetradingconcepts.com/
http://www.economist.com/node/5475381