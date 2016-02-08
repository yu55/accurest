package io.codearte.accurest.assertion

import com.jayway.jsonpath.JsonPath
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import io.codearte.accurest.dsl.internal.ExecutionProperty
import io.codearte.accurest.dsl.internal.OptionalProperty
import io.codearte.accurest.util.MapConverter
import io.codearte.accurest.util.RegexpBuilders

import java.util.regex.Pattern
/**
 * @author Marcin Grzejszczak
 */
class JsonToJsonPathsAssertionsConverter {

	private static final Boolean SERVER_SIDE = false
	private static final Boolean CLIENT_SIDE = true

	public static final String ALL_ELEMENTS = "[*]"

	public static JsonPathsAssertions transformToJsonPathWithTestsSideValues(def json) {
		return transformToJsonPathWithValues(json, SERVER_SIDE)
	}

	public static JsonPathsAssertions transformToJsonPathWithStubsSideValues(def json) {
		return transformToJsonPathWithValues(json, CLIENT_SIDE)
	}

	private static JsonPathsAssertions transformToJsonPathWithValues(def json, boolean clientSide) {
		if(!json) {
			return new JsonPathsAssertions()
		}
		JsonPathsAssertions pathsAndValues = [] as Set
		Object convertedJson = MapConverter.getClientOrServerSideValues(json, clientSide)
		JsonPathAssertionEntry rootEntry = new JsonPathAssertionEntry(JsonPath.parse(JsonOutput.toJson(convertedJson)))
		traverseRecursivelyForKey(convertedJson, rootEntry.root()) { JsonPathAssertionEntry.Asserter key, Object value ->
			if (value instanceof ExecutionProperty) {
				return
			}
			pathsAndValues.add(key)
		}
		return pathsAndValues
	}

	protected static def traverseRecursively(Class parentType, JsonPathAssertionEntry.Asserter key, def value, Closure closure) {
		if (value instanceof String && value) {
			try {
				def json = new JsonSlurper().parseText(value)
				if (json instanceof Map) {
					return convertWithKey(parentType, key, json, closure)
				}
			} catch (Exception ignore) {
				return closure(key, value)
			}
		} else if (isAnEntryWithNonCollectionLikeValue(value)) {
			return convertWithKey(List, key, value as Map, closure)
		} else if (isAnEntryWithoutNestedStructures(value)) {
			return convertWithKey(List, key, value as Map, closure)
		} else if (value instanceof Map) {
			return convertWithKey(Map, key, value as Map, closure)
		} else if (value instanceof List) {
			value.each { def element ->
				traverseRecursively(List, key, element, closure)
			}
			return value
		}
		try {
			return closure(key, value)
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

	private static boolean isAnEntryWithoutNestedStructures(def value) {
		if (!(value instanceof Map)) {
			return false
		}
		Map valueAsMap = ((Map) value)
		return valueAsMap.entrySet().every { Map.Entry entry ->
			[String, Number].any { entry.value.getClass().isAssignableFrom(it) }
		}
	}

	private static Map convertWithKey(Class parentType, JsonPathAssertionEntry.Asserter parentKey, Map map, Closure closureToExecute) {
		return map.collectEntries {
			Object entrykey, value ->
				[entrykey, traverseRecursively(parentType,
						value instanceof List ? parentKey.array(entrykey.toString()) :
						value instanceof Map ? parentKey.field(entrykey.toString()) :
								getValueToInsert(parentKey.fieldBeforeMatching(entrykey.toString()), value)
						, value, closureToExecute)]
		}
	}

	private static void traverseRecursivelyForKey(def json, JsonPathAssertionEntry.Asserter rootKey, Closure closure) {
		traverseRecursively(Map, rootKey, json, closure)
	}

	private static JsonPathAssertionEntry.Asserter getValueToInsert(JsonPathAssertionEntry.Asserter key, Object value) {
		return convertToListElementFiltering(key, value)
	}

	protected static JsonPathAssertionEntry.Asserter convertToListElementFiltering(JsonPathAssertionEntry.Asserter key, Object value) {
		if (value instanceof Pattern) {
			return key.matches((value as Pattern).pattern())
		} else if (value instanceof OptionalProperty) {
			return key.matches((value as OptionalProperty).optionalPattern())
		} else if (value instanceof GString) {
			return key.matches(RegexpBuilders.buildGStringRegexpForTestSide(value))
		}
		return key.isEqualTo(value)
	}

}
