package com.logicnow.comparison.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.logicnow.comparison.ComparatorConfig;
import com.logicnow.comparison.ReportComparator.CombinedRow;
import com.logicnow.comparison.ResultPayload;
import com.logicnow.comparison.utils.CompUtils.CSVRecord;

public class ExcelUtils {

	public static final int MIN_WIDTH = 3000;
	public static final int INFO_COL1_WIDTH = 4000;	
	public static final int SHEET_COL_WIDTH_WIDE = 6000;
	public static final int SHEET_COL_WIDTH_EXTRA_WIDE = 8000;
	public static final int SHEET_COL_WIDTH_MEDIUM = 5000;
	
	public static void writeExcelFile(File file, boolean includeFilteredSheets, ResultPayload... payloads) {
		System.out.println("Writing Excel result file...");
		HSSFWorkbook wb = new HSSFWorkbook();
		Font defaultFont = wb.getFontAt((short)0);
		defaultFont.setFontName("Arial");
		defaultFont.setFontHeight((short)12);
		// Add summary sheet
		for (ResultPayload p : payloads) {
			String product = p.getProduct();
			HSSFSheet sheet = wb.createSheet(product + " Info");
			populateInfoSheet(p, sheet);
		}
		// Add product sheets
		for (ResultPayload p : payloads) {
			String product = p.getProduct();
			HSSFSheet sheet = wb.createSheet(product);
			populateSheet(p, sheet);
		}

		// Add Amarillo All sheet
		addRecordsToSheet(wb, wb.createSheet("Amarillo All"), payloads[0].getAmarilloAllRecords());
		// Add SFDC All sheet
		addRecordsToSheet(wb, wb.createSheet("SFDC All"), payloads[0].getSfdcAllRecords());
		// Add SFDC Feed sheet
		addRecordsToSheet(wb, wb.createSheet("SFDC Feed"), payloads[0].getFeedRecords());

		// Add filtered product sheets
		if (includeFilteredSheets) {
			for (ResultPayload p : payloads) {
				// Add Amarillo Filtered sheet
				HSSFSheet sheet1 = wb.createSheet("Amarillo " + p.getProduct());
				addRecordsToSheet(wb, sheet1, p.getAmarilloRecords(), p.getConfigs().getLeft());
				int index1 = wb.getSheetIndex(sheet1.getSheetName());
				wb.setSheetHidden(index1, 1);
				// Add SFDC Filtered sheet
				HSSFSheet sheet2 = wb.createSheet("SFDC " + p.getProduct());
				addRecordsToSheet(wb, sheet2, p.getSfdcRecords(), p.getConfigs().getRight());
				int index2 = wb.getSheetIndex(sheet2.getSheetName());
				wb.setSheetHidden(index2, 1);
			}
		}

		try {
			FileOutputStream out = new FileOutputStream(file);
			wb.write(out);
			out.close();
			System.out.println("Excel file written successfully\n");
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
		rownum = addDataRow(sheet, rownum, "Dupes in SFDC", new Object[] { dupesSize, CompUtils.toPercentage(dupesSize, totalSize), CompUtils.getDBString(payload.getSfdcDupes()) });
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
		rownum = addBlankRow(sheet, rownum);
		rownum = addDataRow(sheet, rownum, "Config Amarillo", new Object[] { payload.getAmarilloConfig().asString(", ") });
		rownum = addDataRow(sheet, rownum, "Config SFDC", new Object[] { payload.getSfdcConfig().asString(", ") });
		
		setColumnWidth(sheet, 0, INFO_COL1_WIDTH);
	}

	public static List<CombinedRow> getCombinedRecords(ResultPayload payload) {
		List<CombinedRow> records = Lists.newArrayList();
		TreeSet<String> allSorted = CompUtils.getSortedSet(payload.getCombined());
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
			Pair<String, String> reasons = CompUtils.establishReason(payload, row);
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
		// Column Widths
		setColumnWidth(sheet, 0, SHEET_COL_WIDTH_MEDIUM);
		setColumnWidth(sheet, 1, SHEET_COL_WIDTH_MEDIUM);
		setColumnWidth(sheet, 11, SHEET_COL_WIDTH_WIDE);
		setColumnWidth(sheet, 12, SHEET_COL_WIDTH_EXTRA_WIDE);
		setColumnWidth(sheet, 13, SHEET_COL_WIDTH_EXTRA_WIDE);
		// Set Header Row as Data Filter
		setDataFilter(sheet, 0, records.size() - 1, 0, CompUtils.COMBINED_HEADERS.length - 1);
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
		setDataFilter(sheet, 0, records.getRight().size() - 1, 0, headers.length - 1);
	}

	public static void setDataFilter(HSSFSheet sheet, int firstRow, int lastRow, int firstCol, int lastCol) {
		sheet.setAutoFilter(new CellRangeAddress(firstRow, lastRow, firstCol, lastCol));	
	}

	public static void setColumnWidth(HSSFSheet sheet, int column, int width) {
		sheet.setColumnWidth(column, width);
	}

	public static void autoSizeColumn(HSSFSheet sheet, int column) {
		sheet.autoSizeColumn(column);
		if (sheet.getColumnWidth(column) == 0) {
			// Auto-size failed use MIN_WIDTH
			sheet.setColumnWidth(column, MIN_WIDTH);
		}
	}

	public static int addDataRow(HSSFSheet sheet, int rownum, String msg, Object[] data) {
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

	public static int addBlankRow(HSSFSheet sheet, int rownum) {
		Row row = sheet.createRow(rownum++);
		int cellnum = 0;		
		Cell cell = row.createCell(cellnum++);
		cell.setCellValue("");
		return rownum;
	}

	public static int addHeaderRow(HSSFSheet sheet, int rownum, String msg) {
		Row row = sheet.createRow(rownum++);
		int cellnum = 0;		
		Cell cell = row.createCell(cellnum++);
		cell.setCellValue(msg);
		rownum = addBlankRow(sheet, rownum);
		return rownum;
	}

	public void addFilteredAmarilloData(HSSFWorkbook wb, HSSFSheet sheet, ResultPayload payload) {
		addRecordsToSheet(wb, sheet, payload.getAmarilloRecords(), payload.getConfigs().getLeft());
	}

	public void addFilteredSFDCData(HSSFWorkbook wb, HSSFSheet sheet, ResultPayload payload) {		
		addRecordsToSheet(wb, sheet, payload.getSfdcRecords(), payload.getConfigs().getRight());
	}


}
