package io.codearte.accurest.util

class JsonPathEntry {
	String jsonPath
	String optionalSuffix
	Object value

	JsonPathEntry(String jsonPath, String optionalSuffix, Object value) {
		this.jsonPath = jsonPath
		this.optionalSuffix = optionalSuffix
		this.value = value
	}

	static JsonPathEntry simple(String jsonPath, Object value) {
		return new JsonPathEntry(jsonPath, "", value)
	}
}
