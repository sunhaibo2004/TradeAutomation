package com.rami.common.finance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Candles<T> {
    
    public List<T> candleList = null;
    
    public Candles() {
        super();
        candleList = Collections.synchronizedList(new ArrayList<T>(3));
    }
    
    public List<T> getTriad() throws InterruptedException {
        List<T> returnCandleList = Collections.synchronizedList(new ArrayList<T>());
        synchronized (this) {
            if (candleList.size() < 3) {
                this.wait();
            }
            returnCandleList.add(candleList.remove(0));
            returnCandleList.add(candleList.get(0));
            returnCandleList.add(candleList.get(1));
        }
        return returnCandleList;
    }
    
    public void add(T element) {
        synchronized (this) {
            if (candleList.size() >= 3) {
                candleList.remove(0);
            }
            candleList.add(element);
            if (candleList.size() >= 3) {
                this.notifyAll();
            }
        }
    }
}
