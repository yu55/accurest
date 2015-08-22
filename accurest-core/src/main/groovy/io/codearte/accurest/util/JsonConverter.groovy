package io.codearte.accurest.util
import groovy.json.JsonSlurper
/**
 * @author Marcin Grzejszczak
 */
class JsonConverter {

	private static Map convert(Map map, Closure closure) {
		return map.collectEntries {
			key, value ->
				[key, transformValues(value, closure)]
		}
	}

	static def transformValues(def value, Closure closure) {
		if (value instanceof String && value) {
			try {
				def json = new JsonSlurper().parseText(value)
				if (json instanceof Map) {
					return convert(json, closure)
				}
			} catch (Exception ignore) {
				return closure(value)
			}
		} else if (value instanceof Map) {
			return convert(value as Map, closure)
		} else if (value instanceof List) {
			return value.collect({ transformValues(it, closure) })
		}
		try {
			return closure(value)
		} catch (Exception ignore) {
			return value
		}
	}

	static def traverseRecursively(String key, def value, Closure closure) {
		if (value instanceof String && value) {
			try {
				def json = new JsonSlurper().parseText(value)
				if (json instanceof Map) {
					return convertWithKey(key, json, closure)
				}
			} catch (Exception ignore) {
				return closure(key, value)
			}
		} else if (value instanceof Map) {
			return convertWithKey(key, value as Map, closure)
		} else if (value instanceof List) {
			return value.collect({ traverseRecursively(key, it, closure) })
		}
		try {
			return closure(key, value)
		} catch (Exception ignore) {
			return value
		}
	}

	private static Map convertWithKey(String parentKey, Map map, Closure closure) {
		return map.collectEntries {
			String entrykey, value ->
				[entrykey, traverseRecursively("${parentKey}.${entrykey}", value, closure)]
		}
	}

	static Map<String, Object> transformToPathAndValues(def json) {
		Map<String, Object> pathsAndValues = [:]
		traverseRecursively('$', json) { String key, Object value ->
			Object valueToInsert = value
			if (pathsAndValues.containsKey(key)) {
				Object oldValue = pathsAndValues[key]
				valueToInsert = [oldValue, valueToInsert].flatten()
			}
			pathsAndValues[key] = valueToInsert
		}
		return pathsAndValues
	}
}
