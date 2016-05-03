package com.logicnow.comparison;

import java.util.List;
import java.util.Map;

import javax.json.JsonArray;
import javax.json.JsonObject;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class ComparatorConfig {

	private static final String ID = "id";
	private static final String COLS = "cols";
	private static final String COL_TYPES = "coltypes";
	private static final String VALID = "valid";
	private static final String TENANT = "tenant";
	private static final String PRODUCT = "product";
	private static final String SHORT_PRODUCT = "shortProduct";
	private static final String FILTERS = "filters";
	private static final String INCLUDE = "include";
	private static final String EXCLUDE = "exclude";

	private JsonObject config;
	
	public ComparatorConfig(JsonObject config) {
		this.config = config;
	}
	
	public String getIDColumn() {
		return config.getString(ID);
	}

	public Map<String, String> getColumnTypes() {
		return toStringMap(config.getJsonObject(COL_TYPES));
	}

	public String[] getColumns() {
		return toStringArray(config.getJsonArray(COLS));
	}
	
	public String getIsValidColumn() {
		return config.getString(VALID);
	}

	public String getTenantColumn() {
		return config.getString(TENANT);
	}

	public String getProductColumn() {
		return config.getString(PRODUCT);
	}
	
	public String getShortProduct() {
		return config.getString(SHORT_PRODUCT);
	}

	public List<String[]> getIncludeFilters() {
		return getFilters(true);
	}
	
	public List<String[]> getExcludeFilters() {
		return getFilters(false);
	}
	
	private List<String[]> getFilters(boolean include) {
		JsonObject obj = config.getJsonObject(FILTERS);
		List<String[]> filters = null;
		filters = Lists.newArrayList();
		if (obj != null) {
			if (include) {
				JsonArray arr = obj.getJsonArray(INCLUDE);
				if (arr != null) {
					filters = toListStringArray(arr);
				}
			} else {
				JsonArray arr = obj.getJsonArray(EXCLUDE);
				if (arr != null) {
					filters = toListStringArray(arr);
				}
			}
		}
		return filters;
	}

	// ---- Utility methods
	
	private String[] toStringArray(JsonArray arr) {
		List<String> items = Lists.newArrayList();
		if (arr != null) {
			for (int i = 0; i < arr.size(); i++) {
				items.add(arr.getString(i));
			}
		}
		return (String[])items.toArray(new String[items.size()]);
	}

	private Map<String, String> toStringMap(JsonObject obj) {
		Map<String, String> map = Maps.newHashMap();
		if (obj != null) {
			for (int i = 0; i < obj.size(); i++) {
				for (String key : obj.keySet()) {
					String value = obj.getString(key);
					map.put(key, value);
				}
			}
		}
		return map;
	}

	private List<String[]> toListStringArray(JsonArray arr) {
		List<String[]> items = Lists.newArrayList();
		for (int i = 0; i < arr.size(); i++) {
			items.add(toStringArray(arr.getJsonArray(i)));
		}
		return items;
	}
	
}
