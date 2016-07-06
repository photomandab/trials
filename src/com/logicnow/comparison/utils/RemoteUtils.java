package com.logicnow.comparison.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import com.google.common.collect.Lists;

public class RemoteUtils {

	public static File getMostRecentSFDCFeedFile() throws Exception {
		String remoteFilename = RemoteUtils.getMostRecentRemoteFileName();
		return RemoteUtils.retrieveRemoteFile(remoteFilename);
	}

	public static String getMostRecentRemoteFileName() throws Exception {
		// ssh -F ~/.ssh/config ubuntu@amarillo ls -Art /opt/amarillo/data/sfdc_csv/ | tail -n 1
		List<String> params = Lists.newArrayList();
	    params.add("/usr/bin/ssh");
	    params.add("-F");
	    params.add("/Users/davidbigwood/.ssh/config");
	    params.add("ubuntu@amarillo");
	    params.add("ls -Art /opt/amarillo/data/sfdc_csv/ | tail -n 1");
	    Process process = new ProcessBuilder(params).start();
	    InputStream is = process.getInputStream();
	    InputStreamReader isr = new InputStreamReader(is);
	    BufferedReader br = new BufferedReader(isr);
	    String line;
	    StringBuilder buffy = new StringBuilder();
	    while ((line = br.readLine()) != null) {
	      buffy.append(line);
	    }
	    process.waitFor();
		return buffy.toString().trim();
	}
	
	public static File retrieveRemoteFile(String remoteFilename) throws Exception {
		// scp -F ~/.ssh/config ubuntu@amarillo:/opt/amarillo/data/sfdc_csv/Rose_-_2016-07-05_19-55-11.csv ~/Downloads/latest.csv
		List<String> params = Lists.newArrayList();
	    params.add("/usr/bin/scp");
	    params.add("-F");
	    params.add("/Users/davidbigwood/.ssh/config");
	    params.add("ubuntu@amarillo:/opt/amarillo/data/sfdc_csv/" + remoteFilename);
	    params.add("/Users/davidbigwood/Downloads/" + remoteFilename);
	    Process process = new ProcessBuilder(params).start();
	    InputStream is = process.getInputStream();
	    InputStreamReader isr = new InputStreamReader(is);
	    BufferedReader br = new BufferedReader(isr);
	    String line;
	    while ((line = br.readLine()) != null) {
	      System.out.println(line);
	    }
	    process.waitFor();
	    return getLatestFilefromDownloadDir("/Users/davidbigwood/Downloads", ".csv");

	}
	
	public static File getLatestFilefromDownloadDir(String downloadPath, String extension) {
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

}
