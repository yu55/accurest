package io.codearte.accurest.util
import groovy.json.JsonSlurper
/**
 * @author Marcin Grzejszczak
 */
class JsonConverter {


	public static final boolean WITH_A_LIST = true

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

	static def traverseRecursively(Class parentType, String key, def value, Closure closure) {
		if (value instanceof String && value) {
			try {
				def json = new JsonSlurper().parseText(value)
				if (json instanceof Map) {
					return convertWithKey(parentType, key, json, closure)
				}
			} catch (Exception ignore) {
				return closure(parentType == List, key, value)
			}
		} else if (isAnEntryWithNonCollectionLikeValue(value)) {
			return convertWithKey(List, key, value as Map, closure)
		} else if (value instanceof Map) {
			return convertWithKey(Map, key, value as Map, closure)
		} else if (value instanceof List) {
			value.each { def element ->
				traverseRecursively(List, "$key[*]", element, closure)
			}
			return value
		}
		try {
			return closure(parentType == List, key, value)
		} catch (Exception ignore) {
			return value
		}
	}

	private static boolean isAnEntryWithNonCollectionLikeValue(def value) {
		if (!(value instanceof Map)) {
			return false
		}
		Map valueAsMap = ((Map) value)
		boolean mapHasOneEntry = valueAsMap.size() == 1
		if (!mapHasOneEntry) {
			return false
		}
		Object valueOfEntry = valueAsMap.entrySet().first().value
		return !(valueOfEntry instanceof Map || valueOfEntry instanceof List)

	}

	private static Map convertWithKey(Class parentType, String parentKey, Map map, Closure closureToExecute) {
		return map.collectEntries {
			String entrykey, value ->
				[entrykey, traverseRecursively(parentType, "${parentKey}.${entrykey}", value, closureToExecute)]
		}
	}

	static Map<String, Object> transformToJsonPathWithValues(def json) {
		Map<String, Object> pathsAndValues = [:]
		traverseRecursivelyForKey(json, '$', pathsAndValues)
		return pathsAndValues
	}

	private static void traverseRecursivelyForKey(def json, String rootKey, Map<String, Object> pathsAndValues) {
		traverseRecursively(Map, rootKey, json) { boolean applyFiltering = false, String key, Object value ->
			String keyToInsert = getValueToInsert(applyFiltering, key, value)
			Object valueToInsert = value
			if (pathsAndValues.containsKey(keyToInsert)) {
				Object oldValue = pathsAndValues[keyToInsert]
				valueToInsert = [oldValue, valueToInsert].flatten()
			}
			pathsAndValues[keyToInsert] = valueToInsert
		}
	}

	private static Object getValueToInsert(boolean withAList, String key, Object value) {
		return withAList ? convertToListElementFiltering(key, value) : key
	}

	static String convertToListElementFiltering(String key, Object value) {
		int lastDot = key.lastIndexOf('.')
		String keyWithoutLastElement = key.substring(0, lastDot)
		String lastElement = key.substring(lastDot + 1)
		return """$keyWithoutLastElement[?(@.$lastElement == '$value')]""".toString()
	}
}
