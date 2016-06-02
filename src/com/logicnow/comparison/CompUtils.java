package com.logicnow.comparison;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.logicnow.comparison.ReportComparator.CombinedRow;
import com.opencsv.CSVWriter;

public class CompUtils {

	public static final String ENCODING_UTF_8 = "UTF-8";
	public static final SimpleDateFormat DATE_FORMAT_1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSSS");
	public static final SimpleDateFormat DATE_FORMAT_2 = new SimpleDateFormat("yyyy-MM-dd"); 
	public static final SimpleDateFormat DATE_FORMAT_3 = new SimpleDateFormat("MM/dd/yyyy");
	public static final SimpleDateFormat[] DATE_FORMATS = new SimpleDateFormat[] { DATE_FORMAT_1, DATE_FORMAT_2, DATE_FORMAT_3 }; 
	
	public static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss"); 
	public static final String NL = System.getProperty("line.separator");	
	public static final String[] COMBINED_HEADERS = new String[] { 
		"Tenant ID", "Lead ID", "In Both", "In Amarillo Only", "In SFDC Only", 
		"Both Valid", "Neither Valid", "Valid Amarillo Only", "Valid SFDC Only", "Mismatch", 
		"Dupe in SFDC", "Reason", "Details" };

	public static final String MULTIPLE = "(Multiple)";
	
	private static int MIN_WIDTH = 3000;
	
	public static String getMonthStartDate() {
		Calendar c = Calendar.getInstance();
		c.set(Calendar.HOUR, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
    	c.add(Calendar.DATE, 1);
    	return DATE_FORMAT_2.format(c.getTime());
	}


	public static String getYesterdaysDate() {
		Calendar c = Calendar.getInstance();
		c.set(Calendar.HOUR, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
    	c.add(Calendar.DATE, -1);
    	return DATE_FORMAT_2.format(c.getTime());
	}
	
	public static String readFileAsText(File f) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(f), ENCODING_UTF_8));
		try { return readAsText(reader); } finally { reader.close(); }
	}
	
	public static String readAsText(Reader r) throws IOException { return org.apache.commons.io.IOUtils.toString(r); }

	public static String generateAmarilloId(String tenant, String product) {
		String id = tenant;
		if ("RM".equals(product) || "RM(IT)".equals(product)) {
			id = tenant.replace("_", ":");
			int index = id.indexOf(":");
			if (index != -1) {
				String prefix = id.substring(0, index);
				id = id.replace(prefix, "rm:" + prefix);				
			}
		} else if ("BU".equals(product)) {
			id = "backup:" + tenant;
		} else if ("MM".equals(product) || "MM(IT)".equals(product)) {
			id = tenant;
		}
		return id;
	}

	public static void writeCSVFile(File tmpFile, Pair<String[], List<CSVRecord>> records) throws IOException {
		tmpFile.getParentFile().mkdirs();
		try (CSVWriter writer = new CSVWriter(new FileWriter(tmpFile))) {
			writer.writeNext(records.getLeft());
			for (CSVRecord r : records.getRight()) {
				writer.writeNext(r.items);
			}
			System.out.println(MessageFormat.format("{0} filtered rows written to [{1}]", new Object[] { records.getRight().size(), tmpFile }));
		}
	}

	public static String generateAmarilloString(String product, Set<String> set) {
		TreeSet<String> sorted = getSortedSet(set);
		StringBuilder buffy = new StringBuilder();
		if (sorted != null && sorted.size() > 0) {
			buffy.append("(");
			for (String item : sorted) {
				if (buffy.length() > 1) buffy.append(", ");
				String id = CompUtils.generateAmarilloId(item, product);
				buffy.append("'").append(id).append("'");
			}
			buffy.append(")");
		} else {
			buffy.append("");
		}
		return buffy.toString();
	}

	public static String getAmarilloString(Set<String> set, Map<String, List<CSVRecord>> map, String[] headers, String idColumn) {
		TreeSet<String> sorted = getSortedSet(set);
		StringBuilder buffy = new StringBuilder();
		int index = ArrayUtils.indexOf(headers, idColumn);
		if (sorted != null && sorted.size() > 0) {
			buffy.append("(");
			for (String item : sorted) {
				if (buffy.length() > 1) buffy.append(", ");
				String id = map.get(item).get(0).items[index];
				buffy.append("'").append(id).append("'");
			}
			buffy.append(")");
		} else {
			buffy.append("<none>");
		}
		return buffy.toString();
	}

	public static String getDBString(Set<String> set) {
		TreeSet<String> sorted = getSortedSet(set);
		StringBuilder buffy = new StringBuilder();
		if (sorted != null && sorted.size() > 0) {
			buffy.append("(");
			for (String item : sorted) {
				if (buffy.length() > 1) buffy.append(", ");
				buffy.append("'").append(item).append("'");
			}
			buffy.append(")");
		} else {
			buffy.append("");
		}
		return buffy.toString();
	}

	public static String toPercentage(int numerator, int denominator) {
		return toPercentage((float)((float)numerator/(float)denominator));
	}

	public static String toPercentage(float n) {
		return toPercentage(n, 1);
	}

	public static String toPercentage(float n, int digits){
		return String.format("%."+digits+"f",n*100)+"%";
	}

	@SafeVarargs
	public static <T> List<T> newList(T ... vals) {
		return Arrays.asList(vals);
	}

	@SafeVarargs
	public static <T> Set<T> newSet(List<T> ... vals) {
		HashSet<T> set = Sets.newHashSet();
		for (List<T> l : vals) {
			set.addAll(l);
		}
		return set;
	}

	public static String writeSqlWhereClause(String product, List<Set<String>> list) {
		StringBuilder buffy = new StringBuilder();
		Set<String> uniqueLeads = Sets.newHashSet();
		for (int i = 0; i < list.size(); i++) {
			uniqueLeads.addAll(list.get(i));
		}
		if ("MM".equals(product) || "MM(IT)".equals(product)) {
			buffy.append("and tenant_id.value in ");			
		} else {
			buffy.append("and l.lead_id in ");			
		}
		buffy.append(CompUtils.generateAmarilloString(product, uniqueLeads));
		return buffy.toString();
	}

	public static TreeSet<String> getSortedSet(Set<String> set) {
		if (set == null) return null;
		TreeSet<String> sorted = new TreeSet<>();
		sorted.addAll(set);
		return sorted; 
	}

	public static void writeExcelFile(File file, ResultPayload... payloads) {
		System.out.println("Writing Excel result file...");
		HSSFWorkbook wb = new HSSFWorkbook();
		Font defaultFont = wb.getFontAt((short)0);
		defaultFont.setFontName("Arial");
		defaultFont.setFontHeight((short)12);
		// Add summary sheet
		for (ResultPayload p : payloads) {
			String product = p.getProduct();
			HSSFSheet sheet = wb.createSheet(product + " Info");
			CompUtils.populateInfoSheet(p, sheet);
		}
		// Add product sheets
		for (ResultPayload p : payloads) {
			String product = p.getProduct();
			HSSFSheet sheet = wb.createSheet(product);
			CompUtils.populateSheet(p, sheet);
		}

		// Add Amarillo All sheet
		CompUtils.addRecordsToSheet(wb, wb.createSheet("All Amarillo"), payloads[0].getAmarilloAllRecords());
		// Add SFDC All sheet
		CompUtils.addRecordsToSheet(wb, wb.createSheet("All SFDC"), payloads[0].getSfdcAllRecords());
		// Add feed sheet
		CompUtils.addRecordsToSheet(wb, wb.createSheet("SFDC Feed"), payloads[0].getFeedRecords());

		try {
			FileOutputStream out = new FileOutputStream(file);
			wb.write(out);
			out.close();
			System.out.println("Excel file written successfully");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try { wb.close(); } catch (IOException e) {
				e.printStackTrace();
			}			
		}
	}

	public static class CSVRecord {
		public String[] items;
		public CSVRecord(String[] items) {
			this.items = items;
		}
		public Map<String, String> toMap(String[] headers) {
			return toMap(headers, null);
		}
		public Map<String, String> toMap(String[] headers, Set<String> ignored) {
			Map<String, String> map = Maps.newLinkedHashMap();
			for (int i = 0; i < headers.length; i++) {
				if (ignored == null || !ignored.contains(headers[i])) {
					map.put(headers[i], items[i]);
				}
			}
			return map;
		}
	}

	public static void autoSizeColumn(HSSFSheet sheet, int column) {
		sheet.autoSizeColumn(column);
		if (sheet.getColumnWidth(column) == 0) {
			// Auto-size failed use MIN_WIDTH
			sheet.setColumnWidth(column, MIN_WIDTH);
		}
	}

	public static String getStringValue(String str) {
		return str == null ? "" : str.trim();
	}

	public static boolean isBlank(String str) {
		return str == null || str.trim().length() == 0;
	}

	public static boolean isEmpty(Collection<?> c) {
		return c == null || c.size() == 0;
	}

	public static void addRecordsToSheet(HSSFWorkbook wb, HSSFSheet sheet, Pair<String[], List<CSVRecord>> records) {
		addRecordsToSheet(wb, sheet, records, null);
	}

	public static void addRecordsToSheet(HSSFWorkbook wb, HSSFSheet sheet, Pair<String[], List<CSVRecord>> records, ComparatorConfig config) {
		int rownum = 0;
		Row row = sheet.createRow(rownum++);
		int cellnum = 0;
		// Headers
		String[] headers = records.getLeft();
		for (String value : headers) {
			Cell cell = row.createCell(cellnum++);
			cell.setCellValue(value);
		}
		// Data
		Map<String, String> coltypes = config != null ? config.getColumnTypes() : Maps.newHashMap();
		for (CSVRecord r : records.getRight()) {
			row = sheet.createRow(rownum++);
			cellnum = 0;
			for (String item : r.items) {
				String header = headers[cellnum];
				String coltype = coltypes.get(header);
				if ("integer".equals(coltype)) {
					if ("".equals(item)) {
						row.createCell(cellnum++).setCellValue(item);
					} else if (item.indexOf(".") > -1) {
						// parse as a double in case we have trailing decimals then convert
						int intValue = (int)Double.parseDouble(item);						
						row.createCell(cellnum++).setCellValue(intValue);					
					} else {
						// parse as an integer
						row.createCell(cellnum++).setCellValue(Integer.valueOf(item));					
					}
				} else if ("strip".equals(coltype)) {
					// strip leading/trailing formatting from amarillo raw output
					if (item.startsWith("=\"")) {
						item = item.substring(2);
					}
					if (item.endsWith("\"")) {
						item = item.substring(0, item.length() - 1);
					}
					row.createCell(cellnum++).setCellValue(item);
				} else {
					row.createCell(cellnum++).setCellValue(item);
				}
			}			
		}
	}

	public static Date parseDate(String d) {
		Date parsed = null;
		for (SimpleDateFormat format : DATE_FORMATS) {
			try {
				return format.parse(d);
			} catch (ParseException e) {
				
			}
		}
		return parsed;
	}

	public static File fixCSVAnomalies(File f, String... replaceStrings) throws IOException {
		List<String> lines = FileUtils.readLines(f, ENCODING_UTF_8);
		List<String> baseStrs = Lists.newArrayList(replaceStrings);
		for (int i = 0; i < lines.size(); i++) {
			String text = lines.get(i);
			boolean lineModified = false;
			List<String> strs = Lists.newArrayList(baseStrs);
			while (strs.size() > 0) {
				String string1 = strs.remove(0);
				String string2 = strs.remove(0);
				if (text.indexOf(string1) != -1) {
					text = text.replace(string1, string2);
					lineModified = true;
				}
			}
			if (lineModified) {
				lines.set(i, text);
			}
		}
		String filename = f.getName();
		int dotIndex = filename.indexOf(".");
		String updatedFileName = filename.substring(0, dotIndex) + "_x" + filename.substring(dotIndex);
		File updatedFile = new File(f.getParentFile(), updatedFileName);
		FileUtils.writeLines(updatedFile, ENCODING_UTF_8, lines);
		return updatedFile;
	}

	public static String getListValueOrMultiple(List<String> items) {
		if (items.size() == 1) return items.get(0);
		if (items.size() > 1) {
			Set<String> set = Sets.newHashSet(items);
			if (set.size() == 1) return items.get(0);	
			else return MULTIPLE;
		}
		return null;
	}

	public static String mappedProduct(String p) {
		if ("RM".equals(p)) return "RM";
		if ("RM(IT)".equals(p)) return "RM(IT)";
		if ("1 - RM".equals(p)) return "RM";
		if ("3 - Backup".equals(p)) return "BU";
		if ("6 - ControlNow / MAX IT".equals(p)) return "RM(IT)";
		if ("4 - MAX Mail".equals(p)) return "MM";
		if ("5 - MAX Mail IT".equals(p)) return "MM(IT)";
		if ("LN - MAX RM".equals(p)) return "RM";
		if ("LN - MAX Backup".equals(p)) return "BU";
		if ("LN - ControlNow".equals(p)) return "RM(IT)";
		if ("LN - MAXIT".equals(p)) return "RM(IT)";
		if ("LN - MAX Mail".equals(p)) return "MM";
		if ("LN - MAX Mail IT".equals(p)) return "MM(IT)";
		return p;
	}
	
	public static String mappedRegion(String region) {
		if ("NAM".equals(region)) return "NAM";
		if ("1 - North America".equals(region)) return "NAM";
		if ("01 - North America".equals(region)) return "NAM";
		if ("2 - LATAM".equals(region)) return "LATAM";
		if ("02 - LATAM".equals(region)) return "LATAM";
		if (region != null && region.contains("LATAM")) return "LATAM";
		if (region != null && region.startsWith("US")) return "NAM";
		return region;
	}

	public static List<CombinedRow> getCombinedRecords(ResultPayload payload) {
		List<CombinedRow> records = Lists.newArrayList();
		TreeSet<String> allSorted = getSortedSet(payload.getCombined());
		CombinedRow row = null;
		String product = payload.getProduct();
		for (String tenant : allSorted) {
			// We don't need to see items that are both/neither valid
			if (payload.getBothValid().contains(tenant)) continue;
			if (payload.getNeitherValid().contains(tenant)) continue;
			// Generate the remaining entries
			row = new CombinedRow(tenant);
			row.leadId = CompUtils.generateAmarilloId(tenant, product);
			if (payload.getBoth().contains(tenant)) row.inBoth = 1;
			if (payload.getAmarilloOnly().contains(tenant)) row.inAmarillo = 1;
			if (payload.getSfdcOnly().contains(tenant)) row.inSFDC = 1;
			if (payload.getAmarilloValid().contains(tenant)) row.validInAmarillo = 1;
			if (payload.getSfdcValid().contains(tenant)) row.validInSFDC = 1;
			if (payload.getMismatchValidity().contains(tenant)) row.mismatch = 1;
			if (payload.getSfdcDupes().contains(tenant)) row.dupeInSFDC = 1;
			// Try and establish a reason for discrepancy
			Pair<String, String> reasons = establishReason(payload, row);
			row.reason1 = reasons != null ? reasons.getLeft() : "";
			row.reason2 = reasons != null ? reasons.getRight() : "";
			// Add the item to the list
			records.add(row);
		}
		return records;
	}

	public static void populateSheet(ResultPayload payload, HSSFSheet sheet) {
		List<CombinedRow> records = getCombinedRecords(payload);
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

	private static Pair<String, String> establishReason(ResultPayload payload, CombinedRow item) {
		String tenantId = item.tenantId;
		List<CSVRecord> aRecords = payload.getAmarilloAllMap().get(tenantId);
		List<CSVRecord> sRecords = payload.getSfdcAllMap().get(tenantId);
		List<CSVRecord> fRecords = payload.getFeedMap().get(tenantId);
		
		// Ensure we have records to make a comparison
		if (CompUtils.isEmpty(aRecords) && (CompUtils.isEmpty(sRecords) && CompUtils.isEmpty(fRecords)))
			return null;

		Pair<String, String> reason = null;
		if ((reason = checkTerritoryChange(payload, item, aRecords, sRecords, fRecords)) != null) {
			return reason;
		} else if ((reason = checkProductChange(payload, item, aRecords, sRecords, fRecords)) != null) {
			return reason;
		} else if ((reason = checkReUsedTenant(payload, item, aRecords, sRecords, fRecords)) != null) {
			return reason;
		} else if ((reason = checkEmployeeTesting(payload, item, aRecords, sRecords, fRecords)) != null) {
			return reason;
		} else if ((reason = checkSFDCDupesVaryingValidity(payload, item, aRecords, sRecords, fRecords)) != null) {
			return reason;
		} else if ((reason = checkTimingIssue(payload, item, aRecords, sRecords, fRecords)) != null) {
			return reason;
		} else if ((reason = checkCustomerTrial(payload, item, aRecords, sRecords, fRecords)) != null) {
			return reason;
		} else if ((reason = checkOpportunityType(payload, item, aRecords, sRecords, fRecords)) != null) {
			return reason;
		}
		
		return null;
	}

	private static Pair<String, String> checkOpportunityType(ResultPayload payload, CombinedRow item, List<CSVRecord> aRecords, List<CSVRecord> sRecords, List<CSVRecord> fRecords) {
		List<String> attributes = getAttributes(payload.getFeedRecords().getLeft(), fRecords, "OpportunityType");
		String value = CompUtils.getListValueOrMultiple(attributes);
		if (value != null) {
			String val = value.toLowerCase();
			if (val.contains("renewal") || val.contains("migration") || val.contains("customer")) {
				return Pair.of("Excluded from SFDC", "Type " + value);					
			} 
		}
		return null;
	}

	private static Pair<String, String> checkCustomerTrial(ResultPayload payload, CombinedRow item, List<CSVRecord> aRecords, List<CSVRecord> sRecords, List<CSVRecord> fRecords) {
		List<String> attributes = getAttributes(payload.getFeedRecords().getLeft(), fRecords, "Source");
		String value = CompUtils.getListValueOrMultiple(attributes);
		if ("Customer Trial".equals(value)) {
			return Pair.of("Excluded from SFDC", "Source is Customer Trial");					
		}
		return null;
	}

	private static Pair<String, String> checkTimingIssue(ResultPayload payload, CombinedRow item, List<CSVRecord> aRecords, List<CSVRecord> sRecords, List<CSVRecord> fRecords) {
		Date start = payload.getStartDate();
		Date end = payload.getEndDate();
		List<String> attributes = getAttributes(payload.getFeedRecords().getLeft(), fRecords, "Trial_Start");
		String value = CompUtils.getListValueOrMultiple(attributes);
		Date trialStart = (value != null && !CompUtils.MULTIPLE.equals(value)) ? parseDate(value) : null;
		if (trialStart != null) {
			if (trialStart.before(start) || trialStart.after(end)) {
				return Pair.of("Timing Issue", "Trial Start out of Range in SFDC");					
			}
		}
		return null;
	}

	private static Pair<String, String> checkSFDCDupesVaryingValidity(ResultPayload payload, CombinedRow item, List<CSVRecord> aRecords, List<CSVRecord> sRecords, List<CSVRecord> fRecords) {
		if (payload.getSfdcDupes().contains(item.tenantId)) {
			List<String> attributes = getAttributes(payload.getSfdcAllRecords().getLeft(), sRecords, "Is Valid");
			String value = CompUtils.getListValueOrMultiple(attributes);
			if (CompUtils.MULTIPLE.equals(value)) {
				return Pair.of("Multiple entry in SFDC", "Different validity values");
			}
		}
		return null;
	}

	private static Pair<String, String> checkEmployeeTesting(ResultPayload payload, CombinedRow item, List<CSVRecord> aRecords, List<CSVRecord> sRecords, List<CSVRecord> fRecords) {
		List<String> attribution = getAttributes(payload.getAmarilloAllRecords().getLeft(), aRecords, "LN Attribution Group");
		String value = CompUtils.getListValueOrMultiple(attribution);
		if ("Employee Testing".equals(value)) {
			return Pair.of("Employee Testing", "Amarillo identifies as Test Trial");
		}
		return null;
	}

	private static Pair<String, String> checkReUsedTenant(ResultPayload payload, CombinedRow item, List<CSVRecord> aRecords, List<CSVRecord> sRecords, List<CSVRecord> fRecords) {
		if (item.isInAmarilloOnly()) {
			List<String> tenants = getAttributes(payload.getFeedRecords().getLeft(), fRecords, "TenantID");
			if (CompUtils.isEmpty(tenants)) {
				return Pair.of("Not in SFDC Feed", "Validity not read from SFDC");
			}
		}
		return null;
	}

	private static Pair<String, String> checkProductChange(ResultPayload payload, CombinedRow item, List<CSVRecord> aRecords, List<CSVRecord> sRecords, List<CSVRecord> fRecords) {
		List<String> aProducts = getAttributes(payload.getAmarilloAllRecords().getLeft(), aRecords, "Fixed Product");
		List<String> sProducts = getAttributes(payload.getSfdcAllRecords().getLeft(), sRecords, "Core Product");
		List<String> fProducts = getAttributes(payload.getFeedRecords().getLeft(), fRecords, "Product");
		
		String aProduct = !CompUtils.isEmpty(aProducts) ? CompUtils.getListValueOrMultiple(aProducts) : null;		
		String sProduct = !CompUtils.isEmpty(sProducts) ? CompUtils.getListValueOrMultiple(sProducts) : null;
		String fProduct = !CompUtils.isEmpty(fProducts) ? CompUtils.getListValueOrMultiple(fProducts) : null;
		
		aProduct = CompUtils.mappedProduct(aProduct);
		sProduct = CompUtils.mappedProduct(sProduct);
		fProduct = CompUtils.mappedProduct(fProduct);
		
		if (item.isInBoth() && !CompUtils.isBlank(aProduct) && !aProduct.equals(sProduct)) {
			return Pair.of("Product Change", "1:" + aProduct + " in Amarillo, " + sProduct + " in SFDC");
		} else if (item.isInAmarilloOnly() && !CompUtils.isBlank(sProduct) && !CompUtils.isBlank(aProduct) && !aProduct.equals(sProduct)) {
			return Pair.of("Product Change", "2:" + aProduct + " in Amarillo, " + sProduct + " in SFDC");
		} else if (item.isInAmarilloOnly() && !CompUtils.isBlank(fProduct) && !CompUtils.isBlank(aProduct) && !aProduct.equals(fProduct)) {
			return Pair.of("Product Change", "3:" + aProduct + " in Amarillo, " + fProduct + " in SFDC Feed");
		} else if (item.isInSFDCOnly() && !CompUtils.isBlank(sProduct) && !CompUtils.isBlank(aProduct) && !sProduct.equals(aProduct)) {
			return Pair.of("Product Change", "4:" + aProduct + " in Amarillo, " + sProduct + " in SFDC");
		}
		
		return null;
	}

	private static Pair<String, String> checkTerritoryChange(ResultPayload payload, CombinedRow item, List<CSVRecord> aRecords, List<CSVRecord> sRecords, List<CSVRecord> fRecords) {
		List<String> aRegions = getAttributes(payload.getAmarilloAllRecords().getLeft(), aRecords, "Marketing Territory");
		List<String> sRegions = getAttributes(payload.getSfdcAllRecords().getLeft(), sRecords, "Group");
		List<String> fRegions = getAttributes(payload.getFeedRecords().getLeft(), fRecords, "Sub-Region");
		
		String aRegion = aRegions != null && aRegions.size() > 0 ? CompUtils.getListValueOrMultiple(aRegions) : null;		
		String sRegion = sRegions != null && sRegions.size() > 0 ? CompUtils.getListValueOrMultiple(sRegions) : null;
		String fRegion = fRegions != null && fRegions.size() > 0 ? CompUtils.getListValueOrMultiple(fRegions) : null;
		
		aRegion = CompUtils.mappedRegion(aRegion);
		sRegion = CompUtils.mappedRegion(sRegion);
		fRegion = CompUtils.mappedRegion(fRegion);
		
		if (item.isInBoth() && sRegion != null && !CompUtils.isBlank(aRegion) && !aRegion.equals(sRegion)) {
			return Pair.of("Territory Change", "1:" + aRegion + " in Amarillo, " + sRegion + " in SFDC");
		} else if (item.isInAmarilloOnly() && sRegion != null && aRegion != null && !aRegion.equals(sRegion)) {
			return Pair.of("Territory Change", "2:" + aRegion + " in Amarillo, " + sRegion + " in SFDC");
		} else if (item.isInAmarilloOnly() && fRegion != null && aRegion != null && !aRegion.equals(fRegion)) {
			return Pair.of("Territory Change", "3:" + aRegion + " in Amarillo, " + fRegion + " in SFDC Feed");
		} else if (item.isInSFDCOnly() && sRegion != null && aRegion != null && !aRegion.equals(sRegion)) {
			return Pair.of("Territory Change", "4:" + aRegion + " in Amarillo, " + sRegion + " in SFDC");
		}
		
		return null;
	}

	private static List<String> getAttributes(String[] headers, List<CSVRecord> records, String attributeName) {
		List<String> values = Lists.newArrayList();
		if (records != null) {
			for (CSVRecord record : records) {
				String value = record.toMap(headers).get(attributeName);
				if (value != null) values.add(value);
			}
		}
		return values;
	}

	public static void populateInfoSheet(ResultPayload payload, HSSFSheet sheet) {
		// Totals
		int bothSize = payload.getBoth().size();
		int onlyAmarilloSize = payload.getAmarilloOnly().size();
		int onlySFDCSize = payload.getSfdcOnly().size();
		int totalSize = bothSize + onlyAmarilloSize + onlySFDCSize;
		int dupesSize = payload.getSfdcDupes().size();

		int bothValidSize = payload.getBothValid().size();
		int neitherValidSize = payload.getNeitherValid().size();
		int onlyAmarilloValidSize = payload.getAmarilloValid().size();
		int onlySFDCValidSize = payload.getSfdcValid().size();
		int mismatchSize = payload.getMismatchValidity().size();
		int totalValidLeads = bothValidSize + neitherValidSize + onlyAmarilloValidSize + onlySFDCValidSize;
		int diffSize = totalSize - bothValidSize - neitherValidSize;
		
		int rownum = 0;
		String product = payload.getProduct();
		
		// Overview
		rownum = addHeaderRow(sheet, rownum, product + " Trial Count");
		rownum = addDataRow(sheet, rownum, "Total", new Object[] { totalSize, "100%" });
		rownum = addDataRow(sheet, rownum, "In Both", new Object[] { bothSize, CompUtils.toPercentage(bothSize, totalSize) });
		rownum = addDataRow(sheet, rownum, "In Amarillo Only", new Object[] { onlyAmarilloSize, CompUtils.toPercentage(onlyAmarilloSize, totalSize) });
		rownum = addDataRow(sheet, rownum, "In SFDC Only", new Object[] { onlySFDCSize, CompUtils.toPercentage(onlySFDCSize, totalSize) });
		rownum = addDataRow(sheet, rownum, "Dupes in SFDC", new Object[] { dupesSize, CompUtils.toPercentage(dupesSize, totalSize) });
		rownum = addBlankRow(sheet, rownum);
		rownum = addHeaderRow(sheet, rownum, product + " Trial Validity");
		rownum = addDataRow(sheet, rownum, "Total", new Object[] { totalSize, "100%" });
		rownum = addDataRow(sheet, rownum, "Total Valid", new Object[] { totalValidLeads, CompUtils.toPercentage(totalValidLeads, totalSize) });
		rownum = addDataRow(sheet, rownum, "Both", new Object[] { bothValidSize, CompUtils.toPercentage(bothValidSize, totalSize) });
		rownum = addDataRow(sheet, rownum, "Neither", new Object[] { neitherValidSize,CompUtils.toPercentage(neitherValidSize, totalSize) });
		rownum = addDataRow(sheet, rownum, "Amarillo Only", new Object[] { onlyAmarilloValidSize, CompUtils.toPercentage(onlyAmarilloValidSize, totalSize), CompUtils.getDBString(payload.getAmarilloValid())});
		rownum = addDataRow(sheet, rownum, "SFDC Only", new Object[] { onlySFDCValidSize, CompUtils.toPercentage(onlySFDCValidSize, totalSize), CompUtils.getDBString(payload.getSfdcValid()) });
		rownum = addDataRow(sheet, rownum, "Mismatch", new Object[] { mismatchSize, CompUtils.toPercentage(mismatchSize, totalSize), CompUtils.getDBString(payload.getMismatchValidity()) });
		rownum = addDataRow(sheet, rownum, "Differences", new Object[] { diffSize, CompUtils.toPercentage(diffSize, totalSize) });
		rownum = addBlankRow(sheet, rownum);
		rownum = addDataRow(sheet, rownum, "SQL", new Object[] { payload.getSqlWhereClause() });
		
	}

	private static int addDataRow(HSSFSheet sheet, int rownum, String msg, Object[] data) {
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

	private static int addBlankRow(HSSFSheet sheet, int rownum) {
		Row row = sheet.createRow(rownum++);
		int cellnum = 0;		
		Cell cell = row.createCell(cellnum++);
		cell.setCellValue("");
		return rownum;
	}

	private static int addHeaderRow(HSSFSheet sheet, int rownum, String msg) {
		Row row = sheet.createRow(rownum++);
		int cellnum = 0;		
		Cell cell = row.createCell(cellnum++);
		cell.setCellValue(msg);
		rownum = addBlankRow(sheet, rownum);
		return rownum;
	}

	public void addFilteredAmarilloData(HSSFWorkbook wb, HSSFSheet sheet, ResultPayload payload) {
		CompUtils.addRecordsToSheet(wb, sheet, payload.getAmarilloRecords(), payload.getConfigs().getLeft());
	}

	public void addFilteredSFDCData(HSSFWorkbook wb, HSSFSheet sheet, ResultPayload payload) {		
		CompUtils.addRecordsToSheet(wb, sheet, payload.getSfdcRecords(), payload.getConfigs().getRight());
	}

}
