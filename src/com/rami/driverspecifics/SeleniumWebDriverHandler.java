package com.rami.driverspecifics;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import com.rami.common.interfaces.IAssetOps;
import com.rami.common.interfaces.ICommonOps;
import com.rami.common.interfaces.IWebDriverFactory;
import com.rami.common.interfaces.IWorkers;
import com.rami.constants.AllConstants;

public class SeleniumWebDriverHandler implements IAssetOps, ICommonOps {
    
    private static final int     DEPOSIT_AMNT_DOLLARS = 250;
    private static final int     TOTAL_NUM_ASSETS     = 71;
    private Map<String, Integer> assetToIDMap         = new ConcurrentHashMap<String, Integer>();
    private IWebDriverFactory    webDriverFactory     = null;
    private IWorkers             workerFactory        = null;
    private WebElement           rootElement          = null;
    private final String         rootXPath            = "//div[@id='application']/div/table/tbody/tr[2]/td/table/tbody/tr/td[2]/div/table/tbody";
    private final String         assetRowXPath        = rootXPath
                                                              + "/tr[3]/td[1]/table/tbody/tr[2]/td/div/div[1]/div/table/tbody/tr[4]/td/table/tbody/tr[2]/td/table/tbody";
    private final String         highXPath            = rootXPath
                                                              + "/tr[1]/td/div/table/tbody/tr[2]/td/div/div/div/table/tbody/tr/td[1]/table/tbody/tr[2]/td/table/tbody/tr[1]/td/div";
    private final String         lowXPath             = rootXPath
                                                              + "/tr[1]/td/div/table/tbody/tr[2]/td/div/div/div/table/tbody/tr/td[1]/table/tbody/tr[2]/td/table/tbody/tr[3]/td/div";
    private final String         buyXPath             = rootXPath
                                                              + "/tr[1]/td/div/table/tbody/tr[2]/td/div/div/div/table/tbody/tr/td[2]/div/table/tbody/tr[3]/td/table/tbody/tr/td[4]/div";
    private final String         strXPathExpValues    = "/tr[%d]/td/div/div/table/tbody/tr/td[%d]/div";
    private final String         strXPathExpValueList = "/tr[%d]/td/div/div/table/tbody/tr/td[%d]/select";
    
    public SeleniumWebDriverHandler(IWebDriverFactory iWebDriverFactory, IWorkers iWorkers) {
        this.webDriverFactory = iWebDriverFactory;
        this.workerFactory = iWorkers;
    }
    
    @Override
    public void login() {
        Lock rLock = webDriverFactory.getReadLock();
        rLock.lock();
        WebElement txtPassword = webDriverFactory.getWebDriver().findElement(By.xpath("//input[@type='password']"));
        txtPassword.clear();
        txtPassword.sendKeys(AllConstants.PASSWORD);
        WebElement txtUsrName = webDriverFactory.getWebDriver().findElement(By.xpath("//input[@type='text']"));
        txtUsrName.clear();
        txtUsrName.sendKeys(AllConstants.USRNAME);
        webDriverFactory.getWebDriver().findElement(By.name("send")).click();
        rLock.unlock();
    }
    
    @Override
    public long getServerTime() {
        Lock rLock = webDriverFactory.getReadLock();
        rLock.lock();
        rLock.unlock();
        return 0;
    }
    
    @Override
    public synchronized void call(String uniqueID, double probability) {
        double lockValue = getTotalDepositValue() * probability * AllConstants.RISK_PERCENTAGE_OF_TOTAL_DEPOSIT;
        Lock wLock = webDriverFactory.getWriteLock();
        wLock.lock();
        boolean retry = false;
        int retryCount = 2;
        do {
            try {
                retry = false;
                WebElement rootElement = webDriverFactory.getWebDriver().findElement(By.xpath(assetRowXPath));
                rootElement.findElement(
                        By.xpath(assetRowXPath
                                + String.format("/tr[%d]/td/div/div/table/tbody/tr/td[%d]/div", getLatestID(uniqueID),
                                        1))).click();
                Thread.sleep(5000);
                
                rootElement.findElement(By.xpath(highXPath)).click();
                webDriverFactory.getWebDriver()
                        .findElement(By.cssSelector("input.gwt-TextBox.optionMode_amount_textBox"))
                        .sendKeys(String.valueOf(lockValue));
                //rootElement.findElement(By.xpath(buyXPath)).click();
            } catch (Exception e) {
                if (0 != --retryCount)
                    retry = true;
            }
        } while (retry);
        wLock.unlock();
    }
    
    @Override
    public synchronized void put(String uniqueID, double probability) {
        double lockValue = getTotalDepositValue() * probability * AllConstants.RISK_PERCENTAGE_OF_TOTAL_DEPOSIT;
        Lock wLock = webDriverFactory.getWriteLock();
        wLock.lock();
        boolean retry = false;
        int retryCount = 2;
        do {
            try {
                retry = false;
                WebElement rootElement = webDriverFactory.getWebDriver().findElement(By.xpath(assetRowXPath));
                rootElement.findElement(
                        By.xpath(assetRowXPath
                                + String.format("/tr[%d]/td/div/div/table/tbody/tr/td[%d]/div", getLatestID(uniqueID),
                                        1))).click();
                Thread.sleep(5000);
                rootElement.findElement(By.xpath(lowXPath)).click();
                webDriverFactory.getWebDriver()
                        .findElement(By.cssSelector("input.gwt-TextBox.optionMode_amount_textBox"))
                        .sendKeys(String.valueOf(lockValue));
                //rootElement.findElement(By.xpath(buyXPath)).click();
            } catch (Exception e) {
                if (0 != --retryCount)
                    retry = true;
            }
        } while (retry);
        wLock.unlock();
    }
    
    @Override
    public double getPointInTimeValue(String uniqueID) {
        Lock rLock = webDriverFactory.getReadLock();
        double value = 0d;
        try {
            rLock.lock();
            boolean retry = false;
            do {
                try {
                    retry = false;
                    int idnow = getLatestID(uniqueID);
                    WebElement assetValue = rootElement.findElement(By.xpath(assetRowXPath
                            + String.format(strXPathExpValues, idnow, 2)));
                    value = Double.parseDouble(assetValue.getText());
                    retry = uniqueID.equalsIgnoreCase(rootElement
                            .findElement(By.xpath(assetRowXPath + String.format(strXPathExpValues, idnow, 1)))
                            .getText().replaceAll("[^A-Za-z0-9]", "")) ? false : true;
                } catch (Exception e) {
                    // Error while value load, retry
                    //System.out.printf("Error getting asset[%s] value from web-driver.Retrying [%s].\n", uniqueID, e.getMessage());
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e1) {
                    }
                    retry = true;
                }
            } while (retry);
            return value;
        } finally {
            rLock.unlock();
        }
    }
    
    @Override
    public boolean isClosed(String uniqueID) {
        Lock rLock = webDriverFactory.getReadLock();
        try {
            rLock.lock();
            return true;
        } finally {
            rLock.unlock();
        }
    }
    
    @Override
    public List<Asset> getAllAssets() throws InterruptedException {
        rootElement = webDriverFactory.getWebDriver().findElement(By.xpath(this.assetRowXPath));
        this.webDriverFactory
                .getWebDriver()
                .findElement(
                        By.xpath("//div[@id='application']/div/table/tbody/tr[2]/td/table/tbody/tr/td[2]/div/table/tbody/tr[3]/td/table/tbody/tr[2]/td/div/div/div/table/tbody/tr/td/div/div"))
                .click();
        Thread.sleep(5000);
        List<Asset> listOfAssets = new ArrayList<Asset>();
        for (int i = 1; i < TOTAL_NUM_ASSETS; i++) {
            try {
                WebElement assetName = rootElement.findElement(By.xpath(assetRowXPath
                        + String.format(strXPathExpValues, i, 1)));
                WebElement assetValue = rootElement.findElement(By.xpath(assetRowXPath
                        + String.format(strXPathExpValues, i, 2)));
                WebElement assetRet = rootElement.findElement(By.xpath(assetRowXPath
                        + String.format(strXPathExpValues, i, 4)));
                WebElement assetExp = new Select(rootElement.findElement(By.xpath(assetRowXPath
                        + String.format(strXPathExpValueList, i, 5)))).getFirstSelectedOption();
                System.out.printf("%d) %-25s  %-6s  %-4s  %-10s\n", i, assetName.getText(), assetValue.getText(),
                        assetRet.getText(), assetExp.getText());
                Asset asset = new Asset(assetName.getText().replaceAll("[^A-Za-z0-9]", ""), this, this,
                        this.workerFactory);
                assetToIDMap.put(asset.getName(), i);
                listOfAssets.add(asset);
            } catch (Exception e) {
                System.out.printf("(%d) Not found.\n", i);
            }
        }
        this.startAssetNameToIDMapperThread(1, TOTAL_NUM_ASSETS);
        return listOfAssets;
    }
    
    public int getLatestID(String assetName) throws InterruptedException {
        int ID = assetToIDMap.get(assetName);
        if (assetName.equalsIgnoreCase(rootElement
                .findElement(By.xpath(assetRowXPath + String.format(strXPathExpValues, ID, 1))).getText()
                .replaceAll("[^A-Za-z0-9]", ""))) {
            return ID;
        }
        //System.out.printf("%s ID misplaced. Searching...\n", assetName);
        for (int i = 1; i < TOTAL_NUM_ASSETS; i++) {
            try {
                WebElement weAssetName = rootElement.findElement(By.xpath(assetRowXPath
                        + String.format(strXPathExpValues, i, 1)));
                if (assetName.equalsIgnoreCase(weAssetName.getText().replaceAll("[^A-Za-z0-9]", ""))) {
                    assetToIDMap.put(assetName, i);
                    //System.out.printf("%s new ID found.\n", assetName);
                    return i;
                }
            } catch (Exception e) {
            }
        }
        return 0;
    }
    
    public void startAssetNameToIDMapperThread(final int startIndex, final int endIndex) {
        int computedFinalIndex = endIndex;
        if (endIndex - startIndex > 10) {
            startAssetNameToIDMapperThread(startIndex + 10, endIndex);
            computedFinalIndex = startIndex + 10;
        }
        final int endfinalindex = computedFinalIndex;
        Callable<Boolean> assetNameToIDMapperThread = new Callable<Boolean>() {
            @Override
            public Boolean call() {
                try {
                    while (true) {
                        for (int i = startIndex; i < endfinalindex; i++) {
                            try {
                                WebElement weAssetName = rootElement.findElement(By.xpath(assetRowXPath
                                        + String.format(strXPathExpValues, i, 1)));
                                assetToIDMap.put(weAssetName.getText().replaceAll("[^A-Za-z0-9]", ""), i);
                            } catch (Exception e) {
                            }
                        }
                        Thread.sleep(100);
                    }
                } catch (Exception e) {
                }
                return true;
            }
        };
        this.workerFactory.submit(assetNameToIDMapperThread, 1);
    }
    
    @Override
    public long getNextPositionTime(String uniqueID) {
        
        Lock rLock = webDriverFactory.getReadLock();
        long retTime = 10 * 60000; //LET 10 MIN BE THE DEFAULT
        try {
            rLock.lock();
            boolean retry = false;
            do {
                try {
                    retry = false;
                    int idnow = getLatestID(uniqueID);
                    WebElement assetNextCallPositionAt = rootElement.findElement(By.xpath(assetRowXPath
                            + String.format(strXPathExpValueList, idnow, 5)));
                    Select timeSlots = new Select(assetNextCallPositionAt);
                    String nextOptAt = timeSlots.getFirstSelectedOption().getText();
                    if (!nextOptAt.contains("Today")) {
                        throw new Exception("Option closed for today.");
                    }
                    
                    String str[] = nextOptAt.replaceAll("[A-Za-z/ ]", "").split(":");
                    Date then = new Date();
                    then.setHours(Integer.parseInt(str[0]));
                    then.setMinutes(Integer.parseInt(str[1]));
                    retTime = then.getTime();
                    retry = uniqueID.equalsIgnoreCase(rootElement
                            .findElement(By.xpath(assetRowXPath + String.format(strXPathExpValues, idnow, 1)))
                            .getText().replaceAll("[^A-Za-z0-9]", "")) ? false : true;
                } catch (Exception e) {
                    // Error while value load, retry
                    //System.out.printf("Error getting asset[%s] value from web-driver.Retrying [%s].\n", uniqueID, e.getMessage());
                    retry = true;
                }
            } while (retry);
            return retTime;
        } finally {
            rLock.unlock();
        }
        
    }
    
    private double getTotalDepositValue() {
        Lock rLock = webDriverFactory.getReadLock();
        try {
            rLock.lock();
            return DEPOSIT_AMNT_DOLLARS;
        } finally {
            rLock.unlock();
        }
    }
}