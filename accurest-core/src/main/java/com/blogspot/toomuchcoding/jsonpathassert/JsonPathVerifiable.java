package com.blogspot.toomuchcoding.jsonpathassert;

/**
 * Contract to match a parsed JSON via JSON Path
 *
 * @author Marcin Grzejszczak
 */
public interface JsonPathVerifiable extends ReadyToCheck, IteratingOverArray {

	/**
	 * Assertion of a field inside an array
	 */
	JsonPathVerifiable contains(Object value);

	/**
	 * Field assertion. Adds a JSON Path entry for the given field.
	 */
	JsonPathVerifiable field(Object value);

	/**
	 * Special case of field - syntactic sugar that doesn't save a jsonpath entry for the given field
	 *
	 * TODO: Think of removing this
	 */
	JsonPathVerifiable fieldBeforeMatching(Object value);

	/**
	 * When you want to assert values in a array with a given name
	 */
	JsonPathVerifiable array(Object value);

	/**
	 * When you want to compare values of a particular field in a named array
	 */
	JsonPathVerifiable arrayField(Object value);

	/**
	 * When you want to compare values of a field in a nameless array
	 */
	JsonPathVerifiable namelessArrayField(Object value);

	/**
	 * When in JSON path you iterate over a nameless array
	 */
	JsonPathVerifiable namelessArray();

	/**
	 * When in JSON path you iterate over arrays and need to skip iteration
	 *
	 * TODO: Think of removing this
	 */
	JsonPathVerifiable iterationPassingArray();

	//TODO: All below should return ReadyToCheck
	/**
	 * Equality comparison with String
	 */
	JsonPathVerifiable isEqualTo(String value);

	/**
	 * Equality comparison with any object
	 */
	JsonPathVerifiable isEqualTo(Object value);

	/**
	 * Equality comparison with a Number
	 */
	JsonPathVerifiable isEqualTo(Number value);

	/**
	 * Equality comparison to null
	 */
	JsonPathVerifiable isNull();

	/**
	 * Regex matching
	 */
	JsonPathVerifiable matches(String value);

	/**
	 * Equality comparison with a Boolean
	 */
	JsonPathVerifiable isEqualTo(Boolean value);

	/**
	 * Returns current JSON Path expression
	 */
	String jsonPath();
}
