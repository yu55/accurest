package com.blogspot.toomuchcoding.jsonpathassert;

import com.jayway.jsonpath.DocumentContext;

class ArrayAssertion extends JsonPathAsserter {
	protected ArrayAssertion(DocumentContext parsedJson, StringBuffer jsonPathBuffer, Object arrayName) {
		super(parsedJson, jsonPathBuffer, arrayName);
	}

	protected ArrayAssertion(DocumentContext parsedJson, StringBuffer jsonPathBuffer) {
		super(parsedJson, jsonPathBuffer, null);
	}

	@Override
	public FieldAssertion fieldBeforeMatching(final Object value) {
		return new FieldAssertion(parsedJson, jsonPathBuffer, value);
	}

	@Override
	public boolean isIteratingOverArray() {
		return true;
	}
}