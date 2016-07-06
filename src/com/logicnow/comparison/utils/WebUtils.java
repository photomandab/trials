package com.logicnow.comparison.utils;

import java.io.File;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

public class WebUtils {

	public static final String DOWNLOAD_DIR_PARAM = "DOWNLOAD_DIR";

	public WebUtils() {
		super();
	}

	public static File ensureDownloadDirExists() {
		String downloadPath = System.getProperty(DOWNLOAD_DIR_PARAM);
		File downloadDir = new File(downloadPath);
		if (!downloadDir.exists()) downloadDir.mkdirs();
		return downloadDir;
	}

	public static void setElementText(WebDriver driver, By locator, String value) {
		WebElement element = driver.findElement(locator);
		if (element == null) throw new RuntimeException("Unable to find element with specified locator");
		element.clear();
		element.sendKeys(value);
	}
	
	public static void selectDropdown(WebDriver driver, By locator, String value) {
		WebElement element = driver.findElement(locator);
		if (element == null) throw new RuntimeException("Unable to find Dropdown with specified locator");
		Select select = new Select(element);
		select.selectByVisibleText(value);
	}
	
	public static void clickButton(WebDriver driver, By locator) {
		WebElement element = driver.findElement(locator);
		if (element == null) throw new RuntimeException("Unable to find element with specified locator");
		element.click();
	}

	public static WebElement waitFor(WebDriver driver, long timeoutInSeconds, By locator) {
		WebElement dynElement = (new WebDriverWait(driver, timeoutInSeconds)).until(ExpectedConditions.presenceOfElementLocated(locator));
		return dynElement;
	}
	
	public static File getLatestFilefromDownloadDir(String extension) {
		String downloadPath = System.getProperty(DOWNLOAD_DIR_PARAM);
		File dir = new File(downloadPath);
		File[] files = dir.listFiles();
		if (files == null || files.length == 0) {
			return null;
		}	
		File lastModifiedFile = files[0];
		for (int i = 1; i < files.length; i++) {
			File f = files[i];
			if (!f.getName().endsWith(extension)) continue;
			if (lastModifiedFile.lastModified() < f.lastModified()) {
				lastModifiedFile = f;
			}
		}
		return lastModifiedFile;
	}

	public static void close(WebDriver driver) throws Exception {
		driver.close();
	}

	public static WebDriver getFirefoxDriver() throws Exception {
		return getFirefoxDriver(null);
	}

	public static WebDriver getFirefoxDriver(DesiredCapabilities capabilities) throws Exception {
		DesiredCapabilities dc = DesiredCapabilities.firefox();
		if (capabilities != null) dc.merge(capabilities);
		FirefoxProfile profile = new FirefoxProfile();
		profile.setAcceptUntrustedCertificates(true);
		profile.setPreference("browser.download.folderList", 4);
		String browserDownloadDir = System.getProperty(DOWNLOAD_DIR_PARAM);
		if (browserDownloadDir == null) throw new RuntimeException("Must set property " + DOWNLOAD_DIR_PARAM);
		profile.setPreference("browser.download.dir",browserDownloadDir);
		profile.setPreference("browser.download.manager.alertOnEXEOpen", false);
		profile.setPreference("browser.helperApps.neverAsk.saveToDisk", "application/msword, application/csv, application/ris, text/csv, data:image/png, image/png, application/pdf, text/html, text/plain, application/zip, application/x-zip, application/x-zip-compressed, application/download, application/octet-stream");
		profile.setPreference("browser.download.manager.showWhenStarting", false);
		profile.setPreference("browser.download.manager.focusWhenStarting", false);
		profile.setPreference("browser.download.useDownloadDir", true);
		profile.setPreference("browser.helperApps.alwaysAsk.force", false);
		profile.setPreference("browser.download.manager.alertOnEXEOpen", false);
		profile.setPreference("browser.download.manager.closeWhenDone", true);
		profile.setPreference("browser.download.manager.showAlertOnComplete", false);
		profile.setPreference("browser.download.manager.useWindow", false);
		profile.setPreference("browser.download.panel.shown",false);
		dc.setCapability(FirefoxDriver.PROFILE, profile);
		WebDriver driver = new FirefoxDriver(dc);
		driver.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);
		return driver;
	}

	public static void wait(int msecs) {
		try {
			Thread.sleep(msecs);
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}

	public static void clickLink(WebDriver driver, String linkText) {
		driver.findElement(By.linkText(linkText)).click();
	}

}