package com.rami.common.interfaces;

import java.util.List;

import com.rami.driverspecifics.Asset;

public interface IAssetOps {
    public void call(String uniqueID, double chance);
    
    public void put(String uniqueID, double chance);
    
    public double getPointInTimeValue(String uniqueID);
    
    public boolean isClosed(String uniqueID);
    
    public long getNextPositionTime(String uniqueID);
    
    public List<Asset> getAllAssets() throws InterruptedException;
}
