package com.blogspot.toomuchcoding.jsonpathassert;

import com.jayway.jsonpath.DocumentContext;

class ArrayValueAssertion extends FieldAssertion {
	protected ArrayValueAssertion(DocumentContext parsedJson, StringBuffer jsonPathBuffer,
			StringBuffer methodsBuffer, Object arrayName) {
		super(parsedJson, jsonPathBuffer, methodsBuffer, arrayName);
	}

	@Override
	public ArrayValueAssertion contains(Object value) {
		ArrayValueAssertion asserter = new ArrayValueAssertion(parsedJson, jsonPathBuffer,
				methodsBuffer, value);
		asserter.methodsBuffer.append(".contains(").append(wrapValueWithQuotes(value))
				.append(")");
		return asserter;
	}

	@Override
	public ReadyToCheckAsserter isEqualTo(String value) {
		ReadyToCheckAsserter readyToCheck = new ReadyToCheckAsserter(parsedJson,
				jsonPathBuffer, methodsBuffer, fieldName);
		readyToCheck.jsonPathBuffer.append("[?(@ == ")
				.append(wrapValueWithSingleQuotes(value)).append(")]");
		readyToCheck.methodsBuffer.append(".value()");
		return readyToCheck;
	}

	@Override
	public ReadyToCheckAsserter isEqualTo(Number value) {
		ReadyToCheckAsserter readyToCheck = new ReadyToCheckAsserter(parsedJson,
				jsonPathBuffer, methodsBuffer, fieldName);
		readyToCheck.jsonPathBuffer.append("[?(@ == ").append(String.valueOf(value))
				.append(")]");
		readyToCheck.methodsBuffer.append(".value()");
		return readyToCheck;
	}

	@Override
	public ReadyToCheckAsserter matches(String value) {
		ReadyToCheckAsserter readyToCheck = new ReadyToCheckAsserter(parsedJson,
				jsonPathBuffer, methodsBuffer, fieldName);
		readyToCheck.jsonPathBuffer.append("[?(@ =~ /").append(value).append("/)]");
		readyToCheck.methodsBuffer.append(".value()");
		return readyToCheck;
	}

	@Override
	public ReadyToCheckAsserter isEqualTo(Boolean value) {
		ReadyToCheckAsserter readyToCheck = new ReadyToCheckAsserter(parsedJson,
				jsonPathBuffer, methodsBuffer, fieldName);
		readyToCheck.jsonPathBuffer.append("[?(@ == ").append(String.valueOf(value))
				.append(")]");
		readyToCheck.methodsBuffer.append(".value()");
		return readyToCheck;
	}

	@Override
	public boolean isAssertingAValueInArray() {
		return true;
	}
}