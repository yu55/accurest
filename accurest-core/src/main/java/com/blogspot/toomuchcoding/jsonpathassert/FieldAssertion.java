package com.blogspot.toomuchcoding.jsonpathassert;

import com.jayway.jsonpath.DocumentContext;

class FieldAssertion extends JsonPathAsserter {
	protected FieldAssertion(DocumentContext parsedJson, StringBuffer jsonPathBuffer, Object fieldName) {
		super(parsedJson, jsonPathBuffer, fieldName);
	}
}