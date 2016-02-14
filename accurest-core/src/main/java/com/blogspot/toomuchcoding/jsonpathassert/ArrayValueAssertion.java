package com.blogspot.toomuchcoding.jsonpathassert;

import com.jayway.jsonpath.DocumentContext;

class ArrayValueAssertion extends FieldAssertion {
	protected ArrayValueAssertion(DocumentContext parsedJson, StringBuffer jsonPathBuffer, Object arrayName) {
		super(parsedJson, jsonPathBuffer, arrayName);
	}

	@Override
	public ArrayValueAssertion contains(Object value) {
		return new ArrayValueAssertion(parsedJson, jsonPathBuffer, value);
	}

	@Override
	public ReadyToCheckAsserter isEqualTo(String value) {
		ReadyToCheckAsserter readyToCheck = new ReadyToCheckAsserter(parsedJson,
				jsonPathBuffer, fieldName);
		readyToCheck.jsonPathBuffer.append("[?(@ == ")
				.append(wrapValueWithSingleQuotes(value)).append(")]");
		return readyToCheck;
	}

	@Override
	public ReadyToCheckAsserter isEqualTo(Number value) {
		ReadyToCheckAsserter readyToCheck = new ReadyToCheckAsserter(parsedJson,
				jsonPathBuffer, fieldName);
		readyToCheck.jsonPathBuffer.append("[?(@ == ").append(String.valueOf(value))
				.append(")]");
		return readyToCheck;
	}

	@Override
	public ReadyToCheckAsserter matches(String value) {
		ReadyToCheckAsserter readyToCheck = new ReadyToCheckAsserter(parsedJson,
				jsonPathBuffer, fieldName);
		readyToCheck.jsonPathBuffer.append("[?(@ =~ /").append(value).append("/)]");
		return readyToCheck;
	}

	@Override
	public ReadyToCheckAsserter isEqualTo(Boolean value) {
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