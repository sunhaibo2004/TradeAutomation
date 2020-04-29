package com.rami.common.interfaces;

import java.util.concurrent.locks.Lock;

import org.openqa.selenium.WebDriver;

public interface IWebDriverFactory {
	public WebDriver getWebDriver();
	public Lock getReadLock();
	public Lock getWriteLock();
}
