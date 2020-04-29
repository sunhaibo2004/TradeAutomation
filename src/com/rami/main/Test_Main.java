package com.rami.main;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Lock;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.firefox.internal.ProfilesIni;

import com.rami.common.interfaces.IAssetOps;
import com.rami.common.interfaces.ICommonOps;
import com.rami.common.interfaces.IWebDriverFactory;
import com.rami.common.interfaces.IWorkers;
import com.rami.constants.AllConstants;
import com.rami.driverspecifics.Asset;

public class Test_Main implements IAssetOps, IWorkers, ICommonOps, IWebDriverFactory {
    
    private WebDriver    webDriver        = null;
    private final String rootXPathExp     = "//div[@id='application']/div/table/tbody/tr[2]/td/table/tbody/tr/td[2]/div/table/tbody";
    private final String assetrowXPathExp = rootXPathExp
                                                  + "/tr[3]/td[1]/table/tbody/tr[2]/td/div/div[1]/div/table/tbody/tr[4]/td/table/tbody/tr[2]/td/table/tbody";
    private final String highXPath        = rootXPathExp
                                                  + "/tr[1]/td/div/table/tbody/tr[2]/td/div/div/div/table/tbody/tr/td[1]/table/tbody/tr[2]/td/table/tbody/tr[1]/td/div";
    private final String lowXPath         = rootXPathExp
                                                  + "/tr[1]/td/div/table/tbody/tr[2]/td/div/div/div/table/tbody/tr/td[1]/table/tbody/tr[2]/td/table/tbody/tr[3]/td/div";
    private final String buy              = rootXPathExp
                                                  + "/tr[1]/td/div/table/tbody/tr[2]/td/div/div/div/table/tbody/tr/td[2]/div/table/tbody/tr[3]/td/table/tbody/tr/td[4]/div";
    
    private final String xpath2           = "/tr[%d]/td/div/div/table/tbody/tr/td[%d]/div";
    private final String xpath3           = "/tr[%d]/td/div/div/table/tbody/tr/td[%d]/select";
    
    public static void main(String[] args) throws Exception {
        
        Test_Main main = new Test_Main();
        try {
            main.webDriver = createFirefoxDriver();
            main.webDriver.get("https://www.24option.com/24option/#Trade");
            Thread.sleep(10000);
            WebElement rootElement = main.webDriver.findElement(By.xpath(main.assetrowXPathExp));
            rootElement.findElement(
                    By.xpath(main.assetrowXPathExp
                            + String.format("/tr[%d]/td/div/div/table/tbody/tr/td[%d]/div", 1, 1))).click();
            Thread.sleep(5000);
            rootElement.findElement(By.xpath(main.highXPath)).click();
            main.webDriver.findElement(By.cssSelector("input.gwt-TextBox.optionMode_amount_textBox")).sendKeys("25678");
            rootElement.findElement(By.xpath(main.buy)).click();
            main.webDriver.close();
        } finally {
            main.webDriver.close();
        }
    }
    
    private static FirefoxDriver createFirefoxDriver() {
        ProfilesIni allProfiles = new ProfilesIni();
        FirefoxProfile firefoxProfile = allProfiles.getProfile("selenium");
        FirefoxDriver browser = new FirefoxDriver(firefoxProfile);
        return browser;
    }
    
    @Override
    public <T> Future<T> submit(Callable<T> work, int priority) {
        try {
            work.call();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }
    
    @Override
    public WebDriver getWebDriver() {
        return webDriver;
    }
    
    @Override
    public Lock getReadLock() {
        return null;
    }
    
    @Override
    public void login() {
    }
    
    @Override
    public long getServerTime() {
        // TODO Auto-generated method stub
        return 0;
    }
    
    @Override
    public double getPointInTimeValue(String uniqueID) {
        return 1234.56;
    }
    
    @Override
    public boolean isClosed(String uniqueID) {
        // TODO Auto-generated method stub
        return false;
    }
    
    @Override
    public List<Asset> getAllAssets() throws InterruptedException {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public long getNextPositionTime(String uniqueID) {
        // TODO Auto-generated method stub
        return 0;
    }
    
    @Override
    public void call(String uniqueID, double chance) {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public void put(String uniqueID, double chance) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public Lock getWriteLock() {
        // TODO Auto-generated method stub
        return null;
    }
}
