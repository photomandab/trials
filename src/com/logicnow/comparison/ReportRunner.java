package com.logicnow.comparison;

import java.io.File;
import java.util.Calendar;

import com.logicnow.comparison.utils.AmarilloUtils;
import com.logicnow.comparison.utils.CompUtils;
import com.logicnow.comparison.utils.ExcelUtils;
import com.logicnow.comparison.utils.RemoteUtils;
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
		
		File amarilloDownloadedFile = AmarilloUtils.getAmarilloFinanceReport(startDate, endDate);
		String amarilloFilePath = amarilloDownloadedFile.getAbsolutePath();
		
		File sfdcDownloadedFile = SalesforceUtils.getSalesforceReport(CompUtils.getSalesforceDate(startDate), CompUtils.getSalesforceDate(endDate));
		String sfdcFilePath = sfdcDownloadedFile.getAbsolutePath();
		
		File sfdcFeedFile = RemoteUtils.getMostRecentSFDCFeedFile();
		String sfdcFeedFilePath = sfdcFeedFile.getAbsolutePath();
		
		ReportRunner runner = new ReportRunner();
		String dateTime = CompUtils.TIME_FORMAT.format(Calendar.getInstance().getTime()).replace(":", "-");
		ResultPayload r1 = runner.run(startDate, endDate, dateTime, 
				DIR_CONFIG + "config_RM.json", 
				amarilloFilePath, 
				sfdcFilePath, 
				sfdcFeedFilePath);
		ResultPayload r2 = runner.run(startDate, endDate, dateTime, 
				DIR_CONFIG + "config_RMIT.json", 
				amarilloFilePath, 
				sfdcFilePath, 
				sfdcFeedFilePath);
		ResultPayload r3 = runner.run(startDate, endDate, dateTime, 
				DIR_CONFIG + "config_BU.json", 
				amarilloFilePath, 
				sfdcFilePath, 
				sfdcFeedFilePath);
		ResultPayload r4 = runner.run(startDate, endDate, dateTime, 
				DIR_CONFIG + "config_MM.json", 
				amarilloFilePath, 
				sfdcFilePath, 
				sfdcFeedFilePath);
		ResultPayload r5 = runner.run(startDate, endDate, dateTime, 
				DIR_CONFIG + "config_MMIT.json", 
				amarilloFilePath, 
				sfdcFilePath, 
				sfdcFeedFilePath);
		ResultPayload r6 = runner.run(startDate, endDate, dateTime, 
				DIR_CONFIG + "config_RI.json", 
				amarilloFilePath, 
				sfdcFilePath, 
				sfdcFeedFilePath);
		File tmpOutputDir = new File(DIR_OUTPUT);
		File outputDir = new File(tmpOutputDir, dateTime);
		if (!outputDir.exists()) outputDir.mkdirs();
		File excelFile = new File(outputDir, "analysis_" + startDate + "_" + endDate + ".xls");
		ExcelUtils.writeExcelFile(excelFile, false, r1, r2, r3, r4, r5, r6);
		System.out.println(CompUtils.generateCommentedSQL(r1, r2, r3, r4, r5, r6));
	}

}
