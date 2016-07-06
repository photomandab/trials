package com.logicnow.comparison;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;

import com.logicnow.comparison.utils.AmarilloUtils;
import com.logicnow.comparison.utils.CompUtils;

public class AmarilloTest {

	@Test
	public void test() throws Exception {
		String startDate = CompUtils.getProperty("START_DATE", "2016-05-01");
		String endDate = CompUtils.getYesterdaysDate();
		File downloaded = AmarilloUtils.getAmarilloFinanceReport(startDate, endDate);
		assertNotNull(downloaded);
		String path = downloaded.getAbsolutePath();
		System.out.println(path);
		assertTrue(path.contains("report-raw-"));
		assertTrue(path.contains(".csv"));
	}
	
}
