package com.logicnow.comparison;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;

import com.logicnow.comparison.utils.CompUtils;
import com.logicnow.comparison.utils.SalesforceUtils;

public class SalesforceTest {

	@Test
	public void testSalesforceLogin() throws Exception {
		String startDate = CompUtils.getProperty("START_DATE", "5/1/2016");
		String endDate = CompUtils.getYesterdaysDate(CompUtils.DATE_FORMAT_3);
		File downloaded = SalesforceUtils.getSalesforceReport(startDate, endDate);
		assertNotNull(downloaded);
		String path = downloaded.getAbsolutePath();
		System.out.println(path);
		assertTrue(path.contains("report"));
		assertTrue(path.contains(".csv"));
	}
	
}
