package io.codearte.accurest.util

import java.util.regex.Pattern

class JsonPathEntry {
	final String jsonPath
	final String optionalSuffix
	final Object value

	JsonPathEntry(String jsonPath, String optionalSuffix, Object value) {
		this.jsonPath = jsonPath
		this.optionalSuffix = optionalSuffix
		this.value = value
	}
	
	String buildJsonPathComparison(String parsedJsonVariable) {
		if(optionalSuffix) {
			return "${parsedJsonVariable}.read('${jsonPath}').${optionalSuffix} ${operator()} ${potentialyWrappedWithQuotesValue()}"
		} 
		return "${parsedJsonVariable}.read('${jsonPath}') ${operator()} ${potentialyWrappedWithQuotesValue()}"
	}

	String operator() {
		return value instanceof Pattern ? "==~" : "=="
	}

	String potentialyWrappedWithQuotesValue() {
		return value instanceof Number ? value : "'$value'"
	}

	static JsonPathEntry simple(String jsonPath, Object value) {
		return new JsonPathEntry(jsonPath, "", value)
	}
}
