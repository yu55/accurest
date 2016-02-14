package com.blogspot.toomuchcoding.jsonpathassert;

import com.jayway.jsonpath.DocumentContext;

class ArrayAssertion extends JsonPathAsserter {
	protected ArrayAssertion(DocumentContext parsedJson, StringBuffer jsonPathBuffer,
			StringBuffer methodsBuffer, Object arrayName) {
		super(parsedJson, jsonPathBuffer, methodsBuffer, arrayName);
	}

	protected ArrayAssertion(DocumentContext parsedJson, StringBuffer jsonPathBuffer,
			StringBuffer methodsBuffer) {
		super(parsedJson, jsonPathBuffer, methodsBuffer, null);
	}

	@Override
	public FieldAssertion fieldBeforeMatching(final Object value) {
		FieldAssertion asserter = new FieldAssertion(parsedJson, jsonPathBuffer,
				methodsBuffer, value);
		asserter.methodsBuffer.append(".contains(").append(wrapValueWithQuotes(value))
				.append(")");
		return asserter;
	}

	@Override
	public boolean isIteratingOverArray() {
		return true;
	}
}