package com.blogspot.toomuchcoding.jsonpathassert;

import com.jayway.jsonpath.DocumentContext;

class ArrayValueAssertion extends FieldAssertion {
	protected ArrayValueAssertion(DocumentContext parsedJson, StringBuffer jsonPathBuffer, Object arrayName) {
		super(parsedJson, jsonPathBuffer, arrayName);
	}

	protected ArrayValueAssertion(DocumentContext parsedJson, StringBuffer jsonPathBuffer) {
		super(parsedJson, jsonPathBuffer, null);
	}

	@Override
	public JsonPathVerifiable contains(Object value) {
		return new ArrayValueAssertion(parsedJson, jsonPathBuffer, value).isEqualTo(value);
	}

	@Override
	public JsonPathVerifiable isEqualTo(String value) {
		ReadyToCheckAsserter readyToCheck = new ReadyToCheckAsserter(parsedJson,
				jsonPathBuffer, fieldName);
		readyToCheck.jsonPathBuffer.append("[?(@ == ")
				.append(wrapValueWithSingleQuotes(value)).append(")]");
		return readyToCheck;
	}

	@Override
	public JsonPathVerifiable isEqualTo(Number value) {
		ReadyToCheckAsserter readyToCheck = new ReadyToCheckAsserter(parsedJson,
				jsonPathBuffer, fieldName);
		readyToCheck.jsonPathBuffer.append("[?(@ == ").append(String.valueOf(value))
				.append(")]");
		return readyToCheck;
	}

	@Override
	public JsonPathVerifiable matches(String value) {
		ReadyToCheckAsserter readyToCheck = new ReadyToCheckAsserter(parsedJson,
				jsonPathBuffer, fieldName);
		readyToCheck.jsonPathBuffer.append("[?(@ =~ /").append(value).append("/)]");
		return readyToCheck;
	}

	@Override
	public JsonPathVerifiable isEqualTo(Boolean value) {
		ReadyToCheckAsserter readyToCheck = new ReadyToCheckAsserter(parsedJson,
				jsonPathBuffer, fieldName);
		readyToCheck.jsonPathBuffer.append("[?(@ == ").append(String.valueOf(value))
				.append(")]");
		return readyToCheck;
	}

	@Override
	public boolean isAssertingAValueInArray() {
		return true;
	}
}