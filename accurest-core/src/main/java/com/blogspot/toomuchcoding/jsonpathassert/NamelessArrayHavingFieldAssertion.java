package com.blogspot.toomuchcoding.jsonpathassert;

import com.jayway.jsonpath.DocumentContext;

class NamelessArrayHavingFieldAssertion extends FieldAssertion {
	protected NamelessArrayHavingFieldAssertion(DocumentContext parsedJson,
			StringBuffer jsonPathBuffer, StringBuffer methodsBuffer, Object fieldName) {
		super(parsedJson, jsonPathBuffer, methodsBuffer, fieldName);
	}

	@Override
	public boolean isIteratingOverNamelessArray() {
		return true;
	}

}