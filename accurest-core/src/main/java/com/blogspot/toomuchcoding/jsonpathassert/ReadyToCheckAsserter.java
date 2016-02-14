package com.blogspot.toomuchcoding.jsonpathassert;

import com.jayway.jsonpath.DocumentContext;

class ReadyToCheckAsserter extends JsonPathAsserter {

	public ReadyToCheckAsserter(DocumentContext parsedJson, StringBuffer jsonPathBuffer, Object fieldName) {
		super(parsedJson, jsonPathBuffer, fieldName);
	}

	@Override
	public boolean isReadyToCheck() {
		return true;
	}
}