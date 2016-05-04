package com.logicnow.comparison;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.logicnow.comparison.CompUtils.CSVRecord;
import com.logicnow.comparison.ReportComparator.CombinedRow;

public class ResultPayload {

	private static final String MULTIPLE = "(Multiple)";

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
	private Date parseDate(String d) { return CompUtils.parseDate(startDate); }

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

	public Map<String, List<CSVRecord>> geFeedCMap() { return feedMap; }
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

	private TreeSet<String> getSortedSet(Set<String> set) {
		if (set == null) return null;
		TreeSet<String> sorted = new TreeSet<>();
		sorted.addAll(set);
		return sorted; 
	}

	public List<CombinedRow> getCombinedRecords() {
		List<CombinedRow> records = Lists.newArrayList();
		TreeSet<String> allSorted = getSortedSet(combined);
		CombinedRow row = null;
		for (String tenant : allSorted) {
			// We don't need to see items that are both/neither valid
			if (bothValid.contains(tenant)) continue;
			if (neitherValid.contains(tenant)) continue;
			// Generate the remaining entries
			row = new CombinedRow(tenant);
			row.leadId = CompUtils.generateAmarilloId(tenant, product);
			if (both.contains(tenant)) row.inBoth = 1;
			if (amarilloOnly.contains(tenant)) row.inAmarillo = 1;
			if (sfdcOnly.contains(tenant)) row.inSFDC = 1;
			if (amarilloValid.contains(tenant)) row.validInAmarillo = 1;
			if (sfdcValid.contains(tenant)) row.validInSFDC = 1;
			if (mismatchValidity.contains(tenant)) row.mismatch = 1;
			if (sfdcDupes.contains(tenant)) row.dupeInSFDC = 1;
			// Try and establish a reason for discrepancy
			Pair<String, String> reasons = establishReason(row);
			row.reason1 = reasons != null ? reasons.getLeft() : "";
			row.reason2 = reasons != null ? reasons.getRight() : "";
			// Add the item to the list
			records.add(row);
		}
		return records;
	}

	public void populateSheet(HSSFSheet sheet) {
		List<CombinedRow> records = getCombinedRecords();
		int rownum = 0;
		Row row = sheet.createRow(rownum++);
		int cellnum = 0;
		// Headers
		for (String value : CompUtils.COMBINED_HEADERS) {
			Cell cell = row.createCell(cellnum++);
			cell.setCellValue(value);
		}
		// Data
		for (CombinedRow item : records) {
			row = sheet.createRow(rownum++);
			cellnum = 0;
			Cell cell = null;
			for (Object obj : item.getData()) {
				cell = row.createCell(cellnum++);
				if(obj instanceof Date) {
					cell.setCellValue((Date)obj);
				} else if(obj instanceof Boolean) {
					cell.setCellValue((Boolean)obj);
				} else if(obj instanceof String) {
					cell.setCellValue((String)obj);
				} else if(obj instanceof Double) {
					cell.setCellValue((Double)obj);
				} else if(obj instanceof Integer) {
					cell.setCellValue((Integer)obj);
				}
			}
		}
		for (int i = 0; i < cellnum; i++) {
			CompUtils.autoSizeColumn(sheet, i);
		}
	}

	private Pair<String, String> establishReason(CombinedRow item) {
		String tenantId = item.tenantId;
		List<CSVRecord> aRecords = amarilloAllMap.get(tenantId);
		List<CSVRecord> sRecords = sfdcAllMap.get(tenantId);
		List<CSVRecord> fRecords = feedMap.get(tenantId);
		
		// Ensure we have records to make a comparison
		if (CompUtils.isEmpty(aRecords) && (CompUtils.isEmpty(sRecords) && CompUtils.isEmpty(fRecords)))
			return null;

		Pair<String, String> reason = null;
		if ((reason = checkTerritoryChange(item, aRecords, sRecords, fRecords)) != null) {
			return reason;
		} else if ((reason = checkProductChange(item, aRecords, sRecords, fRecords)) != null) {
			return reason;
		} else if ((reason = checkReUsedTenant(item, aRecords, sRecords, fRecords)) != null) {
			return reason;
		} else if ((reason = checkEmployeeTesting(item, aRecords, sRecords, fRecords)) != null) {
			return reason;
		} else if ((reason = checkSFDCDupesVaryingValidity(item, aRecords, sRecords, fRecords)) != null) {
			return reason;
		} else if ((reason = checkTimingIssue(item, aRecords, sRecords, fRecords)) != null) {
			return reason;
		}
		
		return null;
	}

	private Pair<String, String> checkTimingIssue(CombinedRow item, List<CSVRecord> aRecords, List<CSVRecord> sRecords, List<CSVRecord> fRecords) {
		Date start = getStartDate();
		Date end = getEndDate();
		List<String> attributes = getAttributes(sfdcAllRecords.getLeft(), sRecords, "Trial Start");
		String value = getListValueOrMultiple(attributes);
		Date trialStart = value != null && !MULTIPLE.equals(value) ? parseDate(value) : null;
		if (trialStart != null) {
			if (trialStart.before(start) || trialStart.after(end)) {
				return Pair.of("Timing Issue", "Trial Start out of Range in SFDC");					
			}
		}
		
		return null;
	}

	private Pair<String, String> checkSFDCDupesVaryingValidity(CombinedRow item, List<CSVRecord> aRecords, List<CSVRecord> sRecords, List<CSVRecord> fRecords) {
		if (sfdcDupes.contains(item.tenantId)) {
			List<String> attributes = getAttributes(sfdcAllRecords.getLeft(), sRecords, "Is Valid");
			String value = getListValueOrMultiple(attributes);
			if (MULTIPLE.equals(value)) {
				return Pair.of("Multiple entry in SFDC", "Different validity values");
			}
		}
		return null;
	}

	private Pair<String, String> checkEmployeeTesting(CombinedRow item, List<CSVRecord> aRecords, List<CSVRecord> sRecords, List<CSVRecord> fRecords) {
		List<String> attribution = getAttributes(amarilloAllRecords.getLeft(), aRecords, "LN Attribution Group");
		String value = getListValueOrMultiple(attribution);
		if ("Employee Testing".equals(value)) {
			return Pair.of("Employee Testing", "Amarillo identifies as Test Trial");
		}
		return null;
	}

	private Pair<String, String> checkReUsedTenant(CombinedRow item, List<CSVRecord> aRecords, List<CSVRecord> sRecords, List<CSVRecord> fRecords) {
		if (item.isInAmarilloOnly()) {
			List<String> tenants = getAttributes(feedRecords.getLeft(), fRecords, "TenantID");
			if (CompUtils.isEmpty(tenants)) {
				return Pair.of("Not in SFDC Feed", "Validity not read from SFDC");
			}
		}
		return null;
	}

	private Pair<String, String> checkProductChange(CombinedRow item, List<CSVRecord> aRecords, List<CSVRecord> sRecords, List<CSVRecord> fRecords) {
		List<String> aProducts = getAttributes(amarilloAllRecords.getLeft(), aRecords, "Fixed Product");
		List<String> sProducts = getAttributes(sfdcAllRecords.getLeft(), sRecords, "Core Product");
		List<String> fProducts = getAttributes(feedRecords.getLeft(), fRecords, "Product");
		
		String aProduct = !CompUtils.isEmpty(aProducts) ? getListValueOrMultiple(aProducts) : null;		
		String sProduct = !CompUtils.isEmpty(sProducts) ? getListValueOrMultiple(sProducts) : null;
		String fProduct = !CompUtils.isEmpty(fProducts) ? getListValueOrMultiple(fProducts) : null;
		
		aProduct = mappedProduct(aProduct);
		sProduct = mappedProduct(sProduct);
		fProduct = mappedProduct(fProduct);
		
		if (item.isInBoth() && !CompUtils.isBlank(aProduct) && !aProduct.equals(mappedProduct(sProduct))) {
			return Pair.of("Product Change", aProduct + " in Amarillo, " + sProduct + " in SFDC");
		} else if (item.isInAmarilloOnly() && !CompUtils.isBlank(sProduct) && !CompUtils.isBlank(aProduct) && !aProduct.equals(sProduct)) {
			return Pair.of("Product Change", aProduct + " in Amarillo, " + fProduct + " in SFDC");
		} else if (item.isInAmarilloOnly() && !CompUtils.isBlank(fProduct) && !CompUtils.isBlank(aProduct) && !aProduct.equals(fProduct)) {
			return Pair.of("Product Change", aProduct + " in Amarillo, " + fProduct + " in SFDC Feed");
		} else if (item.isInSFDCOnly() && !CompUtils.isBlank(sProduct) && !CompUtils.isBlank(aProduct) && !sProduct.equals(aProduct)) {
			return Pair.of("Product Change", aProduct + " in Amarillo, " + sProduct + " in SFDC");
		}
		
		return null;
	}

	private Pair<String, String> checkTerritoryChange(CombinedRow item, List<CSVRecord> aRecords, List<CSVRecord> sRecords, List<CSVRecord> fRecords) {
		List<String> aRegions = getAttributes(amarilloAllRecords.getLeft(), aRecords, "Marketing Territory");
		List<String> sRegions = getAttributes(sfdcAllRecords.getLeft(), sRecords, "Group");
		List<String> fRegions = getAttributes(feedRecords.getLeft(), fRecords, "Territory___Lead");
		
		String aRegion = aRegions != null && aRegions.size() > 0 ? getListValueOrMultiple(aRegions) : null;		
		String sRegion = sRegions != null && sRegions.size() > 0 ? getListValueOrMultiple(sRegions) : null;
		String fRegion = fRegions != null && fRegions.size() > 0 ? getListValueOrMultiple(fRegions) : null;
		
		aRegion = mappedRegion(aRegion);
		sRegion = mappedRegion(sRegion);
		fRegion = mappedRegion(fRegion);
		
		if (item.isInBoth() && sRegion != null && !CompUtils.isBlank(aRegion) && !aRegion.equals(sRegion)) {
			return Pair.of("Territory Change", aRegion + " in Amarillo, " + sRegion + " in SFDC");
		} else if (item.isInAmarilloOnly() && sRegion != null && aRegion != null && !aRegion.equals(sRegion)) {
			return Pair.of("Territory Change", aRegion + " in Amarillo, " + sRegion + " in SFDC");
		} else if (item.isInAmarilloOnly() && fRegion != null && aRegion != null && !aRegion.equals(fRegion)) {
			return Pair.of("Territory Change", aRegion + " in Amarillo, " + fRegion + " in SFDC Feed");
		} else if (item.isInSFDCOnly() && sRegion != null && aRegion != null && !aRegion.equals(sRegion)) {
			return Pair.of("Territory Change", aRegion + " in Amarillo, " + sRegion + " in SFDC");
		}
		
		return null;
	}

	private String getListValueOrMultiple(List<String> items) {
		if (items.size() == 1) return items.get(0);
		if (items.size() > 1) {
			Set<String> set = Sets.newHashSet(items);
			if (set.size() == 1) return items.get(0);	
			else return MULTIPLE;
		}
		return null;
	}

	private String mappedProduct(String p) {
		if ("1 - RM".equals(p)) return "RM";
		if ("3 - Backup".equals(p)) return "BU";
		if ("6 - ControlNow / MAX IT".equals(p)) return "RM(IT)";
		if ("LN - MAX RM".equals(p)) return "RM";
		if ("LN - MAX Backup".equals(p)) return "BU";
		if ("LN - ControlNow".equals(p)) return "RM(IT)";
		if ("LN - MAXIT".equals(p)) return "RM(IT)";
		return p;
	}
	
	private String mappedRegion(String region) {
		if ("1 - North America".equals(region)) return "NAM";
		if ("2 - LATAM".equals(region)) return "LATAM";
		if (region != null && region.contains("LATAM")) return "LATAM";
		if (region != null && region.startsWith("US")) return "NAM";
		return region;
	}

	private List<String> getAttributes(String[] headers, List<CSVRecord> records, String attributeName) {
		List<String> values = Lists.newArrayList();
		if (records != null) {
			for (CSVRecord record : records) {
				String value = record.toMap(headers).get(attributeName);
				if (value != null) values.add(value);
			}
		}
		return values;
	}

	public void populateInfoSheet(HSSFSheet sheet) {
		// Totals
		int bothSize = both.size();
		int onlyAmarilloSize = amarilloOnly.size();
		int onlySFDCSize = sfdcOnly.size();
		int totalSize = bothSize + onlyAmarilloSize + onlySFDCSize;
		int dupesSize = sfdcDupes.size();

		int bothValidSize = bothValid.size();
		int neitherValidSize = neitherValid.size();
		int onlyAmarilloValidSize = amarilloValid.size();
		int onlySFDCValidSize = sfdcValid.size();
		int mismatchSize = mismatchValidity.size();
		int totalValidLeads = bothValidSize + neitherValidSize + onlyAmarilloValidSize + onlySFDCValidSize;
		int diffSize = totalSize - bothValidSize - neitherValidSize;
		
		int rownum = 0;
		
		// Overview
		rownum = addHeaderRow(sheet, rownum, product + " Trial Count Breakdown");
		rownum = addDataRow(sheet, rownum, "Total", new Object[] { totalSize, "100%" });
		rownum = addDataRow(sheet, rownum, "In Both", new Object[] { bothSize, CompUtils.toPercentage(bothSize, totalSize) });
		rownum = addDataRow(sheet, rownum, "In Amarillo Only", new Object[] { onlyAmarilloSize, CompUtils.toPercentage(onlyAmarilloSize, totalSize) });
		rownum = addDataRow(sheet, rownum, "In SFDC Only", new Object[] { onlySFDCSize, CompUtils.toPercentage(onlySFDCSize, totalSize) });
		rownum = addDataRow(sheet, rownum, "Dupes in SFDC", new Object[] { dupesSize, CompUtils.toPercentage(dupesSize, totalSize) });
		rownum = addBlankRow(sheet, rownum);
		rownum = addHeaderRow(sheet, rownum, product + " Trial Validity Breakdown");
		rownum = addDataRow(sheet, rownum, "Total", new Object[] { totalSize, "100%" });
		rownum = addDataRow(sheet, rownum, "Total Valid", new Object[] { totalValidLeads, CompUtils.toPercentage(totalValidLeads, totalSize) });
		rownum = addDataRow(sheet, rownum, "Both", new Object[] { bothValidSize, CompUtils.toPercentage(bothValidSize, totalSize) });
		rownum = addDataRow(sheet, rownum, "Neither", new Object[] { neitherValidSize,CompUtils.toPercentage(neitherValidSize, totalSize) });
		rownum = addDataRow(sheet, rownum, "Amarillo Only", new Object[] { onlyAmarilloValidSize, CompUtils.toPercentage(onlyAmarilloValidSize, totalSize), CompUtils.getDBString(amarilloValid)});
		rownum = addDataRow(sheet, rownum, "SFDC Only", new Object[] { onlySFDCValidSize, CompUtils.toPercentage(onlySFDCValidSize, totalSize), CompUtils.getDBString(sfdcValid) });
		rownum = addDataRow(sheet, rownum, "Mismatch", new Object[] { mismatchSize, CompUtils.toPercentage(mismatchSize, totalSize), CompUtils.getDBString(mismatchValidity) });
		rownum = addDataRow(sheet, rownum, "Differences", new Object[] { diffSize, CompUtils.toPercentage(diffSize, totalSize) });
		rownum = addBlankRow(sheet, rownum);
		rownum = addDataRow(sheet, rownum, "SQL", new Object[] { CompUtils.writeSqlWhereClause(product, CompUtils.newList(amarilloOnly, sfdcOnly, sfdcDupes, amarilloValid, sfdcValid, mismatchValidity)) });
		
	}

	private int addDataRow(HSSFSheet sheet, int rownum, String msg, Object[] data) {
		Row row = sheet.createRow(rownum++);
		int cellnum = 0;		
		Cell cell = row.createCell(cellnum++);
		cell.setCellValue(msg);
		for (Object obj : data) {
			cell = row.createCell(cellnum++);
			if(obj instanceof Date) {
				cell.setCellValue((Date)obj);
			} else if(obj instanceof Boolean) {
				cell.setCellValue((Boolean)obj);
			} else if(obj instanceof String) {
				cell.setCellValue((String)obj);
			} else if(obj instanceof Double) {
				cell.setCellValue((Double)obj);
			} else if(obj instanceof Integer) {
				cell.setCellValue((Integer)obj);
			}
		}
		return rownum;
	}

	private int addBlankRow(HSSFSheet sheet, int rownum) {
		Row row = sheet.createRow(rownum++);
		int cellnum = 0;		
		Cell cell = row.createCell(cellnum++);
		cell.setCellValue("");
		return rownum;
	}

	private int addHeaderRow(HSSFSheet sheet, int rownum, String msg) {
		Row row = sheet.createRow(rownum++);
		int cellnum = 0;		
		Cell cell = row.createCell(cellnum++);
		cell.setCellValue(msg);
		rownum = addBlankRow(sheet, rownum);
		return rownum;
	}

	public void addFilteredAmarilloData(HSSFWorkbook wb, HSSFSheet sheet) {
		CompUtils.addRecordsToSheet(wb, sheet, amarilloRecords, configs.getLeft());
	}

	public void addFilteredSFDCData(HSSFWorkbook wb, HSSFSheet sheet) {		
		CompUtils.addRecordsToSheet(wb, sheet, sfdcRecords, configs.getRight());
	}

}
