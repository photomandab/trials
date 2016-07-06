package com.logicnow.comparison.utils;

import java.io.File;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class AmarilloUtils extends WebUtils {

	public static File getAmarilloFinanceReport(String startDate, String endDate) throws Exception {
		String user = CompUtils.getProperty("USER_2");
		String pass = CompUtils.getProperty("PASS_2");
		WebDriver driver = getFirefoxDriver();
		login(driver, user, pass);
		clickLink(driver, "Finance");
		enterStartEndDate(driver, startDate, endDate);
		setReportParameters(driver);
		runReport(driver);
		waitFor(driver, 60, By.xpath("//button[contains(text(),'Raw Data')]"));
		wait(500);
		downloadRawData(driver);
		close(driver);
		return getLatestFilefromDownloadDir(".csv");
	}

	public static void login(WebDriver driver, String user, String pass) throws Exception {
		driver.get("http://reporting.logicnowlabs.com/");
		setElementText(driver, By.id("Email"), user);
		clickButton(driver, By.id("next"));
		setElementText(driver, By.id("Passwd"), pass);
		clickButton(driver, By.id("signIn"));
	}

	public static void enterStartEndDate(WebDriver driver, String startDate, String endDate) {
		setElementText(driver, By.id("startDate"), startDate);
		setElementText(driver, By.id("endDate"), endDate);
	}

	private static void setReportParameters(WebDriver driver) {
		// Remove LN Attribution Group from default Filters
		clickButton(driver, By.id("filter-remove-LN Attribution Group"));		
		// Add Fixed Product to Breakouts
		selectDropdown(driver, By.id("breakout-select"), "Fixed Product");
		clickButton(driver, By.id("addBreakout"));
		// Remove Date from Breakouts
		clickButton(driver, By.id("breakout-remove-Date"));
	}

	private static void runReport(WebDriver driver) {
		clickButton(driver, By.xpath("//button[contains(text(),'Run')]"));
	}

	private static void downloadRawData(WebDriver driver) {
		clickButton(driver, By.xpath("//button[contains(text(),'Raw Data')]"));
		wait(60000);
	}

}
