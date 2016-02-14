package com.blogspot.toomuchcoding.jsonpathassert;

import com.jayway.jsonpath.DocumentContext;

public class JsonPathAsserter implements ReadyToCheck, IteratingOverArray {

	protected final DocumentContext parsedJson;
	protected final StringBuffer jsonPathBuffer;
	protected final StringBuffer methodsBuffer;
	protected final Object fieldName;

	protected JsonPathAsserter(DocumentContext parsedJson, StringBuffer jsonPathBuffer,
			StringBuffer methodsBuffer, Object fieldName) {
		this.parsedJson = parsedJson;
		this.jsonPathBuffer = new StringBuffer(jsonPathBuffer.toString());
		this.methodsBuffer = new StringBuffer(methodsBuffer.toString());
		this.fieldName = fieldName;
	}

	/**
	 * Assertion of a field inside an array
	 */
	public FieldAssertion contains(final Object value) {
		FieldAssertion asserter = new FieldAssertion(parsedJson, jsonPathBuffer,
				methodsBuffer, value);
		asserter.methodsBuffer.append(".contains(").append(wrapValueWithQuotes(value))
				.append(")");
		return asserter;
	}

	protected String wrapValueWithQuotes(Object value) {
		return value instanceof String ?
				"\"" + stringWithEscapedQuotes(value) + "\"" :
				value.toString();
	}

	protected String wrapValueWithSingleQuotes(Object value) {
		return value instanceof String ?
				"'" + stringWithEscapedSingleQuotes(value) + "'" :
				value.toString();
	}

	/**
	 * Field assertion. Adds a JSON Path entry for the given field.
	 */
	public FieldAssertion field(final Object value) {
		FieldAssertion asserter = new FieldAssertion(parsedJson, jsonPathBuffer,
				methodsBuffer, value);
		asserter.jsonPathBuffer.append(".").append(String.valueOf(value));
		asserter.methodsBuffer.append(".field(").append(wrapValueWithQuotes(value))
				.append(")");
		return asserter;
	}

	/**
	 * Special case of field - syntactic sugar that doesn't save a jsonpath entry for the given field
	 */
	public FieldAssertion fieldBeforeMatching(final Object value) {
		FieldAssertion asserter = new FieldAssertion(parsedJson, jsonPathBuffer,
				methodsBuffer, value);
		asserter.methodsBuffer.append(".field(").append(wrapValueWithQuotes(value))
				.append(")");
		return asserter;
	}

	/**
	 * When you want to assert values in a array with a given name
	 */
	public ArrayAssertion array(final Object value) {
		ArrayAssertion asserter = new ArrayAssertion(parsedJson, jsonPathBuffer,
				methodsBuffer, value);
		asserter.jsonPathBuffer.append(".").append(String.valueOf(value)).append("[*]");
		asserter.methodsBuffer.append(".array(").append(wrapValueWithQuotes(value))
				.append(")");
		return asserter;
	}

	/**
	 * When you want to compare values of a particular field in a named array
	 */
	public ArrayValueAssertion arrayField(final Object value) {
		ArrayValueAssertion asserter = new ArrayValueAssertion(parsedJson, jsonPathBuffer,
				methodsBuffer, value);
		asserter.jsonPathBuffer.append(".").append(String.valueOf(value));
		asserter.methodsBuffer.append(".array(").append(wrapValueWithQuotes(value))
				.append(")");
		return asserter;
	}

	/**
	 * When you want to compare values of a field in a nameless array
	 */
	public ArrayValueAssertion namelessArrayField(final Object value) {
		return new ArrayValueAssertion(parsedJson, jsonPathBuffer, methodsBuffer, value);
	}

	/**
	 * When in JSON path you iterate over a nameless array
	 */
	public ArrayAssertion namelessArray() {
		ArrayAssertion asserter = new ArrayAssertion(parsedJson, jsonPathBuffer,
				methodsBuffer);
		asserter.jsonPathBuffer.append("[*]");
		asserter.methodsBuffer.append(".array()");
		return asserter;
	}

	/**
	 * When in JSON path you iterate over arrays and need to skip iteration
	 */
	public ArrayAssertion iterationPassingArray() {
		return new ArrayAssertion(parsedJson, jsonPathBuffer, methodsBuffer);
	}

	public ReadyToCheckAsserter isEqualTo(String value) {
		if (value == null) {
			return isNull();
		}
		ReadyToCheckAsserter readyToCheck = new ReadyToCheckAsserter(parsedJson,
				jsonPathBuffer, methodsBuffer, fieldName);
		readyToCheck.jsonPathBuffer.append("[?(@.").append(String.valueOf(fieldName))
				.append(" == ").append(wrapValueWithSingleQuotes(value)).append(")]");
		readyToCheck.methodsBuffer.append(".isEqualTo(")
				.append(wrapValueWithQuotes(value)).append(")");
		return readyToCheck;
	}

	public ReadyToCheckAsserter isEqualTo(Object value) {
		if (value == null) {
			return isNull();
		}
		return isEqualTo(value.toString());
	}

	public ReadyToCheckAsserter isEqualTo(Number value) {
		if (value == null) {
			return isNull();
		}
		ReadyToCheckAsserter readyToCheck = new ReadyToCheckAsserter(parsedJson,
				jsonPathBuffer, methodsBuffer, fieldName);
		readyToCheck.jsonPathBuffer.append("[?(@.").append(String.valueOf(fieldName))
				.append(" == ").append(value).append(")]");
		readyToCheck.methodsBuffer.append(".isEqualTo(").append(String.valueOf(value))
				.append(")");
		return readyToCheck;
	}

	public ReadyToCheckAsserter isNull() {
		ReadyToCheckAsserter readyToCheck = new ReadyToCheckAsserter(parsedJson,
				jsonPathBuffer, methodsBuffer, fieldName);
		readyToCheck.jsonPathBuffer.append("[?(@.").append(String.valueOf(fieldName))
				.append(" == null)]");
		readyToCheck.methodsBuffer.append(".isNull()");
		return readyToCheck;
	}

	public ReadyToCheckAsserter matches(String value) {
		if (value == null) {
			return isNull();
		}
		ReadyToCheckAsserter readyToCheck = new ReadyToCheckAsserter(parsedJson,
				jsonPathBuffer, methodsBuffer, fieldName);
		readyToCheck.jsonPathBuffer.append("[?(@.").append(String.valueOf(fieldName))
				.append(" =~ /").append(stringWithEscapedSingleQuotes(value))
				.append("/)]");
		readyToCheck.methodsBuffer.append(".matches(").append(wrapValueWithQuotes(value))
				.append(")");
		return readyToCheck;
	}

	public ReadyToCheckAsserter isEqualTo(Boolean value) {
		if (value == null) {
			return isNull();
		}
		ReadyToCheckAsserter readyToCheck = new ReadyToCheckAsserter(parsedJson,
				jsonPathBuffer, methodsBuffer, fieldName);
		readyToCheck.jsonPathBuffer.append("[?(@.").append(String.valueOf(fieldName))
				.append(" == ").append(String.valueOf(value)).append(")]");
		readyToCheck.methodsBuffer.append(".isEqualTo(").append(String.valueOf(value))
				.append(")");
		return readyToCheck;
	}

	public String methodWithAppendedCheck() {
		return methodsBuffer.append(".check();").toString();
	}

	public String method() {
		return methodsBuffer.toString();
	}

	public String jsonPath() {
		return jsonPathBuffer.toString();
	}

	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!getClass().equals(o.getClass()))
			return false;
		JsonPathAsserter jsonPathAsserter = (JsonPathAsserter) o;
		if (!fieldName.equals(jsonPathAsserter.fieldName))
			return false;
		return jsonPathBuffer.equals(jsonPathAsserter.jsonPathBuffer) && methodsBuffer
				.equals(jsonPathAsserter.methodsBuffer);

	}

	public int hashCode() {
		int result;
		result = (parsedJson != null ? parsedJson.hashCode() : 0);
		result = 31 * result + (jsonPathBuffer != null ? jsonPathBuffer.hashCode() : 0);
		result = 31 * result + (methodsBuffer != null ? methodsBuffer.hashCode() : 0);
		result = 31 * result + (fieldName != null ? fieldName.hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		return "\\nAsserter{\n    " + "jsonPathBuffer=" + String.valueOf(jsonPathBuffer)
				+ ",\n   " + " methodsBuffer=" + String.valueOf(methodsBuffer) + "\n}";
	}

	@Override
	public boolean isReadyToCheck() {
		return false;
	}

	@Override
	public boolean isIteratingOverNamelessArray() {
		return false;
	}

	@Override
	public boolean isIteratingOverArray() {
		return false;
	}

	@Override
	public boolean isAssertingAValueInArray() {
		return false;
	}

	static String stringWithEscapedQuotes(Object object) {
		String stringValue = object.toString();
		return stringValue.replaceAll("\"", "\\\\\"");
	}

	static String stringWithEscapedSingleQuotes(Object object) {
		String stringValue = object.toString();
		return stringValue.replaceAll("'", "\\\\'");
	}
}