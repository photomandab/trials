package com.logicnow.comparison;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.apache.commons.io.input.BOMInputStream;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.logicnow.comparison.utils.CompUtils;
import com.logicnow.comparison.utils.CompUtils.CSVRecord;
import com.opencsv.CSVReader;

public class ReportComparator {
	
	private static final String GENERATED_TENANT = "Generated Tenant";
	private static final String TENANTID = "TenantID";
	
	private static Map<String, String> PRODUCT_TENANT_MAP;
	{ 
		PRODUCT_TENANT_MAP = Maps.newHashMap();
		PRODUCT_TENANT_MAP.put("1 - RM", "Tenant MAXRM");
		PRODUCT_TENANT_MAP.put("3 - Backup", "Tenant MAXBU");
		PRODUCT_TENANT_MAP.put("4 - MAX Mail", "Tenant MAXML");
		PRODUCT_TENANT_MAP.put("5 - MAX Mail IT", "Tenant MAXML");
		PRODUCT_TENANT_MAP.put("6 - ControlNow / MAX IT", "Tenant MAXRM");
		PRODUCT_TENANT_MAP.put("LN - MAX Backup IT", "Tenant MAXBU");
		PRODUCT_TENANT_MAP.put("LN - MAXRI", "Tenant MAXRI");
	}
	
	
	public ResultPayload compare(String startDate, String endDate, String dateTime, String configPath, String amarilloPath, String sfdcPath, String feedPath) throws Exception {
		
		System.out.println();
		System.out.println("---------------------------------------------------------");
		System.out.println();

		System.out.println(MessageFormat.format("Processing comparison config=[{0}] amarillo=[{1}] sfdc=[{2}] feed=[{3}] ", new Object[] { configPath, amarilloPath, sfdcPath, feedPath }));

		ResultPayload result = new ResultPayload(startDate, endDate, configPath, amarilloPath, sfdcPath, feedPath);

		File configFile = new File(configPath);
		File amarilloFile = new File(amarilloPath);
		File sfdcFile = new File(sfdcPath);
		File feedFile = new File(feedPath);
		
		if (!configFile.exists()) throw new IllegalArgumentException("Config file [" + configFile.getAbsolutePath() + "] does not exist");
		if (!amarilloFile.exists()) throw new IllegalArgumentException("Amarillo CSV file [" + amarilloFile.getAbsolutePath() + "] does not exist");
		if (!sfdcFile.exists()) throw new IllegalArgumentException("SFDC CSV file [" + sfdcFile.getAbsolutePath() + "] does not exist");

		JsonReader jsonReader = Json.createReader(new StringReader(CompUtils.readFileAsText(configFile)));
		JsonObject configs = jsonReader.readObject();
		jsonReader.close();
		
		// Create configuration objects
		ComparatorConfig leftConfig = new ComparatorConfig(configs.getJsonObject(CompUtils.PARAM_LEFT));
		ComparatorConfig rightConfig = new ComparatorConfig(configs.getJsonObject(CompUtils.PARAM_RIGHT));
		
		System.out.println("Amarillo config:");
		System.out.println(leftConfig.asString());
		System.out.println("SFDC config:");
		System.out.println(rightConfig.asString());

		CompUtils.addDatesToConfig(leftConfig, startDate, endDate);
		CompUtils.addDatesToConfig(rightConfig, startDate, endDate);
		
		result.setConfig(Pair.of(leftConfig,  rightConfig));

		Pair<String[], List<CSVRecord>> amarilloRecords = readCSVFile(leftConfig, amarilloFile);
		amarilloRecords = generateTenants(leftConfig, amarilloRecords);
		result.setAmarilloRecords(amarilloRecords);

		Pair<String[], List<CSVRecord>> amarilloAllRecords = readCSVFile(null, amarilloFile);
		amarilloAllRecords = generateTenants(leftConfig, amarilloAllRecords);
		result.setAmarilloAllRecords(amarilloAllRecords);
		
		Pair<String[], List<CSVRecord>> sfdcRecords = readCSVFile(rightConfig, sfdcFile);
		result.setSfdcRecords(sfdcRecords);

		Pair<String[], List<CSVRecord>> sfdcAllRecords = readCSVFile(null, sfdcFile);
		result.setSfdcAllRecords(sfdcAllRecords);

		feedFile = CompUtils.fixCSVAnomalies(feedFile, "\\\"", "\"");
		Pair<String[], List<CSVRecord>> feedRecords = readCSVFile(null, feedFile);
		result.setFeedRecords(feedRecords);

		// Create Tenant to Record maps
		Map<String, List<CSVRecord>> amarilloMap = createTenantToRecordMap(leftConfig, amarilloRecords, GENERATED_TENANT);
		result.setAmarilloMap(amarilloMap);
		Map<String, List<CSVRecord>> amarilloAllMap = createTenantToRecordMap(null, amarilloAllRecords, GENERATED_TENANT);
		result.setAmarilloAllMap(amarilloAllMap);
		Map<String, List<CSVRecord>> sfdcMap = createTenantToRecordMap(rightConfig, sfdcRecords, rightConfig.getTenantColumn());
		result.setSFDCMap(sfdcMap);
		Map<String, List<CSVRecord>> sfdcAllMap = createTenantToRecordMap(null, sfdcAllRecords, null, "Core Product");
		result.setSfdcAllMap(sfdcAllMap);
		Map<String, List<CSVRecord>> feedMap = createTenantToRecordMap(null, feedRecords, TENANTID);
		result.setFeedMap(feedMap);
		
		// Create Tenant Sets
		Set<String> amarilloTenants = amarilloMap.keySet();
		Set<String> sfdcTenants = sfdcMap.keySet();
		
		// Calculate intersection - trials in both amarillo and SFDC
		Set<String> intersection = new HashSet<String>(amarilloTenants);
		intersection.retainAll(sfdcTenants);
		
		// Calculate combined - trials occurring in amarillo and/or SFDC
		Set<String> combined = new HashSet<String>(amarilloTenants);
		combined.addAll(sfdcTenants);
		
		// Create validity containers
		Set<String> amarilloValid = Sets.newHashSet();
		Set<String> sfdcValid = Sets.newHashSet();
		Set<String> bothValid = Sets.newHashSet();
		Set<String> neitherValid = Sets.newHashSet();
		Set<String> onlyAmarilloValid = Sets.newHashSet();
		Set<String> onlySFDCValid = Sets.newHashSet();
		Set<String> mismatchValidity = Sets.newHashSet();
		
		// Calculate validity
		for (String tenant : combined) {
			List<CSVRecord> leftOnes = amarilloMap.get(tenant);
			List<CSVRecord> rightOnes = sfdcMap.get(tenant);
			String leftValidity = allValid(leftOnes, amarilloRecords.getLeft(), leftConfig.getIsValidColumn()); 
			String rightValidity = allValid(rightOnes, sfdcRecords.getLeft(), rightConfig.getIsValidColumn()); 
			if ("1".equals(leftValidity)) amarilloValid.add(tenant);
			if ("1".equals(rightValidity)) sfdcValid.add(tenant);
			boolean inBoth = intersection.contains(tenant);
			if ("1".equals(leftValidity)) {	
				if ("1".equals(rightValidity)) bothValid.add(tenant);
				else onlyAmarilloValid.add(tenant);
			} else {
				if ("1".equals(rightValidity)) onlySFDCValid.add(tenant);
				else if (inBoth) neitherValid.add(tenant);
			}
			if (inBoth && !leftValidity.equals(rightValidity)) mismatchValidity.add(tenant);
		}

		// Totals
		int numLeft = amarilloTenants.size();
		int numLeftValid = amarilloValid.size();
		int numRight = sfdcTenants.size();
		int numRightValid = sfdcValid.size();
		int numDiff = numLeft - numRight;
		int numValidDiff = numLeftValid - numRightValid;
		
		// Present in Amarillo not in SFDC by Tenant
		Set<String> inAmarilloOnly = Sets.newHashSet(amarilloTenants);
		inAmarilloOnly.removeAll(sfdcTenants);

		// Present in SFDC not in Amarillo by Tenant
		Set<String> inSFDCOnly = new HashSet<String>(sfdcTenants);
		inSFDCOnly.removeAll(amarilloTenants);

		// Duplicates in SFDC
		Set<String> dupesInSFDC = Sets.newHashSet();
		for (String tenant : sfdcTenants) {
			if (sfdcMap.get(tenant).size() > 1) {
				dupesInSFDC.add(tenant);
			}
		}
		String product = leftConfig.getShortProduct();

		int bothSize = intersection.size();
		int onlyAmarilloSize = inAmarilloOnly.size();
		int onlySFDCSize = inSFDCOnly.size();
		int totalSize = bothSize + onlyAmarilloSize + onlySFDCSize;
		int dupesSize = dupesInSFDC.size();
		
		// Overview
		printHeader(leftConfig.getShortProduct(), "Trial Count Breakdown");
		
		System.out.println(MessageFormat.format("Total:            ({0})", new Object[] { totalSize }));
		System.out.println(MessageFormat.format("In Both:          ({0})", new Object[] { bothSize }));
		System.out.println(MessageFormat.format("In Amarillo Only: ({0})", new Object[] { onlyAmarilloSize }));
		System.out.println(MessageFormat.format("In SFDC Only:     ({0})", new Object[] { onlySFDCSize }));
		System.out.println(MessageFormat.format("Dupes in SFDC:    ({0})", new Object[] { dupesSize }));
		System.out.println();
		System.out.println(MessageFormat.format("Amarillo Total/Valid: ({0}/{1}) ({2}) ({3}/{4}) ({5})", new Object[] { numLeft, numLeftValid, CompUtils.toPercentage(numLeftValid, numLeft), totalSize, numLeftValid, CompUtils.toPercentage(numLeftValid, totalSize) }));
		System.out.println(MessageFormat.format("SFDC     Total/Valid: ({0}/{1}) ({2}) ({3}/{4}) ({5})", new Object[] { numRight, numRightValid, CompUtils.toPercentage(numRightValid, numRight), totalSize, numRightValid, CompUtils.toPercentage(numRightValid, totalSize) }));
		System.out.println(MessageFormat.format("Diff     Total/Valid: ({0}/{1}) ({2}) ({3}/{4}) ({5})", new Object[] { numDiff, numValidDiff, CompUtils.toPercentage(numValidDiff, numDiff), totalSize, numValidDiff, CompUtils.toPercentage(numValidDiff, totalSize) }));
		System.out.println(MessageFormat.format("Percent  Total/Valid: ({0}/{1})", new Object[] { CompUtils.toPercentage(numRight-numLeft, numLeft), CompUtils.toPercentage(numRightValid-numLeftValid, numLeftValid) }));
		System.out.println();
		System.out.println(MessageFormat.format("In Both:\n{0}", new Object[] { CompUtils.getDBString(intersection) }));
		System.out.println(MessageFormat.format("In Amarillo Only:\n{0}", new Object[] { CompUtils.getDBString(inAmarilloOnly) }));
		System.out.println(MessageFormat.format("In SFDC Only:\n{0}", new Object[] { CompUtils.getDBString(inSFDCOnly) } ));
		System.out.println(MessageFormat.format("Dupes in SFDC:\n{0}", new Object[] { CompUtils.getDBString(dupesInSFDC) }));

		printHeader(leftConfig.getShortProduct(), "Validity Breakdown");

		int bothValidSize = bothValid.size();
		int neitherValidSize = neitherValid.size();
		int onlyAmarilloValidSize = onlyAmarilloValid.size();
		int onlySFDCValidSize = onlySFDCValid.size();
		int mismatchSize = mismatchValidity.size();
		int totalValidLeads = bothValidSize + neitherValidSize + onlyAmarilloValidSize + onlySFDCValidSize;
		
		int diffSize = totalSize - bothValidSize - neitherValidSize;
		
		System.out.println(MessageFormat.format("Total:         ({0}) ({1})", new Object[] { totalSize, "100%" }));
		System.out.println(MessageFormat.format("Total Valid:   ({0}) ({1})", new Object[] { totalValidLeads, CompUtils.toPercentage(totalValidLeads, totalSize) }));
		System.out.println(MessageFormat.format("Both:          ({0}) ({1})", new Object[] { bothValidSize, CompUtils.toPercentage(bothValidSize, totalSize) }));
		System.out.println(MessageFormat.format("Neither:       ({0}) ({1})", new Object[] { neitherValidSize, CompUtils.toPercentage(neitherValidSize, totalSize) }));
		System.out.println(MessageFormat.format("Amarillo Only: ({0}) ({1})", new Object[] { onlyAmarilloValidSize, CompUtils.toPercentage(onlyAmarilloValidSize, totalSize) }));
		System.out.println(MessageFormat.format("SFDC Only:     ({0}) ({1})", new Object[] { onlySFDCValidSize, CompUtils.toPercentage(onlySFDCValidSize, totalSize) }));
		System.out.println(MessageFormat.format("Mismatch:      ({0}) ({1})", new Object[] { mismatchSize, CompUtils.toPercentage(mismatchSize, totalSize) }));
		System.out.println(MessageFormat.format("Differences    ({0}) ({1})", new Object[] { diffSize, CompUtils.toPercentage(diffSize, totalSize) }));		
		System.out.println();
		System.out.println(MessageFormat.format("Amarillo Only:\n{0}", new Object[] { CompUtils.getDBString(onlyAmarilloValid) }));
		System.out.println(MessageFormat.format("SFDC Only:\n{0}", new Object[] { CompUtils.getDBString(onlySFDCValid) }));
		System.out.println(MessageFormat.format("Mismatch:\n{0}", new Object[] { CompUtils.getDBString(mismatchValidity) }));
		System.out.println();
		
		result.setproduct(product);
		result.setCombined(combined);
		result.setBoth(intersection);
		result.setAmarilloOnly(inAmarilloOnly);
		result.setSfdcOnly(inSFDCOnly);
		result.setSfdcDupes(dupesInSFDC);
		result.setBothValid(bothValid);
		result.setNeitherValid(neitherValid);
		result.setAmarilloValid(onlyAmarilloValid);
		result.setSfdcValid(onlySFDCValid);
		result.setMismatchValidity(mismatchValidity);
		
		return result;
	}

	private void printHeader(String product, String header) {
		System.out.println(MessageFormat.format("\n{0} {1}\n", new Object[] { product, header }));
	}

	private String allValid(List<CSVRecord> recs, String[] headers, String isValidColumn) {
		if (recs == null) return "0";
		
		for (CSVRecord rec : recs) {
			String actual = rec.toMap(headers).get(isValidColumn);
			if ("0".equals(actual)) {
				return "0";
			}
		}
		return "1";
	}
	private Map<String, List<CSVRecord>> createTenantToRecordMap(ComparatorConfig config, Pair<String[], List<CSVRecord>> recs, String tenantColumn) {
		return createTenantToRecordMap(config, recs, tenantColumn, null);
	}

	private Map<String, List<CSVRecord>> createTenantToRecordMap(ComparatorConfig config, Pair<String[], List<CSVRecord>> recs, String tenantColumn, String productColumn) {
		Map<String, List<CSVRecord>> map = Maps.newLinkedHashMap();
		for (CSVRecord r : recs.getRight()) {
			Map<String, String> recMap = r.toMap(recs.getLeft());
			String productValue = tenantColumn == null ? recMap.get(productColumn) : null;
			String tenCol = tenantColumn != null ? tenantColumn : PRODUCT_TENANT_MAP.get(productValue);
			String tenant = recMap.get(tenCol);
			List<CSVRecord> tenantRecs = map.get(tenant);
			if (tenantRecs == null) {
				tenantRecs = Lists.newArrayList();
				map.put(tenant, tenantRecs);
			}
			tenantRecs.add(r);
		}
		return map;
	}
	
	

	private Pair<String[], List<CSVRecord>> generateTenants(ComparatorConfig config, Pair<String[], List<CSVRecord>> recs) {
		String[] headers = recs.getLeft();
		String[] newHeaders = ArrayUtils.add(headers, GENERATED_TENANT);
		String idColumn = config.getIDColumn();
		String productColumn = config.getProductColumn();
		String tenantColumn = config.getTenantColumn();
		for (CSVRecord r : recs.getRight()) {
			Map<String, String> map = r.toMap(headers);
			String id = map.get(idColumn);
			String product = map.get(productColumn);
			String tenant = map.get(tenantColumn);
			String generatedTenant = generateTenant(id, tenant, product);
			String[] newItems = ArrayUtils.add(r.items, generatedTenant);
			r.items = newItems;
		}
		return Pair.of(newHeaders, recs.getRight());
	}

	protected String generateTenant(String id, String tenant, String product) {
		if (tenant != null) return tenant;
		if ("RM".equals(product) || "RM(IT)".equals(product)) {
			if (id.startsWith("salesforce")) {
				return id.replace("salesforce:", "");
			}
			return id.replace("rm:", "").replace(":", "_");
		} else if ("BU".equals(product)) {
			return id.replace("backup:", "");
		}
		return id;
	}

	private Pair<String[], List<CSVRecord>> readCSVFile(ComparatorConfig config, File csv) throws IOException {
		return getCsvData(csv.getAbsolutePath(), config, new FileInputStream(csv));
	}

	public Pair<String[], List<CSVRecord>> getCsvData(String key, ComparatorConfig config, InputStream in) throws IOException {
		List<CSVRecord> records = new ArrayList<CSVRecord>();
		String[] headerItems = null;
		try (CSVReader reader = new CSVReader(new BufferedReader(new InputStreamReader(new BOMInputStream(in), "UTF-8")))) {
			headerItems = reader.readNext();
			String[] lineItems;
			while ((lineItems = reader.readNext()) != null) {
				// break at first empty row
				if (lineItems.length == 1 && "".equals(lineItems[0])) {
					break;
				}
				records.add(new CSVRecord(lineItems));
		    }
		}
//		System.out.println(MessageFormat.format("{0} rows read from [{1}]", new Object[] { records.size(), key }));
    	return CompUtils.filterColumns(config, headerItems, records);
	}

	public static class CombinedRow {
		public String tenantId = "";
		public String leadId = "";
		public int inBoth = 0;
		public int inAmarillo = 0;
		public int inSFDC = 0;
		public int bothValid = 0;
		public int neitherValid = 0;
		public int validInAmarillo = 0;
		public int validInSFDC = 0;
		public int mismatch = 0;
		public int dupeInSFDC = 0; 
		public String reason1 = "";
		public String reason2 = "";
		public String reason3 = "";
		public CombinedRow(String tenantId) { this.tenantId = tenantId; }
		public Object[] getData() {
			return new Object[] { 
					tenantId, 
					leadId, 
					inBoth, 
					inAmarillo, 
					inSFDC,
					bothValid,
					neitherValid,
					validInAmarillo,
					validInSFDC,
					mismatch,
					dupeInSFDC,
					reason1,
					reason2,
					reason3
				};
		}
		public boolean isInBoth() { return inBoth == 1; }
		public boolean isInAmarilloOnly() { return inAmarillo == 1; }
		public boolean isInSFDCOnly() { return inSFDC == 1; }
		
	}
	
}
