package io.codearte.accurest.util

import com.jayway.jsonpath.JsonPath
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import io.codearte.accurest.dsl.internal.ExecutionProperty
import io.codearte.accurest.dsl.internal.OptionalProperty
import io.codearte.accurest.util.JsonPathEntry.Asserter

import java.util.regex.Pattern
/**
 * @author Marcin Grzejszczak
 */
class JsonToJsonPathsConverter {

	private static final Boolean SERVER_SIDE = false
	private static final Boolean CLIENT_SIDE = true

	public static JsonPaths transformToJsonPathWithTestsSideValues(def json) {
		return transformToJsonPathWithValues(json, SERVER_SIDE)
	}

	public static JsonPaths transformToJsonPathWithStubsSideValues(def json) {
		return transformToJsonPathWithValues(json, CLIENT_SIDE)
	}

	private static JsonPaths transformToJsonPathWithValues(def json, boolean clientSide) {
		if(!json) {
			return new JsonPaths()
		}
		JsonPaths pathsAndValues = [] as Set
		Object convertedJson = MapConverter.getClientOrServerSideValues(json, clientSide)
		JsonPathEntry rootEntry = new JsonPathEntry(JsonPath.parse(JsonOutput.toJson(convertedJson)))
		traverseRecursivelyForKey(convertedJson, rootEntry.root()) { Asserter key, Object value ->
			if (value instanceof ExecutionProperty) {
				return
			}
			pathsAndValues.add(key)
		}
		return pathsAndValues
	}

	protected static def traverseRecursively(Class parentType, Asserter key, def value, Closure closure) {
		if (value instanceof String && value) {
			try {
				def json = new JsonSlurper().parseText(value)
				if (json instanceof Map) {
					return convertWithKey(parentType, key, json, closure)
				}
			} catch (Exception ignore) {
				return runClosure(closure, key, value)
			}
		} else if (isAnEntryWithNonCollectionLikeValue(value)) {
			return convertWithKey(List, key, value as Map, closure)
		} else if (isAnEntryWithoutNestedStructures(value)) {
			return convertWithKey(List, key, value as Map, closure)
		} else if (value instanceof Map) {
			return convertWithKey(Map, key, value as Map, closure)
		} else if (value instanceof List) {
			Asserter asserter = key instanceof JsonPathEntry.RootFieldAssertion ?
					((JsonPathEntry.RootFieldAssertion) key).array() : key
			value.each { def element ->
				traverseRecursively(List, asserter instanceof JsonPathEntry.ArrayValueAssertion ?
						((JsonPathEntry.ArrayValueAssertion) asserter).contains(element) : asserter
						, element, closure)
			}
			return value
		}
		try {
			return runClosure(closure, key, value)
		} catch (Exception ignore) {
			return value
		}
	}

	private static def runClosure(Closure closure, Asserter key, def value) {
		if (key instanceof JsonPathEntry.ArrayValueAssertion) {
			return closure(valueToAsserter(key, value), value)
		}
		return closure(key, value)
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
			[String, Number, Boolean].any { entry.value.getClass().isAssignableFrom(it) }
		}
	}

	private static boolean listContainsOnlyPrimitives(List list) {
		return list.every { def element ->
			[String, Number, Boolean].any { element.getClass().isAssignableFrom(it) }
		}
	}

	private static Map convertWithKey(Class parentType, Asserter parentKey, Map map, Closure closureToExecute) {
		return map.collectEntries {
			Object entrykey, value ->
				[entrykey, traverseRecursively(parentType,
							value instanceof List ? listContainsOnlyPrimitives(value) ?
									parentKey.arrayField(entrykey) :
									parentKey.array(entrykey) :
							value instanceof Map ? parentKey.field(entrykey) :
									valueToAsserter(parentKey.fieldBeforeMatching(entrykey), value)
							, value, closureToExecute)]
		}
	}

	private static void traverseRecursivelyForKey(def json, Asserter rootKey, Closure closure) {
		traverseRecursively(Map, rootKey, json, closure)
	}

	protected static Asserter valueToAsserter(Asserter key, Object value) {
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
