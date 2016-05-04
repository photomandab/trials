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
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;

import com.google.common.collect.Maps;
import com.opencsv.CSVWriter;

public class CompUtils {

	private static int MIN_WIDTH = 3000;
	public static final String ENCODING_UTF_8 = "UTF-8";
	public static final SimpleDateFormat DATE_FORMAT_1 = new SimpleDateFormat("yyyy-MM-dd"); 
	public static final SimpleDateFormat DATE_FORMAT_2 = new SimpleDateFormat("MM/dd/yyyy");
	public static final SimpleDateFormat[] DATE_FORMATS = new SimpleDateFormat[] { DATE_FORMAT_1, DATE_FORMAT_2 }; 
	
	public static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss"); 
	public static final String NL = System.getProperty("line.separator");	
	public static final String[] COMBINED_HEADERS = new String[] { 
		"Tenant ID", "Lead ID", "In Both", "In Amarillo Only", "In SFDC Only", 
		"Both Valid", "Neither Valid", "Valid Amarillo Only", "Valid SFDC Only", "Mismatch", 
		"Dupe in SFDC", "Reason", "Details" };

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
		boolean first = true;
		for (int i = 0; i < list.size(); i++) {
			Set<String> tenants = list.get(i);
			if (tenants.size() > 0) {
				if (first) {
					buffy.append("and");
					first = false;
				} else {
					buffy.append(" or");
				}
				buffy.append(" l.lead_id in ").append(CompUtils.generateAmarilloString(product, tenants)).append(NL);
			}
		}
		return buffy.toString();
	}

	public static TreeSet<String> getSortedSet(Set<String> set) {
		if (set == null) return null;
		TreeSet<String> sorted = new TreeSet<>();
		sorted.addAll(set);
		return sorted; 
	}

	public static void writeExcelFile(File file, String d, ResultPayload[] payloads) {
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
				parsed = format.parse(d);
			} catch (ParseException e) {
			}
			if (parsed != null) break;
		}
		return parsed;
	}

}
