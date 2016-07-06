package com.logicnow.comparison.utils;

import java.io.File;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.Select;

public class SalesforceUtils extends WebUtils {

	public static void salesforceLogin(WebDriver driver, String user, String pass) throws Exception {
		driver.get("https://logicnow.my.salesforce.com/");
		wait(5000);
		setElementText(driver, By.id("userNameInput"), user);
		setElementText(driver, By.id("passwordInput"), pass);
		clickButton(driver, By.id("submitButton"));
		wait(5000);
	}

	public static void runReport(WebDriver driver, String reportId, String startDate, String endDate) throws Exception {
		driver.get("https://logicnow.my.salesforce.com/" + reportId);
		wait(5000);
		setElementText(driver, By.id("quarter_s"), startDate);
		setElementText(driver, By.id("quarter_e"), endDate);
		clickButton(driver, By.name("run"));
		wait(10000);
		clickButton(driver, By.name("csvsetup"));
		Select select = new Select(driver.findElement(By.id("xf")));
		select.selectByValue("localecsv");
		clickButton(driver, By.name("export"));
		wait(20000);
	}

	public static File getSalesforceReport(String startDate, String endDate) throws Exception {
		WebDriver driver = SalesforceUtils.getFirefoxDriver();
		String user = CompUtils.getProperty("USER_1");
		String pass = CompUtils.getProperty("PASS_1");
		salesforceLogin(driver, user, pass);
		runReport(driver, "00O500000046Yft", startDate, endDate);
		close(driver);
		return getLatestFilefromDownloadDir(".csv");
	}

}

