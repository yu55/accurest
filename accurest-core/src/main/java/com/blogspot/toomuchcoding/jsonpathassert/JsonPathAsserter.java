package com.blogspot.toomuchcoding.jsonpathassert;

import com.jayway.jsonpath.DocumentContext;

import net.minidev.json.JSONArray;

class JsonPathAsserter implements JsonPathVerifiable {

	protected final DocumentContext parsedJson;
	protected final StringBuffer jsonPathBuffer;
	protected final Object fieldName;

	protected JsonPathAsserter(DocumentContext parsedJson, StringBuffer jsonPathBuffer, Object fieldName) {
		this.parsedJson = parsedJson;
		this.jsonPathBuffer = new StringBuffer(jsonPathBuffer.toString());
		this.fieldName = fieldName;
	}

	@Override
	public FieldAssertion contains(final Object value) {
		return new FieldAssertion(parsedJson, jsonPathBuffer, value);
	}

	@Override
	public FieldAssertion field(final Object value) {
		FieldAssertion asserter = new FieldAssertion(parsedJson, jsonPathBuffer, value);
		asserter.jsonPathBuffer.append(".").append(String.valueOf(value));
		return asserter;
	}

	@Override
	public FieldAssertion fieldBeforeMatching(final Object value) {
		return new FieldAssertion(parsedJson, jsonPathBuffer, value);
	}

	@Override
	public ArrayAssertion array(final Object value) {
		ArrayAssertion asserter = new ArrayAssertion(parsedJson, jsonPathBuffer, value);
		asserter.jsonPathBuffer.append(".").append(String.valueOf(value)).append("[*]");
		return asserter;
	}

	@Override
	public ArrayValueAssertion arrayField(final Object value) {
		ArrayValueAssertion asserter = new ArrayValueAssertion(parsedJson, jsonPathBuffer, value);
		asserter.jsonPathBuffer.append(".").append(String.valueOf(value));
		return asserter;
	}

	@Override
	public ArrayValueAssertion namelessArrayField(final Object value) {
		return new ArrayValueAssertion(parsedJson, jsonPathBuffer, value);
	}

	@Override
	public ArrayAssertion namelessArray() {
		ArrayAssertion asserter = new ArrayAssertion(parsedJson, jsonPathBuffer);
		asserter.jsonPathBuffer.append("[*]");
		return asserter;
	}

	@Override
	public ArrayAssertion iterationPassingArray() {
		return new ArrayAssertion(parsedJson, jsonPathBuffer);
	}

	@Override
	public ReadyToCheckAsserter isEqualTo(String value) {
		if (value == null) {
			return isNull();
		}
		ReadyToCheckAsserter readyToCheck = new ReadyToCheckAsserter(parsedJson,
				jsonPathBuffer, fieldName);
		readyToCheck.jsonPathBuffer.append("[?(@.").append(String.valueOf(fieldName))
				.append(" == ").append(wrapValueWithSingleQuotes(value)).append(")]");
		return readyToCheck;
	}

	@Override
	public ReadyToCheckAsserter isEqualTo(Object value) {
		if (value == null) {
			return isNull();
		}
		return isEqualTo(value.toString());
	}

	@Override
	public ReadyToCheckAsserter isEqualTo(Number value) {
		if (value == null) {
			return isNull();
		}
		ReadyToCheckAsserter readyToCheck = new ReadyToCheckAsserter(parsedJson,
				jsonPathBuffer, fieldName);
		readyToCheck.jsonPathBuffer.append("[?(@.").append(String.valueOf(fieldName))
				.append(" == ").append(value).append(")]");
		return readyToCheck;
	}

	@Override
	public ReadyToCheckAsserter isNull() {
		ReadyToCheckAsserter readyToCheck = new ReadyToCheckAsserter(parsedJson,
				jsonPathBuffer, fieldName);
		readyToCheck.jsonPathBuffer.append("[?(@.").append(String.valueOf(fieldName))
				.append(" == null)]");
		return readyToCheck;
	}

	@Override
	public ReadyToCheckAsserter matches(String value) {
		if (value == null) {
			return isNull();
		}
		ReadyToCheckAsserter readyToCheck = new ReadyToCheckAsserter(parsedJson,
				jsonPathBuffer, fieldName);
		readyToCheck.jsonPathBuffer.append("[?(@.").append(String.valueOf(fieldName))
				.append(" =~ /").append(stringWithEscapedSingleQuotes(value))
				.append("/)]");
		return readyToCheck;
	}

	@Override
	public ReadyToCheckAsserter isEqualTo(Boolean value) {
		if (value == null) {
			return isNull();
		}
		ReadyToCheckAsserter readyToCheck = new ReadyToCheckAsserter(parsedJson,
				jsonPathBuffer, fieldName);
		readyToCheck.jsonPathBuffer.append("[?(@.").append(String.valueOf(fieldName))
				.append(" == ").append(String.valueOf(value)).append(")]");
		return readyToCheck;
	}

	@Override
	public void check() {
		assert !parsedJson.read(jsonPathBuffer.toString(), JSONArray.class).isEmpty();
	}

	@Override
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
		return jsonPathBuffer.equals(jsonPathAsserter.jsonPathBuffer);

	}

	public int hashCode() {
		int result;
		result = (parsedJson != null ? parsedJson.hashCode() : 0);
		result = 31 * result + (jsonPathBuffer != null ? jsonPathBuffer.hashCode() : 0);
		result = 31 * result + (fieldName != null ? fieldName.hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		return "\\nAsserter{\n    " + "jsonPathBuffer=" + String.valueOf(jsonPathBuffer)
				+ "\n}";
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
}