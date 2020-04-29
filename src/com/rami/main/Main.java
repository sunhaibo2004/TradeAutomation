package com.rami.main;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.firefox.internal.ProfilesIni;

import com.rami.common.interfaces.IPersist;
import com.rami.common.interfaces.IWebDriverFactory;
import com.rami.common.interfaces.IWorkers;
import com.rami.constants.AllConstants;
import com.rami.driverspecifics.Asset;
import com.rami.driverspecifics.SeleniumWebDriverHandler;
import com.rami.persistance.XPersistor;

public class Main implements IWorkers, IWebDriverFactory {
    
    private static final ReadWriteLock  webDriverLock     = new ReentrantReadWriteLock();
    private static final Lock            readLock          = webDriverLock.readLock();
    private static final Lock           writeLock         = webDriverLock.writeLock();
    private WebDriver                   webDriver         = createFirefoxDriver();
    public static final ExecutorService threadPool[]      = new ExecutorService[] { Executors.newCachedThreadPool(),Executors.newCachedThreadPool()};
    private static final IPersist       persistanceHelper = new XPersistor();
    private List<Asset>                 listOfAssets      = null;
    
    private static FirefoxDriver createFirefoxDriver() {
        writeLock.lock();
        try {
            ProfilesIni allProfiles = new ProfilesIni();
            FirefoxProfile firefoxProfile = allProfiles.getProfile("selenium");
            FirefoxDriver browser = new FirefoxDriver(firefoxProfile);
            return browser;
        } finally {
            writeLock.unlock();
        }
    }
    
    public static void main(String[] args) throws Exception {
        
        Main main = new Main();
        try {
            //main.webDriver.manage().window().setPosition(new Point(-2000, 0));
            main.webDriver.manage().window().setPosition(new Point(0, 0));
            main.webDriver.get(AllConstants.URL);
            SeleniumWebDriverHandler defaultWebDriverHandler = new SeleniumWebDriverHandler(main, main);
            defaultWebDriverHandler.login();
            Thread.sleep(7000);
            main.listOfAssets = defaultWebDriverHandler.getAllAssets();
            for (Asset a : main.listOfAssets) {
                a.startMACDComputationThreads();
                a.startTrackingAssetThread();
            }
            main.startPersistanceThread();
            Thread.sleep(Long.MAX_VALUE);
        } finally {
            System.out.printf("Closing driver...\n");
            main.webDriver.quit();
            System.out.printf("Closed driver.\n");
        }
    }
    
    @Override
    public <T> Future<T> submit(Callable<T> work, int priority) {
        return threadPool[priority % threadPool.length].submit(work);
    }
    
    @Override
    public WebDriver getWebDriver() {
        return webDriver;
    }
    
    @Override
    public Lock getReadLock() {
        return readLock;
    }
    
    @Override
    public Lock getWriteLock() {
        return writeLock;
    }
    /**
     * START THREAD WHICH SAVES OBJECT STATE EVERY <PERSISTANCE_INTERVAL>
     * MILLISECONDS
     */
    public void startPersistanceThread() {
        final Main assetHolder = this;
        Callable<Boolean> persistorThread = new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                try {
                    while (true) {
                        Thread.sleep(AllConstants.PERSISTANCE_INTERVAL);
                        for (Asset tgtAsset : assetHolder.listOfAssets) {
                            persistanceHelper.setPersistedObj(tgtAsset);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;
            }
        };
        assetHolder.threadPool[1].submit(persistorThread);
    }
}