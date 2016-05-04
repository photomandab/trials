package com.logicnow.comparison;

import java.io.File;
import java.util.Calendar;

public class ReportRunner {
	
	public static final String DATE = "2016-05-03";
	public static final String DIR_TARGET = "target";	
	public static final String DIR_CONFIG = "test/data/";	
	public static final String DIR_DATA = DIR_TARGET + "/data/";	
	public static final String DIR_OUTPUT = DIR_TARGET + "/output";	
	public static final String PREFIX_AMARILLO = DIR_DATA + "amarillo_";
	public static final String PREFIX_SFDC = DIR_DATA + "sfdc_";
	public static final String PREFIX_FEED = DIR_DATA + "feed_";
	public static final String PREFIX_ANALYSIS = "analysis_";
	
	public ResultPayload run(String d, String dateTime, String config, String amarillo, String sfdc, String feed) throws Exception {
		ReportComparator comparator = new ReportComparator();
		return comparator.compare(d, dateTime, config, amarillo, sfdc, feed);
	}

	public static void main(String[] args) throws Exception {
		ReportRunner runner = new ReportRunner();
		String dateTime = CompUtils.TIME_FORMAT.format(Calendar.getInstance().getTime()).replace(":", "-");
		ResultPayload r1 = runner.run(DATE, dateTime, DIR_CONFIG + "RM/config_1.json", PREFIX_AMARILLO + DATE + ".csv", PREFIX_SFDC + DATE + ".csv", PREFIX_FEED + DATE + ".csv");
		ResultPayload r2 = runner.run(DATE, dateTime, DIR_CONFIG + "RMIT/config_1.json", PREFIX_AMARILLO + DATE + ".csv", PREFIX_SFDC + DATE + ".csv", PREFIX_FEED + DATE + ".csv");
		ResultPayload r3 = runner.run(DATE, dateTime, DIR_CONFIG + "BU/config_1.json", PREFIX_AMARILLO + DATE + ".csv", PREFIX_SFDC + DATE + ".csv", PREFIX_FEED + DATE + ".csv");
		File tmpOutputDir = new File(DIR_OUTPUT);
		File outputDir = new File(tmpOutputDir, dateTime);
		if (!outputDir.exists()) outputDir.mkdirs();
		File excelFile = new File(outputDir, PREFIX_ANALYSIS + DATE + ".xls");
		CompUtils.writeExcelFile(excelFile, DATE, new ResultPayload[] { r1, r2, r3 });
	}

}
