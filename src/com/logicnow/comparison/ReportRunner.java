package com.logicnow.comparison;

import java.io.File;
import java.util.Calendar;

import com.logicnow.comparison.utils.AmarilloUtils;
import com.logicnow.comparison.utils.CompUtils;
import com.logicnow.comparison.utils.ExcelUtils;
import com.logicnow.comparison.utils.SalesforceUtils;

public class ReportRunner {
	
	public static final String DIR_TARGET = "target";	
	public static final String DIR_CONFIG = "test/data/";	
	public static final String DIR_DATA = DIR_TARGET + "/data/";	
	public static final String DIR_OUTPUT = DIR_TARGET + "/output";	
	public static final String PREFIX_AMAR = DIR_DATA + "amarillo_";
	public static final String PREFIX_SFDC = DIR_DATA + "sfdc_";
	public static final String PREFIX_FEED = DIR_DATA + "feed_";
	
	public ResultPayload run(String startDate, String endDate, String dateTime, String config, String amarillo, String sfdc, String feed) throws Exception {
		ReportComparator comparator = new ReportComparator();
		return comparator.compare(startDate, endDate, dateTime, config, amarillo, sfdc, feed);
	}
	
	public static void main(String[] args) throws Exception {
		String startDate = CompUtils.getMonthStartDate();
		String endDate = CompUtils.getYesterdaysDate();
		if (args.length > 0) {
			startDate = args[0];
			if (args.length > 1) {
				endDate = args[1];
			}
		}
		
		File sfdcDownloadedFile = SalesforceUtils.getSalesforceReport(CompUtils.getSalesforceDate(startDate), CompUtils.getSalesforceDate(endDate));
		String sfdcFilePath = sfdcDownloadedFile.getAbsolutePath();
		
		File amarilloDownloadedFile = AmarilloUtils.getAmarilloFinanceReport(startDate, endDate);
		String amarilloFilePath = amarilloDownloadedFile.getAbsolutePath();
		
		ReportRunner runner = new ReportRunner();
		String dateTime = CompUtils.TIME_FORMAT.format(Calendar.getInstance().getTime()).replace(":", "-");
		ResultPayload r1 = runner.run(startDate, endDate, dateTime, 
				DIR_CONFIG + "RM/config_1.json", 
				amarilloFilePath, 
				sfdcFilePath, 
				PREFIX_FEED + endDate + ".csv");
		ResultPayload r2 = runner.run(startDate, endDate, dateTime, 
				DIR_CONFIG + "RMIT/config_1.json", 
				amarilloFilePath, 
				sfdcFilePath, 
				PREFIX_FEED + endDate + ".csv");
		ResultPayload r3 = runner.run(startDate, endDate, dateTime, 
				DIR_CONFIG + "BU/config_1.json", 
				amarilloFilePath, 
				sfdcFilePath, 
				PREFIX_FEED + endDate + ".csv");
		ResultPayload r4 = runner.run(startDate, endDate, dateTime, 
				DIR_CONFIG + "MM/config_1.json", 
				amarilloFilePath, 
				sfdcFilePath, 
				PREFIX_FEED + endDate + ".csv");
		ResultPayload r5 = runner.run(startDate, endDate, dateTime, 
				DIR_CONFIG + "MMIT/config_1.json", 
				amarilloFilePath, 
				sfdcFilePath, 
				PREFIX_FEED + endDate + ".csv");
		File tmpOutputDir = new File(DIR_OUTPUT);
		File outputDir = new File(tmpOutputDir, dateTime);
		if (!outputDir.exists()) outputDir.mkdirs();
		File excelFile = new File(outputDir, "analysis_" + startDate + "_" + endDate + ".xls");
		ExcelUtils.writeExcelFile(excelFile, false, r1, r2, r3, r4, r5);
		System.out.println(CompUtils.generateCommentedSQL(r1, r2, r3, r4, r5));
	}

}
