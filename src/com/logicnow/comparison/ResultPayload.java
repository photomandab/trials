package com.logicnow.comparison;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.Sets;
import com.logicnow.comparison.utils.CompUtils;
import com.logicnow.comparison.utils.CompUtils.CSVRecord;

public class ResultPayload {

	private String product;

	private Map<String, List<CSVRecord>> amarilloMap;
	private Map<String, List<CSVRecord>> amarilloAllMap;
	private Map<String, List<CSVRecord>> sfdcMap;
	private Map<String, List<CSVRecord>> sfdcAllMap;
	private Map<String, List<CSVRecord>> feedMap;
	
	private Set<String> combined = Sets.newHashSet();
	private Set<String> both = Sets.newHashSet();
	private Set<String> amarilloOnly = Sets.newHashSet();
	private Set<String> sfdcOnly = Sets.newHashSet();
	private Set<String> bothValid = Sets.newHashSet();
	private Set<String> neitherValid = Sets.newHashSet();
	private Set<String> amarilloValid = Sets.newHashSet();
	private Set<String> sfdcValid = Sets.newHashSet();
	private Set<String> sfdcDupes = Sets.newHashSet();
	private Set<String> mismatchValidity = Sets.newHashSet();

	private Pair<String[], List<CompUtils.CSVRecord>> amarilloRecords;
	private Pair<String[], List<CompUtils.CSVRecord>> amarilloAllRecords;
	private Pair<String[], List<CompUtils.CSVRecord>> sfdcRecords;
	private Pair<String[], List<CompUtils.CSVRecord>> sfdcAllRecords;
	private Pair<String[], List<CompUtils.CSVRecord>> feedRecords;

	private Pair<ComparatorConfig, ComparatorConfig> configs;

	private String startDate;
	private String endDate;
	private String configFile;
	private String amarilloFile;
	private String sfdcFile;
	private String feedFile;

	public ResultPayload(String startDate, String endDate, String configFile, String amarilloFile, String sfdcFile, String feedFile) {
		this.startDate = startDate;
		this.endDate = endDate;
		this.configFile = configFile;
		this.amarilloFile = amarilloFile;
		this.sfdcFile = sfdcFile;
		this.feedFile = feedFile;
	}
	
	public Date getStartDate() { return parseDate(startDate); }
	public Date getEndDate() { return parseDate(endDate); }
	private Date parseDate(String d) { return CompUtils.parseDate(d); }

	public Set<String> getBoth() { return both; }
	public void setBoth(Set<String> both) { this.both = both; }

	public Set<String> getAmarilloOnly() { return amarilloOnly; }
	public void setAmarilloOnly(Set<String> amarilloOnly) { this.amarilloOnly = amarilloOnly; }

	public Set<String> getSfdcOnly() { return sfdcOnly; }
	public void setSfdcOnly(Set<String> sfdcOnly) { this.sfdcOnly = sfdcOnly; }

	public Set<String> getBothValid() { return bothValid; }
	public void setBothValid(Set<String> bothValid) { this.bothValid = bothValid; }

	public Set<String> getNeitherValid() { return neitherValid; }
	public void setNeitherValid(Set<String> neitherValid) { this.neitherValid = neitherValid; }

	public Set<String> getAmarilloValid() { return amarilloValid; }
	public void setAmarilloValid(Set<String> amarilloValid) { this.amarilloValid = amarilloValid; }

	public Set<String> getSfdcValid() { return sfdcValid; }
	public void setSfdcValid(Set<String> sfdcValid) { this.sfdcValid = sfdcValid; }

	public Set<String> getSfdcDupes() { return sfdcDupes; }
	public void setSfdcDupes(Set<String> dupesInSFDC) { this.sfdcDupes = dupesInSFDC; }

	public Set<String> getMismatchValidity() { return mismatchValidity; }
	public void setMismatchValidity(Set<String> mismatchValidity) { this.mismatchValidity = mismatchValidity; }

	public Set<String> getCombined() { return combined; }
	public void setCombined(Set<String> combined) { this.combined = combined; }

	public Pair<String[], List<CompUtils.CSVRecord>> getAmarilloRecords() { return amarilloRecords; }
	public void setAmarilloRecords(Pair<String[], List<CompUtils.CSVRecord>> amarilloRecords) { this.amarilloRecords = amarilloRecords; }

	public Pair<String[], List<CompUtils.CSVRecord>> getSfdcRecords() { return sfdcRecords; }
	public void setSfdcRecords(Pair<String[], List<CompUtils.CSVRecord>> sfdcRecords) { this.sfdcRecords = sfdcRecords; }

	public Pair<String[], List<CompUtils.CSVRecord>> getFeedRecords() { return feedRecords; }
	public void setFeedRecords(Pair<String[], List<CompUtils.CSVRecord>> feedRecords) { this.feedRecords = feedRecords; }

	public Map<String, List<CSVRecord>> getAmarilloMap() { return amarilloMap; }
	public void setAmarilloMap(Map<String, List<CSVRecord>> amarilloMap) { this.amarilloMap = amarilloMap; }

	public Map<String, List<CSVRecord>> getAmarilloAllMap() { return amarilloAllMap; }
	public void setAmarilloAllMap(Map<String, List<CSVRecord>> amarilloAllMap) { this.amarilloAllMap = amarilloAllMap; }

	public Map<String, List<CSVRecord>> getSFDCMap() { return sfdcMap; }
	public void setSFDCMap(Map<String, List<CSVRecord>> sfdcMap) { this.sfdcMap = sfdcMap; }
	
	public Map<String, List<CSVRecord>> getSfdcAllMap() { return sfdcAllMap; }
	public void setSfdcAllMap(Map<String, List<CSVRecord>> sfdcAllMap) { this.sfdcAllMap = sfdcAllMap; }

	public Map<String, List<CSVRecord>> getFeedMap() { return feedMap; }
	public void setFeedMap(Map<String, List<CSVRecord>> feedMap) { this.feedMap = feedMap; }

	public Pair<String[], List<CSVRecord>> getSfdcAllRecords() { return sfdcAllRecords; }
	public void setSfdcAllRecords(Pair<String[], List<CSVRecord>> sfdcAllRecords) { this.sfdcAllRecords = sfdcAllRecords; }
	
	public Pair<String[], List<CSVRecord>> getAmarilloAllRecords() { return amarilloAllRecords; }
	public void setAmarilloAllRecords(Pair<String[], List<CSVRecord>> amarilloAllRecords) { this.amarilloAllRecords = amarilloAllRecords; }

	public Pair<ComparatorConfig, ComparatorConfig> getConfigs() { return configs; }
	public void setConfig(Pair<ComparatorConfig, ComparatorConfig> configs) { this.configs = configs; }

	public String getDate() { return endDate;}
	public String getConfigFile() { return configFile; }
	public String getAmarilloFile() { return amarilloFile; }
	public String getSfdcFile() { return sfdcFile; }
	public String getFeedFile() { return feedFile; }

	public void setproduct(String product) { this.product = product; }
	public String getProduct() { return product; }

	public String getSqlWhereClause() {
		return CompUtils.writeSqlWhereClause(product, CompUtils.newList(amarilloOnly, sfdcOnly, sfdcDupes, amarilloValid, sfdcValid, mismatchValidity));
	}

}
