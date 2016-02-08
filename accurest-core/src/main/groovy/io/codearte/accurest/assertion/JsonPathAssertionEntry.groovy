package io.codearte.accurest.assertion

import java.util.regex.Pattern

class JsonPathAssertionEntry {
	final String jsonPath
	final String optionalSuffix
	final Object value

	JsonPathAssertionEntry(String jsonPath, String optionalSuffix, Object value) {
		this.jsonPath = jsonPath
		this.optionalSuffix = optionalSuffix
		this.value = value
	}
	
	List<String> buildJsonPathComparison(String parsedJsonVariable) {
		if (optionalSuffix) {
			return ["!${parsedJsonVariable}.read('''${jsonPath}''', JSONArray).empty"]
		} else if (traversesOverCollections()) {
			return ["${parsedJsonVariable}.read('''${jsonPath}''', JSONArray).get(0) ${operator()} ${potentiallyWrappedWithQuotesValue()}"]
		}
		return ["${parsedJsonVariable}.read('''${jsonPath}''') ${operator()} ${potentiallyWrappedWithQuotesValue()}"]
	}

	private boolean traversesOverCollections() {
		return jsonPath.contains('[*]')
	}

	String operator() {
		return value instanceof Pattern ? "==~" : "=="
	}

	String potentiallyWrappedWithQuotesValue() {
		return value instanceof Number ? value : "'''$value'''"
	}

	static JsonPathAssertionEntry simple(String jsonPath, Object value) {
		return new JsonPathAssertionEntry(jsonPath, "", value)
	}
}
