package io.codearte.accurest.assertion

class JsonPathsAssertions extends HashSet<JsonPathAssertionEntry> {

	Object getAt(String key) {
		return find {
			it.jsonPath == key
		}?.value
	}

	Object putAt(String key, Object value) {
		JsonPathAssertionEntry entry = find {
			it.jsonPath == key
		}
		if (!entry) {
			return null
		}
		Object oldValue = entry.value
		add(new JsonPathAssertionEntry(entry.jsonPath, entry.optionalSuffix, value))
		return oldValue
	}
}

