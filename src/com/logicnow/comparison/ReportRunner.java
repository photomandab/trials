package com.logicnow.comparison;

import java.io.File;
import java.util.Calendar;

public class ReportRunner {
	
	public static final String START_DATE = "2016-05-10";
	public static final String END_DATE = "2016-05-19";
	public static final String DIR_TARGET = "target";	
	public static final String DIR_CONFIG = "test/data/";	
	public static final String DIR_DATA = DIR_TARGET + "/data/";	
	public static final String DIR_OUTPUT = DIR_TARGET + "/output";	
	public static final String PREFIX_AMARILLO = DIR_DATA + "amarillo_";
	public static final String PREFIX_SFDC = DIR_DATA + "sfdc_";
	public static final String PREFIX_FEED = DIR_DATA + "feed_";
	public static final String PREFIX_ANALYSIS = "analysis_";
	
	public ResultPayload run(String startDate, String endDate, String dateTime, String config, String amarillo, String sfdc, String feed) throws Exception {
		ReportComparator comparator = new ReportComparator();
		return comparator.compare(startDate, endDate, dateTime, config, amarillo, sfdc, feed);
	}

	public static void main(String[] args) throws Exception {
		ReportRunner runner = new ReportRunner();
		String dateTime = CompUtils.TIME_FORMAT.format(Calendar.getInstance().getTime()).replace(":", "-");
		ResultPayload r1 = runner.run(START_DATE, END_DATE, dateTime, 
				DIR_CONFIG + "RM/config_1.json", 
				PREFIX_AMARILLO + START_DATE + "_" + END_DATE + ".csv", 
				PREFIX_SFDC + START_DATE + "_" + END_DATE + ".csv", 
				PREFIX_FEED + START_DATE + "_" + END_DATE + ".csv");
		ResultPayload r2 = runner.run(START_DATE, END_DATE, dateTime, 
				DIR_CONFIG + "RMIT/config_1.json", 
				PREFIX_AMARILLO + START_DATE + "_" + END_DATE + ".csv", 
				PREFIX_SFDC + START_DATE + "_" + END_DATE + ".csv", 
				PREFIX_FEED + START_DATE + "_" + END_DATE + ".csv");
		ResultPayload r3 = runner.run(START_DATE, END_DATE, dateTime, 
				DIR_CONFIG + "BU/config_1.json", 
				PREFIX_AMARILLO + START_DATE + "_" + END_DATE + ".csv", 
				PREFIX_SFDC + START_DATE + "_" + END_DATE + ".csv", 
				PREFIX_FEED + START_DATE + "_" + END_DATE + ".csv");
		File tmpOutputDir = new File(DIR_OUTPUT);
		File outputDir = new File(tmpOutputDir, dateTime);
		if (!outputDir.exists()) outputDir.mkdirs();
		File excelFile = new File(outputDir, PREFIX_ANALYSIS + START_DATE + "_" + END_DATE + ".xls");
		CompUtils.writeExcelFile(excelFile, r1, r2, r3);
	}

}
