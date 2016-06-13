package com.logicnow.comparison;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.StringReader;
import java.util.List;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.logicnow.comparison.CompUtils.CSVRecord;

public class ComparatorTest {
	
	private static final File RM_CONFIG = new File("test/data/RM/config_1.json");
	private static final String START_DATE = "2016-05-10";
	private static final String END_DATE = "2016-06-09";

	@Test
	public void testFilters() throws Exception {
		ComparatorConfig config = getConfigs(RM_CONFIG).getLeft();		
		List<String[]> includeFilters = createIncludeFilters();
		List<String[]> excludeFilters = createExcludeFilters();
		String[] headers = new String[] { "Core Product", "Group", "Trial Date", "Close Date"};		

		CSVRecord r1 = getCSVRecord("1 - RM", "01 - North America", "5/26/2016", "5/18/2016");
		CSVRecord r2 = getCSVRecord("1 - RM", "01 - North America", "5/26/2016", "5/01/2016"); 
		CSVRecord r3 = getCSVRecord("1 - RM", "ROW", "5/26/2016", "5/18/2016");

		assertTrue("All filters match 1", CompUtils.allFiltersMatch(config, headers, includeFilters, r1));
		assertTrue("All filters match 2", CompUtils.allFiltersMatch(config, headers, includeFilters, r2));
		assertFalse("All filters match 3", CompUtils.allFiltersMatch(config, headers, includeFilters, r3));
		
		assertFalse("Any filters match 1", CompUtils.anyFiltersMatch(config, headers, excludeFilters, r1));
		assertTrue("Any filters match 2", CompUtils.anyFiltersMatch(config, headers, excludeFilters, r2));
		assertFalse("Any filters match 3", CompUtils.anyFiltersMatch(config, headers, excludeFilters, r3));
	}

	private List<String[]> createIncludeFilters() {
		List<String[]> filters = Lists.newArrayList();
		filters.add(new String[] { "Core Product", "=", "1 - RM" });
		filters.add(new String[] { "Group", "=", "01 - North America" });
		return filters;
	}
	
	private List<String[]> createExcludeFilters() {
		List<String[]> filters = Lists.newArrayList();
		filters.add(new String[] { "Close Date", "<", "$START_DATE" });
		return filters;
	}
	
	private CSVRecord getCSVRecord(String... items) {
		return new CSVRecord(items);
	}
	
	private Pair<ComparatorConfig, ComparatorConfig> getConfigs(File file) throws Exception {
		JsonReader jsonReader = Json.createReader(new StringReader(CompUtils.readFileAsText(file)));
		JsonObject configs = jsonReader.readObject();
		jsonReader.close();
		
		// Create configuration objects
		ComparatorConfig leftConfig = new ComparatorConfig(configs.getJsonObject(CompUtils.PARAM_LEFT));
		ComparatorConfig rightConfig = new ComparatorConfig(configs.getJsonObject(CompUtils.PARAM_RIGHT));
		
		CompUtils.addDatesToConfig(leftConfig, START_DATE, END_DATE);
		CompUtils.addDatesToConfig(rightConfig, START_DATE, END_DATE);
		
		return Pair.of(leftConfig, rightConfig);
	}
	
}
