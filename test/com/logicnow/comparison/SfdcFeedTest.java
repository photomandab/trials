package com.logicnow.comparison;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;

import com.logicnow.comparison.utils.RemoteUtils;

public class SfdcFeedTest {

	@Test
	public void test() throws Exception {
		File downloaded = RemoteUtils.getMostRecentSFDCFeedFile();
		assertNotNull(downloaded);
		assertTrue(downloaded.exists());
	}

}
