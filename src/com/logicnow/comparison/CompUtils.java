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
			p.populateInfoSheet(sheet);
		}
		// Add product sheets
		for (ResultPayload p : payloads) {
			String product = p.getProduct();
			HSSFSheet sheet = wb.createSheet(product);
			p.populateSheet(sheet);
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


}
